package ch.elexis.exchange;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.XidElement;
import ch.elexis.util.XMLTool;
import ch.rgw.tools.StringTool;

public class XIDHandler {
	public static final String XID_ELEMENT="xid";
	public static final String XID_UUID="uuid";
	public static final String XID_IDENTITY="identity";
	public static final String XID_DOMAIN="domain";
	public static final String XID_DOMAIN_ID="domainID";
	public static final String XID_QUALITY="quality";
	public static final String XID_GUID="isGUID";
	public static final String[] XID_QUALITIES={
		"unknownAssignment",
		"localAssignment",
		"regionalAssignment",
		"globalAssignment"
	};
	
	
	
	

	public enum XIDMATCH{NONE,POSSIBLE,SURE};
	/**
	 * Compare a XID XML-Element with the xids of a PersistentObject
	 * @param eXid a XID Element conforming to xchange.xsd
	 * @param po a PersistentObject to match
	 * @return XIDMATCH.SURE if both xids match in one or more identities of GUID quality or in two or more
	 *  identities without GUID quality but more than local assignment.<br/>
	 *  XIDMATCH.POSSIBLE if the xids match in one identity without GUID quality
	 *  XIDMATCH.NONE otherwise.
	 */
	@SuppressWarnings("unchecked")
	public XIDMATCH match(Element eXid, PersistentObject po) {
		if(po.getId().equals(eXid.getAttributeValue(XID_UUID))){
			return XIDMATCH.SURE;
		}
		int sure=0;
		List<Xid> poXids=po.getXids();
		List<Element> idents=eXid.getChildren(XID_IDENTITY, eXid.getNamespace());
		for(Xid xid:poXids){
			String domain=xid.getDomain();
			String domid=xid.getDomainId();
			for(Element ident:idents){
				if(ident.getAttributeValue(XID_DOMAIN).equals(domain) &&
						ident.getAttributeValue(XID_DOMAIN_ID).equals(domid)){
					if(XidElement.isUUID(xid)){
						return XIDMATCH.SURE;
					}else{
						if(xid.getQuality()>Xid.ASSIGNMENT_LOCAL){
							sure++;
						}
					}
				}
			}
		}
		switch(sure){
			case 0:return XIDMATCH.NONE;
			case 1: return XIDMATCH.POSSIBLE;
			default: return XIDMATCH.SURE;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<PersistentObject> findObject(Element eXid){
		List<Element> idents=eXid.getChildren(XID_IDENTITY, eXid.getNamespace());
		List<PersistentObject> candidates=new LinkedList<PersistentObject>();
		boolean lastGuid=false;
		int lastQuality=0;
		for(Element ident:idents){
			String domain=ident.getAttributeValue(XID_DOMAIN);
			String domain_id=ident.getAttributeValue(XID_DOMAIN_ID);
			String quality=ident.getAttributeValue(XID_QUALITY);
			String isguid=ident.getAttributeValue(XID_GUID);
			PersistentObject cand=Xid.findObject(domain, domain_id);
			
			if(cand!=null){
				boolean actGuid=Boolean.parseBoolean(isguid);
				int actQuality=Integer.parseInt(quality);
				if(candidates.contains(cand)){
					if((lastGuid==true) && (actGuid==false)){
						continue;
					}
					if(actQuality<lastQuality){
						continue;
					}
					candidates.remove(cand);
				}
				candidates.add(cand);
				lastQuality=actQuality;
				lastGuid=actGuid;
			}
		}
		
		return candidates;
	}
}
