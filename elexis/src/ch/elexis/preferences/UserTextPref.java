/*******************************************************************************
 * Copyright (c) 2006, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *  $Id: UserTextPref.java 1867 2007-02-21 06:10:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.text.EnhancedTextField;
import ch.rgw.IO.InMemorySettings;

/**
 * Benutzerspezifische Einstellungen
 */
public class UserTextPref extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public static final String ID = "ch.elexis.preferences.UserPreferences";

    private static final String[] fields = {
    	EnhancedTextField.MACRO_KEY
    };
    
    private static final String[] texte = {
    	"Makro-Zeichen"
    };
    
	public UserTextPref() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(new InMemorySettings()));
		setDescription("Benutzereinstellungen");
	}

	@Override
	protected void createFieldEditors() {
        for (int i=0; i < fields.length; i++) {
            addField(new StringFieldEditor(fields[i], texte[i], getFieldEditorParent()));
        }
    }

	public void init(IWorkbench workbench) {
        for (String field : fields) {
        	String value = Hub.userCfg.get(field, EnhancedTextField.MACRO_KEY_DEFAULT);
            getPreferenceStore().setValue(field, value);
        }
    }

        @Override
	public boolean performOk() {
		super.performOk();

        for(String field : fields) {
        	String value = getPreferenceStore().getString(field);
        	Hub.userCfg.set(field, value);
        }

        return true;
	}
    
	@Override
    protected void performDefaults()
    {  
       this.initialize();
    }
}
