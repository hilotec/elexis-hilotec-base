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
 *  $Id: Note.java 5056 2009-01-27 13:04:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.notes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

/**
 * A Note is an arbitrary Text or BLOB with a name and optional keywords. A Note can consist of or
 * contain external links to files in the file system or URLs. Notes are stored hierarchically in a
 * tree-like structure (which ist mapped to a flat database table via a "parent"-field).
 * 
 * @author gerry
 * 
 */
public class Note extends PersistentObject {
	private static final String TABLENAME = "CH_ELEXIS_NOTES";
	private static final String DBVERSION = "0.3.2";
	
	/**
	 * The String that defines the database. Will be used only once at first start of this plugin
	 */
	private static final String create =
		"CREATE TABLE " + TABLENAME + " (" + "ID				VARCHAR(25)," + "lastupdate BIGINT,"
			+ "deleted 		CHAR(1) default '0'," + "Parent 		VARCHAR(25)," + "Title			VARCHAR(80),"
			+ "Date			CHAR(8)," + "Contents		BLOB," + "keywords       VARCHAR(255),"
			+ "mimetype		VARCHAR(80)," + "refs			TEXT);" + "INSERT INTO " + TABLENAME
			+ " (ID,Title,Parent) VALUES('1','" + DBVERSION + "','xxx');";
	
	/**
	 * Update to Version 0.31
	 */
	private static final String upd031 =
		"ALTER TABLE " + TABLENAME + " ADD keywords VARCHAR(255);" + "ALTER TABLE " + TABLENAME
			+ " ADD mimetype VARCHAR(80);";
	
	/**
	 * Update to Version 0.32
	 */
	private static final String upd032 = "ALTER TABLE " + TABLENAME + " ADD lastupdate BIGINT;";
	
	/**
	 * Initaialization: Create the table mappings (@see PersistentObject), check the version and
	 * create or update the table if necessary
	 */
	static {
		addMapping(TABLENAME, "Parent", "Title", "Contents", "Datum=S:D:Date", "refs", "keywords",
			"mimetype");
		Note start = load("1");
		if (!start.exists()) {
			createTable(TABLENAME, create);
		} else {
			VersionInfo vi = new VersionInfo(start.get("Title"));
			if (vi.isOlder(DBVERSION)) {
				if (vi.isOlder("0.2.0")) {
					getConnection().exec(
						"ALTER TABLE " + TABLENAME + " ADD deleted CHAR(1) default '0';");
				}
				if (vi.isOlder("0.3.1")) {
					createTable(TABLENAME, upd031);
				}
				if (vi.isOlder("0.3.2")) {
					createTable(TABLENAME, upd032);
				}
				
				start.set("Title", DBVERSION);
			}
		}
	}
	
	/**
	 * Create a new Note with text content
	 * 
	 * @param parent
	 *            the parent note or null if this is a top level note
	 * @param title
	 *            a Title for this note
	 * @param text
	 *            the text content of this note
	 */
	public Note(Note parent, String title, String text){
		create(null);
		set(new String[] {
			"Title", "Datum", "mimetype"
		}, title, new TimeTool().toString(TimeTool.DATE_GER), "text/plain");
		try {
			setContent(text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			// should never happen
		}
		if (parent != null) {
			set("Parent", parent.getId());
		}
	}
	
	/**
	 * Create a new Note with binary content
	 * 
	 * @param parent
	 *            the parent note or null if this is a top level note
	 * @param title
	 *            a Title for this note
	 * @param contents
	 *            the contents of this note in
	 * @param mimettype
	 *            the mimetype of the contents
	 */
	
	public Note(Note parent, String title, byte[] contents, String mimetype){
		create(null);
		set(new String[] {
			"Title", "Datum", "mimetype"
		}, title, new TimeTool().toString(TimeTool.DATE_GER), mimetype);
		setContent(contents);
		if (parent != null) {
			set("Parent", parent.getId());
		}
	}
	
	/**
	 * find the parent note of this note
	 * 
	 * @return the parent note or null if this is a top level note.
	 */
	public Note getParent(){
		String pid = get("Parent");
		if (pid == null) {
			return null;
		}
		Note p = Note.load(pid);
		return p;
	}
	
	/**
	 * find the children of this note
	 * 
	 * @return a list of all Notes that are children of the current note. The list might me empty
	 *         but is never null.
	 */
	public List<Note> getChildren(){
		Query<Note> qbe = new Query<Note>(Note.class);
		qbe.add("Parent", "=", getId());
		return qbe.execute();
	}
	
	/**
	 * Set new binary content to the current note. Any old content will be overwritten.
	 * 
	 * @param cnt
	 *            the new content
	 */
	public void setContent(byte[] cnt){
		setBinary("Contents", cnt);
		set("Datum", new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	/**
	 * retrieve the content of this note
	 * 
	 * @return a byte[] containing the data for this note's content
	 */
	public byte[] getContent(){
		return getBinary("Contents");
	}
	
	/**
	 * retrieve the keywords that are associated with this note.
	 * 
	 * @return a String with a comma separated list of keywords that may be empty but is never null
	 */
	public String getKeywords(){
		return checkNull(get("keywords"));
	}
	
	/**
	 * Enter keywords for this note
	 * 
	 * @param kw
	 *            a string with a comma separated list of keywords (at most 250 chars)
	 */
	public void setKeywords(String kw){
		set("keywords", StringTool.limitLength(kw.toLowerCase(), 250));
	}
	
	/**
	 * Return externals references associated with this Note
	 * 
	 * @return a List with urls of external refs
	 */
	public List<String> getRefs(){
		String all = get("refs");
		if (StringTool.isNothing(all)) {
			return new ArrayList<String>();
		}
		return StringTool.splitAL(all, ",");
	}
	
	/**
	 * Add a new external ref
	 * 
	 * @param ref
	 *            a string representing an URL
	 */
	public void addRef(String ref){
		List<String> refs = getRefs();
		refs.add(ref);
		set("refs", StringTool.join(refs, ","));
	}
	
	/**
	 * remove an external reference
	 * 
	 * @param ref
	 *            the reference to remove
	 */
	public void removeRef(String ref){
		List<String> refs = getRefs();
		refs.remove(ref);
		set("refs", StringTool.join(refs, ","));
	}
	
	@Override
	public String getLabel(){
		return get("Title");
	}
	
	@Override
	public boolean delete(){
		Query<Note> qbe = new Query<Note>(Note.class);
		qbe.add("Parent", "=", getId());
		List<Note> list = qbe.execute();
		for (Note note : list) {
			note.delete();
		}
		return super.delete();
	}
	
	@Override
	protected String getTableName(){
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
