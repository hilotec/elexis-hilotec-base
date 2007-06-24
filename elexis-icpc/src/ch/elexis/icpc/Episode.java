/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Episode.java 1712 2007-02-02 06:21:46Z rgw_ch $
 *******************************************************************************/
package ch.elexis.icpc;

import java.io.ByteArrayInputStream;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;

public class Episode extends PersistentObject {
	private static final String VERSION="0.1.0";
	private final static String TABLENAME="CH_ELEXIS_ICPC_EPISODES";
		
	private final static String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25),"+
		"PatientID		VARCHAR(25),"+
		"Title			VARCHAR(80)"+
		");"+
		
		"CREATE INDEX "+TABLENAME+"1 ON "+TABLENAME+" (PatientID);"+
		
		"INSERT INTO "+TABLENAME+" (ID,Title) VALUES ('1',"+JdbcLink.wrap(VERSION)+");";
	
	static{
		addMapping(TABLENAME, "PatientID","Title");
		
		if(!load("1").exists()){
			try{
				ByteArrayInputStream bais=new ByteArrayInputStream(createDB.getBytes("UTF-8"));
				j.execScript(bais,true, false);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}
	}
	
	public Episode(Patient pat, String title){
		create(null);
		set(new String[]{"PatientID","Title"},pat.getId(),title);
	}
	@Override
	public String getLabel() {
		return get("Title");
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	
	public static Episode load(String id){
		return new Episode(id);
	}
	
	protected Episode(String id){
		super(id);
	}
	protected Episode(){}
	
	@Override
	public boolean isDragOK() {
		return true;
	}

}
