/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Organisation.java 2976 2007-08-10 13:54:03Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

/**
 * Eine Organisation ist eine Kontakt, die ein Kollektiv darstellt.
 * Also eine Firma, eine Versicherung, ein Labor etc.
 * @author gerry
 *
 */
public class Organisation extends Kontakt {
	static{
		addMapping("KONTAKT",
				"Name	=Bezeichnung1",
				"Zusatz1=Bezeichnung2",
				"Zusatz2=ExtInfo",
				"Ansprechperson=Bezeichnung3","istOrganisation",
				"Zusatz3=TITEL",
				"Tel. direkt=NatelNr"
				);
	}
	
	@Override
	public boolean isValid(){
		return super.isValid();
	}
	@Override
	protected String getTableName() {
		return "KONTAKT";
	}
	Organisation(){/* leer */}
	protected Organisation(final String id){
	    super(id);
    }
	/** Eine Organisation bei gegebener ID aus der Datenbank einlesen */
    public static Organisation load(final String id){
        return new Organisation(id);
    }
    /** Eine neue Organisation erstellen */
    public Organisation(final String Name, final String Zusatz1){
    	create(null);
    	set(new String[]{"Name","Zusatz1"},new String[]{Name,Zusatz1});
    }
	@Override
	protected String getConstraint() {
		return "istOrganisation='1'";
	}
	@Override
	protected void setConstraint() {
		set("istOrganisation","1");
	}
    
}
