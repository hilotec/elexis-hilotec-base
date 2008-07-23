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
 *  $Id: EpisodeElement.java 4169 2008-07-23 11:55:30Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class EpisodeElement extends XChangeElement{
	public static final String XMLNAME="episode";
	public static final String ATTR_BEGINDATE="beginDate";
	public static final String ATTR_ENDDATE="endDate";
	public static final String ATTR_TITLE="name";
	public static final String ELEMENT_DIAGNOSIS="diagnosis";
	public static final String ATTR_CODESYSTEM="codesystem";
	public static final String ATTR_CODE="code";
	
	public String getXMLName(){
		return XMLNAME;
	}
	public EpisodeElement(XChangeContainer parent){
		super(parent);
	}
	
	public EpisodeElement(XChangeContainer parent, Konsultation k, IDiagnose dg){
		super(parent);
		setAttribute(ATTR_BEGINDATE,new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO));
		setAttribute("id",StringTool.unique("episode"));
		DiagnosisElement eDiag=new DiagnosisElement(parent,dg);
		addContent(eDiag);
		setAttribute(ATTR_TITLE,dg.getLabel());
		InsuranceElement eInsurance=new InsuranceElement(parent,k);
		addContent(eInsurance);
	}
	// public EpisodeElement(XChangeContainer parent, )
	public String getBeginDate(){
		return getAttr(ATTR_BEGINDATE);
	}
	public String getEndDate(){
		return getAttr(ATTR_ENDDATE);
	}
	public String getTitle(){
		return getAttr(ATTR_TITLE);
	}
	public String getText(){
		Element text=getChild("text", getContainer().getNamespace());
		if(text!=null){
			return text.getText();
		}
		return "";
	}
	
	public String getDiagnosis(){
		Element dia=getChild(ELEMENT_DIAGNOSIS, getContainer().getNamespace());
		if(dia!=null){
			DiagnosisElement de=new DiagnosisElement(getContainer());
			String ret=de.getCode()+" ("+de.getCodeSystem()+")";
			return ret;
		}
		return "";
	}
	
	static class DiagnosisElement extends XChangeElement{
		public String getXMLName(){
			return ELEMENT_DIAGNOSIS;
		}
		public DiagnosisElement(XChangeContainer parent) {
			super(parent);
		}
		
		public DiagnosisElement(XChangeContainer parent, IDiagnose dg) {
			super(parent);
			setAttribute(ATTR_CODESYSTEM,dg.getCodeSystemName());
			setAttribute(ATTR_CODE,dg.getCode());
		}

		public String getCodeSystem(){
			return getAttr(ATTR_CODESYSTEM);
		}
		public String getCode(){
			return getAttr(ATTR_CODE);
		}
	}
}
