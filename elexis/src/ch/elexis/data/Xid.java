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
 * $Id: Xid.java 3016 2007-08-26 13:26:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.List;

import ch.elexis.Hub;

public class Xid extends PersistentObject {
	private static final String TABLENAME="XID";
	/**
	 * Quality value for an ID that is valid only in the context of the issuing program
	 */
	public static final int QUALITY_LOCAL=0;
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a nationally
	 * assigned ID)
	 */
	public static final int QUALITY_REGIONAL=1;
	/**
	 * Quality value for an ID that is guaranteed to be globally unique
	 */
	public static final int QUALITY_GLOBAL=2;
	/**
	 * Quality value for an ID that is globally unique AND can be used to retrieve the identified identity
	 * independently form the issuing program
	 */
	public static final int QUALITY_ULTIMATE=3;

	public static final String DOMAIN_ELEXIS="www.elexis.ch/xid";
	public static final String DOMAIN_AHV="www.ahv.ch/xid";
	public static final String DOMAIN_COVERCARD="www.covercard.ch/xid";
	public static final String DOMAIN_BSVNUM="www.xid.ch/id/kknum";
	public static final String DOMAIN_SWISS_PASSPORT="www.xid.ch/id/passport/ch";
	public static final String DOMAIN_AUSTRIAN_PASSPORT="www.xid.ch/id/passport/at";
	public static final String DOMAIN_GERMAN_PASSPORT="www.xid.ch/id/passport/de";
	public final static String DOMAIN_EAN ="www.xid.ch/id/ean";
	public final static String DOMAIN_OID ="www.xid.ch/id/oid";
	public static final String DOMAIN_KSK="www.xid.ch/id/ksk";
	public static final String DOMAIN_NIF="www.xid.ch/id/nif";
	
	static{
		addMapping(TABLENAME, "type", "object","domain","domain_id","quality");
	}
	/**
	 * create a new XID. Does nothing if identical XIX already exists.
	 * @param o the object to identify with the new XID
	 * @param domain the domain from wich the identifier is (e.g. DOMAIN_COVERCARD)
	 * @param domain_id the id from that domain that identifies the object
	 * @param quality the quality of this identifier
	 * @throws XIDException if a XID with same domain and domain_id but different object or quality already exists.
	 */
	public Xid(final PersistentObject o, final String domain, final String domain_id, final int quality) throws XIDException{
		Xid xid=findXID(domain,domain_id);
		if(xid!=null){
			if((xid.get("object").equals(o.getId())) && (xid.getQuality()==quality)){
				return;
			}
			throw new XIDException("XID "+domain+":"+domain_id+" is not unique");
		}
		xid=findXID(o,domain);
		if(xid!=null){
			throw new XIDException("XID "+domain+": "+domain_id+" was already assigned");
		}
		create(null);
		set(new String[]{"type", "object","domain","domain_id","quality"},
				new String[]{o.getClass().getName(),o.getId(),domain,domain_id,Integer.toString(quality)});
	}

	/**
	 * Get the quality of this xid
	 * @return the quality
	 */
	public int getQuality(){
		return checkZero(get("quality"));
	}
	
	public String getDomain(){
		return get("domain");
	}
	
	public String getDomainId(){
		return get("domain_id");
	}
	
	/**
	 * Get the object that is identified with this XID
	 * @return the object or null if it could not be restored.
	 */
	public PersistentObject getObject(){
		PersistentObject po=Hub.poFactory.createFromString(get("type")+"::"+get("object"));
		return po;
	}
	@Override
	public String getLabel() {
		PersistentObject po=getObject();
		String text="unknown object";
		if(po!=null){
			text=po.getLabel();
		}
		StringBuilder ret=new StringBuilder();
		ret.append(text).append(": ").append(get("domain")).append("->").append(get("domain_id"));
		return ret.toString();
	}

	public static Xid load(final String id){
		return new Xid(id);
	}
	
	/**
	 * Find a XID from a domain and a domain_id
	 * @param domain the domain to search an id from (e.g. www.ahv.ch)
	 * @param id the id out of domain to retrieve
	 * @return the xid holding that id from that domain or null if no such xid was found
	 */
	public static Xid findXID(final String domain, final String id){
		Query<Xid> qbe=new Query<Xid>(Xid.class);
		qbe.add("domain", "=", domain);
		qbe.add("domain_id", "=", id);
		List<Xid> ret=qbe.execute();
		if(ret.size()==1){
			return ret.get(0);
		}
		return null;
	}
	
	/**
	 * Find a PersistentObject from a domain and a domain_id
	 * @param domain the domain to search an id from (e.g. www.ahv.ch)
	 * @param id the id out of domain to retrieve
	 * @return the PersistentObject identified by that id from that domain or null if no such Object was found
	 */
	public static PersistentObject findObject(final String domain, final String id){
		Xid xid=findXID(domain,id);
		if(xid!=null){
			return xid.getObject();
		}
		return null;
	}
	
	public static Xid findXID(final PersistentObject o, final String domain){
		Query<Xid> qbe=new Query<Xid>(Xid.class);
		qbe.add("domain", "=", domain);
		qbe.add("object", "=", o.getId());
		List<Xid> ret=qbe.execute();
		if(ret.size()==1){
			return ret.get(0);
		}
		return null;
	}
	
	protected Xid(final String id){
		super(id);
	}
	protected Xid(){}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static class XIDException extends Exception{
		public XIDException( final String reason) {
			super(reason);
		}
	}
}
