package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Query;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;

@SuppressWarnings("serial")
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
				add(se);
			}
		}
	}
	
	public static Leistungsblock createObject(XChangeContainer home, Element el){
		String name=el.getAttributeValue(ATTR_NAME);
		if(!StringTool.isNothing(name)){
			Leistungsblock ret=new Leistungsblock(name,Hub.actMandant);		
			return ret;
		}
		return null;
	}
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
}
