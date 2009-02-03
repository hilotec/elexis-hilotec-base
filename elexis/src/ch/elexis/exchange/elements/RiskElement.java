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
 *  $Id: RiskElement.java 5080 2009-02-03 18:28:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

@SuppressWarnings("serial")
public class RiskElement extends XChangeElement {
	public static final String XMLNAME = "risk";
	public static final String ATTR_CONFIRMEDBY = "confirmedBy";
	public static final String ATTR_FIRSTMENTIONED = "firstMentioned";
	public static final String ATTR_SUBSTANCE = "substance";
	public static final String ATTR_RELEVANCE = "relevance";
	public static String ELEMENT_META = "meta";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public RiskElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	
	public RiskElement(XChangeContainer parent, String name){
		super(parent);
		setAttribute(ATTR_SUBSTANCE, name);
		parent.addChoice(this, name);
	}
}
