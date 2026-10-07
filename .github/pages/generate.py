from __future__ import annotations

import argparse
import html
import json
import os
import re
import shutil
import subprocess
import sys
import urllib.request
from pathlib import Path
from pygments import highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import (
  JavaLexer,
  JsonLexer,
  MarkdownLexer,
  PropertiesLexer,
  TextLexer,
  XmlLexer,
  YamlLexer,
)

try:
  import markdown
except ImportError:
  markdown = None

ROOT = Path.cwd()
PAGE_DIR = ROOT / ".github" / "pages"
SITE = ROOT / "_site"
TEMPLATE = PAGE_DIR / "template.html"
STYLE = PAGE_DIR / "style.css"
SITE_JS = PAGE_DIR / "site.js"

REPO = os.environ.get("GITHUB_REPOSITORY", "")
REPO_NAME = REPO.split("/", 1)[1] if "/" in REPO else ROOT.name
RUNNER_TEMP = Path(os.environ.get("RUNNER_TEMP", "/tmp"))
MERGED_PRS_FILE = RUNNER_TEMP / "merged-prs.json"

BOOTSTRAP_CSS_URL = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
BOOTSTRAP_JS_URL = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"

SOURCE_EXTENSIONS = {
  ".java", ".xml", ".yml", ".yaml", ".properties",
  ".md", ".txt", ".json", ".gradle", ".kts",
}
SEARCH_EXTENSIONS = {".java"}

EXCLUDED_NAMES = {
  ".git",
  ".github",
  "_site",
  ".idea",
  "target",
  "README.md",
  "README.MD",
  "README.markdown",
  "merged-prs.json",
  ".DS_Store",
}

_gitignore_cache: dict[str, bool] = {}


def is_gitignored(path: Path) -> bool:
  try:
    relative = path.relative_to(ROOT).as_posix()
  except ValueError:
    return False

  if relative in _gitignore_cache:
    return _gitignore_cache[relative]

  result = subprocess.run(
    [
      "git",
      "check-ignore",
      "--no-index",
      "-q",
      "--",
      relative,
    ],
    cwd=ROOT,
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL,
  )

  ignored = result.returncode == 0
  _gitignore_cache[relative] = ignored
  return ignored


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


def clean_site() -> None:
  if SITE.exists():
    shutil.rmtree(SITE)
  SITE.mkdir(parents=True)


def is_hidden(path: Path) -> bool:
  if any(part in EXCLUDED_NAMES for part in path.parts):
    return True

  if os.environ.get("LOCAL_PREVIEW") == "1":
    return is_gitignored(path)

  return False


def iter_visible_paths(source_root: Path):
  for current, dirs, files in os.walk(source_root):
    current_path = Path(current)

    dirs[:] = [
      name
      for name in dirs
      if not is_hidden(current_path / name)
    ]

    for name in files:
      path = current_path / name
      if not is_hidden(path):
        yield path


def is_source(path: Path) -> bool:
  return path.is_file() and not is_hidden(path) and path.suffix.lower() in SOURCE_EXTENSIONS


def is_searchable(path: Path) -> bool:
  return path.is_file() and not is_hidden(path) and path.suffix.lower() in SEARCH_EXTENSIONS


def site_base() -> str:
  # Local preview is served from /; GitHub Pages is normally /REPOSITORY/.
  if os.environ.get("LOCAL_PREVIEW") == "1":
    return "/"
  return "/" + REPO_NAME.strip("/") + "/"


def version_base(kind: str, number: int | None = None) -> str:
  base = site_base()
  if kind == "main":
    return base
  if kind == "pr":
    return f"{base}pr/{number}/"
  raise ValueError(kind)


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


def lexer_for(path: Path):
  suffix = path.suffix.lower()
  if suffix == ".java":
    return JavaLexer()
  if suffix == ".xml":
    return XmlLexer()
  if suffix in {".yml", ".yaml"}:
    return YamlLexer()
  if suffix == ".json":
    return JsonLexer()
  if suffix == ".properties":
    return PropertiesLexer()
  if suffix == ".md":
    return MarkdownLexer()
  return TextLexer()


def highlighted_code(text: str, path: Path) -> str:
  return highlight(
    text,
    lexer_for(path),
    HtmlFormatter(nowrap=True),
  )


def render_tree(path: Path, version_url: str, tree_id: str = "root") -> str:
  dirs = []
  files = []

  children = sorted(
    path.iterdir(),
    key=lambda p: (p.is_file(), p.name.lower()),
  )

  for child in children:
    if is_hidden(child):
      continue
    if child.is_dir():
      if child.name == ".git":
        continue
      dirs.append(child)
    elif child.name == "pom.xml" or is_source(child):
      files.append(child)

  parts = ['<div class="project-tree">']

  for index, directory in enumerate(dirs):
    child_id = f"{tree_id}-{index}"
    href = (
      version_url
      + "/".join(directory.relative_to(path.parent).parts)
      + "/index.html"
    )
    # Directory links are intentionally not navigational; the button controls collapse.
    parts.append(
      '<div class="tree-item py-1">'
      f'<button class="btn btn-sm p-0 me-1" type="button" '
      f'data-tree-toggle="{esc(child_id)}" aria-expanded="true">▾</button>'
      f'<span class="tree-dir">{esc(directory.name)}</span>'
      f'<div id="{esc(child_id)}" class="tree-indent">'
      f'{render_tree(directory, version_url, child_id)}'
      '</div>'
      '</div>'
    )

  for file in files:
    relative = file.relative_to(ROOT).as_posix()
    href = version_url + relative
    href = href.rsplit("/", 1)[0] + "/" + Path(relative).name + ".html"
    parts.append(
      '<div class="tree-item py-1 ps-4">'
      f'<a class="tree-link" href="{esc(href)}">{esc(file.name)}</a>'
      '</div>'
    )

  parts.append("</div>")
  return "".join(parts)


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


def render_sidebar(version_url: str) -> str:
  return f"""
<div class="sidebar-content">
    {render_version_selector(version_url)}
    {render_search_box()}
    <div class="card">
        <div class="card-header fw-semibold">Projektstruktur</div>
        <div class="card-body p-2">{render_tree(ROOT, version_url)}</div>
    </div>
</div>
"""


def load_prs() -> list[dict]:
  if not MERGED_PRS_FILE.exists():
    return []

  raw = json.loads(safe_read(MERGED_PRS_FILE))
  prs = []

  for item in raw:
    merge_commit = item.get("mergeCommit") or {}
    sha = merge_commit.get("oid")

    if not sha:
      continue

    prs.append({
      "number": int(item["number"]),
      "title": item.get("title", ""),
      "sha": sha,
      "mergedAt": item.get("mergedAt", ""),
    })

  return sorted(
    prs,
    key=lambda x: x["mergedAt"],
    reverse=True,
  )


def render_version_selector(current_url: str) -> str:
  options = [("main", "Aktueller Stand", version_base("main"))]
  for pr in load_prs():
    options.append((
      f"pr-{pr['number']}",
      f"PR #{pr['number']} – {pr['title']}",
      version_base("pr", pr["number"]),
    ))

  option_html = []
  for value, label, url in options:
    selected = " selected" if url == current_url else ""
    option_html.append(
      f'<option value="{esc(url)}"{selected}>{esc(label)}</option>'
    )

  return f"""
<div class="mb-3">
    <label for="versionSelect" class="form-label small fw-semibold">Version</label>
    <select id="versionSelect" class="form-select"
            onchange="if(this.value) window.location.href=this.value">
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


def render_shell(title: str, version_url: str, main_content: str) -> str:
  sidebar = render_sidebar(version_url)
  body = f"""
<div class="app-shell d-flex flex-column">
    <header class="project-header py-3 px-3 px-lg-4">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
            <div>
                <div class="small text-body-secondary">Java / Maven</div>
                <h1 class="h3 mb-0">{esc(title)}</h1>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-outline-primary" href="{esc(version_url)}">
                    Projektübersicht
                </a>
                <button id="themeToggle" class="btn btn-outline-secondary"
                        type="button">☾ Dunkel</button>
            </div>
        </div>
    </header>

    <div class="d-flex flex-grow-1">
        <aside id="appSidebar" class="app-sidebar">{sidebar}</aside>
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
  return render_page(title, body, version_url)


def render_overview(source_root: Path, version_url: str, kind: str, pr=None) -> str:
  if kind == "main":
    title = "Maven-Projekt"
    intro = "Aktueller Stand aus dem ausgecheckten Branch."
  else:
    title = f"PR #{pr['number']} – {pr['title']}"
    intro = "Projektstand nach dem Merge dieses Pull Requests."

  readme = source_root / "README.md"
  if not readme.exists():
    readme = source_root / "README.MD"

  readme_html = ""
  if readme.exists():
    if markdown:
      readme_html = markdown.markdown(
        safe_read(readme),
        extensions=["fenced_code", "tables", "toc"],
      )
    else:
      readme_html = f"<pre>{esc(safe_read(readme))}</pre>"

  pom = source_root / "pom.xml"
  pom_html = ""
  if pom.exists():
    pom_html = f"""
<section class="card mb-4">
    <div class="card-header fw-semibold">pom.xml</div>
    <div class="card-body p-0 code-scroll">
        <pre class="m-0 p-3"><code>{highlighted_code(safe_read(pom), pom)}</code></pre>
    </div>
</section>
"""

  content = f"""
<div class="mb-4">
    <div class="text-body-secondary">{esc(intro)}</div>
</div>
{pom_html}
{f'<section class="card"><div class="card-header fw-semibold">README</div><div class="card-body">{readme_html}</div></section>' if readme_html else ''}
"""
  return render_shell(title, version_url, content)


def render_source(path: Path, source_root: Path, version_url: str) -> str:
  relative = path.relative_to(source_root).as_posix()
  title = relative
  source = safe_read(path)
  highlighted = highlighted_code(source, path)

  lines = highlighted.splitlines()
  code_rows = []
  for number, line in enumerate(lines, 1):
    code_rows.append(
      f'<tr><td class="code-line-number">{number}</td>'
      f'<td class="code-line"><div class="highlight">{line}</div></td></tr>'
    )

  content = f"""
<div class="mb-3">
    <a href="{esc(version_url)}" class="text-decoration-none">← Projektübersicht</a>
</div>
<div class="card code-card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <span class="fw-semibold">{esc(title)}</span>
        <span class="badge text-bg-secondary">{esc(path.suffix.lower() or 'text')}</span>
    </div>
    <div class="code-scroll">
        <table class="code-table">
            <tbody>{''.join(code_rows)}</tbody>
        </table>
    </div>
</div>
"""
  return render_shell(title, version_url, content)


def write_search_index(source_root: Path, version_url: str) -> None:
  items = []
  for path in sorted(iter_visible_paths(source_root)):
    if not is_searchable(path):
      continue
    rel = path.relative_to(source_root).as_posix()
    items.append({
      "name": path.name,
      "path": rel,
      "url": version_url + rel + ".html",
      "content": safe_read(path),
    })

  write(
    SITE / ("search-index.js" if version_url == version_base("main") else
            Path(version_url.rstrip("/")).name / "search-index.js"),
    json.dumps(items, ensure_ascii=False),
  )


def generate_version(source_root: Path, kind: str, pr=None) -> None:
  if kind == "main":
    destination = SITE
    url = version_base("main")
  else:
    destination = SITE / "pr" / str(pr["number"])
    url = version_base("pr", pr["number"])

  destination.mkdir(parents=True, exist_ok=True)

  # Main overview.
  write(
    destination / "index.html",
    render_overview(source_root, url, kind, pr),
  )

  # Source pages. Only the project files are included; .idea/README etc. are filtered.
  for path in sorted(iter_visible_paths(source_root)):
    if not is_source(path):
      continue
    rel = path.relative_to(source_root)
    output = destination / (str(rel) + ".html")
    output.parent.mkdir(parents=True, exist_ok=True)
    write(output, render_source(path, source_root, url))

  # Search index belongs to the version directory.
  items = []
  for path in sorted(iter_visible_paths(source_root)):
    if not is_searchable(path):
      continue
    rel = path.relative_to(source_root).as_posix()
    items.append({
      "name": path.name,
      "path": rel,
      "url": url + rel + ".html",
      "content": safe_read(path),
    })
  write(destination / "search-index.js", json.dumps(items, ensure_ascii=False))


def snapshot(commit: str, destination: Path) -> None:
  destination.mkdir(parents=True, exist_ok=True)
  archive = subprocess.Popen(
    ["git", "archive", commit],
    cwd=ROOT,
    stdout=subprocess.PIPE,
  )
  subprocess.run(["tar", "-x", "-C", str(destination)], stdin=archive.stdout, check=True)
  archive.stdout.close()
  archive.wait()
  if archive.returncode != 0:
    raise RuntimeError(f"git archive failed for {commit}")


def download(url: str, target: Path) -> None:
  target.parent.mkdir(parents=True, exist_ok=True)
  print(f"Downloading {url}")
  with urllib.request.urlopen(url) as response:
    target.write_bytes(response.read())


def ensure_bootstrap(local: bool) -> None:
  css = SITE / "assets" / "bootstrap.min.css"
  js = SITE / "assets" / "bootstrap.bundle.min.js"

  if local:
    if not css.exists():
      download(BOOTSTRAP_CSS_URL, css)
    if not js.exists():
      download(BOOTSTRAP_JS_URL, js)

  if not css.exists() or not js.exists():
    raise RuntimeError("Bootstrap assets missing. GitHub Actions must download them after generation.")


def copy_static_assets() -> None:
  write(SITE / "style.css", safe_read(STYLE))
  write(SITE / "site.js", safe_read(SITE_JS))


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
    help="Generate merged PR snapshots from $RUNNER_TEMP/merged-prs.json.",
  )
  args = parser.parse_args()

  if args.local and args.with_prs:
    parser.error("--local and --with-prs are mutually exclusive")

  local = args.local or not args.with_prs
  if local:
    os.environ["LOCAL_PREVIEW"] = "1"

  clean_site()
  copy_static_assets()

  if local:
    generate_version(ROOT, "main")
    ensure_bootstrap(local=True)
    print(f"Generated local preview in {SITE}")
    return

  main_commit = git("rev-parse", "HEAD")
  main_source = ROOT / ".pages-main-source"
  if main_source.exists():
    shutil.rmtree(main_source)
  snapshot(main_commit, main_source)
  try:
    generate_version(main_source, "main")

    for pr in load_prs():
      source = ROOT / f".pages-pr-{pr['number']}-source"
      if source.exists():
        shutil.rmtree(source)
      snapshot(pr["sha"], source)
      try:
        generate_version(source, "pr", pr)
      finally:
        shutil.rmtree(source, ignore_errors=True)
  finally:
    shutil.rmtree(main_source, ignore_errors=True)

  ensure_bootstrap(local=False)

  if (SITE / "merged-prs.json").exists():
    raise RuntimeError("merged-prs.json must never be published")

  print(f"Generated GitHub Pages site in {SITE}")


if __name__ == "__main__":
  main()
