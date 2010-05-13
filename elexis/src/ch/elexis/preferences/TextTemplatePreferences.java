package ch.elexis.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

public class TextTemplatePreferences extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String BRANCH="textvariables/";
	
	public TextTemplatePreferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
	}


	@Override
	protected void createFieldEditors() {
		IExtensionRegistry exr = Platform.getExtensionRegistry();
		IExtensionPoint exp = exr.getExtensionPoint("ch.elexis.documentTemplates");
		if (exp != null) {
			IExtension[] extensions = exp.getExtensions();
			for (IExtension ex : extensions) {
				IConfigurationElement[] elems = ex.getConfigurationElements();
				for (IConfigurationElement el : elems) {
					String n=el.getAttribute("name");
					addField(new StringFieldEditor(BRANCH+n, n, getFieldEditorParent()));
				}
			}
			
		}
	}

	
	@Override
	protected void performApply() {
		Hub.localCfg.flush();
	}


	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
