/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich, SGAM.informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AddressElement.java 4290 2008-08-17 16:16:49Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.data.Anschrift;
import ch.elexis.exchange.XChangeContainer;

@SuppressWarnings("serial")
public class AddressElement extends XChangeElement {
	
	public static final String XMLNAME="address";
	public static final String ATTR_STREET="street";
	public static final String ATTR_ZIP="zip";
	public static final String ATTR_CITY="city";
	public static final String ATTR_COUNTRY="country";
	public static final String ATTR_DESCRIPTION="description";
	public static final String VALUE_DEFAULT="default";
	
	public AddressElement(XChangeContainer parent){
		super(parent);
	}
	
	public AddressElement(XChangeContainer parent, Anschrift an, String bezug){
		super(parent);
		setAnschrift(an);
		setBezug(bezug);
	}
	
	public void setAnschrift(Anschrift an){
		setAttribute(ATTR_STREET, an.getStrasse());
		setAttribute(ATTR_ZIP,an.getPlz());
		setAttribute(ATTR_CITY, an.getOrt());
		setAttribute(ATTR_COUNTRY,an.getLand());
	}
	
	public void setBezug(String bezug){
		setAttribute(ATTR_DESCRIPTION, bezug);
	}
	public String getBezug(){
		return getAttr(ATTR_DESCRIPTION);
	}
	
	public Anschrift getAnschrift(){
		Anschrift ret=new Anschrift();
		ret.setLand(getAttr(ATTR_COUNTRY));
		ret.setOrt(getAttr(ATTR_CITY));
		ret.setPlz(getAttr(ATTR_ZIP));
		ret.setStrasse(getAttr(ATTR_STREET));
		return ret;
	}
	
	public String toString(){
		StringBuilder ret=new StringBuilder();
		ret.append(getAttr("street")).append(", ")
			.append(getAttr("zip")).append(" ")
			.append(getAttr("city")).append(" ")
			.append(getAttr("country"));
		return ret.toString();
	}

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

}
