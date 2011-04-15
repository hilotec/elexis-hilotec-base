package ch.elexis.laborimport.medics.views;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.labor.medics.MedicsPreferencePage;
import ch.elexis.labor.medics.Messages;
import ch.elexis.util.SWTHelper;

public class MedicsBrowserView extends ViewPart {
	public static final String ID = "ch.elexis.medics.views.MedicsBrowserView"; //$NON-NLS-1$
	
	private Browser browser = null;
	
	@Override
	public void createPartControl(Composite parent){
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		
		URL url = null;
		try {
			url = MedicsPreferencePage.getIMedUrl();
		} catch (Throwable e) {
			SWTHelper.showError(Messages.MedicsBrowserView_errorOpeningBrowserURL, e.getMessage());
		}
		
		if (url != null) {
			browser = new Browser(container, SWT.NONE);
			browser.setUrl(url.toString());
		}
	}
	
	@Override
	public void setFocus(){
		if (browser != null) {
			browser.setFocus();
		}
	}
	
}
