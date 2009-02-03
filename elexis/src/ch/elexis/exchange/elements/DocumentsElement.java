package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class DocumentsElement extends XChangeElement {
	
	public DocumentsElement(XChangeContainer p, Element el){
		super(p, el);
		
	}
	
	@Override
	public String getXMLName(){
		return getContainer().ENCLOSE_DOCUMENTS;
	}
	
}
