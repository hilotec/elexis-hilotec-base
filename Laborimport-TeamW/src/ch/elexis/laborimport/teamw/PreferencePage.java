/**
 * (c) 2007 by G. Weirich
 * All rights reserved
 * 
 * From: Laborimport Viollier
 * 
 * Adapted to Bioanalytica by Daniel Lutz <danlutz@watz.ch>
 * Adapted to Risch by Gerry Weirich
 * 
 * $Id: PreferencePage.java 396 2007-12-17 05:37:27Z Gerry $
 */

package ch.elexis.laborimport.teamw;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public static final String FTP_HOST ="teamw/ftp_host"; //$NON-NLS-1$
	public static final String FTP_USER ="teamw/ftp_user"; //$NON-NLS-1$
	public static final String FTP_PWD ="teamw/ftp_pwd"; //$NON-NLS-1$
	public static final String DL_DIR = "teamw/downloaddir"; //$NON-NLS-1$
	
	public static final String DEFAULT_FTP_HOST = "195.144.61.180"; //$NON-NLS-1$
	public static final String DEFAULT_FTP_USER = ""; //$NON-NLS-1$
	public static final String DEFAULT_FTP_PWD = ""; //$NON-NLS-1$
	public static final String DEFAULT_DL_DIR = "/tmp/teamw"; //$NON-NLS-1$
	
	SettingsPreferenceStore prefs = new SettingsPreferenceStore(Hub.globalCfg);
	
	public PreferencePage(){
		super(GRID);
		prefs.setDefault(FTP_HOST, DEFAULT_FTP_HOST); //$NON-NLS-1$
	    prefs.setDefault(FTP_USER, DEFAULT_FTP_USER); //$NON-NLS-1$
	    prefs.setDefault(FTP_PWD, DEFAULT_FTP_PWD); //$NON-NLS-1$
	    
	    prefs.setDefault(DL_DIR, DEFAULT_DL_DIR); //$NON-NLS-1$
	    
		setPreferenceStore(prefs);
		
		setDescription(Messages.getString("PreferencePage.title.description"));  //$NON-NLS-1$
	}
	
	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(FTP_HOST, Messages.getString("PreferencePage.label.host"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new StringFieldEditor(FTP_USER, Messages.getString("PreferencePage.label.user"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new StringFieldEditor(FTP_PWD, Messages.getString("PreferencePage.label.password"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new DirectoryFieldEditor(DL_DIR, Messages.getString("PreferencePage.label.download"), getFieldEditorParent())); //$NON-NLS-1$
	}

	public void init(final IWorkbench workbench) {
		// Do nothing
	}
}
