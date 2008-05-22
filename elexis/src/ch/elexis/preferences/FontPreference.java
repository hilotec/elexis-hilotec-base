package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

public class FontPreference extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public FontPreference() {
		super("Schriftarten", GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.userCfg));
	}


	@Override
	protected void createFieldEditors() {
		addField(new FontFieldEditor(PreferenceConstants.USR_DEFAULTFONT,
				"Standardschriftart","Elexis",getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean performOk() {
		
		return super.performOk();
	}
	

}
