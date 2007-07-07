/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich, D.Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DBLog.java 2736 2007-07-07 14:07:40Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.Hub;
import ch.rgw.net.NetTool;
import ch.rgw.tools.TimeTool;

public class DBLog extends PersistentObject {
	private static final String TABLENAME="LOGS";
	public static enum TYP{DELETE,UNDELETE};
	static{
		addMapping(TABLENAME,"OID","Datum=S:D:datum","typ","userID=user","station","ExtInfo");
	}
	
	public DBLog(PersistentObject obj,TYP typ){
		create(null);
		set(new String[]{"OID","Datum","typ","userID","station"},new String[]{
				obj.getId(),
				new TimeTool().toString(TimeTool.DATE_GER),
				typ.name(),
				Hub.actUser.getId(),
				NetTool.hostname
		});
	}
	
	public static DBLog load(String id){
		return new DBLog(id);
	}
	protected DBLog(String id){
		super(id);
	}
	protected DBLog(){}
	@Override
	public String getLabel() {
		return "DB-Log";
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
