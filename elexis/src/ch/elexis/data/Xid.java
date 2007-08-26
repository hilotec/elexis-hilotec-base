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
 * $Id: Xid.java 3018 2007-08-26 14:46:31Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;
import java.util.List;

import ch.elexis.Hub;
import ch.elexis.util.Log;

public class Xid extends PersistentObject {
	private static final String TABLENAME="XID";
	private static Log log=Log.get("XID");
	/**
	 * Quality value for an ID that is valid only in the context of the issuing program
	 */
	public static final int ASSIGNMENT_LOCAL=1<<0;
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a nationally
	 * assigned ID)
	 */
	public static final int ASSIGNEMENT_REGIONAL=1<<1;
	/**
	 * Quality value for an ID that can be used as global identifier
	 */
	public static final int ASSIGNMENT_GLOBAL=1<<2;
	
	/**
	 * Marker that the ID is a GUID (that is, guaranteed to exist only once)
	 */
	public static final int QUALITY_GUID=1<<16;
	
	private static Hashtable<String,Integer> domains;
	
	public static final String DOMAIN_ELEXIS="www.elexis.ch/xid";
	public static final String DOMAIN_AHV="www.ahv.ch/xid";
	public static final String DOMAIN_SWISS_PASSPORT="www.xid.ch/id/passport/ch";
	public static final String DOMAIN_AUSTRIAN_PASSPORT="www.xid.ch/id/passport/at";
	public static final String DOMAIN_GERMAN_PASSPORT="www.xid.ch/id/passport/de";
	public final static String DOMAIN_EAN ="www.xid.ch/id/ean";
	public final static String DOMAIN_OID ="www.xid.ch/id/oid";
	
	static{
		addMapping(TABLENAME, "type", "object","domain","domain_id","quality");
		domains=new Hashtable<String,Integer>();
		String storedDomains=Hub.globalCfg.get("LocalXIDDomains", null);
		if(storedDomains==null){
			domains.put(DOMAIN_ELEXIS,ASSIGNMENT_LOCAL|QUALITY_GUID);
			domains.put(DOMAIN_AHV,ASSIGNEMENT_REGIONAL|QUALITY_GUID);
			domains.put(DOMAIN_OID,ASSIGNMENT_GLOBAL|QUALITY_GUID);
			domains.put(DOMAIN_EAN,ASSIGNMENT_GLOBAL|QUALITY_GUID);
			storeDomains();
		}else{
			for(String dom:storedDomains.split(";")){
				String[] spl=dom.split("#");
				if(spl.length!=2){
					log.log("Fehler in XID-Domain "+dom, Log.ERRORS);
				}
				domains.put(spl[0],Integer.parseInt(spl[1]));
			}
		}
	}
	/**
	 * create a new XID. Does nothing if identical XIX already exists.
	 * @param o the object to identify with the new XID
	 * @param domain the domain from wich the identifier is (e.g. DOMAIN_COVERCARD). Must be a registered domain
	 * @param domain_id the id from that domain that identifies the object
	 * @param quality the quality of this identifier
	 * @throws XIDException if a XID with same domain and domain_id but different object or quality already exists.
	 */
	public Xid(final PersistentObject o, final String domain, final String domain_id) throws XIDException{
		Integer val=domains.get(domain);
		if(val==null){
			throw new XIDException("XID Domain "+domain+" is not registered");
		}
		Xid xid=findXID(domain,domain_id);
		if(xid!=null){
			if(xid.get("object").equals(o.getId())){
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
				new String[]{o.getClass().getName(),o.getId(),domain,domain_id,Integer.toString(val)});
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
	
	/**
	 * Register a new domain for use with our XID System locally (this will not affect the
	 * centra XID registry at www.xid.ch)
	 * @param domain the domain to register
	 * @param quality the quality an ID of that domain will have
	 * @return true on success, false if that domain could not be registered
	 */
	public static boolean localRegisterXIDDomain(String domain, int quality){
		if(domains.contains(domain)){
			log.log("XID Domain "+domain+" bereits registriert", Log.ERRORS);
		}else{
			if(domain.matches(".*[;#].*")){
				log.log("XID Domain "+domain+" ungültig", Log.ERRORS);
			}else{
				domains.put(domain,quality);
				storeDomains();
				return true;
			}
		}
		return false;
	}
	
	public static boolean localRegisterXIDDomainIfNotExists(String domain, int quality){
		if(domains.get(domain)!=null){
			return true;
		}
		return localRegisterXIDDomain(domain, quality);
	}
	public static Integer getXIDDomainQuality(String xidDomain){
		return domains.get(xidDomain);
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

	private static void storeDomains(){
		StringBuilder sb=new StringBuilder();
		for(String k:domains.keySet()){
			sb.append(k).append("#").append(domains.get(k)).append(";");
		}
		Hub.globalCfg.set("LocalXIDDomains", sb.toString());
	}

}
