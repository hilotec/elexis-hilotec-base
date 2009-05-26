/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, SGAM.informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ContactsElement.java 5319 2009-05-26 14:55:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class ContactsElement extends XChangeElement {
	
	public ContactsElement(XChangeContainer home){
		super(home);
	}
	
	public ContactsElement(XChangeContainer p, Element el){
		super(p, el);
	}
	
	@Override
	public String getXMLName(){
		return XChangeContainer.ENCLOSE_CONTACTS;
	}
	
}
