package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class ServiceBlocksElement extends XChangeElement {
	
	public ServiceBlocksElement(XChangeContainer p, Element el){
		super(p, el);
	}
	
	@Override
	public String getXMLName(){
		return ServiceBlockElement.ENCLOSING;
	}
	
}
