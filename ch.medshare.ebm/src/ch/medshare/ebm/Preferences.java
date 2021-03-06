package ch.medshare.ebm;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.util.SWTHelper;

public class Preferences extends PreferencePage implements IWorkbenchPreferencePage {
	
	public static final String EBM_BASE = "ebm/";
	public static final String PREFERENCES = "Preferences";
	public static final String URL = "url";
	public static final String USER = "user";
	public static final String PASS = "pass";
	public static final String TITLE = "title";
	public static final String LOGGEDIN = "";
	
	public static class Defaults {
		public static final String DEFAULTS = PREFERENCES + "." + "Defaults";
		
		public static final String URL = Messages.getString(DEFAULTS, Preferences.URL);
		public static final String USER = Messages.getString(DEFAULTS, Preferences.USER);
		public static final String PASS = Messages.getString(DEFAULTS, Preferences.PASS);
		public static final String LOGGEDIN = Messages.getString(DEFAULTS, Preferences.LOGGEDIN);
	}
	
	private Text url, user, pass;
	
	public Preferences(){
		super(Messages.getString(PREFERENCES, TITLE));
		setPreferenceStore(new SettingsPreferenceStore(Hub.userCfg));
	}
	
	@Override
	protected Control createContents(final Composite parent){
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		new Label(ret, SWT.NONE).setText(Messages.getString(PREFERENCES, URL));
		url = new Text(ret, SWT.BORDER);
		url.setText(Hub.userCfg.get(URL, Defaults.URL));
		
		new Label(ret, SWT.NONE).setText(Messages.getString(PREFERENCES, USER));
		user = new Text(ret, SWT.BORDER);
		user.setText(Hub.userCfg.get(USER, Defaults.USER));
		
		new Label(ret, SWT.NONE).setText(Messages.getString(PREFERENCES, PASS));
		pass = new Text(ret, SWT.BORDER);
		pass.setText(Hub.userCfg.get(PASS, Defaults.PASS));
		
		return ret;
	}
	
	public void init(final IWorkbench workbench){

	}
	
	@Override
	public boolean performOk(){
		Hub.userCfg.set(URL, url.getText());
		Hub.userCfg.set(USER, user.getText());
		Hub.userCfg.set(PASS, pass.getText());
		Hub.userCfg.flush();
		return super.performOk();
	}
	
	@Override
	public void performDefaults(){
		Hub.userCfg.set(URL, Defaults.URL);
		url.setText(Defaults.URL);
		performOk();
	}
}