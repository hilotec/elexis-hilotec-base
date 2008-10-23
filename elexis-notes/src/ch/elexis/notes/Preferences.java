package ch.elexis.notes;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String CFGTREE="notes/basedir";
	
	public Preferences(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
	}
	
	
	@Override
	protected void createFieldEditors(){
		addField(new DirectoryFieldEditor(CFGTREE,"Basisverzeichnis",getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
	// TODO Auto-generated method stub
	
	}
	
}
