/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: NamedBlob.java 2706 2007-07-06 09:38:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.Hashtable;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.rgw.Compress.CompEx;
import ch.rgw.tools.TimeTool;

/**
 * A named Blob is just that: An arbitrarly named piece of arbitrary data. The name must be unique (among
 * NamedBlobs). We provide methods to store and retrueve data as Hashtables and Strings (Both will be stored
 * in zip-compressed form)
 * @author Gerry
 *
 */
public class NamedBlob extends PersistentObject {

	/**
	 * return the contents as Hashtable (will probably fail if the data was not stored using put(Hashtable)
	 * @return the previously stored Hashtable
	 */
	public Hashtable getHashtable(){
		return getHashtable("inhalt");
	}
	/**
	 * Put the contents as Hashtable. The Hashtable will be compressed
	 * @param in a Hashtable
	 */
	public void put(Hashtable in){
		setHashtable("inhalt",in);
		set("Datum",new TimeTool().toString(TimeTool.DATE_GER));
	}
	/**
	 * return the contents as String (will probably fail if the data was not stored using putString) 
	 * @return the previously stored string
	 */
	public String getString(){
		byte[] comp=getBinary("inhalt");
		byte[] exp=CompEx.expand(comp);
		try{
			return new String(exp,"utf-8");
		}catch(Exception ex){
			// should really not happen
			return null;
		}
	}
	
	/**
	 * Store a String. The String will be stored as compressed byte[]
	 * @param string
	 */
	public void putString(String string){
		byte[] comp=CompEx.Compress(string, CompEx.ZIP);
		setBinary("inhalt", comp);
		set("Datum",new TimeTool().toString(TimeTool.DATE_GER));
	}
	@Override
	public String getLabel() {
		return getId();
	}

	@Override
	protected String getTableName() {
		return "HEAP";
	}
	
	static{
		addMapping("HEAP","inhalt","S:D:Datum=datum");
	}
	/**
	 * Ask if this NamedBlob exists 
	 * @param id the unique name of the NamedBlob to query
	 * @return true if a NamedBlob with this name exists
	 */
	public static boolean exists(String id){
		NamedBlob ni=new NamedBlob(id);
		return ni.exists();
	}
	
	/**
	 * Load or create a NamedBloc with a given Name. Caution: This will never return an inexistent
	 * NamedBlob, because it will be created if necessary. Use exists() to check, whether a NamedBlob
	 * exists.
	 * @param id the unique name
	 * @return the NamedBlob with that Name (which may be just newly created)
	 */
	public static NamedBlob load(String id){
		NamedBlob ni=new NamedBlob(id);
		if(ni.exists()==false){
			ni.create(id);
		}
		return ni;
	}
	private NamedBlob(){};
	private NamedBlob(String id){
		super(id);
	}

	/**
	 * remove all BLOBS with a given name prefix and a last write time older than the given value
	 * needs the administrative right AC_PURGE
	 * @param prefix
	 * @param older
	 */
	public static void cleanup(String prefix,TimeTool older){
		if(Hub.acl.request(AccessControlDefaults.AC_PURGE)){
			Query<NamedBlob> qbe=new Query<NamedBlob>(NamedBlob.class);
			qbe.add("Datum", "<", older.toString(TimeTool.DATE_COMPACT));
			for(NamedBlob nb:qbe.execute()){
				if(nb.getId().startsWith(prefix)){
					nb.delete();
				}
			}
		}
	}
}
