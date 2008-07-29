package ch.elexis.images;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom.Element;

import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.MarkupElement;
import ch.elexis.exchange.elements.MetaElement;
import ch.elexis.exchange.elements.RecordElement;
import ch.elexis.text.Samdas;
import ch.elexis.text.Samdas.Record;
import ch.elexis.text.Samdas.XRef;

public class ExchangeContributor implements IExchangeContributor {
	public static final String PLUGIN_ID="ch.elexis.bildanzeige";

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
						MarkupElement eXref=new MarkupElement(container);
						eXref.setAttribute("type",PLUGIN_ID);
						eXref.setAttribute("pos",Integer.toString(xref.getPos()));
						eXref.setAttribute("len",Integer.toString(xref.getLength()));
						eXref.setAttribute("hint","warn Bild konnte nicht angezeigt werden");
						MetaElement mID=new MetaElement(container,"idref",bild.getId());
						eXref.addContent(mID);
						container.addBinary(bild.getId(), data);
						//container.addChoice(key, name)
					}
				}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public void importHook(XChangeContainer container, PersistentObject context) {
		String rootpath=container.getProperty("ROOTPATH");
		List<RecordElement> records=(List<RecordElement>) container.getElements(rootpath+"/records/record");
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
