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
 * $Id$
 *******************************************************************************/

package ch.elexis.messages;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.TimeTool;

public class Message extends PersistentObject {
	private static final String TABLENAME="CH_ELEXIS_MESSAGES";
	String createDB="CREATE TABLE "+TABLENAME+" ("
		+"ID			VARCHAR(25) primary key,"
		+"deleted		CHAR(1) default '0',"
		+"origin		VARCHAR(25),"
		+"destination	VARCHAR(25),"
		+"dateTime		CHAR(14),"			// yyyymmddhhmmss
		+"msg			TEXT);";
		
	
	static{
		addMapping(TABLENAME,"from=origin","to=destination","time=dateTime",
				"Text=msg");
		
	}
	public Message(final Anwender an, final String text){
		create(null);
		TimeTool tt=new TimeTool();
		String dt=tt.toString(TimeTool.TIMESTAMP);
		set(new String[]{"from","to","time","Text"},new String[]{
				Hub.actUser.getId(),an.getId(),dt,text
		});
		
	}
	
	public Anwender getSender(){
		Anwender an=Anwender.load(get("from"));
		return an;
	}
	
	public Anwender getDest(){
		Anwender an=Anwender.load(get("to"));
		return an;
	}
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		return sb.toString();
	}

	@Override
	protected String getTableName() {
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
