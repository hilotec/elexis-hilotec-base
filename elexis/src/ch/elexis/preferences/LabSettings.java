package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.preferences.inputs.PrefAccessDenied;

public class LabSettings extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String KEEP_UNSEEN_LAB_RESULTS="lab/keepUnseen";
	
	
	public LabSettings() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.userCfg));
	}

		@Override
	protected Control createContents(final Composite parent) {
		if(Hub.acl.request(AccessControlDefaults.LAB_SEEN)){		
			return super.createContents(parent);
		}else{
			return new PrefAccessDenied(parent);
		}
	}

		@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(KEEP_UNSEEN_LAB_RESULTS,"Neue Laborwerte anzeigen (Tage)"
					,getFieldEditorParent()));
		
	}

	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performOk() {
		Hub.userCfg.flush();
		return super.performOk();
	}

	
}
