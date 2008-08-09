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

import ch.elexis.befunde.Messwert;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.exchange.elements.ResultElement;
import ch.elexis.util.XMLTool;

@SuppressWarnings("serial")
public class BefundElement extends ResultElement {
	
	public static BefundElement addBefund(MedicalElement me, Messwert mw, String[] fl){
		List<FindingElement> findings=me.getAnalyses();
		for(String field:fl){
			String raw_id=mw.getId()+field;
			String id=XMLTool.idToXMLID(raw_id);
			for(FindingElement fe:findings){
				if(fe.getXid().getID().equals(id)){
					BefundElement bf=new BefundElement(me.getContainer(),mw);
					me.addAnalyse(bf);
					return bf;
				}
			}
			BefundeItem bi=new BefundeItem(me.getContainer());
			me.addFindingItem(bi);
		}
		BefundElement bf=new BefundElement(me.getContainer(),mw);
		me.addAnalyse(bf);
		return bf;
	}
	BefundElement(XChangeContainer home, Messwert mw){
		super(home);
	
	}
}
