# Lokale Vorschau

Voraussetzungen:

- Python 3.11+ mit den Paketen aus `requirements.txt`
  (`pip install -r .github/pages/requirements.txt`)
- Git
- Aufruf von irgendwo innerhalb des Repos

Die lokale Vorschau erzeugt **nur den aktuellen Stand** aus dem ausgecheckten Working Tree. Es werden keine PR-Daten und kein `prs.json` benötigt.

```bash
python .github/pages/generate.py --local
python -m http.server 8000 --directory .github/pages/.tmp/_site
```

Dann im Browser öffnen:

```text
http://localhost:8000/
```

Optional kann die Vorschau direkt über das Hilfsskript gestartet werden:

```bash
python .github/pages/preview.py
```

Mit `--port 8080` lässt sich ein anderer Port verwenden.

## Probe-PR

Lokal gibt es keine Pull Requests. Um die Änderungsansicht trotzdem zu testen,
erzeugt `--simulate-pr` zusätzlich einen Probe-PR: Er vergleicht das
Arbeitsverzeichnis (inklusive nicht committeter Dateien) mit dem Stand, an dem
der aktuelle Branch von `main` abzweigt.

```bash
python .github/pages/preview.py --simulate-pr
```

Der Probe-PR erscheint in der Versionsauswahl. Statt `main` kann jede andere
Basis angegeben werden, z. B. `--simulate-pr HEAD~3`, um die letzten drei
Commits als Änderungen zu sehen. Im Ordner `.github/pages` geht das auch per
`make preview-pr` bzw. `make preview-pr BASE=HEAD~3`.

## Ausgeschlossene Dateien

Angezeigt werden alle Dateien des Repos außer denen, die `.gitignore` oder
`.github/pages/.pages-ignore` ausschließen. `.pages-ignore` hat dieselbe Syntax
wie `.gitignore`, die Pfade gelten relativ zum Wurzelverzeichnis des Repos.

## GitHub-Workflow

Der Workflow erzeugt die Seite mit allen offenen und gemergten PRs:

```bash
python .github/pages/generate.py --with-prs
```

Die PR-Daten liest er aus `$RUNNER_TEMP/prs.json` (per `gh pr list`). Er
läuft bei jedem Push auf `main` und bei jedem Push in einen offenen PR.

- **Gemergte PRs** zeigen, was der Merge auf `main` geändert hat – bei
  Merge-Commits, Squash- und Rebase-Merges.
- **Offene PRs** zeigen den Stand des letzten Pushs gegenüber dem Abzweig von
  `main`, wie unter „Files changed“ auf GitHub. PRs aus Forks werden nicht
  veröffentlicht.
