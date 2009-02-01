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
 * $Id: Xid.java 5073 2009-02-01 15:24:52Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.rgw.tools.VersionInfo;

public class Xid extends PersistentObject {
	private static final String VERSION = "1.0.0";
	private static final String TABLENAME = "XID";
	private static Log log = Log.get("XID");
	/**
	 * Quality value for an ID that is valid only in the context of the issuing program
	 */
	public static final int ASSIGNMENT_LOCAL = 1;
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a
	 * nationally assigned ID)
	 */
	public static final int ASSIGNMENT_REGIONAL = 2;
	/**
	 * Quality value for an ID that can be used as global identifier
	 */
	public static final int ASSIGNMENT_GLOBAL = 3;
	
	/**
	 * Marker that the ID is a GUID (that is, guaranteed to exist only once)
	 */
	public static final int QUALITY_GUID = 4;
	
	private static Hashtable<String, XIDDomain> domains;
	
	public static final String DOMAIN_ELEXIS = "www.elexis.ch/xid";
	public static final String DOMAIN_AHV = "www.ahv.ch/xid";
	public static final String DOMAIN_SWISS_PASSPORT = "www.xid.ch/id/passport/ch";
	public static final String DOMAIN_AUSTRIAN_PASSPORT = "www.xid.ch/id/passport/at";
	public static final String DOMAIN_GERMAN_PASSPORT = "www.xid.ch/id/passport/de";
	public final static String DOMAIN_EAN = "www.xid.ch/id/ean";
	public final static String DOMAIN_OID = "www.xid.ch/id/oid";
	
	static {
		addMapping(TABLENAME, "type", "object", "domain", "domain_id", "quality");
		domains = new Hashtable<String, XIDDomain>();
		String storedDomains = Hub.globalCfg.get("LocalXIDDomains", null);
		if (storedDomains == null) {
			domains.put(DOMAIN_ELEXIS, new XIDDomain(DOMAIN_ELEXIS, "UUID", ASSIGNMENT_LOCAL
				| QUALITY_GUID, "ch.elexis.data.PersistentObject"));
			domains.put(DOMAIN_AHV, new XIDDomain(DOMAIN_AHV, "AHV", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Person"));
			domains.put(DOMAIN_OID, new XIDDomain(DOMAIN_OID, "OID", ASSIGNMENT_GLOBAL
				| QUALITY_GUID, "ch.elexis.data.PersistentObject"));
			domains.put(DOMAIN_EAN, new XIDDomain(DOMAIN_EAN, "EAN", ASSIGNMENT_REGIONAL,
				"ch.elexis.data.Kontakt"));
			storeDomains();
		} else {
			for (String dom : storedDomains.split(";")) {
				String[] spl = dom.split("#");
				if (spl.length < 2) {
					log.log("Fehler in XID-Domain " + dom, Log.ERRORS);
				}
				String simpleName = "";
				if (spl.length >= 3) {
					simpleName = spl[2];
				}
				String displayOptions = "Kontakt";
				if (spl.length >= 4) {
					displayOptions = spl[3];
				}
				domains.put(spl[0], new XIDDomain(spl[0], simpleName, Integer.parseInt(spl[1]),
					displayOptions));
			}
		}
		VersionInfo vv = new ch.rgw.tools.VersionInfo(Hub.Version);
		if (vv.isOlder("1.3.2")) {
			XIDDomain xd = domains.get(DOMAIN_EAN);
			xd.addDisplayOption(Person.class);
			xd.addDisplayOption(Organisation.class);
			xd = domains.get(DOMAIN_AHV);
			xd.addDisplayOption(Person.class);
		}
	}
	
	/**
	 * create a new XID. Does nothing if identical XIX already exists.
	 * 
	 * @param o
	 *            the object to identify with the new XID
	 * @param domain
	 *            the domain from wich the identifier is (e.g. DOMAIN_COVERCARD). Must be a
	 *            registered domain
	 * @param domain_id
	 *            the id from that domain that identifies the object
	 * @param quality
	 *            the quality of this identifier
	 * @throws XIDException
	 *             if a XID with same domain and domain_id but different object or quality already
	 *             exists.
	 */
	public Xid(final PersistentObject o, final String domain, final String domain_id)
		throws XIDException{
		Integer val = domains.get(domain).quality;
		if (val == null) {
			throw new XIDException("XID Domain " + domain + " is not registered");
		}
		if (val > 9) {
			val = (val & 7) + 4;
		}
		Xid xid = findXID(domain, domain_id);
		if (xid != null) {
			if (xid.get("object").equals(o.getId())) {
				return;
			}
			throw new XIDException("XID " + domain + ":" + domain_id + " is not unique");
		}
		xid = findXID(o, domain);
		if (xid != null) {
			throw new XIDException("XID " + domain + ": " + domain_id + " was already assigned");
		}
		create(null);
		set(new String[] {
			"type", "object", "domain", "domain_id", "quality"
		}, new String[] {
			o.getClass().getName(), o.getId(), domain, domain_id, Integer.toString(val)
		});
	}
	
	/**
	 * Get the quality of this xid
	 * 
	 * @return the quality
	 */
	public int getQuality(){
		return checkZero(get("quality"));
	}
	
	/**
	 * get the Domain this Xid is from
	 * @return
	 */
	public String getDomain(){
		return get("domain");
	}
	
	/**
	 * get the id of this Xid in its domain
	 * @return
	 */
	public String getDomainId(){
		return get("domain_id");
	}
	
	/**
	 * Get the object that is identified with this XID
	 * 
	 * @return the object or null if it could not be restored.
	 */
	public PersistentObject getObject(){
		PersistentObject po = Hub.poFactory.createFromString(get("type") + "::" + get("object"));
		return po;
	}
	
	@Override
	public String getLabel(){
		PersistentObject po = getObject();
		String text = "unknown object";
		if (po != null) {
			text = po.getLabel();
		}
		StringBuilder ret = new StringBuilder();
		ret.append(text).append(": ").append(get("domain")).append("->").append(get("domain_id"));
		return ret.toString();
	}
	
	public static Xid load(final String id){
		return new Xid(id);
	}
	
	/**
	 * Find a XID from a domain and a domain_id
	 * 
	 * @param domain
	 *            the domain to search an id from (e.g. www.ahv.ch)
	 * @param id
	 *            the id out of domain to retrieve
	 * @return the xid holding that id from that domain or null if no such xid was found
	 */
	public static Xid findXID(final String domain, final String id){
		Query<Xid> qbe = new Query<Xid>(Xid.class);
		qbe.add("domain", "=", domain);
		qbe.add("domain_id", "=", id);
		List<Xid> ret = qbe.execute();
		if (ret.size() == 1) {
			return ret.get(0);
		}
		return null;
	}
	
	/**
	 * Find a PersistentObject from a domain and a domain_id
	 * 
	 * @param domain
	 *            the domain to search an id from (e.g. www.ahv.ch)
	 * @param id
	 *            the id out of domain to retrieve
	 * @return the PersistentObject identified by that id from that domain or null if no such Object
	 *         was found
	 */
	public static PersistentObject findObject(final String domain, final String id){
		Xid xid = findXID(domain, id);
		if (xid != null) {
			return xid.getObject();
		}
		return null;
	}
	
	/**
	 * Find the Xid of a given domain for the given ibject
	 * 
	 * @param o
	 *            the object whose Xid should be find
	 * @param domain
	 *            the domain the Xid should be from
	 * @return the Xid or null if no xid for the given domain was found on the given object
	 */
	public static Xid findXID(final PersistentObject o, final String domain){
		Query<Xid> qbe = new Query<Xid>(Xid.class);
		qbe.add("domain", "=", domain);
		qbe.add("object", "=", o.getId());
		List<Xid> ret = qbe.execute();
		if (ret.size() == 1) {
			return ret.get(0);
		}
		return null;
	}
	
	/**
	 * Register a new domain for use with our XID System locally (this will not affect the central
	 * XID registry at www.xid.ch)
	 * 
	 * @param domain
	 *            the domain to register
	 * @param quality
	 *            the quality an ID of that domain will have
	 * @return true on success, false if that domain could not be registered
	 */
	public static boolean localRegisterXIDDomain(final String domain, String simpleName,
		final int quality){
		if (domains.contains(domain)) {
			log.log("XID Domain " + domain + " bereits registriert", Log.ERRORS);
		} else {
			if (domain.matches(".*[;#].*")) {
				log.log("XID Domain " + domain + " ungültig", Log.ERRORS);
			} else {
				domains.put(domain, new XIDDomain(domain, simpleName == null ? "" : simpleName,
					quality, "Kontakt"));
				storeDomains();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Register a local xid domain if it does not exist. Does nothing if a domain with the given
	 * domain name exists already
	 * 
	 * @param domain
	 *            name of the domain
	 * @param simpleName
	 *            short name for the domain
	 * @param quality
	 *            the wuality of an ID of that domain will have
	 * @return true on success
	 */
	public static boolean localRegisterXIDDomainIfNotExists(final String domain, String simpleName,
		final int quality){
		if (domains.get(domain) != null) {
			return true;
		}
		return localRegisterXIDDomain(domain, simpleName, quality);
	}
	
	/**
	 * Get the ID quality of an Object of a given domain
	 * 
	 * @param xidDomain
	 *            the domain to query
	 * @return obne of the Quality-ID constants or null if no such domain ist registered
	 */
	public static Integer getXIDDomainQuality(final String xidDomain){
		XIDDomain xd = domains.get(xidDomain);
		if (xd == null) {
			return null;
		}
		return xd.getQuality();
	}
	
	public static String getSimpleNameForXIDDomain(final String domain){
		XIDDomain xd = domains.get(domain);
		if (xd == null) {
			return domain;
		}
		return xd.simple_name;
	}
	
	public static XIDDomain getDomain(String name){
		return domains.get(name);
	}
	
	protected Xid(final String id){
		super(id);
	}
	
	protected Xid(){}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@SuppressWarnings("serial")
	public static class XIDException extends Exception {
		public XIDException(final String reason){
			super(reason);
		}
	}
	
	private static void storeDomains(){
		StringBuilder sb = new StringBuilder();
		for (String k : domains.keySet()) {
			XIDDomain xd = domains.get(k);
			sb.append(k).append("#").append(xd.getQuality()).append("#").append(xd.getSimpleName())
				.append("#").append(xd.getDisplayOptions()).append(";");
		}
		Hub.globalCfg.set("LocalXIDDomains", sb.toString());
	}
	
	/**
	 * return a list of all known domains
	 * 
	 * @return
	 */
	public static Set<String> getXIDDomains(){
		return domains.keySet();
	}
	
	public static class XIDDomain {
		String domain_name;
		String simple_name;
		int quality;
		ArrayList<Class<? extends PersistentObject>> displayOptions =
			new ArrayList<Class<? extends PersistentObject>>();
		
		@SuppressWarnings("unchecked")
		public XIDDomain(String dname, String simplename, int quality, String options){
			domain_name = dname;
			simple_name = simplename;
			this.quality = quality;
			for (String op : options.split(",")) {
				try {
					Class clazz = Class.forName(op);
					displayOptions.add(clazz);
				} catch (Exception ex) {}
			}
		}
		
		public String getSimpleName(){
			return simple_name;
		}
		
		public void setSimpleName(String simple_name){
			this.simple_name = simple_name;
			storeDomains();
		}
		
		public String getDomainName(){
			return domain_name;
		}
		
		public int getQuality(){
			return quality;
		}
		
		public void addDisplayOption(Class<? extends PersistentObject> clazz){
			if (!displayOptions.contains(clazz)) {
				displayOptions.add(clazz);
				storeDomains();
			}
		}
		
		public void removeDisplayOption(Class<? extends PersistentObject> clazz){
			displayOptions.remove(clazz);
			storeDomains();
		}
		
		public boolean isDisplayedFor(Class<? extends PersistentObject> clazz){
			return displayOptions.contains(clazz);
		}
		
		String getDisplayOptions(){
			StringBuilder r = new StringBuilder();
			for (Class<? extends PersistentObject> clazz : displayOptions) {
				r.append(clazz.getName()).append(",");
			}
			return r.toString();
		}
	}
}
