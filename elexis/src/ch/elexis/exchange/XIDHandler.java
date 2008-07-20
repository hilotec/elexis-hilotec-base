package ch.elexis.exchange;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;

public class XIDHandler {
	public static final String XID_ELEMENT="xid";
	public static final String XID_UUID="uuid";
	public static final String XID_IDENTITY="identity";
	public static final String XID_DOMAIN="domain";
	public static final String XID_DOMAIN_ID="domainID";
	public static final String XID_QUALITY="quality";
	public static final String XID_GUID="isGUID";
	public static final String[] XID_QUALITIES={
		"localAssignement",
		"regionalAssignment",
		"globalAssignment"
	};
	
	public boolean isUUID(Xid xid){
		return (xid.getQuality()&4)!=0;
	}
	
	public Element createXidElement(PersistentObject po, Namespace ns){
		List<Xid> xids=po.getXids();
		Xid best=po.getXid();
		String id=po.getId();
		if(best.getQuality()>=Xid.QUALITY_GUID){
			id=best.getDomainId();
		}
		Element ret=new Element(XID_ELEMENT,ns);
		ret.setAttribute(XID_UUID, id);
		for(Xid xid:xids){
			Element ident=new Element(XID_IDENTITY,ns);
			ident.setAttribute(XID_DOMAIN, xid.getDomain());
			ident.setAttribute(XID_DOMAIN_ID, xid.getDomainId());
			int val=xid.getQuality();
			int v1=val&3;
			ident.setAttribute(XID_QUALITY,XID_QUALITIES[v1]);
			ident.setAttribute(XID_GUID, Boolean.toString(isUUID(xid)));
		}
		return ret;
	}
}
