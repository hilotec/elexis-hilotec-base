package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class RisksElement extends XChangeElement {
	
	public RisksElement(XChangeContainer home, Element el){
		super(home, el);
	}
	
	@Override
	public String getXMLName(){
		return getContainer().ENCLOSE_RISKS;
	}
	
}
