package ch.medshare.ebm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;

public class BrowserView extends ViewPart {
	public static final String ID="ebm-guidelines";
	private Browser browser;
	
	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		if(browser != null){
			browser.setUrl(getURL());
		}
	}
	
	private String getURL(){
		String url = Hub.userCfg.get(Preferences.LOGGEDIN, Preferences.Defaults.LOGGEDIN);
		if(url.length()==2) url="https://www.ebm-guidelines.ch/";
		return url;
	}

}
