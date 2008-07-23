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
 *  $Id: ContactRefElement.java 4169 2008-07-23 11:55:30Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.exchange.XChangeContainer;

public class ContactRefElement extends XChangeElement {
	
	public static final String CONTACTREF_DESCRIPTION="description";
	public static final String CONTACTREF_REFID="refID";
	
	public String getXMLName(){
		return "contactref";
	}
	
	public ContactRefElement(XChangeContainer parent){
		super(parent);
	}
	
	public ContactRefElement(XChangeContainer parent, BezugsKontakt bk){
		super(parent);
		Kontakt bezug=bk.getBezugsKontakt();
		String beziehung=bk.getBezug();
		setDescription(beziehung);
		ContactElement ce=parent.addContact(bezug);
		setId(ce.getAttr("id"));
	}
	
	public void setDescription(String type){
		setAttribute(CONTACTREF_DESCRIPTION, type);
	}
	
	public String getDescription(){
		return getAttr(CONTACTREF_DESCRIPTION);
	}
	
	public void setId(String id){
		setAttribute(CONTACTREF_REFID, id);
	}
	
	public String getId(){
		return getAttr(CONTACTREF_REFID);
	}
}
