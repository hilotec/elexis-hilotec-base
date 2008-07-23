/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DocumentElement.java 4169 2008-07-23 11:55:30Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class DocumentElement extends XChangeElement{
	public static final String XMLNAME="document";
	public static final String ATTR_TITLE="title";
	public static final String ATTR_ORIGIN="origin";
	public static final String ATTR_DESTINATION="destination";
	public static final String ATTR_MIMETYPE="mimetype";
	public static final String ATTR_SUBJECT="subject";
	public static final String ATTR_PLACEMENT="placement";
	public static final String ATTR_DATE="date";
	public static final String ATTR_RECORDREF="recordref";
	
	public static final String ELEMENT_XID="xid";
	public static final String ELEMENT_HINT="hint";
	public static final String ELEMENT_CONTENTS="contents";
	
	public static final String PLACEMENT_INLINE="inline";
	public static final String PLACEMENT_INFILE="infile";
	public static final String PLACEMENT_URL="url";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public DocumentElement(XChangeContainer parent){
		super(parent);
	}
	
	public DocumentElement(XChangeContainer parent, Brief b){
		this(parent);
		setAttribute(ATTR_MIMETYPE,b.getMimeType());
		setAttribute("id",b.getId());
		setAttribute(ATTR_PLACEMENT,PLACEMENT_INFILE);
		parent.addBinary(b.getId(), b.loadBinary());
		setAttribute(ATTR_TITLE,b.getLabel());
		ContactElement eAdr=parent.addContact(b.getAdressat());
		setAttribute(ATTR_DESTINATION,eAdr.getAttributeValue("id"));
		ContactElement eAbs=parent.addContact(Kontakt.load(b.get("AbsenderID")));
		setAttribute(ATTR_ORIGIN,eAbs.getAttributeValue("id"));
		setAttribute(ATTR_DATE,new TimeTool(b.getDatum()).toString(TimeTool.DATE_ISO));
		String idex=b.get("BehandlungsID");
		if(idex!=null){
			setAttribute(ATTR_RECORDREF, idex);
		}
	}
	
	/*
	public Result<String> doImport(Patient p){
		XChangeContainer parent=getContainer();
		String id=getAttributeValue("id");
		String title=getAttributeValue("title");
		String mimetype=getAttributeValue("type");
		if(!Brief.canHandle(mimetype)){
			return new Result<String>(Log.WARNINGS,1,"Unknown mimetype","Cannot handle "+mimetype,true);
		}
		Kontakt sender=parent.findContact(getAttributeValue("sender"));
		Kontakt dest=parent.findContact(getAttributeValue("destination"));
		String idex=getAttributeValue("recordref");
		String idi=parent.mapExtToIntID(idex);
		Konsultation kons=Konsultation.load(idi);
		Brief brief=new Brief(title,new TimeTool(getAttributeValue("date")),sender,dest,kons,Brief.UNKNOWN);
		if(!brief.save(parent.getBinary(id), mimetype)){
			return new Result<String>(Log.WARNINGS,2,"Error while saving","Could not save "+title,true);
		}
		return new Result<String>("OK");

	}
	*/
}
