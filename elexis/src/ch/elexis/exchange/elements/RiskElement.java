package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

@SuppressWarnings("serial")
public class RiskElement extends XChangeElement {
	public static final String XMLNAME="risk";
	public static final String ATTR_CONFIRMEDBY="confirmedBy";
	public static final String ATTR_FIRSTMENTIONED="firstMentioned";
	public static final String ATTR_SUBSTANCE="substance";
	public static final String ATTR_RELEVANCE="relevance";
	public static String ELEMENT_META="meta";
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}
	
	public RiskElement(XChangeContainer parent){
		super(parent);
	}

	public RiskElement(XChangeContainer parent, String name){
		super(parent);
		setAttribute(ATTR_SUBSTANCE, name);
		parent.addChoice(this, name);
	}
}
