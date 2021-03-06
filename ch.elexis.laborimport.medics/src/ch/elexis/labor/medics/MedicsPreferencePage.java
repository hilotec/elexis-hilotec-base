package ch.elexis.labor.medics;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;

/**
 * Einstellungen für Medics Plugin
 * 
 * @author immi
 */
public class MedicsPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public static final String DOWNLOAD_DIR = "medics/download"; //$NON-NLS-1$
	public static final String UPLOAD_DIR = "medics/upload"; //$NON-NLS-1$
	public static final String ARCHIV_DIR = "medics/archiv"; //$NON-NLS-1$
	public static final String ERROR_DIR = "medics/error"; //$NON-NLS-1$
	public static final String I_MED_URL = "medics/imed/url"; //$NON-NLS-1$
	public static final String BROWSER_EXTERN = "medics/imed/extern"; //$NON-NLS-1$
	public static final String DOKUMENT_CATEGORY = "medics/extern"; //$NON-NLS-1$
	public static final String DELETE_ARCHIV_DAYS = "medics/del_archiv/days"; //$NON-NLS-1$
	
	private static final String DEFAULT_DOWNLOAD = ""; //$NON-NLS-1$
	private static final String DEFAULT_UPLOAD = ""; //$NON-NLS-1$
	private static final String DEFAULT_ARCHIV = ""; //$NON-NLS-1$
	private static final String DEFAULT_MEDICS_URL = Messages.MedicsPreferencePage_defaultMedicsUrl;
	private static final boolean DEFAULT_BROWSER_EXTERN = false;
	private static final String DEFAULT_DOKUMENT_CATEGORY =
		Messages.MedicsPreferencePage_documentCategoryName;
	private static final int DEFAULT_DELETE_ARCHIV_DAYS = 30;
	
	public MedicsPreferencePage(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
		getPreferenceStore().setDefault(DOWNLOAD_DIR, DEFAULT_DOWNLOAD);
		getPreferenceStore().setDefault(UPLOAD_DIR, DEFAULT_UPLOAD);
		getPreferenceStore().setDefault(ARCHIV_DIR, DEFAULT_ARCHIV);
		getPreferenceStore().setDefault(I_MED_URL, DEFAULT_MEDICS_URL);
		getPreferenceStore().setDefault(BROWSER_EXTERN, DEFAULT_BROWSER_EXTERN);
		getPreferenceStore().setDefault(DOKUMENT_CATEGORY, DEFAULT_DOKUMENT_CATEGORY);
		getPreferenceStore().setDefault(DELETE_ARCHIV_DAYS, DEFAULT_DELETE_ARCHIV_DAYS);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new DirectoryFieldEditor(DOWNLOAD_DIR,
			Messages.MedicsPreferencePage_labelDownloadDir, getFieldEditorParent()));
		addField(new DirectoryFieldEditor(UPLOAD_DIR, Messages.MedicsPreferencePage_labelUploadDir,
			getFieldEditorParent()));
		addField(new DirectoryFieldEditor(ARCHIV_DIR, Messages.MedicsPreferencePage_labelArchivDir,
			getFieldEditorParent()));
		addField(new DirectoryFieldEditor(ERROR_DIR, Messages.MedicsPreferencePage_labelErrorDir,
			getFieldEditorParent()));
		addField(new StringFieldEditor(DOKUMENT_CATEGORY,
			Messages.MedicsPreferencePage_labelDocumentCategory, getFieldEditorParent()));
		addField(new StringFieldEditor(DELETE_ARCHIV_DAYS,
			"Archiv bereinigen (Tage)", getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {
	}
	
	public static String getUploadDir(){
		return Hub.localCfg.get(UPLOAD_DIR, DEFAULT_UPLOAD);
	}
	
	public static String getDownloadDir(){
		return Hub.localCfg.get(DOWNLOAD_DIR, DEFAULT_DOWNLOAD);
	}
	
	public static String getArchivDir(){
		return Hub.localCfg.get(ARCHIV_DIR, DEFAULT_ARCHIV);
	}
	
	public static String getErrorDir(){
		return Hub.localCfg.get(ERROR_DIR, null);
	}
	
	public static int getDeleteArchivDays(){
		return Hub.localCfg.get(DELETE_ARCHIV_DAYS, DEFAULT_DELETE_ARCHIV_DAYS);
	}
	
	public static URL getIMedUrl() throws MalformedURLException{
		String urlStr = Hub.localCfg.get(I_MED_URL, DEFAULT_MEDICS_URL);
		if (!urlStr.startsWith("http")) { //$NON-NLS-1$
			urlStr += "http://" + urlStr; //$NON-NLS-1$
		}
		return new URL(urlStr);
	}
	
	public static boolean showExtern(){
		return Hub.localCfg.get(BROWSER_EXTERN, DEFAULT_BROWSER_EXTERN);
	}
	
	public static String getDokumentKategorie(){
		return Hub.localCfg.get(DOKUMENT_CATEGORY, DEFAULT_DOKUMENT_CATEGORY);
	}
}
