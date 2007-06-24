package ch.elexis.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.inputs.DecoratedStringChooser;
import ch.elexis.util.DecoratedString;
import ch.rgw.IO.Settings;

public class ReminderPrefences extends PreferencePage implements
		IWorkbenchPreferencePage {
	Settings cfg;
	DecoratedString[] strings;
	
	public ReminderPrefences() {
		super("Pendenzen");
		cfg=Hub.userCfg.getBranch(PreferenceConstants.USR_REMINDERCOLORS, true);
		strings=new DecoratedString[3];
		strings[0]=new DecoratedString("geplant");
		strings[1]=new DecoratedString("fällig");
		strings[2]=new DecoratedString("überfällig");
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret,SWT.NONE).setText("Farben für die Anzeige einstellen");
		new DecoratedStringChooser(ret,cfg,strings);
		return ret;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performOk() {
		Hub.userCfg.flush();
		return super.performOk();
	}
	

}
