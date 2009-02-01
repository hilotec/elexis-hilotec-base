package ch.elexis.exchange.elements;

import java.util.List;

import ch.elexis.data.ICodeElement;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Leistungsblock;
import ch.elexis.exchange.XChangeContainer;

public class ServiceBlockElement extends XChangeElement {
	public static final String XMLNAME="serviceblock";
	public static final String ENCLOSING="serviceblocks";
	public static final String ATTR_NAME="name";
	
	public ServiceBlockElement(XChangeContainer p, Leistungsblock lb){
		super(p);
		setAttribute(ATTR_NAME, lb.getName());
		List<ICodeElement> ics=lb.getElements();
		for(ICodeElement ic:ics){
			if(ic instanceof IVerrechenbar ){
				IVerrechenbar iv=(IVerrechenbar) ic;
				ServiceElement se=new ServiceElement(p,iv);
				addContent(se);
			}
		}
	}
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
}
