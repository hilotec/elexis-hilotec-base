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
 * $Id: Xid.java 2976 2007-08-10 13:54:03Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.Hub;

public class Xid extends PersistentObject {
	private static final String TABLENAME="XID";
	public static final int QUALITY_LOCAL=0;
	public static final int QUALITY_REGIONAL=1;
	public static final int QUALITY_GLOBAL=2;
	public static final int QUALITY_ULTIMATE=3;

	static{
		addMapping(TABLENAME, "object","domain","domain_id","quality");
	}
	public Xid(final PersistentObject o, final String domain, final String domain_id, final int quality){
		create(null);
		set(new String[]{"type", "object","domain","domain_id","quality"},
				new String[]{o.getId(),domain,domain_id,Integer.toString(quality)});
	}

	public int getQuality(){
		return checkZero(get("quality"));
	}
	
	public String getDomain(){
		return get("domain");
	}
	
	public String getDomainId(){
		return get("domain_id");
	}
	
	public PersistentObject getObject(){
		PersistentObject po=Hub.poFactory.createFromString(get("type")+"::"+get("object"));
		return po;
	}
	@Override
	public String getLabel() {
		PersistentObject po=getObject();
		StringBuilder ret=new StringBuilder();
		ret.append(po.getLabel()).append(": ").append(get("domain")).append("->").append(get("domain_id"));
		return ret.toString();
	}

	public static Xid load(final String id){
		return new Xid(id);
	}
	
	protected Xid(final String id){
		super(id);
	}
	protected Xid(){}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
