package com.example.demo

class SRPrinter {

	static void main(String[] args) {
		new SRPrinter().print()		
	}
	
	def print() {
		Node<Object> tree = new SRParser().read()
		// FIXME - Handle main containers' continuity.
		tree.children.each {
			handleChildren(it)
		}
	}
	
	def handleChildren(node) {
		def toPrint = []
		switch(node.data.type.trim()) {
			case "CONTAINER":
				printContainerContents(toPrint, node)
				break
			case "UIDREF":
				println "here in uidref"
				break
			default:
				println "Not sure how to present this: ${node.data.type}"
				break
		}
		println "Extracted: ${toPrint}"
	}
	
	def printContainerContents(toPrint, container) {
		if (container.data.ContinuityOfContent == "SEPARATE") {
			// Print individually
			container.children.each {childNode ->
				toPrint.add(childNode.data)
			}
		} else {
			toPrint.add(container.children.data.CodeValue.join(" "))
		}
	}
}
