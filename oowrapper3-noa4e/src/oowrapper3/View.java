package oowrapper3;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.util.PlatformHelper;

import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.noa4e.ui.widgets.OfficePanel;

public class View extends ViewPart {
	
	private OfficePanel officePanel = null;
	
	public void createPartControl(Composite parent){
		officePanel = new OfficePanel(parent, SWT.NONE);
		String myBase=PlatformHelper.getBasePath("ch.elexis.oowrapper3");
		final String empty=myBase+File.separator+"rsc"+File.separator+"empty.odt";
		officePanel.loadDocument(false, empty, DocumentDescriptor.DEFAULT);
	}
	
	public void setFocus(){
		officePanel.setFocus();
	}
	
	/**
	 * Eine Methode zum Laden eines Dokumentes auf der Grundlage des absoluten Pfades.
	 */
	public void loadDocument(String documentPath){
		officePanel.loadDocument(false, documentPath, null);
	}
	
}
