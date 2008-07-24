/*******************************************************************************
 * Copyright (c) 2008 by G. Weirich 
 * This program is based on the Sgam-Exchange project, 
 * (c) SGAM-Informatics
 * All rights resevred 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XChangeElement.java 4173 2008-07-24 10:25:05Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.XMLTool;

public abstract class XChangeElement extends Element{
	private XChangeContainer parent;

	
	public enum FORMAT{PLAIN,XML,HTML};
	public static final int OK=0;
	public static final int FORMAT_NOT_SUPPORTED=1;
	
	protected XChangeElement(final XChangeContainer p){
		super();
		parent=p;
		setNamespace(p.ns);
		setName(getXMLName());
	}

	public XChangeContainer getContainer(){
		return parent;
	}
	
	public abstract String getXMLName();
	
	/**
	 * return an attribute value of the underlying element.
	 * @param name name of the atribute
	 * @return the value which can be an empty String but is never null.
	 */
	public String getAttr(final String name){
		String ret=getAttributeValue(name);
		return ret==null ? "" : ret;
	}
	
	public void setID(String id){
		setAttribute("id", XMLTool.idToXMLID(id));
	}
	public String getID(){
		return XMLTool.xmlIDtoID(getAttr("id"));
	}
	protected void add(final XChangeElement el){
		addContent(el);
	}
	
	@SuppressWarnings("unchecked")
	protected List<Element> getElements(final String name){
		return getChildren(name,getContainer().getNamespace());
	}
	
	@Override
	public Element getChild(String name){
		return super.getChild(name, getContainer().getNamespace());
	}
	
	public Result<String> toString(final FORMAT format){
		return new Result<String>(Log.ERRORS,FORMAT_NOT_SUPPORTED,"Format not supported",null,true);
	}

	//public abstract boolean writeToXML(Patient p);
}
