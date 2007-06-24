package ch.elexis.exchange.elements;

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
}
