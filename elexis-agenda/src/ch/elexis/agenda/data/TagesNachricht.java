/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TagesNachricht.java 5189 2009-02-24 15:48:00Z rgw_ch $
 *******************************************************************************/
package ch.elexis.agenda.data;

import java.io.ByteArrayInputStream;

import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class TagesNachricht extends PersistentObject {
	private static final String TABLENAME = "CH_ELEXIS_AGENDA_DAYMSG";
	private static final String VERSION = "0.2.0";
	private static final String createDB =
		"CREATE TABLE " + TABLENAME + "(" + "ID		CHAR(8) primary key,"
			+ "deleted	CHAR(1) default '0'," + "Kurz		VARCHAR(80)," + "Msg		TEXT" + ");"
			+ "INSERT INTO " + TABLENAME + "(ID,Kurz) VALUES (1,'" + VERSION + "')";
	
	private static final String update020 =
		"ALTER TABLE " + TABLENAME + " ADD deleted CHAR(1) default '0';" + "UPDATE " + TABLENAME
			+ " SET KURZ='0.2.0' WHERE ID='1';";
	static {
		addMapping(TABLENAME, "Zeile=Kurz", "Text=Msg");
		TagesNachricht start = load("1");
		if (!start.exists()) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(createDB.getBytes("UTF-8"));
				getConnection().execScript(bais, true, false);
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		} else {
			VersionInfo vi = new VersionInfo(TagesNachricht.load("1").get("Zeile"));
			if (vi.isOlder(VERSION)) {
				if (vi.isOlder("0.2.0")) {
					createOrModifyTable(update020);
				}
			}
			
		}
		
	}
	
	public TagesNachricht(TimeTool date, String kurz, String lang){
		if (date == null) {
			date = new TimeTool();
		}
		create(date.toString(TimeTool.DATE_COMPACT));
		set(new String[] {
			"Zeile", "Text"
		}, kurz, lang);
	}
	
	public String getZeile(){
		return get("Zeile");
	}
	
	public String getLangtext(){
		return get("Text");
	}
	
	public void setZeile(String zeile){
		set("Zeile", zeile);
	}
	
	public void setLangtext(String text){
		set("Text", text);
	}
	
	@Override
	public String getLabel(){
		return get("Zeile");
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected TagesNachricht(String id){
		super(id);
	}
	
	protected TagesNachricht(){}
	
	public static TagesNachricht load(TimeTool tt){
		return new TagesNachricht(tt.toString(TimeTool.DATE_COMPACT));
	}
	
	public static TagesNachricht load(String id){
		return new TagesNachricht(id);
	}
}
