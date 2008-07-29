package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

@SuppressWarnings("serial")
public class MarkupElement extends XChangeElement {
	public static final String XMLNAME="markup";
	public static final String ATTR_POS="pos";
	public static final String ATTR_LEN="length";
	public static final String ATTR_TYPE="type";
	public static final String ATTR_TEXT="text";
	public static final String ELEME_META="meta";
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public MarkupElement(XChangeContainer parent){
		super(parent);
	}
}
