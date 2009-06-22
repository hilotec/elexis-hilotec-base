/*******************************************************************************
 * Copyright (c) 2008-2009 by G. Weirich 
 * This program is based on the Sgam-Exchange project, 
 * (c) SGAM-Informatics
 * All rights reserved 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XChangeElement.java 5382 2009-06-22 16:19:21Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public abstract class XChangeElement {
	protected static final String ID="id";
	private XChangeContainer parent;
	private Element ex = null;
	
	public enum FORMAT {
		PLAIN, XML, HTML
	};
	
	public static final int OK = 0;
	public static final int FORMAT_NOT_SUPPORTED = 1;
	
	protected XChangeElement(final XChangeContainer p){
		ex = new Element(getXMLName(), XChangeContainer.ns);
		parent = p;
	}
	
	protected XChangeElement(XChangeContainer parent, Element el){
		this.parent = parent;
		ex = el == null ? new Element(getXMLName(), XChangeContainer.ns) : el;
	}
	
	public Element getElement(){
		return ex;
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
		String ret = ex.getAttributeValue(name);
		return ret == null ? "" : ret;
	}
	
	public void setDefaultXid(String id){
		XidElement xid = new XidElement(getContainer());
		xid.addIdentity(Xid.DOMAIN_ELEXIS, id, Xid.ASSIGNMENT_LOCAL, true);
		xid.setMainID(null);
		add(xid);
	}
	
	public String getID(){
		String rawID = getAttr(ID);
		if (rawID.length() == 0) {
			XidElement eXid = getXid();
			if (eXid != null) {
				rawID = eXid.getAttr(ID);
			}
		}
		// return XMLTool.xmlIDtoID(rawID);
		return rawID;
	}
	
	public void add(final XChangeElement el){
		ex.addContent(el.ex);
	}
	
	public XidElement getXid(){
		return new XidElement(getContainer(), ex.getChild(XidElement.XMLNAME, getContainer()
			.getNamespace()));
	}
	
	public List<? extends XChangeElement> getChildren(final String name,
		final Class<? extends XChangeElement> clazz){
		LinkedList<XChangeElement> ret = new LinkedList<XChangeElement>();
		for (Object el : ex.getChildren(name, XChangeContainer.ns)) {
			try {
				XChangeElement xc =
					clazz.getConstructor(XChangeContainer.class, Element.class).newInstance(
						getContainer(), (Element) el);
				ret.add(xc);
			} catch (Exception e) {
				ExHandler.handle(e);
				return null;
			}
		}
		return ret;
	}
	
	public XChangeElement getChild(String name, Class<? extends XChangeElement> clazz){
		Element el = ex.getChild(name, getContainer().getNamespace());
		if(el==null){
			return null;
		}
		XChangeElement ret;
		try {
			ret =
				clazz.getConstructor(XChangeContainer.class, Element.class).newInstance(
					getContainer(), el);
			return ret;
		} catch (Exception e) {
			ExHandler.handle(e);
			return null;
		}
	}
	
	public Result<String> toString(final FORMAT format){
		return new Result<String>(Result.SEVERITY.ERROR, FORMAT_NOT_SUPPORTED,
			"Format not supported", null, true);
	}
	
	public void setAttribute(String attr, String value){
		ex.setAttribute(attr, value);
	}
}
