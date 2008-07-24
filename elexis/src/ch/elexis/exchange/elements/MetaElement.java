package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

@SuppressWarnings("serial")
public class MetaElement extends XChangeElement {
	public static final String XMLNAME="meta";
	public static final String ATTR_NAME="name";
	public static final String ATTR_VALUE="value";
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}
	
	public MetaElement(XChangeContainer home, String name, String value){
		super(home);
		setAttribute(ATTR_NAME, name);
		setAttribute(ATTR_VALUE,value);
	}

}
