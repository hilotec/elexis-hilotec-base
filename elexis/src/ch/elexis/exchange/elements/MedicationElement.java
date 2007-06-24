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
 *  $Id: MedicationElement.java 2629 2007-06-24 16:31:32Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.TimeTool;

public class MedicationElement extends XChangeElement {

	public MedicationElement(XChangeContainer parent, Element el){
		super(parent,el);
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
		return e.getText();
	}
	public String getDosage(){
		return getAttr("dosage");
	}
	public String getSubstance(){
		return getAttr("substance");
	}
}
