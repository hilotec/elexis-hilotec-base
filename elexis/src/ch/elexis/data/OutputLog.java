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
 *  $Id: OutputLog.java 6052 2010-02-02 17:33:46Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.rgw.tools.TimeTool;



public class OutputLog extends PersistentObject{
	public static final String FLD_OBJECT_ID = "BriefID";
	public static final String FLD_OUTPUTTER = "Outputter";
	static final String TABLENAME="OUTPUT_LOG";
	
	static{
		addMapping(TABLENAME, FLD_OBJECT_ID, FLD_OUTPUTTER, DATE_FIELD, FLD_EXTINFO);
	}
	
	public OutputLog(PersistentObject po,IOutputter io){
		create(null);
		set(new String[]{FLD_OBJECT_ID,DATE_FIELD,FLD_OUTPUTTER},po.getId(),new TimeTool().toString(TimeTool.DATE_GER),io.getOutputterID());
	}
	@Override
	public String getLabel(){
		return get(DATE_FIELD)+":"+get(FLD_OUTPUTTER);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static OutputLog load(String id){
		return new OutputLog(id);
	}
	protected OutputLog(String id){
		super(id);
	}
}
