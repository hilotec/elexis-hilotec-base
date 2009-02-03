package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class AnalysesElement extends XChangeElement {
	
	public AnalysesElement(XChangeContainer p){
		super(p);
	}
	
	public AnalysesElement(XChangeContainer p, Element el){
		super(p, el);
	}
	
	@Override
	public String getXMLName(){
		return FindingElement.ENCLOSING;
	}
	
}
