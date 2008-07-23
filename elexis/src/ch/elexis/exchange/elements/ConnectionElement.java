package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

/**
 * A connection e.g. phone or mail 
 * @author gerry
 *
 */
@SuppressWarnings("serial")
public class ConnectionElement extends XChangeElement {

	@Override
	public String getXMLName() {
		return "connection";
	}

	public ConnectionElement(XChangeContainer parent){
		super(parent);
	}
	
	public ConnectionElement(XChangeContainer parent, String type, String cx){
		super(parent);
	}
}
