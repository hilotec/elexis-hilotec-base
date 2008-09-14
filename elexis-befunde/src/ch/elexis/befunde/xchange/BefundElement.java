/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: PrintFindingsDialog.java 2516 2007-06-12 15:56:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde.xchange;

import java.util.List;

import org.jdom.Element;

import ch.elexis.befunde.Messwert;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.exchange.elements.MetaElement;
import ch.elexis.exchange.elements.ResultElement;
import ch.elexis.exchange.elements.XidElement;
import ch.elexis.util.XMLTool;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class BefundElement extends ResultElement {
	
	/**
	 * Ein neues Resultat hinzufügen. Erstellt ggf. das dazugehörige FindingElement. ID des FindingElements ist die id des Messwerts
	 * mit angehängtem Spalten-Namen
	 * @param me 
	 * @param mw
	 * @param fl
	 * @return
	 */
	public static BefundElement addBefund(MedicalElement me, Messwert mw, String field){
		List<FindingElement> findings=me.getAnalyses();
		String raw_id=mw.getId()+field;
		String id=XMLTool.idToXMLID(raw_id);
		for(FindingElement fe:findings){
			XidElement eXid=fe.getXid();
			if(eXid!=null){
				if(id.equals(eXid.getID())){
					BefundElement bf=new BefundElement(me.getContainer(), mw, field);
					me.addAnalyse(bf);
					return bf;
				}
			}
		}
		BefundeItem bi=new BefundeItem(me.getContainer(),mw,field);
		me.addFindingItem(bi);
		BefundElement bf=new BefundElement(me.getContainer(),mw,field);
		me.addAnalyse(bf);
		return bf;
	}
	
	BefundElement(XChangeContainer home, Messwert mw, String field){
		super(home);
		TimeTool tt=new TimeTool(mw.getDate());
		String date=tt.toString(TimeTool.DATE_COMPACT);
		String raw_id=mw.getId()+field+date;
		setAttribute("id",XMLTool.idToXMLID(raw_id));
		setAttribute(ATTR_DATE,tt.toString(TimeTool.DATETIME_XML));
		setAttribute(ATTR_LABITEM,XMLTool.idToXMLID(mw.getId()+field));
		add(new MetaElement(home,ATTRIB_CREATOR,Messwert.PLUGIN_ID));
		Element eResult=new Element(ELEMENT_TEXTRESULT,home.getNamespace());
		eResult.setText(mw.getResult(field));
		addContent(eResult);
		home.addChoice(this, mw.getLabel()+":"+field, mw);
	}
}
