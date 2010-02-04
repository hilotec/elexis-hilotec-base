/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id: DBImage.java 6058 2010-02-03 15:02:13Z rgw_ch $
 *******************************************************************************/

package ch.elexis.developer.resources;

import ch.elexis.data.PersistentObject;

/**
 * This is an example on how to derive your own type from PersistentObject and make it persisten
 * @author gerry
 *
 */
public class SampleDataType extends PersistentObject {
	/** 
	 * The Name of the Table objects of this class will reside in. If a plugin creates its
	 * own table, the name MUST begin with the plugin ID to avoid name clashes. Note that dots
	 * must be replaced by underscores due to naming restrictions of the database engines.
	 */
	static final String TABLENAME="ch_elexis_developer_resources_sampletable";
	
	/** Definition of the database table */
	static final String createDB="CREATE TABLE "+TABLENAME+"("+
	"ID				VARCHAR(25) primary key,"+		// This field must always be present
	"lastupdate		BIGINT,"+						// This field must always be present
	"deleted		CHAR(1) default '0',"+			// This field must always be present
	"Title          VARCHAR(50),"+
	"FunFactor		VARCHAR(6),"+					// No numeric fields
	"BoreFactor		VARCHAR(6),"+
	"Date			CHAR(8),"+						// use always this for dates
	"Remarks		TEXT,"+
	"FunnyStuff		BLOB);"+
	"CREATE INDEX "+TABLENAME+"idx1 on "+TABLENAME+" (FunFactor)";
	
	/**
	 * In the static initializer we construct the table mappings and create or update the table
	 */
	static{
		addMapping(TABLENAME, "Title","Fun=FunFactor","Bore=BoreFactor","Date=S:D:Date","Remarks","FunnyStuff");
	}
	/**
	 * This should return a human readable short description of this object
	 */
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		synchronized(sb){
			sb.append(get("title")).append(get("FunFactor"));
		}
		return sb.toString();
	}

	/**
	 * This must return the name of the Table this class will reside in. This may be an existent table
	 * or one specificallym created by this plugin.
	 */
	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
