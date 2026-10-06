# Lokale Vorschau

Voraussetzungen:

- Python 3.11+
- Git
- im Projektverzeichnis ausgeführt

Die lokale Vorschau erzeugt **nur den aktuellen Stand** aus dem ausgecheckten Working Tree. Es werden keine PR-Snapshots und kein `merged-prs.json` benötigt.

```bash
python .github/pages/generate.py --local
python -m http.server 8000 --directory _site
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

Für den GitHub-Workflow werden die historischen PR-Snapshots weiterhin über:

```bash
python .github/pages/generate.py --with-prs
```

erzeugt.
