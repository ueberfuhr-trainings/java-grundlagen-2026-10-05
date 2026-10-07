from __future__ import annotations

import argparse
import difflib
import hashlib
import html
import json
import os
import shutil
import subprocess
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path, PurePosixPath
from pygments import highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import TextLexer, XmlLexer, get_lexer_for_filename
from pygments.util import ClassNotFound

try:
  import markdown
except ImportError:
  markdown = None

# Wurzel des Repos, unabhängig vom Verzeichnis, aus dem aufgerufen wird.
ROOT = Path(subprocess.run(
  ["git", "rev-parse", "--show-toplevel"],
  text=True,
  capture_output=True,
  check=True,
).stdout.strip())
PAGE_DIR = ROOT / ".github" / "pages"
# Downloads und erzeugte Seite; per .gitignore ausgeschlossen.
TMP_DIR = PAGE_DIR / ".tmp"
SITE = TMP_DIR / "_site"
TEMPLATE = PAGE_DIR / "template.html"
STYLE = PAGE_DIR / "style.css"
SITE_JS = PAGE_DIR / "site.js"
PAGES_IGNORE = PAGE_DIR / ".pages-ignore"

REPO = os.environ.get("GITHUB_REPOSITORY", "")
REPO_NAME = REPO.split("/", 1)[1] if "/" in REPO else ROOT.name
RUNNER_TEMP = Path(os.environ.get("RUNNER_TEMP", "/tmp"))
PRS_FILE = RUNNER_TEMP / "prs.json"

# Fremddateien (Bootstrap, Schrift JetBrains Mono) werden heruntergeladen und
# nur mit passender Prüfsumme (SHA-256) verwendet.
JETBRAINS_MONO_URL = "https://cdn.jsdelivr.net/npm/@fontsource-variable/jetbrains-mono@5.3.0/files"
VENDOR_FILES = {
  "bootstrap.min.css": (
    "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css",
    "d85327d99c7a3ee1f9b5d0500d1370acea3ad2db39c163c2f51f232baedbdede",
  ),
  "bootstrap.bundle.min.js": (
    "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js",
    "e4fd49181388c48ec5040bd3fe66f57c29c8e67fcd8502b3354b96ec7ab47cc7",
  ),
  "jetbrains-mono-latin-wght-normal.woff2": (
    f"{JETBRAINS_MONO_URL}/jetbrains-mono-latin-wght-normal.woff2",
    "18be452724bfdc236c074ca94a249a7f41a86752c7d04ab258ce9ed5651f6a7e",
  ),
  "jetbrains-mono-latin-wght-italic.woff2": (
    f"{JETBRAINS_MONO_URL}/jetbrains-mono-latin-wght-italic.woff2",
    "a8afa085e9ca5e53434e2ee918ba6b65c7dd4dda56509976b36591478c99d62e",
  ),
}

SEARCH_EXTENSIONS = {".java"}
IMAGE_EXTENSIONS = {".gif", ".jpeg", ".jpg", ".png", ".svg", ".webp"}
# Dateiendungen, die Pygments nicht von selbst erkennt.
LEXER_OVERRIDES = {".svg": XmlLexer}
README_NAMES = ("README.md", "README.MD", "README.markdown")
# Darin werden Unterordner als Java-Packages angezeigt (de.schulung.java).
JAVA_SOURCE_ROOTS = ("src/main/java", "src/test/java")

LOCAL_PR_ID = "lokal"
STATUS_LABELS = {"A": "hinzugefügt", "M": "geändert", "D": "gelöscht"}
# Unveränderte Zeilen vor und nach jeder Änderung, zur Orientierung.
DIFF_CONTEXT_LINES = 5


def run(*args: str, capture: bool = True) -> str:
  result = subprocess.run(
    list(args),
    cwd=ROOT,
    text=True,
    capture_output=capture,
    check=True,
  )
  return result.stdout.strip() if capture else ""


def git(*args: str) -> str:
  return run("git", *args)


def git_bytes(*args: str) -> bytes:
  return subprocess.run(
    ["git", *args],
    cwd=ROOT,
    capture_output=True,
    check=True,
  ).stdout


def split_nul(data: bytes) -> list[str]:
  return [item.decode("utf-8") for item in data.split(b"\0") if item]


def esc(value: object) -> str:
  return html.escape(str(value), quote=True)


def safe_read(path: Path) -> str:
  try:
    return path.read_text(encoding="utf-8")
  except UnicodeDecodeError:
    return path.read_text(encoding="utf-8", errors="replace")


def write(path: Path, content: str) -> None:
  path.parent.mkdir(parents=True, exist_ok=True)
  path.write_text(content, encoding="utf-8")


def write_bytes(path: Path, content: bytes) -> None:
  path.parent.mkdir(parents=True, exist_ok=True)
  path.write_bytes(content)


def clean_site() -> None:
  if SITE.exists():
    shutil.rmtree(SITE)
  SITE.mkdir(parents=True)


# ---------------------------------------------------------------------------
# Sichtbarkeit: .gitignore und .pages-ignore
# ---------------------------------------------------------------------------

_ignore_cache: dict[str, bool] = {}


def visible(paths: list[str]) -> list[str]:
  """Entfernt alle Pfade, die .gitignore oder .pages-ignore ausschließen.

  Git wertet beide Dateien selbst aus: .pages-ignore wird als
  core.excludesFile übergeben, die Pfade darin gelten daher relativ
  zum Wurzelverzeichnis – mit derselben Syntax wie .gitignore.
  """
  unknown = [path for path in paths if path not in _ignore_cache]

  if unknown:
    excludes = PAGES_IGNORE if PAGES_IGNORE.exists() else Path(os.devnull)
    result = subprocess.run(
      [
        "git",
        "-c",
        f"core.excludesFile={excludes}",
        "check-ignore",
        "--no-index",
        "--stdin",
        "-z",
      ],
      cwd=ROOT,
      input="\0".join(unknown).encode("utf-8") + b"\0",
      capture_output=True,
    )
    # Exit-Code 1 bedeutet: keiner der Pfade wird ignoriert.
    if result.returncode not in (0, 1):
      raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))

    ignored = set(split_nul(result.stdout))
    for path in unknown:
      _ignore_cache[path] = path in ignored

  return [path for path in paths if not _ignore_cache[path]]


# ---------------------------------------------------------------------------
# Projektstände
# ---------------------------------------------------------------------------

class Source:
  """Ein Projektstand: welche Dateien es gibt und was in ihnen steht.

  Die Basisklasse ist ein leerer Stand, z. B. vor dem ersten Commit.
  """

  def __init__(self) -> None:
    self._all: list[str] | None = None
    self._visible: list[str] | None = None
    self._contents: dict[str, bytes] = {}

  def all_paths(self) -> list[str]:
    if self._all is None:
      self._all = sorted(set(self._list()))
    return self._all

  def paths(self) -> list[str]:
    if self._visible is None:
      self._visible = visible(self.all_paths())
    return self._visible

  def read(self, path: str) -> bytes:
    if path not in self._contents:
      self._contents[path] = self._load(path)
    return self._contents[path]

  def _list(self) -> list[str]:
    return []

  def _load(self, path: str) -> bytes:
    raise KeyError(path)


class WorkTree(Source):
  """Das Arbeitsverzeichnis inklusive noch nicht committeter Dateien."""

  def _list(self) -> list[str]:
    files = split_nul(git_bytes(
      "ls-files", "-z", "--cached", "--others", "--exclude-standard",
    ))
    return [path for path in files if (ROOT / path).is_file()]

  def _load(self, path: str) -> bytes:
    return (ROOT / path).read_bytes()


class CommitTree(Source):
  """Der Stand eines Commits, direkt aus Git gelesen."""

  def __init__(self, commit: str) -> None:
    super().__init__()
    self.commit = commit

  def _list(self) -> list[str]:
    paths = []
    # Format je Eintrag: "<mode> <type> <object>\t<path>"
    for entry in split_nul(git_bytes("ls-tree", "-r", "-z", self.commit)):
      meta, path = entry.split("\t", 1)
      if meta.split(" ")[1] == "blob":
        paths.append(path)
    return paths

  def _load(self, path: str) -> bytes:
    return git_bytes("cat-file", "blob", f"{self.commit}:{path}")


@dataclass
class Change:
  status: str  # "A", "M" oder "D"
  old: bytes | None
  new: bytes | None


def compute_changes(base: Source, target: Source) -> dict[str, Change]:
  old_paths = set(base.paths())
  new_paths = set(target.paths())
  candidates = sorted(old_paths | new_paths)

  # Zwischen zwei Commits kennt Git die geänderten Pfade schon.
  if isinstance(base, CommitTree) and isinstance(target, CommitTree):
    changed = set(split_nul(git_bytes(
      "diff", "--name-only", "--no-renames", "-z", base.commit, target.commit,
    )))
    candidates = [path for path in candidates if path in changed]

  changes = {}
  for path in candidates:
    old = base.read(path) if path in old_paths else None
    new = target.read(path) if path in new_paths else None
    if old == new:
      continue
    status = "A" if old is None else "D" if new is None else "M"
    changes[path] = Change(status, old, new)

  return changes


@dataclass
class Version:
  label: str
  title: str
  intro: str
  url: str
  destination: Path
  source: Source
  # None: kein PR, also auch keine Änderungsansicht.
  changes: dict[str, Change] | None = None
  sidebar: str = field(default="", repr=False)


def site_base() -> str:
  # Local preview is served from /; GitHub Pages is normally /REPOSITORY/.
  if os.environ.get("LOCAL_PREVIEW") == "1":
    return "/"
  return "/" + REPO_NAME.strip("/") + "/"


def main_version(source: Source) -> Version:
  return Version(
    label="Aktueller Stand",
    title="Maven-Projekt",
    intro="Aktueller Stand aus dem ausgecheckten Branch.",
    url=site_base(),
    destination=SITE,
    source=source,
  )


def pr_version(pr_id: object, label: str, intro: str, base: Source, target: Source) -> Version:
  return Version(
    label=label,
    title=label,
    intro=intro,
    url=f"{site_base()}pr/{pr_id}/",
    destination=SITE / "pr" / str(pr_id),
    source=target,
    changes=compute_changes(base, target),
  )


def resolve_commit(revision: str) -> str | None:
  result = subprocess.run(
    ["git", "rev-parse", "--verify", "--quiet", f"{revision}^{{commit}}"],
    cwd=ROOT,
    text=True,
    capture_output=True,
  )
  return result.stdout.strip() if result.returncode == 0 else None


def count_rebased_copies(merged: str, head: str) -> int:
  """Zählt, wie viele Commits auf main Kopien der PR-Commits sind.

  Beim Rebase-Merge legt GitHub jeden PR-Commit neu auf main ab: Autor,
  Autorzeit und Betreffzeile bleiben erhalten, die Commit-IDs nicht.
  Beide Historien werden daher rückwärts verglichen, bis sie sich
  unterscheiden oder in einen gemeinsamen Commit münden.
  """
  log_format = "--format=%H%x09%ae%x09%at%x09%s"
  merged_log = git("log", "--first-parent", "-n200", log_format, merged).splitlines()
  head_log = git("log", "--first-parent", "-n200", log_format, head).splitlines()

  count = 0
  for merged_line, head_line in zip(merged_log, head_log):
    merged_sha, merged_meta = merged_line.split("\t", 1)
    head_sha, head_meta = head_line.split("\t", 1)
    if merged_sha == head_sha or merged_meta != head_meta:
      break
    count += 1
  return count


def merged_pr_base(pr: dict) -> str | None:
  """Der Stand von main direkt vor dem Merge des PRs."""
  parents = git("rev-list", "--parents", "-n", "1", pr["sha"]).split()[1:]

  # Merge-Commit: Der erste Elternteil ist main vor dem Merge.
  if len(parents) != 1:
    return parents[0] if parents else None

  # Ein Elternteil: Squash-Merge (ein Commit) oder Rebase-Merge (n Commits).
  # Den Vergleich ermöglicht der Original-Stand des PRs (refs/pull/<n>/head).
  if pr["head"] and resolve_commit(pr["head"]):
    copies = count_rebased_copies(pr["sha"], pr["head"])
    if copies > 1:
      return resolve_commit(f"{pr['sha']}~{copies}")
  return parents[0]


# ---------------------------------------------------------------------------
# Text und Syntax-Highlighting
# ---------------------------------------------------------------------------

def decode_text(data: bytes | None) -> str | None:
  """Liefert den Text einer Datei oder None, wenn sie binär ist."""
  if data is None or b"\0" in data[:8000]:
    return None
  return data.decode("utf-8", errors="replace").replace("\r\n", "\n")


def text_lines(text: str | None) -> list[str]:
  if not text:
    return []
  lines = text.split("\n")
  if lines[-1] == "":
    lines.pop()
  return lines


def lexer_for(path: str):
  name = PurePosixPath(path).name
  override = LEXER_OVERRIDES.get(PurePosixPath(path).suffix.lower())
  if override:
    return override(stripnl=False)
  try:
    return get_lexer_for_filename(name, stripnl=False)
  except ClassNotFound:
    return TextLexer(stripnl=False)


def code_classes(path: str) -> str:
  """CSS-Klassen für Code, inklusive Sprache (z. B. lang-java, lang-xml)."""
  return f"highlight lang-{lexer_for(path).aliases[0]}"


def highlighted_lines(text: str | None, path: str) -> list[str]:
  """Hebt den Text hervor und liefert eine HTML-Zeile je Textzeile."""
  lines = text_lines(text)
  if not lines:
    return []

  rendered = highlight(text, lexer_for(path), HtmlFormatter(nowrap=True)).split("\n")
  if rendered and rendered[-1] == "":
    rendered.pop()

  # Die Zeilen müssen exakt zu den Textzeilen passen, sonst stimmen
  # Zeilennummern und Diff-Markierungen nicht.
  if len(rendered) != len(lines):
    return [esc(line) for line in lines]
  return rendered


@dataclass
class DiffRow:
  kind: str  # "same", "add" oder "del"
  old_number: int | None
  new_number: int | None
  html: str


def diff_rows(path: str, old_text: str | None, new_text: str | None) -> list[DiffRow]:
  old_lines = text_lines(old_text)
  new_lines = text_lines(new_text)
  old_html = highlighted_lines(old_text, path)
  new_html = highlighted_lines(new_text, path)

  rows = []
  matcher = difflib.SequenceMatcher(None, old_lines, new_lines, autojunk=False)
  for tag, i1, i2, j1, j2 in matcher.get_opcodes():
    if tag == "equal":
      rows.extend(
        DiffRow("same", i + 1, j + 1, new_html[j])
        for i, j in zip(range(i1, i2), range(j1, j2))
      )
      continue
    rows.extend(DiffRow("del", i + 1, None, old_html[i]) for i in range(i1, i2))
    rows.extend(DiffRow("add", None, j + 1, new_html[j]) for j in range(j1, j2))

  return rows


# ---------------------------------------------------------------------------
# HTML
# ---------------------------------------------------------------------------

def load_template() -> str:
  return safe_read(TEMPLATE)


def render_page(title: str, body: str, version_url: str) -> str:
  return (
    load_template()
    .replace("__TITLE__", esc(title))
    .replace("__BODY__", body)
    .replace("__SITE_BASE__", site_base())
    .replace("__VERSION_BASE__", version_url)
  )


def render_status(status: str, css_class: str) -> str:
  return (
    f'<span class="{css_class} {css_class}-{status}" '
    f'title="{esc(STATUS_LABELS[status])}">{esc(status)}</span>'
  )


def build_tree(paths: list[str]) -> dict:
  root: dict = {}
  for path in paths:
    *directories, name = path.split("/")
    node = root
    for directory in directories:
      node = node.setdefault(directory, {})
    node[name] = None
  return root


def in_java_source_root(path: str) -> bool:
  return any(path.startswith(root + "/") for root in JAVA_SOURCE_ROOTS)


def compact_directory(name: str, node: dict, path: str) -> tuple[str, dict, str]:
  """Fasst Ordner zusammen, die nur genau einen Unterordner enthalten.

  Aus src → main → java wird eine Zeile „src/main/java“, innerhalb eines
  Java-Quellordners aus de → schulung → java das Package „de.schulung.java“.
  Ein Quellordner selbst bleibt die Grenze, Packages hängen nie an ihm.
  """
  label = name
  while path not in JAVA_SOURCE_ROOTS and len(node) == 1:
    (child_name, child), = node.items()
    if child is None:
      break
    label += ("." if in_java_source_root(path) else "/") + child_name
    node = child
    path += "/" + child_name
  return label, node, path


def render_tree(
  node: dict,
  version_url: str,
  tree_id: str,
  marks: dict[str, str],
  prefix: str = "",
) -> str:
  dirs = sorted((name for name, child in node.items() if child is not None), key=str.lower)
  files = sorted((name for name, child in node.items() if child is None), key=str.lower)

  parts = ['<div class="project-tree">']

  for index, name in enumerate(dirs):
    child_id = f"{tree_id}-{index}"
    label, child, path = compact_directory(name, node[name], prefix + name)

    parts.append(
      '<div class="tree-item py-1">'
      f'<button class="btn btn-sm p-0 me-1" type="button" '
      f'data-tree-toggle="{esc(child_id)}" aria-expanded="true">▾</button>'
      f'<span class="tree-dir">{esc(label)}</span>'
      f'<div id="{esc(child_id)}" class="tree-indent">'
      f'{render_tree(child, version_url, child_id, marks, path + "/")}'
      '</div>'
      '</div>'
    )

  for name in files:
    relative = prefix + name
    href = version_url + relative + ".html"
    status = marks.get(relative)

    parts.append(
      '<div class="tree-item py-1 ps-4">'
      f'<a class="tree-link" href="{esc(href)}">{esc(name)}</a>'
      f'{render_status(status, "tree-status") if status else ""}'
      '</div>'
    )

  parts.append("</div>")
  return "".join(parts)


def render_full_toggle(toggle_id: str) -> str:
  return f"""
<div class="form-check m-0 text-nowrap">
    <input class="form-check-input" type="checkbox" id="{toggle_id}">
    <label class="form-check-label small" for="{toggle_id}">Kompletter Stand</label>
</div>
"""


def render_search_box() -> str:
  return """
<div class="mb-3">
    <label for="searchInput" class="form-label small fw-semibold">Suche</label>
    <div class="input-group">
        <input id="searchInput" class="form-control" type="search"
               placeholder="Java-Dateien durchsuchen …" autocomplete="off">
        <button class="btn btn-primary" type="button" data-open-search>
            Suchen
        </button>
    </div>
</div>
"""


def render_project_tree(version: Version) -> str:
  full_tree = build_tree(version.source.paths())

  if version.changes is None:
    return f"""
<div class="card">
    <div class="card-header fw-semibold">Projektstruktur</div>
    <div class="card-body p-2">{render_tree(full_tree, version.url, "root", {})}</div>
</div>
"""

  marks = {path: change.status for path, change in version.changes.items()}
  if version.changes:
    changes_html = render_tree(build_tree(list(version.changes)), version.url, "changes", marks)
  else:
    changes_html = '<div class="small text-body-secondary p-2">Keine Dateiänderungen.</div>'

  return f"""
<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center gap-2">
        <span class="fw-semibold">Projektstruktur</span>
        {render_full_toggle("treeFullToggle")}
    </div>
    <div class="card-body p-2">
        <div data-tree-view="changes">{changes_html}</div>
        <div data-tree-view="full" class="d-none">
            {render_tree(full_tree, version.url, "full", marks)}
        </div>
    </div>
</div>
"""


def render_sidebar(version: Version, versions: list[Version]) -> str:
  return f"""
<div class="sidebar-content">
    {render_version_selector(version, versions)}
    {render_search_box()}
    {render_project_tree(version)}
</div>
"""


def load_prs() -> list[dict]:
  """Offene PRs (neueste zuerst), danach gemergte (zuletzt gemergte zuerst)."""
  if not PRS_FILE.exists():
    return []

  open_prs = []
  merged_prs = []

  for item in json.loads(safe_read(PRS_FILE)):
    state = item.get("state")

    if state == "MERGED":
      sha = (item.get("mergeCommit") or {}).get("oid")
      target = merged_prs
    elif state == "OPEN" and not item.get("isCrossRepository"):
      # PRs aus Forks bleiben außen vor: ihr Inhalt ist ungeprüft und
      # würde sonst ungefragt auf der Seite des Repos erscheinen.
      sha = item.get("headRefOid")
      target = open_prs
    else:
      continue

    if not sha:
      continue
    if not resolve_commit(sha):
      print(f"PR #{item['number']}: Commit {sha} fehlt lokal, PR wird übersprungen.")
      continue

    target.append({
      "number": int(item["number"]),
      "title": item.get("title", ""),
      "state": state,
      "sha": sha,
      "mergedAt": item.get("mergedAt") or "",
      "head": item.get("headRefOid"),
    })

  open_prs.sort(key=lambda pr: pr["number"], reverse=True)
  merged_prs.sort(key=lambda pr: pr["mergedAt"], reverse=True)
  return open_prs + merged_prs


def render_version_selector(current: Version, versions: list[Version]) -> str:
  option_html = []
  for version in versions:
    selected = " selected" if version is current else ""
    option_html.append(
      f'<option value="{esc(version.url)}"{selected}>{esc(version.label)}</option>'
    )

  return f"""
<div class="mb-3">
    <label for="versionSelect" class="form-label small fw-semibold">Version</label>
    <select id="versionSelect" class="form-select">
        {''.join(option_html)}
    </select>
</div>
"""


def render_search_modal() -> str:
  return """
<div class="modal fade search-modal" id="searchModal" tabindex="-1"
     aria-labelledby="searchModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-scrollable modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="searchModalLabel">Suche</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal"
                        aria-label="Schließen"></button>
            </div>
            <div class="modal-body p-0">
                <div id="searchResults" class="list-group list-group-flush">
                    <div class="search-empty">Suchbegriff eingeben.</div>
                </div>
            </div>
        </div>
    </div>
</div>
"""


def render_shell(title: str, version: Version, main_content: str) -> str:
  body = f"""
<div class="app-shell d-flex flex-column">
    <header class="project-header py-3 px-3 px-lg-4">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
            <div>
                <div class="small text-body-secondary">Java / Maven</div>
                <h1 class="h3 mb-0">{esc(title)}</h1>
            </div>
            <button id="themeToggle" class="btn btn-outline-secondary"
                    type="button">☾ Dunkel</button>
        </div>
    </header>

    <div class="d-flex flex-grow-1">
        <aside id="appSidebar" class="app-sidebar">{version.sidebar}</aside>
        <div id="sidebarSplitter" class="sidebar-splitter"
             role="separator" aria-label="Breite der Seitenleiste ändern"
             aria-orientation="vertical"></div>
        <main class="app-main p-3 p-lg-4">
            {main_content}
        </main>
    </div>
</div>

{render_search_modal()}
"""

  return render_page(title, body, version.url)


def render_overview(version: Version) -> str:
  source = version.source
  available = set(source.all_paths())

  readme_html = ""
  readme = next((name for name in README_NAMES if name in available), None)
  if readme:
    readme_text = decode_text(source.read(readme)) or ""
    if markdown:
      readme_html = markdown.markdown(
        readme_text,
        extensions=["fenced_code", "tables", "toc"],
      )
    else:
      readme_html = f"<pre>{esc(readme_text)}</pre>"

  pom_html = ""
  if "pom.xml" in available:
    pom_code = "\n".join(highlighted_lines(decode_text(source.read("pom.xml")), "pom.xml"))
    pom_html = f"""
<section class="card mb-4">
    <div class="card-header fw-semibold">pom.xml</div>
    <div class="card-body p-0 code-scroll">
        <pre class="m-0 p-3 {code_classes("pom.xml")}"><code>{pom_code}</code></pre>
    </div>
</section>
"""

  content = f"""
<div class="mb-4">
    <div class="text-body-secondary">{esc(version.intro)}</div>
</div>
{pom_html}
{f'<section class="card"><div class="card-header fw-semibold">README</div><div class="card-body">{readme_html}</div></section>' if readme_html else ''}
"""
  return render_shell(version.title, version, content)


def render_code_table(path: str, text: str) -> str:
  rows = [
    f'<tr><td class="code-line-number">{number}</td>'
    f'<td class="code-line">{line}</td></tr>'
    for number, line in enumerate(highlighted_lines(text, path), 1)
  ]
  return f'<table class="code-table {code_classes(path)}"><tbody>{"".join(rows)}</tbody></table>'


def render_diff_row(row: DiffRow) -> str:
  marker = {"add": "+", "del": "−", "same": ""}[row.kind]
  return (
    f'<tr class="diff-{row.kind}">'
    f'<td class="code-line-number">{row.old_number or ""}</td>'
    f'<td class="code-line-number">{row.new_number or ""}</td>'
    f'<td class="diff-marker">{marker}</td>'
    f'<td class="code-line">{row.html}</td>'
    '</tr>'
  )


def render_diff_table(path: str, rows: list[DiffRow]) -> str:
  # Geänderte Zeilen samt Kontext; Lücken dazwischen werden als ⋯ angedeutet.
  shown = set()
  for index, row in enumerate(rows):
    if row.kind != "same":
      shown.update(range(
        max(0, index - DIFF_CONTEXT_LINES),
        min(len(rows), index + DIFF_CONTEXT_LINES + 1),
      ))

  changes = []
  previous = None
  for index in sorted(shown):
    if previous is not None and index != previous + 1:
      changes.append('<tr class="diff-gap"><td colspan="4">⋯</td></tr>')
    changes.append(render_diff_row(rows[index]))
    previous = index

  if not changes:
    changes.append(
      '<tr class="diff-gap"><td colspan="4">'
      'Keine geänderten Zeilen (z. B. nur Zeilenenden).'
      '</td></tr>'
    )

  return f'<table class="code-table {code_classes(path)}"><tbody>{"".join(changes)}</tbody></table>'


def render_source(path: str, version: Version) -> str:
  change = (version.changes or {}).get(path)
  if change:
    old, new = change.old, change.new
  else:
    old = new = version.source.read(path)

  suffix = PurePosixPath(path).suffix.lower()
  old_text, new_text = decode_text(old), decode_text(new)
  is_text = (old is None or old_text is not None) and (new is None or new_text is not None)

  preview = ""
  if suffix in IMAGE_EXTENSIONS:
    # Die Bilddatei liegt neben ihrer Seite und wird von dort eingebunden.
    image = new if new is not None else old
    write_bytes(version.destination / path, image)
    preview = (
      '<div class="image-preview p-3">'
      f'<img src="{esc(PurePosixPath(path).name)}" alt="{esc(path)}">'
      '</div>'
    )

  toggle = ""
  if change and is_text:
    # „Kompletter Stand“ zeigt die Datei nach der Änderung, ohne Diff.
    if new_text is None:
      final = '<div class="p-3 text-body-secondary">Die Datei wurde gelöscht.</div>'
    else:
      final = render_code_table(path, new_text)
    body = (
      f'<div data-diff-view="changes">{render_diff_table(path, diff_rows(path, old_text, new_text))}</div>'
      f'<div data-diff-view="full" class="d-none">{final}</div>'
    )
    toggle = render_full_toggle("fileFullToggle")
  elif change:
    body = (
      '<div class="p-3 text-body-secondary">'
      f'Binärdatei {esc(STATUS_LABELS[change.status])} – keine Textansicht.'
      '</div>'
    )
  elif new_text is not None:
    body = render_code_table(path, new_text)
  else:
    body = '<div class="p-3 text-body-secondary">Binärdatei – keine Textansicht.</div>'

  content = f"""
<div class="mb-3">
    <a href="{esc(version.url)}" class="btn btn-sm btn-outline-primary">← Projektübersicht</a>
</div>
<div class="card code-card">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="d-flex align-items-center gap-2">
            <span class="fw-semibold">{esc(path)}</span>
            {render_status(change.status, "file-status") if change else ""}
        </span>
        <span class="d-flex align-items-center gap-3">
            {toggle}
            <span class="badge text-bg-secondary">{esc(suffix or 'text')}</span>
        </span>
    </div>
    {preview}
    <div class="code-scroll">{body}</div>
</div>
"""
  return render_shell(path, version, content)


def write_search_index(version: Version) -> None:
  items = []
  for path in version.source.paths():
    if PurePosixPath(path).suffix.lower() not in SEARCH_EXTENSIONS:
      continue
    content = decode_text(version.source.read(path))
    if content is None:
      continue
    items.append({
      "name": PurePosixPath(path).name,
      "path": path,
      "url": version.url + path + ".html",
      "content": content,
    })

  write(version.destination / "search-index.json", json.dumps(items, ensure_ascii=False))


def generate_version(version: Version, versions: list[Version]) -> None:
  version.destination.mkdir(parents=True, exist_ok=True)
  # Die Seitenleiste ist für alle Seiten einer Version gleich.
  version.sidebar = render_sidebar(version, versions)

  write(version.destination / "index.html", render_overview(version))

  # Gelöschte Dateien bekommen ebenfalls eine Seite, damit ihr Diff sichtbar ist.
  pages = list(version.source.paths())
  if version.changes:
    pages += [path for path, change in version.changes.items() if change.status == "D"]

  for path in pages:
    write(version.destination / (path + ".html"), render_source(path, version))

  write_search_index(version)


def ensure_vendor_files() -> None:
  TMP_DIR.mkdir(parents=True, exist_ok=True)

  for name, (url, checksum) in VENDOR_FILES.items():
    path = TMP_DIR / name

    if not path.exists():
      urllib.request.urlretrieve(url, path)

    actual = hashlib.sha256(path.read_bytes()).hexdigest()
    if actual != checksum:
      path.unlink()
      raise RuntimeError(
        f"{name}: Prüfsumme {actual} statt {checksum} – Datei wurde gelöscht."
      )


def copy_static_assets() -> None:
  write(SITE / "style.css", safe_read(STYLE))
  write(SITE / "site.js", safe_read(SITE_JS))

  assets = SITE / "assets"
  assets.mkdir(parents=True, exist_ok=True)

  for name in VENDOR_FILES:
    shutil.copy2(TMP_DIR / name, assets / name)


def main() -> None:
  parser = argparse.ArgumentParser()
  parser.add_argument(
    "--local",
    action="store_true",
    help="Generate only the current working tree; no PR snapshots.",
  )
  parser.add_argument(
    "--with-prs",
    action="store_true",
    help="Generate open and merged PRs from $RUNNER_TEMP/prs.json.",
  )
  parser.add_argument(
    "--simulate-pr",
    nargs="?",
    const="main",
    metavar="BASE",
    help="Local only: add a simulated PR comparing the working tree "
         "against BASE (default: main).",
  )
  args = parser.parse_args()

  if args.with_prs and (args.local or args.simulate_pr):
    parser.error("--with-prs cannot be combined with --local or --simulate-pr")

  local = not args.with_prs
  if local:
    os.environ["LOCAL_PREVIEW"] = "1"

  clean_site()
  ensure_vendor_files()
  copy_static_assets()

  versions = []

  if local:
    work_tree = WorkTree()
    versions.append(main_version(work_tree))

    if args.simulate_pr:
      base = git("merge-base", args.simulate_pr, "HEAD")
      versions.append(pr_version(
        LOCAL_PR_ID,
        "Probe-PR (lokal)",
        f"Simulierter Pull Request: Arbeitsverzeichnis gegenüber {args.simulate_pr}.",
        CommitTree(base),
        work_tree,
      ))
  else:
    main_commit = git("rev-parse", "HEAD")
    versions.append(main_version(CommitTree(main_commit)))

    for pr in load_prs():
      if pr["state"] == "OPEN":
        # Wie auf GitHub: Änderungen gegenüber dem Abzweig von main.
        base = git("merge-base", main_commit, pr["sha"])
        label = f"PR #{pr['number']} (offen) – {pr['title']}"
        intro = "Offener Pull Request: Änderungen gegenüber main, Stand des letzten Pushs."
      else:
        base = merged_pr_base(pr)
        label = f"PR #{pr['number']} – {pr['title']}"
        intro = "Änderungen dieses Pull Requests."

      versions.append(pr_version(
        pr["number"],
        label,
        intro,
        CommitTree(base) if base else Source(),
        CommitTree(pr["sha"]),
      ))

  for version in versions:
    generate_version(version, versions)

  if (SITE / PRS_FILE.name).exists():
    raise RuntimeError(f"{PRS_FILE.name} must never be published")

  print(f"Generated {'local preview' if local else 'GitHub Pages site'} in {SITE}")


if __name__ == "__main__":
  main()
