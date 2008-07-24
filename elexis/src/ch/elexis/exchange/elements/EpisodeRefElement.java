package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

public class EpisodeRefElement extends XChangeElement {
	public static final String XMLNAME="episode";
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public EpisodeRefElement(XChangeContainer parent){
		super(parent);
	}
	
	public EpisodeRefElement(XChangeContainer parent, EpisodeElement episode){
		super(parent);
		setAttribute("ref", episode.getAttributeValue("id"));
	}
}
