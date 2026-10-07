from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
import argparse
import subprocess
import os
import sys

ROOT = Path(__file__).resolve().parents[2]
SITE = ROOT / "_site"

parser = argparse.ArgumentParser()
parser.add_argument("--port", type=int, default=8000)
parser.add_argument(
    "--simulate-pr",
    nargs="?",
    const="main",
    metavar="BASE",
    help="Probe-PR: Arbeitsverzeichnis gegenüber BASE (Standard: main)",
)
args = parser.parse_args()

generate_args = ["--local"]
if args.simulate_pr:
    generate_args += ["--simulate-pr", args.simulate_pr]

subprocess.run(
    [sys.executable, str(ROOT / ".github/pages/generate.py"), *generate_args],
    cwd=ROOT,
    check=True,
)

os.chdir(SITE)

server = ThreadingHTTPServer(("127.0.0.1", args.port), SimpleHTTPRequestHandler)
print(f"Lokale Vorschau: http://127.0.0.1:{args.port}/")
print("Beenden mit Ctrl+C")
server.serve_forever()
