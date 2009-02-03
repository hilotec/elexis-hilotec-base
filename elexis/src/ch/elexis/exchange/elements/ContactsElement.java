package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class ContactsElement extends XChangeElement {
	
	public ContactsElement(XChangeContainer home){
		super(home);
	}
	
	public ContactsElement(XChangeContainer p, Element el){
		super(p, el);
	}
	
	@Override
	public String getXMLName(){
		return XChangeContainer.ENCLOSE_CONTACTS;
	}
	
}
