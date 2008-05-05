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
 *  $Id: Note.java 3874 2008-05-05 16:59:38Z rgw_ch $
 *******************************************************************************/
package ch.elexis.notes;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class Note extends PersistentObject {
	private static final String TABLENAME="CH_ELEXIS_NOTES";
	private static final String DBVERSION="0.2.0";
	
	private static final String create=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25),"+
		"deleted 		CHAR(1) default '0',"+	
		"Parent 		VARCHAR(25),"+
		"Title			VARCHAR(80),"+
		"Date			CHAR(8),"+
		"Contents		BLOB,"+
		"refs			TEXT);"+
		"INSERT INTO "+TABLENAME+" (ID,Title,Parent) VALUES('1','"+DBVERSION+"','xxx');";
	
	static{
		addMapping(TABLENAME,"Parent","Title","Contents","Datum=S:D:Date","refs");
		Note start=load("1");
		if(!start.exists()){
			createTable(TABLENAME, create);
		}else{
			VersionInfo vi=new VersionInfo(start.get("Title"));
			if(vi.isOlder(DBVERSION)){
				if(vi.isOlder("0.2.0")){
					getConnection().exec("ALTER TABLE "+TABLENAME+" ADD deleted CHAR(1) default '0';");
					start.set("Title", DBVERSION);
				}
			}
		}
	}
	
	public Note(Note parent, String title, String text){
		create(null);
		set(new String[]{"Title","Datum"},title,new TimeTool().toString(TimeTool.DATE_GER));
		try {
			setContent(text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			// should never happen
		}
		if(parent!=null){
			set("Parent",parent.getId());
		}
	}
	public void setContent(byte[] cnt){
		setBinary("Contents",cnt);
		set("Datum",new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	public byte[] getContent(){
		return getBinary("Contents");
	}
	public List<String> getRefs(){
		String all=get("refs");
		if(StringTool.isNothing(all)){
			return new ArrayList<String>();
		}
		return StringTool.splitAL(all, ",");
	}
	public void addRef(String ref){
		List<String> refs=getRefs();
		refs.add(ref);
		set("refs",StringTool.join(refs, ","));
	}
	
	public void removeRef(String ref){
		List<String> refs=getRefs();
		refs.remove(ref);
		set("refs",StringTool.join(refs, ","));
	}
	@Override
	public String getLabel() {
		return get("Title");
	}
	

	@Override
	public boolean delete() {
		Query<Note> qbe=new Query<Note>(Note.class);
		qbe.add("Parent", "=", getId());
		List<Note> list=qbe.execute();
		for(Note note:list){
			note.delete();
		}
		return super.delete();
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	public static Note load(String id){
		return new Note(id);
	}
	protected Note(String id){
		super(id);
	}

	protected Note(){}
	
	
}
