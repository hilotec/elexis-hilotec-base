package ch.elexis.exchange;

import java.io.FileOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.elements.ServiceBlockElement;
import ch.elexis.exchange.elements.ServiceBlocksElement;
import ch.elexis.exchange.elements.XChangeElement;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class BlockExporter extends XChangeExporter {
	Document doc;
	Element eRoot;
	ServiceBlocksElement lbs;
	
	public BlockExporter(){
		doc = new Document();
		eRoot = new Element(getContainer().ROOT_ELEMENT, getContainer().ns);
		eRoot.addNamespaceDeclaration(getContainer().nsxsi);
		eRoot.addNamespaceDeclaration(getContainer().nsschema);
		eRoot.setAttribute("timestamp", new TimeTool().toString(TimeTool.DATETIME_XML)); //$NON-NLS-1$
		eRoot.setAttribute("id", XMLTool.idToXMLID(StringTool.unique("xChange"))); //$NON-NLS-1$ //$NON-NLS-2$
		eRoot.setAttribute("origin", XMLTool.idToXMLID(Hub.actMandant.getId())); //$NON-NLS-1$
		eRoot.setAttribute("destination", "undefined"); //$NON-NLS-1$ //$NON-NLS-2$
		eRoot.setAttribute("responsible", XMLTool.idToXMLID(Hub.actMandant.getId())); //$NON-NLS-1$
		doc.setRootElement(eRoot);
		//lbs = new ServiceBlocksElement(this, null);
		eRoot.addContent(lbs.getElement());
	}
	
	
	
	
	
	public boolean canHandle(Class<? extends PersistentObject> clazz){
		if (clazz.equals(Leistungsblock.class)) {
			return true;
		}
		return false;
	}
	
	public void finalizeExport() throws XChangeException{
		FileDialog fd = new FileDialog(Desk.getTopShell(), SWT.SAVE);
		fd.setText(Messages.BlockContainer_Blockbeschreibung);
		fd.setFilterExtensions(new String[] {
			"*.xchange" //$NON-NLS-1$
		});
		fd.setFilterNames(new String[] {
			Messages.BlockContainer_xchangefiles
		});
		String filename = fd.open();
		if (filename != null) {
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8"); //$NON-NLS-1$
			XMLOutputter xmlo = new XMLOutputter(format);
			String xmlAspect = xmlo.outputString(doc);
			try {
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(xmlAspect.getBytes());
				fos.close();
			} catch (Exception ex) {
				ExHandler.handle(ex);
				throw new XChangeException("Output failed "+ex.getMessage());
			}
		}
		
	}
	
	public XChangeElement store(Object output) throws XChangeException{
		if (output instanceof Leistungsblock) {
			ServiceBlockElement sbe = new ServiceBlockElement().asExporter(this, (Leistungsblock) output);
			lbs.add(sbe);
			return sbe;
		}
		throw new XChangeException("Can't handle object type "+output.getClass().getName());
	}
	
	
}
