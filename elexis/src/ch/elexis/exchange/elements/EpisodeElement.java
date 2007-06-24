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
 *  $Id: DocumentElement.java 1270 2006-11-12 18:08:43Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class EpisodeElement extends XChangeElement{
	
	public EpisodeElement(XChangeContainer parent, Element el){
		super(parent,el);
	}
	public String getBeginDate(){
		return getAttr("date");
	}
	public String getEndDate(){
		return getAttr("inactive");
	}
	public String getTitle(){
		return getAttr("title");
	}
	public String getText(){
		Element text=e.getChild("text", XChangeContainer.ns);
		if(text!=null){
			return text.getText();
		}
		return "";
	}
	
	public String getDiagnosis(){
		Element dia=e.getChild("diagnosis", XChangeContainer.ns);
		if(dia!=null){
			DiagnosisElement de=new DiagnosisElement(parent,dia);
			String ret=de.getCode()+" ("+de.getCodeSystem()+")";
			return ret;
		}
		return "";
	}
	static class DiagnosisElement extends XChangeElement{
		public DiagnosisElement(XChangeContainer parent, Element el) {
			super(parent,el);
		}
		public String getCodeSystem(){
			return getAttr("codesystem");
		}
		public String getCode(){
			return getAttr("code");
		}
	}
}
