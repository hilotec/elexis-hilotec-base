package ch.elexis.views.codesystems;

import java.io.FileInputStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.ServiceBlockElement;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class BlockImporter extends ImporterPage {
	
	@Override
	public Composite createPage(Composite parent){
		FileBasedImporter fbi = new FileBasedImporter(parent, this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		
		String filename = results[0];
		if (StringTool.isNothing(filename)) {
			return new Status(SWT.ERROR, "ch.elexis", "No file given");
		}
		SAXBuilder builder = new SAXBuilder();
		try {
			FileInputStream fips = new FileInputStream(filename);
			Document doc = builder.build(fips);
			monitor.beginTask("Importiere Blöcke", IProgressMonitor.UNKNOWN);
			Element eRoot = doc.getRootElement();
			if (eRoot != null) {
				Element eBlocks = eRoot.getChild("serviceblocks", XChangeContainer.ns);
				if (eBlocks != null) {
					List<Element> lBlocks =
						eBlocks.getChildren("serviceblock", XChangeContainer.ns);
					for (Element eBlock : lBlocks) {
						ServiceBlockElement.createObject(null, eBlock);
					}
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		} catch (Exception ex) {
			return new Status(SWT.ERROR, "ch.elexis", "parse error: " + ex.getMessage());
		}
		
	}
	
	@Override
	public String getDescription(){
		return "Leistungsblöcke importieren";
	}
	
	@Override
	public String getTitle(){
		return "Leistungsblöcke";
	}
	
}
