package ch.elexis.images;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom.Element;

import sun.misc.BASE64Decoder;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas;
import ch.elexis.text.Samdas.Record;
import ch.elexis.text.Samdas.XRef;
import ch.rgw.tools.ExHandler;

public class ExchangeContributor implements IExchangeContributor {

	public void exportHook(XChangeContainer container, PersistentObject object) {
		if(object instanceof Konsultation){
			Konsultation k=(Konsultation)object;
			Samdas smd=new Samdas(k.getEintrag().getHead());
			Record record=smd.getRecord();
			List<XRef> xrefs=record.getXrefs();
			for(XRef xref:xrefs){
				if(xref.getProvider().equals("bildanzeige")){
					Bild bild=new Bild(xref.getID());
					byte[] data=bild.getData();
					if(data!=null && data.length>0){
						Element eXref=container.createElement("xref");
						eXref.setAttribute("id",bild.getId());
						eXref.setAttribute("type","image/jpeg");
						eXref.setAttribute("pos",Integer.toString(xref.getPos()));
						eXref.setAttribute("len",Integer.toString(xref.getLength()));
						eXref.setAttribute("hint","warn Bild konnte nicht angezeigt werden");
						//BASE64Encoder b64=new BASE64Encoder();
						//String base64=b64.encode(data);
						//eXref.setAttribute("content","inline");
						//eXref.addContent(base64);
						eXref.setAttribute("content","ext");
						container.addBinary(bild.getId(), data);
						//exporting.addContent(eXref);
					}
				}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public void importHook(XChangeContainer container) {
		String rootpath=container.getProperty("ROOTPATH");
		List<Element> records=container.getElements(rootpath+"/records/record");
		for(Element re:records){
			/*
			List<Element> xrefs=re.getChildren("xref", container.getNamespace());
			Samdas smd=new Samdas(k.getEintrag().getHead());
			if(xrefs!=null){
				for(Element e:xrefs){
					String type=e.getAttributeValue("type");
					if(type.equalsIgnoreCase("image/jpeg") || type.equalsIgnoreCase("image/png")){
						String content=e.getAttributeValue("content");
						String id=e.getAttributeValue("id");
						byte[] data=null;
						if(content.equals("inline")){
							BASE64Decoder b64=new BASE64Decoder();
							try {
								data=b64.decodeBuffer(e.getText());
							} catch (IOException e1) {
								ExHandler.handle(e1);
								continue;
							}
						}else{
							data=container.getBinary(id);
						}
						Patient pat=k.getFall().getPatient();
						Bild bild=new Bild(pat,"type",data);
						String spos=e.getAttributeValue("pos");
						int pos=(spos==null) ? 0 : Integer.parseInt(spos);
						String slen=e.getAttributeValue("len");
						int len=(slen==null) ? 0 : Integer.parseInt(slen);
						Samdas.Record rec=smd.getRecord();
						Samdas.XRef xr=new Samdas.XRef("bildanzeige",bild.getId(),pos,len);
						rec.add(xr);
						k.updateEintrag(smd.toString(), true);
					}
				}
			}
*/
		}
		
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// Nothing
		
	}

	
}
