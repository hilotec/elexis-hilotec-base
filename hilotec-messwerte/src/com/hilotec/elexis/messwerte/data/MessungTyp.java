/*******************************************************************************
 * Copyright (c) 2009, A. Kaufmann and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id: MessungTyp.java 5386 2009-06-23 11:34:17Z rgw_ch $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.util.ArrayList;

/**
 * Typ einer Messung 
 * @author Antoine Kaufmann
 */
public class MessungTyp {
	String name;
	String title;
	ArrayList<IMesswertTyp> fields;
	
	public MessungTyp(String n, String t) {
		name = n;
		title = t;
		fields = new ArrayList<IMesswertTyp>();
	}
	
	/**
	 * Neuen Messwerttyp hinzufügen
	 */
	public void addField(IMesswertTyp f) {
		fields.add(f);
	}
	
	/**
	 * @return Interner Name dieses Messungstyps
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Beschriftung die dem Benutzer angezeigt werden kann
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Typen saemtlicher Messwerte in dieser Messung holen
	 * @return Liste aller Messwert-Typen
	 */
	public ArrayList<IMesswertTyp> getMesswertTypen() {
		return fields;
	}
	
	/**
	 * Bestimmten Messwert-Typ dieser Messung anhand seines Namens heraussuchen
	 * @param name
	 * @return
	 */
	public IMesswertTyp getMesswertTyp(String name) {
		for (IMesswertTyp f: fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
}
