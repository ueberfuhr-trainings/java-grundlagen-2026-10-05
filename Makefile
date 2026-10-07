.PHONY: render-uml

UML_SOURCE ?= docs
UML_TARGET ?= $(UML_SOURCE)

render-uml:
	./.tools/render-uml.sh $(UML_SOURCE) $(UML_TARGET)
