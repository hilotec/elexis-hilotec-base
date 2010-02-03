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
 *  $Id: OutputLog.java 6055 2010-02-03 07:24:52Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.List;

import ch.rgw.tools.TimeTool;


/**
 * An OutputLog instance carries the information when and where to a PersistentObject has been sent.
 * 
 * @author gerry
 *
 */
public class OutputLog extends PersistentObject{
	public static final String FLD_OBJECT_ID = "ObjectID";
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
	
	public static List<OutputLog> getOutputs(PersistentObject po){
		Query<OutputLog> qbe=new Query<OutputLog>(OutputLog.class);
		qbe.add(FLD_OBJECT_ID, Query.EQUALS, po.getId());
		return qbe.execute();
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
