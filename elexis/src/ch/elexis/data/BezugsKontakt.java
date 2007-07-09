/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: BezugsKontakt.java 2765 2007-07-09 10:47:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

public class BezugsKontakt extends PersistentObject {
	private static final String tablename="KONTAKT_ADRESS_JOINT";
	static{
		addMapping(tablename,"myID","otherID","Bezug");
	}
	
	public BezugsKontakt(Kontakt kontakt, Kontakt adr, String bezug) {
		create(null);
		set(new String[]{"myID","otherID","Bezug"},kontakt.getId(),adr.getId(),bezug);
	}
	@Override
	public String getLabel() {
		Kontakt k=Kontakt.load(get("otherID"));
		if(k.isValid()){
			return get("Bezug")+": "+k.getLabel();
		}else{
			return "Angegebener Kontakt nicht vorhanden";
		}
		
	}
	public static BezugsKontakt load(String id){
		return new BezugsKontakt(id);
	}

	@Override
	protected String getTableName() {
		return tablename;
	}

	protected BezugsKontakt(){}
	protected BezugsKontakt(String id){
		super(id);
	}
	
		
}
