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
 * $Id$
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.List;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Query;

public class AgendaDruck extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	SettingsPreferenceStore prefs = new SettingsPreferenceStore(Hub.localCfg);
	 
    public AgendaDruck() {
        super(GRID);
        
        prefs.setDefault(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE,
        		PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE_DEFAULT);

        setPreferenceStore(prefs);

        setDescription("Einstellungen für Agenda-Ausdruck");
    }

     @Override
    protected void createFieldEditors() {
    	 Brief[] templates = getSystemTemplates();
    	 String[][] entryNamesAndValues = new String[templates.length][];
    	 for (int i = 0; i < templates.length; i++) {
    		 Brief brief = templates[i];
    		 String[] nameAndValue = new String[] {
    				 brief.getBetreff(),
    				 brief.getBetreff(),
    		 };
    		 entryNamesAndValues[i] = nameAndValue;
    	 }
    	 
    	 
    	 addField(new ComboFieldEditor(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE,
    			 "Systemvorlage für Terminkarte", entryNamesAndValues, getFieldEditorParent()));
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
	
	private Brief[] getSystemTemplates() {
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add("Typ","=", Brief.TEMPLATE);
		qbe.add("BehandlungsID", "=", "SYS");
		qbe.startGroup();
		qbe.add("DestID", "=", Hub.actMandant.getId());
		qbe.or();
		qbe.add("DestID", "=", "");
		qbe.endGroup();
		qbe.and();
		qbe.add("geloescht", "<>", "1");

		qbe.orderBy(false, "Datum");
		List<Brief> l = qbe.execute();
		if (l != null) {
			return l.toArray(new Brief[0]);
		} else {
			return new Brief[0];
		}
	}

}
