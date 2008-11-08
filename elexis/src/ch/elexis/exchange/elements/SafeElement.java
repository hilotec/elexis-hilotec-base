package ch.elexis.exchange.elements;

import org.jdom.Element;
import org.jdom.Namespace;

public class SafeElement extends Element {
	public SafeElement(String name, Namespace ns){
		super(name,ns);
	}
	
	public SafeElement(String name){
		super(name);
	}
	
	/**
	 * This sets an attribute in a safe manner: If the value is null, some
	 * useful action is taken instead og throwing an exception
	 * @param name name of the attribute
	 * @param value value of the attribute
	 * @param defaultValue default to use if value resolves to null. If defaultValue is null, 
	 * the attrbute will not be set at all.
	 */
	public void setAttributeEx(String name,String value, String defaultValue){
		if( (name==null) || (name.length()==0)){
			return;
		}
		if(value==null){
			if(defaultValue==null){
				return;
			}else{
				setAttribute(name,defaultValue);
			}
		}else{
			setAttribute(name,value);
		}
	}
}
