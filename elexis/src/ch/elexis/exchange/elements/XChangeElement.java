package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;

public class XChangeElement {
	Element e;
	XChangeContainer parent;
	
	public enum FORMAT{PLAIN,XML,HTML};
	public static final int OK=0;
	public static final int FORMAT_NOT_SUPPORTED=1;
	
	protected XChangeElement(final XChangeContainer p,final Element el){
		parent=p;
		e=el;
	}
	
	protected XChangeElement(final XChangeContainer p){
		parent=p;
	}
	public Element getElement(){
		return e;
	}
	public XChangeContainer getParent(){
		return parent;
	}
	
	/**
	 * return an attribute value of the underlying element.
	 * @param name name of the atribute
	 * @return the value which can be an empty String but is never null.
	 */
	public String getAttr(final String name){
		String ret=e.getAttributeValue(name);
		return ret==null ? "" : ret;
	}
	
	protected void add(final XChangeElement el){
		e.addContent(el.getElement());
	}
	
	@SuppressWarnings("unchecked")
	protected List<Element> getElements(final String name){
		return e.getChildren(name,XChangeContainer.ns);
	}
	
	public Result<String> toString(final FORMAT format){
		return new Result<String>(Log.ERRORS,FORMAT_NOT_SUPPORTED,"Format not supported",null,true);
	}
}
