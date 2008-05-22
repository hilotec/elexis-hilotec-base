/*******************************************************************************
 * Copyright (c) 2005-2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 * $Id: AgendaAnzeige.java 3947 2008-05-22 18:33:28Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.rgw.tools.StringTool;

public class AgendaAnzeige extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	SettingsPreferenceStore prefs = new SettingsPreferenceStore(Hub.userCfg);
	 
    public AgendaAnzeige() {
        super(GRID);
        
        prefs.setDefault(PreferenceConstants.AG_SHOW_REASON, false);

        setPreferenceStore(prefs);

        setDescription(Messages.AgendaAnzeige_options); 
    }

     @Override
    protected void createFieldEditors() {
    	 addField(new BooleanFieldEditor(PreferenceConstants.AG_SHOW_REASON,
    			 Messages.AgendaAnzeige_showReason, getFieldEditorParent()));
    }
     
    @Override
	public boolean performOk() {
    	prefs.flush();
    	return super.performOk();
	}

	public void init(IWorkbench workbench)
    {
        // TODO Auto-generated method stub

    }

}
