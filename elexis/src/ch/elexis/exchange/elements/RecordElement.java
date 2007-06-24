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
 *  $Id$
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.*;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas;
import ch.elexis.util.Result;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;

@SuppressWarnings("serial")
public class RecordElement extends XChangeElement{
	
	public RecordElement(XChangeContainer c){
		super(c);
		e=new Element("record",XChangeContainer.ns);
	}
	
	public RecordElement(XChangeContainer c,Element el){
		super(c,el);
	}
	
	public RecordElement(XChangeContainer c, Konsultation k){
		this(c);
		e.setAttribute("date",new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO));
		ContactElement cMandant=parent.addContact(k.getMandant(), false);
		e.setAttribute("responsible",cMandant.e.getAttributeValue("id"));
		e.setAttribute("id",k.getId());
		VersionedResource vr=k.getEintrag();
    	ResourceItem entry=vr.getVersion(vr.getHeadVersion());
    	if(entry!=null){
	    	e.setAttribute("author",entry.remark);
	   	
			Samdas samdas=new Samdas(k.getEintrag().getHead());
			Element sRecord=samdas.getRecordElement();
			if(sRecord!=null){
				//Samdas.Record record=new Samdas.Record(sRecord);
				Element sText=sRecord.getChild("text",Samdas.ns);
				if(sText!=null){
					Element eText=new Element("text",XChangeContainer.ns);
					eText.addContent(sText.getText());
					e.addContent(eText);
				}
			}
    	}
    	parent.callExportHooks(e, k);
	}
	
	public Result<String> doImport(Patient p){
		String kid=e.getAttributeValue("responsible");
		String auth=e.getAttributeValue("author");
		Fall[] faelle=p.getFaelle();
		Fall fall;
		if((faelle==null) || (faelle.length==0)){
			fall=p.neuerFall("Import", "unbekannt", "unbekannt");
		}else{
			fall=faelle[0];
		}
		Konsultation k=fall.neueKonsultation();
		parent.putExtToIntIDMapping(e.getAttributeValue("id"),k.getId());
		Kontakt kResp=parent.findContact(kid);
		kResp.set("istMandant", "1");
		k.set("MandantID",kid);
		k.setDatum(e.getAttributeValue("date"), true);
		Samdas samdas=new Samdas();
		Samdas.Record rec=samdas.getRecord();
		Element eText=e.getChild("text", XChangeContainer.ns);
		if(eText!=null){
			String text=eText.getText();
			if(text!=null){
				rec.setText(text);
			}else {
				rec.setText("");
			}
		}
		
		VersionedResource ve=VersionedResource.load(null);
		ve.update(samdas.toString(), auth);
		k.setEintrag(ve, true);
		parent.callImportHooks(e, k);
		
		// TODO parent.anamnesis.doImport(this, k);
		return new Result<String>("OK");
	}
	
}
