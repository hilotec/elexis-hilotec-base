/**
 * (c) 2007 by G. Weirich
 * All rights reserved
 * $Id: PreferencePage.java 116 2007-06-07 07:06:44Z gerry $
 */
package ch.elexis.laborimport.viollier;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public static final String DL_DIR= "viollier/downloaddir";
	public static final String OPENMEDICAL_KEY="viollier/openmedical_key";
	
	public PreferencePage(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
	}
	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(DL_DIR,"Download Verzeichnis",getFieldEditorParent()));
		addField(new StringFieldEditor(OPENMEDICAL_KEY,"Openmedical Passwort",
				getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
