package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class XChangeElement {
	Element e;
	XChangeContainer parent;
	protected XChangeElement(XChangeContainer p,Element el){
		parent=p;
		e=el;
	}
	
	protected XChangeElement(XChangeContainer p){
		parent=p;
	}
	public Element getElement(){
		return e;
	}
	public XChangeContainer getParent(){
		return parent;
	}
	
	/**
	 * return an attribute value of the underlying element.
	 * @param name name of the atribute
	 * @return the value which can be an empty String but is never null.
	 */
	public String getAttr(String name){
		String ret=e.getAttributeValue(name);
		return ret==null ? "" : ret;
	}
	
	protected void add(XChangeElement el){
		e.addContent(el.getElement());
	}
	
	@SuppressWarnings("unchecked")
	protected List<Element> getElements(String name){
		return e.getChildren(name,XChangeContainer.ns);
	}
}
