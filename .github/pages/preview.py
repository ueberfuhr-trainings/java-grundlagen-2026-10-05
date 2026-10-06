from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
import argparse
import subprocess
import os

ROOT = Path(__file__).resolve().parents[2]
SITE = ROOT / "_site"

parser = argparse.ArgumentParser()
parser.add_argument("--port", type=int, default=8000)
args = parser.parse_args()

subprocess.run(
    ["python", str(ROOT / ".github/pages/generate.py"), "--local"],
    cwd=ROOT,
    check=True,
)

os.chdir(SITE)

server = ThreadingHTTPServer(("127.0.0.1", args.port), SimpleHTTPRequestHandler)
print(f"Lokale Vorschau: http://127.0.0.1:{args.port}/")
print("Beenden mit Ctrl+C")
server.serve_forever()
