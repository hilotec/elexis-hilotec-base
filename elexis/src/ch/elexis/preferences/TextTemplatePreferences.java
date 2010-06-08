package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.util.SWTHelper;

public class TextTemplatePreferences extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String BRANCH="document_templates/";
	public static final String SUFFIX_STATION=BRANCH+"suffix_station";
	
	public TextTemplatePreferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
	}


	@Override
	protected void createFieldEditors() {
		Label expl=new Label(getFieldEditorParent(),SWT.WRAP);
		expl.setText("Geben Sie hier an, welche Suffix an Dokumentvorlagen von dieser Station angehängt\n"+
				"werden soll. Wenn Sie  '_pc1' angeben, dann würde zum Beispiel anstelle der Vorlage\n"+
				"'Rezept' von diesem PC aus die Vorlage 'Rezept_pc1' angewendet.\n");
		expl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		addField(new StringFieldEditor(SUFFIX_STATION,"Suffix für diese Station", getFieldEditorParent()));
		/*
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
		*/
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
