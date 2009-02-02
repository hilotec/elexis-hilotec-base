/*******************************************************************************
 * Copyright (c) 2008-2009 by G. Weirich 
 * This program is based on the Sgam-Exchange project, 
 * (c) SGAM-Informatics
 * All rights reserved 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XChangeElement.java 5076 2009-02-02 05:55:39Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.Result;

@SuppressWarnings("serial")
public abstract class XChangeElement extends Element {
	private XChangeContainer parent;
	
	public enum FORMAT {
		PLAIN, XML, HTML
	};
	
	public static final int OK = 0;
	public static final int FORMAT_NOT_SUPPORTED = 1;
	
	protected XChangeElement(final XChangeContainer p){
		super();
		parent = p;
		setNamespace(XChangeContainer.ns);
		setName(getXMLName());
	}
	
	public XChangeContainer getContainer(){
		return parent;
	}
	
	public void setContainer(XChangeContainer c){
		parent = c;
	}
	
	public abstract String getXMLName();
	
	/**
	 * return an attribute value of the underlying element.
	 * 
	 * @param name
	 *            name of the atribute
	 * @return the value which can be an empty String but is never null.
	 */
	public String getAttr(final String name){
		String ret = getAttributeValue(name);
		return ret == null ? "" : ret;
	}
	
	public void setDefaultXid(String id){
		XidElement xid = new XidElement(getContainer());
		xid.addIdentity(Xid.DOMAIN_ELEXIS, id, Xid.ASSIGNMENT_LOCAL, true);
		xid.setMainID(null);
		add(xid);
	}
	
	public String getID(){
		String rawID = getAttr("id");
		if (rawID.length() == 0) {
			XidElement eXid = getXid();
			if (eXid != null) {
				rawID = eXid.getAttr("id");
			}
		}
		// return XMLTool.xmlIDtoID(rawID);
		return rawID;
	}
	
	protected void add(final XChangeElement el){
		addContent(el);
	}
	
	public XidElement getXid(){
		return (XidElement) getChild(XidElement.XMLNAME, getContainer().getNamespace());
	}
	
	@SuppressWarnings("unchecked")
	protected List<Element> getElements(final String name){
		return getChildren(name, getContainer().getNamespace());
	}
	
	@Override
	public Element getChild(String name){
		return super.getChild(name, getContainer().getNamespace());
	}
	
	public Result<String> toString(final FORMAT format){
		return new Result<String>(Result.SEVERITY.ERROR, FORMAT_NOT_SUPPORTED,
			"Format not supported", null, true);
	}
	
}
