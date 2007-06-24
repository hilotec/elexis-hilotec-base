/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
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

import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.exchange.Container;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class LabElement extends XChangeElement{
	
	LabElement(XChangeContainer p){
		super(p);
		e=new Element("analyse",Container.ns);
	}
	
	public LabElement(XChangeContainer container, Element el) {
		super(container,el);
	}

	public LabElement(XChangeContainer parent, LabResult lr){
		this(parent);
		LabItem li=lr.getItem();
		e.setAttribute("date", new TimeTool(lr.getDate()).toString(TimeTool.DATE_ISO));
		e.setAttribute("classification","lab");
		e.setAttribute("param", li.getKuerzel());
		Labor lab=li.getLabor();
	    ContactElement cLabor=parent.addContact(lab,false);
		e.setAttribute("lab",cLabor.e.getAttributeValue("id"));
		if(li.getTyp().equals(LabItem.typ.NUMERIC)){
			e.setAttribute("type","number");
			e.setAttribute("normrange",li.getRefM());		// TODO anpassen
			e.setAttribute("unit",li.getEinheit());
			
		}else if(li.getTyp().equals(LabItem.typ.ABSOLUTE)){
			e.setAttribute("type","absolute");
		}else if(li.getTyp().equals(LabItem.typ.TEXT)){
			e.setAttribute("type","docref");
		}
		Element eResult=new Element("result",Container.ns);
		e.addContent(eResult);
		eResult.setText(lr.getResult());
		e.setAttribute("abnormal","indeterminate");
		parent.callExportHooks(e, lr);
	}
	
	public LabElement(XChangeContainer parent, Patient p){
		this(parent);
		Kontakt kLab=parent.findContact(e.getAttributeValue("lab"));
		Result<String> ret=new Result<String>("OK");
		if(kLab==null){
			ret.add(Log.ERRORS, 0, "Couldnt find lab", e.getAttributeValue("lab"), true);
			return ;
		}
		String classification=e.getAttributeValue("classification");
		String param=e.getAttributeValue("param");
		String unit=e.getAttributeValue("unit");
		String type=e.getAttributeValue("type");
		LabItem.typ typ;
		if(type.equalsIgnoreCase("number")){
			typ=LabItem.typ.NUMERIC;
		}else if(type.equalsIgnoreCase("text")){
			typ=LabItem.typ.TEXT;
		}else{
			typ=LabItem.typ.ABSOLUTE;
		}
		Query<LabItem> qli=new Query<LabItem>(LabItem.class);
		qli.startGroup();
		qli.add("kuerzel", "=", param);
		qli.or();
		qli.add("titel", "=", param);
		qli.endGroup();
		qli.and();
		qli.add("LaborID", "=", kLab.getId());
		qli.add("Einheit", "=", unit);
		List<LabItem> ll=qli.execute();
		LabItem li;
		String ref=e.getAttributeValue("normrange");
		
		if(ll.size()<1){
			li=new LabItem(param,param,kLab,ref,ref,unit,typ,"import","1");
		}else{
			li=ll.get(0);
			if(p.getGeschlecht().equals("m")){
				li.setRefM(ref);
			}else{
				li.setRefW(ref);
			}
		}
		TimeTool tt=new TimeTool(e.getAttributeValue("date"));
		LabResult lr=new LabResult(p,tt,li,e.getChildText("result",Container.ns),"");
		parent.callImportHooks(e, lr);
	}
	
}
