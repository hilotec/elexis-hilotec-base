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
 * $Id: Message.java 5182 2009-02-24 15:47:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.messages;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class Message extends PersistentObject {
	private static final String TABLENAME = "CH_ELEXIS_MESSAGES";
	private static final String VERSION = "0.2.0";
	private static final String createDB =
		"CREATE TABLE " + TABLENAME + " (" + "ID			VARCHAR(25) primary key," + "lastupdate BIGINT,"
			+ "deleted		CHAR(1) default '0'," + "origin		VARCHAR(25),"
			+ "destination	VARCHAR(25),"
			+ "dateTime		CHAR(14)," // yyyymmddhhmmss
			+ "msg			TEXT);" + "INSERT INTO " + TABLENAME + " (ID,origin) VALUES ('VERSION','"
			+ VERSION + "');";
	
	static {
		addMapping(TABLENAME, "from=origin", "to=destination", "time=dateTime", "Text=msg");
		
		Message ver = load("VERSION");
		if (ver.state() < PersistentObject.DELETED) {
			initialize();
		} else {
			VersionInfo vi = new VersionInfo(ver.get("from"));
			if (vi.isOlder(VERSION)) {
				if (vi.isOlder("0.2.0")) {
					createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD lastupdate BIGINT;");
					ver.set("from", VERSION);
				}
			}
		}
		
	}
	
	static void initialize(){
		createOrModifyTable(createDB);
	}
	
	public Message(final Anwender an, final String text){
		create(null);
		TimeTool tt = new TimeTool();
		String dt = tt.toString(TimeTool.TIMESTAMP);
		set(new String[] {
			"from", "to", "time", "Text"
		}, new String[] {
			Hub.actUser.getId(), an.getId(), dt, text
		});
		
	}
	
	public Anwender getSender(){
		Anwender an = Anwender.load(get("from"));
		return an;
	}
	
	public Anwender getDest(){
		Anwender an = Anwender.load(get("to"));
		return an;
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}
	
	public String getText(){
		return checkNull(get("Text"));
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Message load(final String id){
		return new Message(id);
	}
	
	protected Message(final String id){
		super(id);
	}
	
	protected Message(){}
}
