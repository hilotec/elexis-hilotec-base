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
 *  $Id: MedicationElement.java 5319 2009-05-26 14:55:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class MedicationElement extends XChangeElement {
	public static final String XMLNAME = "medication";
	public static final String ATTRIB_BEGINDATE = "startDate";
	public static final String ATTRIB_ENDDATE = "stopDate";
	public static final String ATTRIB_PRODUCT = "product";
	public static final String ATTRIB_DOSAGE = "dosage";
	public static final String ATTRIB_UNITS = "dosageUnit";
	public static final String ATTRIB_FREQUENCY = "frequency";
	public static final String ATTRIB_SUBSTANCE = "substance";
	public static final String ATTRIB_REMARK = "remark";
	public static final String ELEMENT_XID = "xid";
	public static final String ELEMENT_META = "meta";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MedicationElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	
	public MedicationElement(XChangeContainer parent, Prescription pr){
		super(parent);
		Artikel art = pr.getArtikel();
		String begin = pr.getBeginDate();
		String end = pr.getEndDate();
		String dose = pr.getDosis();
		String remark = pr.getBemerkung();
		setAttribute(ATTRIB_BEGINDATE, XMLTool.dateToXmlDate(begin));
		if (!StringTool.isNothing(end)) {
			setAttribute(ATTRIB_ENDDATE, XMLTool.dateToXmlDate(end));
		}
		setAttribute(ATTRIB_FREQUENCY, dose);
		setAttribute(ATTRIB_PRODUCT, art.getLabel());
		setAttribute(ATTRIB_REMARK, remark);
		add(new XidElement(parent, art));
		parent.addChoice(this, pr.getLabel(), pr);
	}
	
	public String getFirstDate(){
		String begin = getAttr(ATTRIB_BEGINDATE);
		return new TimeTool(begin).toString(TimeTool.DATE_GER);
	}
	
	public String getLastDate(){
		String last = getAttr(ATTRIB_ENDDATE);
		return new TimeTool(last).toString(TimeTool.DATE_GER);
	}
	
	public String getText(){
		return getAttr(ATTRIB_SUBSTANCE);
	}
	
	public String getDosage(){
		return getAttr(ATTRIB_DOSAGE);
	}
	
	public String getSubstance(){
		return getAttr(ATTRIB_SUBSTANCE);
	}
}
