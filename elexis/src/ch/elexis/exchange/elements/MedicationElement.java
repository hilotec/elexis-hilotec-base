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
 *  $Id: MedicationElement.java 4173 2008-07-24 10:25:05Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class MedicationElement extends XChangeElement {
	public static final String XMLNAME="medication";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MedicationElement(XChangeContainer parent){
		super(parent);
	}
	public String getFirstDate(){
		String begin=getAttr("start");
		return new TimeTool(begin).toString(TimeTool.DATE_GER);
	}
	
	public String getLastDate(){
		String last=getAttr("last");
		return new TimeTool(last).toString(TimeTool.DATE_GER);
	}
	
	public String getText(){
		return getText();
	}
	public String getDosage(){
		return getAttr("dosage");
	}
	public String getSubstance(){
		return getAttr("substance");
	}
}
