package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.XMLTool;
import ch.rgw.tools.StringTool;

public class XidElement extends XChangeElement {
	public static final String XMLNAME="xid";
	public static final String ATTR_UUID="uuid";
	public static final String ELEMENT_IDENTITY="identity";
	public static final String ATTR_IDENTITY_DOMAIN="domain";
	public static final String ATTR_IDENTITY_DOMAIN_ID="domainID";
	public static final String ATTR_IDENTITY_QUALITY="quality";
	public static final String ATTR_ISGUID="isGUID";
	
	public static final String[] IDENTITY_QUALITIES={
		"unknownAssignment",
		"localAssignment",
		"regionalAssignment",
		"globalAssignment"
	};
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public static boolean isUUID(Xid xid){
		return (xid.getQuality()&4)!=0;
	}
	
	public XidElement(XChangeContainer parent){
		super(parent);
	}
	
	public XidElement(XChangeContainer home, LabItem li){
		super(home);
		setID(li.getId());
		String domain=FindingElement.XIDBASE+li.getLabor().getLabel();
		Xid.localRegisterXIDDomainIfNotExists(domain, li.getLabel(), Xid.ASSIGNMENT_LOCAL);
		Identity ident=new Identity(home,domain,li.getName(),1,true);
		addContent(ident);

	}
	
	public XidElement(XChangeContainer home, Artikel art){
		super(home);
		setID(art.getId());
		String ean=art.getEAN();
		if(!StringTool.isNothing(ean)){
			Identity ident=new Identity(home,Xid.DOMAIN_EAN,ean,2,false);
			addContent(ident);
		}
		String pk=art.getPharmaCode();
		if(!StringTool.isNothing(pk)){
			Identity ident=new Identity(home,Artikel.XID_PHARMACODE,pk,2,false);
			addContent(ident);
		}

	}
	
	public XidElement(XChangeContainer home, Kontakt k){
		super(home);
		Xid best=k.getXid();
		String id=XMLTool.idToXMLID(k.getId());
		if((best.getQuality()&7)>=Xid.QUALITY_GUID){
			id=XMLTool.idToXMLID(best.getDomainId());
		}else{
			k.addXid(Xid.DOMAIN_ELEXIS, id, true);
		}
		setID(id);
		List<Xid> xids=k.getXids();
		for(Xid xid:xids){
			int val=xid.getQuality();
			int v1=val&3;
			Identity ident=new Identity(home,xid.getDomain(),xid.getDomainId(),v1,isUUID(xid));
			addContent(ident);
		}

	}
	@SuppressWarnings("serial")
	static class Identity extends XChangeElement{
		public String getXMLName(){
			return ELEMENT_IDENTITY;
		}
		public Identity(XChangeContainer home){
			super(home);
		}
		public Identity(XChangeContainer home, String domain, String domain_id,
						int quality, boolean isGuid){
			super(home);
			setAttribute(ATTR_IDENTITY_DOMAIN, domain);
			setAttribute(ATTR_IDENTITY_DOMAIN_ID, domain_id);
			setAttribute(ATTR_IDENTITY_QUALITY, IDENTITY_QUALITIES[quality]);
			setAttribute(ATTR_ISGUID,Boolean.toString(isGuid));
		}
	}
}
