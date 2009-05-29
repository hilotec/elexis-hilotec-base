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
 *    $Id: FieldDescriptor.java 5322 2009-05-29 10:59:45Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util.viewers;

import ch.elexis.data.PersistentObject;
import ch.elexis.selectors.IObjectLink;

public class FieldDescriptor<T extends PersistentObject> {
	
	public enum Typ {
		STRING, INT, CURRENCY, LIST, HYPERLINK, DATE, COMBO
	};
	
	String sAnzeige, sFeldname, sHashname;
	Typ tFeldTyp;
	Object ext;
	
	public FieldDescriptor(String anzeige, String feldname, Typ feldtyp, String hashname){
		sAnzeige = anzeige;
		sFeldname = feldname;
		tFeldTyp = feldtyp;
		sHashname = hashname;
		
	}
	
	public FieldDescriptor(String all){
		sAnzeige = all;
		sFeldname = all;
		tFeldTyp = Typ.STRING;
		sHashname = null;
	}
	
	public FieldDescriptor(String anzeige, String feldname, IObjectLink<T> cp){
		sAnzeige = anzeige;
		sFeldname = feldname;
		ext = cp;
		tFeldTyp = Typ.HYPERLINK;
	}
	
	public FieldDescriptor(String anzeige, String feldname, String hashname, String[] choices){
		sAnzeige = anzeige;
		sFeldname = feldname;
		sHashname = hashname;
		tFeldTyp = Typ.LIST;
		ext = choices;
	}
	
	public FieldDescriptor(String anzeige, String feldname, String hashname, String[] comboItems,
		boolean bDropDown){
		sAnzeige = anzeige;
		sFeldname = feldname;
		sHashname = hashname;
		tFeldTyp = Typ.COMBO;
		ext = comboItems;
	}
}
