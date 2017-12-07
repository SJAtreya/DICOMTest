package com.example.demo

import java.util.concurrent.ConcurrentHashMap.ForEachEntryTask

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Keyword
import org.dcm4che3.data.Sequence
import org.dcm4che3.data.Tag
import org.dcm4che3.io.DicomInputStream

class SRParser {

	static def file1 = "C:/Users/sudharshana/Workspace/fuji/samples/srdoc103/srdoc103/report12.dcm"
	static def file2 = "C:/Users/sudharshana/Workspace/fuji/samples/srdoc103/srdoc103/test.dcm"
	static def file3 = "C:/Tools/jdicom/srdoc103/P21/P21/P21_Cardiac__(_Temp010270210910_)/T4S69RS_/S0000002/R0000001/SRRPRT01"

	static def tree = null

	static void main(String[] args) {
		new SRParser().read()
	}

	def read() {
		Attributes attributes = new DicomInputStream(new File(file2)).readDataset(-1, -1)
		attributes.values.eachWithIndex{it, index->
			if (it instanceof Sequence) {
				if (!tree) {
					createBaseContainer(it, attributes)
				} else {
					findChild(it, tree)
				}
			}
		}
		println tree
		tree
	}

	void createBaseContainer(container, attributes) {
		def valueType = null
		container[0].parent.tags.eachWithIndex{tag, tagIndex ->
			if (Tag.ValueType.value == tag) {
				valueType = new String(container[0].parent.values[tagIndex])
			}
		}
		tree = new Node<Object>(decodeData(container, valueType, false))
		tree.data.put("ContinuityOfContent", new String(attributes.getValue(Tag.ContinuityOfContent.value)))
	}

	private void findChild(sequence, node) {
		sequence.each{item ->
			parse(item, node)
		}
	}

	private def parse(item, node) {
		def (hasRelation, childIndex) = [false, -1]
		def (relationType, valueType, child) = [null, null, null]
		item.tags.eachWithIndex{tag, index ->
			if (Tag.RelationshipType.value == tag) {
				relationType = new String(item.values[index])
				hasRelation = true
			}
			if (Tag.ValueType.value == tag) {
				valueType = new String(item.values[index])
			}
		}
		item.vrs.eachWithIndex{vr, index ->
			if (vr.toString() == "SQ") {
				childIndex = index
			}
		}
		if  (hasRelation) {
			if (childIndex > -1) {
				findChild(item.values[childIndex], node.addChild(decodeData(item, valueType, true)))
			}
		}
	}

	private def decodeData(item, valueType, traverseThroughChild) {
		def valueTags = null, sequenceTag = null
		switch(valueType.trim()) {
			case "CONTAINER":
				valueTags = [Tag.ContinuityOfContent.value, Tag.CodeMeaning.value]
				break
			case "TEXT":
				valueTags = [Tag.TextValue.value]
				break
			case "CODE":
				valueTags = [Tag.CodeMeaning.value]
				break
			case "NUM":
				valueTags = [Tag.NumericValue.value, Tag.CodeValue.value]
				sequenceTag = Tag.MeasuredValueSequence.value
				break
			case "PNAME":
				valueTags = [Tag.CodeMeaning.value]
				break
			case "DATE":
				valueTags = [Tag.Date.value]
				break
			case "DATETIME":
				valueTags = [Tag.DateTime.value]
				break
			case "TIME":
				valueTags = [Tag.Time.value]
				break
			case "UIDREF":
				valueTags = [Tag.UID.value]
			// Refers to other studies or series or instances. Find from the DB and show as link.
				break
			case "IMAGE":
				valueTags = [Tag.ReferencedSOPClassUID.value, Tag.ReferencedSOPInstanceUID.value, Tag.FrameNumbersOfInterest.value]
				break
			case "WAVEFORM":
				valueTags = [Tag.ReferencedSOPClassUID.value, Tag.ReferencedSOPInstanceUID.value, Tag.ReferencedSOPInstanceUID.value]
				break
			case "COMPOSITE":
				valueTags = [Tag.ReferencedSOPClassUID.value, Tag.ReferencedSOPInstanceUID.value, Tag.ReferencedSOPInstanceUID.value]
				break
			case "SCOORD":
				valueTags = [Tag.ReferencedSOPClassUID.value, Tag.ReferencedSOPInstanceUID.value, Tag.GraphicType.value, Tag.GraphicData.value]
				break
			case "TCOORD":
				valueTags = [Tag.ReferencedSOPClassUID.value, Tag.ReferencedSOPInstanceUID.value, Tag.TemporalRangeType.value, Tag.ReferencedTimeOffsets.value]
				break
			default:
				valueTags = [Tag.CodeMeaning.value]
				break
		}
		getData(item, valueTags + defaultTags(), sequenceTag) << [type:valueType]	
	}

	private def getData(item, matchingTags, sequenceTag) {
		def retVal = [:]
		if (item instanceof Sequence) {
			item.each {retVal << extractData(it, matchingTags, sequenceTag)}
		} else {
			retVal << extractData(item, matchingTags, sequenceTag)
		}
		retVal
	}

	private def extractData(attributes, matchingTags, sequenceTag) {
		def map = [:]
		def tag = null
		def subItems = []
		def subSequence = null
		attributes.vrs.eachWithIndex{vr, index->
			tag = attributes.tags[index]
			if (tag in matchingTags) {
				def matchingTag = Keyword.valueOf(tag)
				def value= new String(attributes.values[index])
				map.put(matchingTag, value)
			}
			if (vr.toString() == "SQ") {
				subItems.add(attributes.values[index])
			}
		}	
		if (sequenceTag && ((subSequence = attributes.getValue(sequenceTag))!=null)) {
			 subItems.add(subSequence)
		}
		subItems.each { map << getData(it, matchingTags, null) }		
		map
	}

	def defaultTags() {
		[Tag.CodeMeaning.value, Tag.CodingSchemeDesignator.value, Tag.CodeValue.value]
	}
}