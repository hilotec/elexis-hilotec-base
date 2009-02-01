/*******************************************************************************
 * Copyright (c) 2008-2009 by G. Weirich 
 * This program is based on the Sgam-Exchange project, 
 * (c) SGAM-Informatics
 * All rights resevred 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XidElement.java 5073 2009-02-01 15:24:52Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Artikel;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.XMLTool;

@SuppressWarnings("serial")
public class XidElement extends XChangeElement {
	public static final String XMLNAME = "xid";
	public static final String ATTR_ID = "id";
	public static final String ELEMENT_IDENTITY = "identity";
	public static final String ATTR_IDENTITY_DOMAIN = "domain";
	public static final String ATTR_IDENTITY_DOMAIN_ID = "domainID";
	public static final String ATTR_IDENTITY_QUALITY = "quality";
	public static final String ATTR_ISGUID = "isGUID";
	
	public static final String[] IDENTITY_QUALITIES = {
		"unknownAssignment", "localAssignment", "regionalAssignment", "globalAssignment"
	};
	
	public enum XIDMATCH {
		NONE, POSSIBLE, SURE
	};
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public static boolean isUUID(Xid xid){
		return (xid.getQuality() & 4) != 0;
	}
	
	public static int getPureQuality(Xid xid){
		return (xid.getQuality()&3);
	}
	public XidElement(XChangeContainer parent){
		super(parent);
	}
	
	public XidElement(XChangeContainer home, IVerrechenbar iv){
		super(home);
		if(iv instanceof PersistentObject){
			PersistentObject po=(PersistentObject)iv;
			setAttribute(ATTR_ID, XMLTool.idToXMLID(po.getId()));
			addIdentities(po, Leistungsblock.XIDDOMAIN, po.getId(), Xid.ASSIGNMENT_LOCAL, true);

		}
	}
	
	public XidElement(XChangeContainer home, LabItem li){
		super(home);
		setAttribute(ATTR_ID, XMLTool.idToXMLID(li.getId()));
		StringBuilder domainRoot = new StringBuilder(FindingElement.XIDBASE);
		Labor lab = li.getLabor();
		if (lab == null || (!lab.isValid())) {
			domainRoot.append("unknown");
		} else {
			domainRoot.append(lab.get("Bezeichnung1"));
		}
		String domain = domainRoot.toString();
		Xid.localRegisterXIDDomainIfNotExists(domain, li.getLabel(), Xid.ASSIGNMENT_LOCAL);
		addIdentities(li, domain, li.getName(), Xid.ASSIGNMENT_LOCAL, true);
	}
	
	public XidElement(XChangeContainer home, Artikel art){
		super(home);
		setAttribute(ATTR_ID, XMLTool.idToXMLID(art.getId()));
		String ean = art.getEAN();
		if (!StringTool.isNothing(ean)) {
			addIdentities(art, Xid.DOMAIN_EAN, ean, Xid.ASSIGNMENT_REGIONAL, false);
		}
		String pk = art.getPharmaCode();
		if (!StringTool.isNothing(pk)) {
			addIdentities(art, Artikel.XID_PHARMACODE, pk, Xid.ASSIGNMENT_REGIONAL, false);
		}
		
	}
	
	public XidElement(XChangeContainer home, Kontakt k){
		super(home);
		Xid best = k.getXid();
		String id = XMLTool.idToXMLID(k.getId());
		if ((best.getQuality() & 7) >= Xid.QUALITY_GUID) {
			id = XMLTool.idToXMLID(best.getDomainId());
		} else {
			k.addXid(Xid.DOMAIN_ELEXIS, id, true);
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(k.getId()));
		List<Xid> xids = k.getXids();
		for (Xid xid : xids) {
			int val = xid.getQuality();
			int v1 = val & 3;
			Identity ident =
				new Identity(home, xid.getDomain(), xid.getDomainId(), v1, isUUID(xid));
			addContent(ident);
		}
		
	}
	
	private void addIdentities( PersistentObject po, String domain, String domid, int q, boolean bGuid){
		List<Xid> xids=po.getXids();
	
		boolean bDomain=false;
		XChangeContainer home=getContainer();
		for(Xid xid:xids){
			if(xid.getDomain().equals(domain)){
				bDomain=true;
			}
			Identity ident=new Identity(home, xid.getDomain(),xid.getDomainId(), getPureQuality(xid), isUUID(xid) );
			addContent(ident);
		}
		if(bDomain==false){
			po.addXid(domain, domid, false);
			Identity ident=new Identity(home,domain, domid,q,bGuid);
			addContent(ident);
		}
	}
	
	public void addIdentity(String domain, String domainID, int quality, boolean isGuid){
		add(new Identity(getContainer(), domain, domainID, quality, isGuid));
	}
	
	public void setMainID(String domain){
		Identity best = null;
		for (Element ident : getElements(ELEMENT_IDENTITY)) {
			Identity cand = (Identity) ident;
			if (domain != null) {
				if (cand.getAttr(ATTR_IDENTITY_DOMAIN).equalsIgnoreCase(domain)) {
					best = cand;
					break;
				}
			} else {
				if (best == null) {
					best = cand;
				} else {
					
					if (best.isGuid()) {
						if (cand.isGuid()) {
							if (cand.getQuality() > best.getQuality()) {
								best = cand;
							}
						}
					} else {
						if (cand.isGuid()) {
							best = cand;
						} else {
							if (cand.getQuality() > best.getQuality()) {
								best = cand;
							}
						}
					}
					
				}
				
			}
		}
		if (best == null || (!best.isGuid())) {
			best =
				new Identity(getContainer(), Xid.DOMAIN_ELEXIS, StringTool.unique("xidID"),
					Xid.ASSIGNMENT_LOCAL, true);
			add(best);
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(best.getAttr(ATTR_IDENTITY_DOMAIN_ID)));
	}
	
	/**
	 * Compare this XID -Element with the xids of a PersistentObject
	 * 
	 * @param po
	 *            a PersistentObject to match
	 * @return XIDMATCH.SURE if both xids match in one or more identities of GUID quality or in two
	 *         or more identities without GUID quality but more than local assignment.<br/>
	 *         XIDMATCH.POSSIBLE if the xids match in one identity without GUID quality
	 *         XIDMATCH.NONE otherwise.
	 */
	@SuppressWarnings("unchecked")
	public XIDMATCH match(PersistentObject po){
		if (po.getId().equals(getAttributeValue(XidElement.ATTR_ID))) {
			return XIDMATCH.SURE;
		}
		int sure = 0;
		List<Xid> poXids = po.getXids();
		List<Element> idents = getChildren(ELEMENT_IDENTITY, getContainer().getNamespace());
		for (Xid xid : poXids) {
			String domain = xid.getDomain();
			String domid = xid.getDomainId();
			for (Element ident : idents) {
				if (ident.getAttributeValue(ATTR_IDENTITY_DOMAIN).equals(domain)
					&& ident.getAttributeValue(ATTR_IDENTITY_DOMAIN_ID).equals(domid)) {
					if (XidElement.isUUID(xid)) {
						return XIDMATCH.SURE;
					} else {
						if (xid.getQuality() > Xid.ASSIGNMENT_LOCAL) {
							sure++;
						}
					}
				}
			}
		}
		switch (sure) {
		case 0:
			return XIDMATCH.NONE;
		case 1:
			return XIDMATCH.POSSIBLE;
		default:
			return XIDMATCH.SURE;
		}
		
	}
	
	/**
	 * Find the Object(s) possibly matching this Xid-Element
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PersistentObject> findObject(){
		List<Element> idents = getChildren(ELEMENT_IDENTITY, getContainer().getNamespace());
		List<PersistentObject> candidates = new LinkedList<PersistentObject>();
		boolean lastGuid = false;
		int lastQuality = 0;
		for (Element ident : idents) {
			String domain = ident.getAttributeValue(ATTR_IDENTITY_DOMAIN);
			String domain_id = ident.getAttributeValue(ATTR_IDENTITY_DOMAIN_ID);
			String quality = ident.getAttributeValue(ATTR_IDENTITY_QUALITY);
			String isguid = ident.getAttributeValue(XidElement.ATTR_ISGUID);
			PersistentObject cand = Xid.findObject(domain, domain_id);
			if (cand != null) {
				boolean actGuid = Boolean.parseBoolean(isguid);
				int actQuality = Integer.parseInt(quality);
				if (candidates.contains(cand)) {
					if ((lastGuid == true) && (actGuid == false)) {
						continue;
					}
					if (actQuality < lastQuality) {
						continue;
					}
					candidates.remove(cand);
				}
				candidates.add(cand);
				lastQuality = actQuality;
				lastGuid = actGuid;
			}
		}
		
		return candidates;
	}
	
	@SuppressWarnings("serial")
	static class Identity extends XChangeElement {
		public String getXMLName(){
			return ELEMENT_IDENTITY;
		}
		
		public Identity(XChangeContainer home){
			super(home);
		}
		
		public Identity(XChangeContainer home, String domain, String domain_id, int quality,
			boolean isGuid){
			super(home);
			setAttribute(ATTR_IDENTITY_DOMAIN, domain);
			setAttribute(ATTR_IDENTITY_DOMAIN_ID, domain_id);
			setAttribute(ATTR_IDENTITY_QUALITY, IDENTITY_QUALITIES[quality]);
			setAttribute(ATTR_ISGUID, Boolean.toString(isGuid));
		}
		
		public int getQuality(){
			String sq = getAttr(ATTR_IDENTITY_QUALITY);
			int idx = StringTool.getIndex(IDENTITY_QUALITIES, sq);
			return idx > 0 ? idx : 0;
		}
		
		public boolean isGuid(){
			return Boolean.parseBoolean(getAttr(ATTR_ISGUID));
		}
	}
}
