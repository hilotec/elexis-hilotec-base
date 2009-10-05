/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich, D.Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DBLog.java 5767 2009-10-05 05:11:47Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.Hub;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.net.NetTool;

public class DBLog extends PersistentObject {
	private static final String TABLENAME = "LOGS";

	public static enum TYP {
		DELETE, UNDELETE, UNKNOWN
	};

	static {
		addMapping(TABLENAME, "OID", "Datum=S:D:datum", "typ", "userID",
				"station", "ExtInfo");
	}

	public DBLog(PersistentObject obj, TYP typ) {
		create(null);
		if (typ==null){
			typ=TYP.UNKNOWN;
		}
		String user="?";
		if(Hub.actUser!=null){
			user=Hub.actUser.getId();
		}
		String hostname="?";
		if(NetTool.hostname!=null){
			hostname=NetTool.hostname;
		}
		set(new String[] { "OID", "Datum", "typ", "userID", "station" },
				new String[] { obj.getId(),
						new TimeTool().toString(TimeTool.DATE_GER), typ.name(),
						user, hostname});
	}

	public static DBLog load(String id) {
		return new DBLog(id);
	}

	protected DBLog(String id) {
		super(id);
	}

	protected DBLog() {
	}

	@Override
	public String getLabel() {
		return "DB-Log";
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
