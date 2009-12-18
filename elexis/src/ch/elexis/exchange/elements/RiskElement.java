/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: RiskElement.java 5877 2009-12-18 17:34:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.exchange.xChangeExporter;

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
	
	
	public RiskElement asExporter(xChangeExporter parent, String name){
		asExporter(parent);
		setAttribute(ATTR_SUBSTANCE, name);
		parent.getContainer().addChoice(this, name);
		return this;
	}
}
