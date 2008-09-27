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
 * $Id: DBLog.java 4450 2008-09-27 19:49:01Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.Hub;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.net.NetTool;

public class DBLog extends PersistentObject {
	private static final String TABLENAME = "LOGS";

	public static enum TYP {
		DELETE, UNDELETE
	};

	static {
		addMapping(TABLENAME, "OID", "Datum=S:D:datum", "typ", "userID",
				"station", "ExtInfo");
	}

	public DBLog(PersistentObject obj, TYP typ) {
		create(null);
		set(new String[] { "OID", "Datum", "typ", "userID", "station" },
				new String[] { obj.getId(),
						new TimeTool().toString(TimeTool.DATE_GER), typ.name(),
						Hub.actUser.getId(), NetTool.hostname });
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
