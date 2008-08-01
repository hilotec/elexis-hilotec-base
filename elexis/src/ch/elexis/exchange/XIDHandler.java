package ch.elexis.exchange;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;
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
	
	public boolean isUUID(Xid xid){
		return (xid.getQuality()&4)!=0;
	}
	public Element createXidElement(Artikel art, Namespace ns){
		String id=XMLTool.idToXMLID(art.getId());
		Element ret=new Element(XID_ELEMENT,ns);
		ret.setAttribute(XID_UUID, id);
		String ean=art.getEAN();
		if(!StringTool.isNothing(ean)){
			Element ident=new Element(XID_IDENTITY,ns);
			ident.setAttribute(XID_DOMAIN, Xid.DOMAIN_EAN);
			ident.setAttribute(XID_DOMAIN_ID,ean);
			ident.setAttribute(XID_QUALITY, XID_QUALITIES[2]);
			ident.setAttribute(XID_GUID,Boolean.toString(false));
			ret.addContent(ident);
		}
		String pk=art.getPharmaCode();
		if(!StringTool.isNothing(pk)){
			Element ident=new Element(XID_IDENTITY,ns);
			ident.setAttribute(XID_DOMAIN, Artikel.XID_PHARMACODE);
			ident.setAttribute(XID_DOMAIN_ID,pk);
			ident.setAttribute(XID_QUALITY, XID_QUALITIES[2]);
			ident.setAttribute(XID_GUID,Boolean.toString(false));
			ret.addContent(ident);
		}
		return ret;
	}
	
	public Element createXidElement(Kontakt po, Namespace ns){
		Xid best=po.getXid();
		String id=XMLTool.idToXMLID(po.getId());
		if((best.getQuality()&7)>=Xid.QUALITY_GUID){
			id=XMLTool.idToXMLID(best.getDomainId());
		}else{
			po.addXid(Xid.DOMAIN_ELEXIS, id, true);
		}
		Element ret=new Element(XID_ELEMENT,ns);
		ret.setAttribute(XID_UUID, id);
		List<Xid> xids=po.getXids();
		for(Xid xid:xids){
			Element ident=new Element(XID_IDENTITY,ns);
			ident.setAttribute(XID_DOMAIN, xid.getDomain());
			ident.setAttribute(XID_DOMAIN_ID, xid.getDomainId());
			int val=xid.getQuality();
			int v1=val&3;
			ident.setAttribute(XID_QUALITY,XID_QUALITIES[v1]);
			ident.setAttribute(XID_GUID, Boolean.toString(isUUID(xid)));
			ret.addContent(ident);
		}
		return ret;
	}

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
					if(isUUID(xid)){
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
