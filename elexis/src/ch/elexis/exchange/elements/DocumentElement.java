/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DocumentElement.java 5080 2009-02-03 18:28:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

@SuppressWarnings("serial")
public class DocumentElement extends XChangeElement {
	public static final String XMLNAME = "document";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_ORIGIN = "origin";
	public static final String ATTR_DESTINATION = "destination";
	public static final String ATTR_MIMETYPE = "mimetype";
	public static final String ATTR_SUBJECT = "subject";
	public static final String ATTR_PLACEMENT = "placement";
	public static final String ATTR_DATE = "date";
	public static final String ATTR_RECORDREF = "recordref";
	
	public static final String ELEMENT_XID = "xid";
	public static final String ELEMENT_HINT = "hint";
	public static final String ELEMENT_CONTENTS = "contents";
	
	public static final String PLACEMENT_INLINE = "inline";
	public static final String PLACEMENT_INFILE = "infile";
	public static final String PLACEMENT_URL = "url";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public DocumentElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	
	public DocumentElement(XChangeContainer parent, Brief b){
		super(parent);
		setAttribute(ATTR_MIMETYPE, b.getMimeType());
		setDefaultXid(b.getId());
		setAttribute(ATTR_PLACEMENT, PLACEMENT_INFILE);
		parent.addBinary(getID(), b.loadBinary());
		
		setTitle(b.getLabel());
		setDestination(b.getAdressat());
		setOriginator(Kontakt.load(b.get("AbsenderID")));
		setDate(b.getDatum());
		
		String idex = b.get("BehandlungsID");
		if (idex != null) {
			setAttribute(ATTR_RECORDREF, XMLTool.idToXMLID(idex));
		}
		setHint("Dies ist ein Dokument im OpenDocument-Format. Sie können es zum Beispiel mit OpenOffice (http://www.openoffice.org) lesen");
		parent.addChoice(this, b.getLabel(), b);
	}
	
	public void setTitle(String title){
		setAttribute(ATTR_TITLE, title);
	}
	
	public void setOriginator(Kontakt k){
		if (k != null && k.isValid()) {
			ContactElement ce = getContainer().addContact(k);
			setAttribute(ATTR_ORIGIN, ce.getID());
		}
	}
	
	public void setDestination(Kontakt k){
		if (k != null && k.isValid()) {
			ContactElement ce = getContainer().addContact(k);
			setAttribute(ATTR_DESTINATION, ce.getID());
		}
	}
	
	public void addMeta(String name, String value){
		MetaElement meta = new MetaElement(getContainer(), name, value);
		add(meta);
	}
	
	public void setDate(String date){
		TimeTool tt = new TimeTool(date);
		setAttribute(ATTR_DATE, tt.toString(TimeTool.DATE_ISO));
	}
	
	public void setHint(String hint){
		Element eHint = new Element(ELEMENT_HINT, getContainer().getNamespace());
		eHint.setText(hint);
		getElement().addContent(eHint);
	}
	
	public void setSubject(String subject){
		setAttribute(ATTR_SUBJECT, subject);
	}
	
	public void setMimetype(String desc){
		setAttribute(ATTR_MIMETYPE, desc);
	}
	/*
	 * public Result<String> doImport(Patient p){ XChangeContainer parent=getContainer(); String
	 * id=getAttributeValue("id"); String title=getAttributeValue("title"); String
	 * mimetype=getAttributeValue("type"); if(!Brief.canHandle(mimetype)){ return new
	 * Result<String>(Log.WARNINGS,1,"Unknown mimetype","Cannot handle "+mimetype,true); } Kontakt
	 * sender=parent.findContact(getAttributeValue("sender")); Kontakt
	 * dest=parent.findContact(getAttributeValue("destination")); String
	 * idex=getAttributeValue("recordref"); String idi=parent.mapExtToIntID(idex); Konsultation
	 * kons=Konsultation.load(idi); Brief brief=new Brief(title,new
	 * TimeTool(getAttributeValue("date")),sender,dest,kons,Brief.UNKNOWN);
	 * if(!brief.save(parent.getBinary(id), mimetype)){ return new
	 * Result<String>(Log.WARNINGS,2,"Error while saving","Could not save "+title,true); } return
	 * new Result<String>("OK");
	 * 
	 * }
	 */
}
