BASE ?= main

preview:
	python preview.py
preview-pr:
	python preview.py --simulate-pr $(BASE)
validate:
	python -m py_compile generate.py
