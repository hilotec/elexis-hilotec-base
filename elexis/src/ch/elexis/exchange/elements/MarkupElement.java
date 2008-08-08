package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas.XRef;

@SuppressWarnings("serial")
public class MarkupElement extends XChangeElement {
	public static final String XMLNAME="markup";
	public static final String ATTR_POS="pos";
	public static final String ATTR_LEN="length";
	public static final String ATTR_TYPE="type";
	public static final String ATTR_TEXT="text";
	public static final String ATTRIB_HINT="hint";
	public static final String ELEME_META="meta";
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public MarkupElement(XChangeContainer parent){
		super(parent);
	}
	
	public MarkupElement(XChangeContainer home, XRef xref){
		super(home);
		setAttribute(ATTR_POS, Integer.toString(xref.getPos()));
		setAttribute(ATTR_LEN,Integer.toString(xref.getLength()));
		setAttribute(ATTR_TYPE,xref.getProvider());
		add(new MetaElement(home,"id",xref.getID()));
		add(new MetaElement(home,"provider",xref.getProvider()));
	}
}
