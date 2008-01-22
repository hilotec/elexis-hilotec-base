/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: AutoUpdate.java 2280 2007-04-20 17:04:27Z rgw_ch $
 *******************************************************************************/

package ch.elexis.update;

import java.io.File;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	public static final String UPDATE_SITE="updater/url";
	public static final String AUTO_UPDATE_INTERVAL="updater/auto";
	public static final String TEMPDIR="updater/tempdir";
	public static final String DAYS_UNTIL_NEXT_UPDATE="updater/daysleft";
	public static final String DELETE_FILES_ON_FINISH="updater/deletefiles";
	
	public Preferences(){
		super(GRID);
		//SettingsPreferenceStore cfg=new SettingsPreferenceStore(Hub.localCfg);
		if(Hub.localCfg.get(UPDATE_SITE, null)==null){
			Hub.localCfg.set(UPDATE_SITE, "http://www.rgw.ch/update12.php");
		}
		if(Hub.localCfg.get(AUTO_UPDATE_INTERVAL, null)==null){
			Hub.localCfg.set(AUTO_UPDATE_INTERVAL, "0");
		}
		File tempdir=new File(Hub.localCfg.get(TEMPDIR, ""));
		if(!tempdir.isDirectory()){
			String temp=System.getProperty("java.io.tmpdir");
			Hub.localCfg.set(TEMPDIR, temp);
		}
		SettingsPreferenceStore spr=new SettingsPreferenceStore(Hub.localCfg);
		spr.setDefault(DELETE_FILES_ON_FINISH, true);
		setPreferenceStore(spr);
		
		setDescription("Einstellungen für den Auto-Updater");
		
	}
	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(UPDATE_SITE,
				"Update Site (URL)",getFieldEditorParent()));
		
		addField(new StringFieldEditor(AUTO_UPDATE_INTERVAL,
				"Alle (0=nie) Tage automatisch suchen",getFieldEditorParent()));
		
		addField(new DirectoryFieldEditor(TEMPDIR,
				"Verzeichnis für Zwischenspeicherung",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(DELETE_FILES_ON_FINISH,"Heruntergeladene Dateien nach Update löschen",
				getFieldEditorParent()));
		
	}
	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}


}
