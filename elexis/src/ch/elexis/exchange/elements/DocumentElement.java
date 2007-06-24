/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DocumentElement.java 2623 2007-06-24 11:06:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.TimeTool;

public class DocumentElement extends XChangeElement{
		
	public DocumentElement(XChangeContainer parent){
		super(parent);
		e=new Element("document",XChangeContainer.ns);
	}
	public DocumentElement(XChangeContainer parent, Element e){
		super(parent,e);
	}
	
	public DocumentElement(XChangeContainer parent, Brief b){
		this(parent);
		e.setAttribute("type",b.getMimeType());
		e.setAttribute("id",b.getId());
		e.setAttribute("content","ext");
		parent.addBinary(b.getId(), b.loadBinary());
		e.setAttribute("title",b.getLabel());
		ContactElement eAdr=parent.addContact(b.getAdressat(), false);
		e.setAttribute("destination",eAdr.e.getAttributeValue("id"));
		ContactElement eAbs=parent.addContact(Kontakt.load(b.get("AbsenderID")),false);
		e.setAttribute("sender",eAbs.e.getAttributeValue("id"));
		e.setAttribute("date",new TimeTool(b.getDatum()).toString(TimeTool.DATE_ISO));
		String idex=b.get("BehandlungsID");
		if(idex!=null){
			e.setAttribute("recordref", idex);
		}
		parent.callExportHooks(e, b);
	}
	
	public Result<String> doImport(Patient p){
		
		String id=e.getAttributeValue("id");
		String title=e.getAttributeValue("title");
		String mimetype=e.getAttributeValue("type");
		if(!Brief.canHandle(mimetype)){
			return new Result<String>(Log.WARNINGS,1,"Unknown mimetype","Cannot handle "+mimetype,true);
		}
		Kontakt sender=parent.findContact(e.getAttributeValue("sender"));
		Kontakt dest=parent.findContact(e.getAttributeValue("destination"));
		String idex=e.getAttributeValue("recordref");
		String idi=parent.mapExtToIntID(idex);
		Konsultation kons=Konsultation.load(idi);
		Brief brief=new Brief(title,new TimeTool(e.getAttributeValue("date")),sender,dest,kons,Brief.UNKNOWN);
		if(!brief.save(parent.getBinary(id), mimetype)){
			return new Result<String>(Log.WARNINGS,2,"Error while saving","Could not save "+title,true);
		}else{
			parent.callImportHooks(e, brief);
		}
		return new Result<String>("OK");

	}
}
