package ch.elexis.artikel_at.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class FachinformationArzneimittel extends ViewPart {
	public static final String URL_BASE="https://root.ami-info.at/company/ami-info/fachinformation.asp?";
	public static final String ID="elexis-artikel-oesterreich.fachinformationarzneimittel";
	private static final String uid = "";
	public static String CURRENT_PhZnR = "";
	public static String CURRENT_ZNr = "";
	
	public static boolean setActiveMedikament(String PhZNr, String ZNr) {
		CURRENT_PhZnR=PhZNr;
		CURRENT_ZNr=ZNr;
		return true;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		final Browser browser=new Browser(parent,SWT.NONE);
		StringBuilder sb = new StringBuilder();
		sb.append(URL_BASE);
		sb.append("uid="+uid);
		sb.append("&pid="+CURRENT_PhZnR);
		sb.append("&znr="+CURRENT_ZNr);
		//System.out.println(sb.toString());
		browser.setUrl(sb.toString());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
