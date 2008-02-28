/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Egclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Labor.java 3713 2008-02-28 18:02:52Z danlutz $
 *******************************************************************************/

package ch.elexis.data;

public class Labor extends Organisation {
	static{
		addMapping("KONTAKT",
				"Name	=Bezeichnung1",
				"Zusatz1=Bezeichnung2",
				"Zusatz2=ExtInfo",
				"Kuerzel=PatientNr",
				"Ansprechperson=Bezeichnung3","istOrganisation","istLabor"
				);
	}
	@Override
	protected String getConstraint() {
		return "istLabor='1'";
	}
	@Override
	protected void setConstraint() {
		set(new String[]{"istLabor","istOrganisation"},"1","1");
	}
	public Labor(String Kuerzel,String Name){
		super(Name,"Labor");
		set("Kuerzel",Kuerzel);
	}
	public static Labor load(String id){
		 Labor ret=new Labor(id);
		 if(ret.exists()){
	        return ret;
		 }
		 return null;
	}
	protected Labor(String id){
		super(id);
	}
	protected Labor(){}
}
