package ch.elexis.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import ch.elexis.Hub;

public class Script extends FieldEditorPreferencePage {
	public static final String SCRIPT_CLASSPATH_EXT="script/classpath";
	
	public Script(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.userCfg));
	}
	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(SCRIPT_CLASSPATH_EXT,"Erweiterungen",
				getFieldEditorParent()));

	}

}
