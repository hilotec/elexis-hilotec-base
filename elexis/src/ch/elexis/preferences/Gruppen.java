// $Id: Gruppen.java 1849 2007-02-19 07:52:43Z rgw_ch $
/*
 * Created on 11.08.2005
 */
package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.preferences.inputs.PrefAccessDenied;
import ch.elexis.preferences.inputs.StringListFieldEditor;

public class Gruppen extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public Gruppen() {
        super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.globalCfg));
        setDescription("Gruppen und Rechte");
    }

    public void init(IWorkbench workbench)
    {
    	String groups=Hub.globalCfg.get(PreferenceConstants.ACC_GROUPS, null);
    	if(groups==null){
    		Hub.globalCfg.set(PreferenceConstants.ACC_GROUPS, "Admin,Anwender,Alle");
    	}

    }

	@Override
	protected void createFieldEditors() {
		if(Hub.acl.request(AccessControlDefaults.ACL_USERS)){
			addField(new StringListFieldEditor(PreferenceConstants.ACC_GROUPS,"Gruppen","Bitte geben Sie einen Namen für die neu zu erstellende Gruppe ein",
					"Gruppen",getFieldEditorParent()));
		}else{
			new PrefAccessDenied(getFieldEditorParent());
		}
		
	}

}
