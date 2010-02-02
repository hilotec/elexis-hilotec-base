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
 *  $Id: BriefAusgabe.java 6050 2010-02-02 16:49:34Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.rgw.tools.TimeTool;

public class BriefAusgabe extends PersistentObject {
	public static final String FLD_BRIEF_ID = "BriefID";
	public static final String FLD_OUTPUTTER = "Outputter";
	static final String TABLENAME="BRIEFE_AUSGABE_LOG";
	
	static{
		addMapping(TABLENAME, FLD_BRIEF_ID, FLD_OUTPUTTER, DATE_FIELD, FLD_EXTINFO);
	}
	
	public BriefAusgabe(Brief brief){
		create(null);
		set(new String[]{FLD_BRIEF_ID,DATE_FIELD},brief.getId(),new TimeTool().toString(TimeTool.DATE_GER));
	}
	@Override
	public String getLabel(){
		return get(DATE_FIELD)+":"+get(FLD_OUTPUTTER);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static BriefAusgabe load(String id){
		return new BriefAusgabe(id);
	}
	protected BriefAusgabe(String id){
		super(id);
	}
}
