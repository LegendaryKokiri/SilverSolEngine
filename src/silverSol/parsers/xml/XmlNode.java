package silverSol.parsers.xml;

import java.util.ArrayList;
import java.util.List;

public class XmlNode {

	private String name;
	private String content;
	private List<XmlAttribute> attributes;
	
	private XmlNode parentNode;
	private List<XmlNode> childNodes;
	
	public XmlNode() {
		this.name = "";
		this.content = "";
		this.attributes = new ArrayList<>();
		
		this.parentNode = null;
		this.childNodes = new ArrayList<>();
	}
	
	public XmlNode getChild(String name) {
		for(XmlNode childNode : childNodes) {
			if(childNode.getName().equals(name)) return childNode;
		}
		
		return null;
	}
	
	public List<XmlNode> getChildren(String name) {
		List<XmlNode> children = new ArrayList<>();
		
		for(XmlNode childNode : childNodes) {
			if(childNode.getName().equals(name)) children.add(childNode);
		}
		
		return children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<XmlAttribute> getAttributes() {
		return attributes;
	}
	
	public String getAttribute(String targetAttribute) {
		for(XmlAttribute attribute : attributes) {
			if(attribute.getName().equals(targetAttribute)) return attribute.getValue();
		}
		
		return "";
	}

	public void addAttribute(XmlAttribute attribute) {
		this.attributes.add(attribute);
	}
	
	public void setAttributes(List<XmlAttribute> attributes) {
		this.attributes = attributes;
	}

	public XmlNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(XmlNode parentNode) {
		this.parentNode = parentNode;
	}

	public List<XmlNode> getChildNodes() {
		return childNodes;
	}

	public void addChildNode(XmlNode childNode) {
		this.childNodes.add(childNode);
	}
	
	protected XmlNode generateChildNode() {
		XmlNode node = new XmlNode();
		node.setParentNode(this);
		this.childNodes.add(node);
		return node;
	}
	
	public void setChildNodes(List<XmlNode> childNodes) {
		this.childNodes = childNodes;
	}
	
	@Override
	public String toString() {
		return "XmlNode " + name;
	}
	
}
