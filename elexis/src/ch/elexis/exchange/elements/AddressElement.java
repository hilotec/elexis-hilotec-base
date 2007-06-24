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
 *  $Id: AddressElement.java 2627 2007-06-24 14:23:27Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.Anschrift;
import ch.elexis.exchange.XChangeContainer;

public class AddressElement extends XChangeElement {
	public AddressElement(XChangeContainer parent, Anschrift an, String bezug){
		super(parent);
		e=new Element("address",XChangeContainer.ns);
		setAnschrift(an);
		setBezug(bezug);
	}
	public AddressElement(XChangeContainer parent, Element el){
		super(parent,el);
	}
	
	public void setAnschrift(Anschrift an){
		e.setAttribute("street", an.getStrasse());
		e.setAttribute("zip",an.getPlz());
		e.setAttribute("city", an.getOrt());
		e.setAttribute("country",an.getLand());
	}
	
	public void setBezug(String bezug){
		e.setAttribute("type", bezug);
	}
	public String getBezug(){
		return getAttr("type");
	}
	public Anschrift getAnschrift(){
		Anschrift ret=new Anschrift();
		ret.setLand(getAttr("country"));
		ret.setOrt(getAttr("city"));
		ret.setPlz(getAttr("zip"));
		ret.setStrasse(getAttr("street"));
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
}
