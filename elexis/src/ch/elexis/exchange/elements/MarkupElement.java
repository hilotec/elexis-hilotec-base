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
 *  $Id: MarkupElement.java 5080 2009-02-03 18:28:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas.XRef;

@SuppressWarnings("serial")
public class MarkupElement extends XChangeElement {
	public static final String XMLNAME = "markup";
	public static final String ATTR_POS = "pos";
	public static final String ATTR_LEN = "length";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_TEXT = "text";
	public static final String ATTRIB_HINT = "hint";
	public static final String ELEME_META = "meta";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MarkupElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	
	public MarkupElement(XChangeContainer home, XRef xref){
		super(home);
		setAttribute(ATTR_POS, Integer.toString(xref.getPos()));
		setAttribute(ATTR_LEN, Integer.toString(xref.getLength()));
		setAttribute(ATTR_TYPE, xref.getProvider());
		add(new MetaElement(home, "id", xref.getID()));
		add(new MetaElement(home, "provider", xref.getProvider()));
	}
}
