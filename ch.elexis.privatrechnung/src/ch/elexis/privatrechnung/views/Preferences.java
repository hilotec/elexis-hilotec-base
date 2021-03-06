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
 * $Id: Preferences.java 4470 2008-09-27 19:53:47Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.views;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.preferences.inputs.KontaktFieldEditor;
import ch.elexis.privatrechnung.data.PreferenceConstants;
import ch.rgw.io.Settings;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	Settings cfg;
	
	public Preferences(){
		super(GRID);
		// cfg=Hub.globalCfg.getBranch(PreferenceConstants.cfgBase, true);
		cfg = Hub.globalCfg;
		setPreferenceStore(new SettingsPreferenceStore(cfg));
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new StringFieldEditor(PreferenceConstants.cfgTemplateESR, "Vorlage mit ESR",
			getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.cfgTemplateBill, "Vorlage ohne ESR",
			getFieldEditorParent()));
		addField(new KontaktFieldEditor(Hub.globalCfg, PreferenceConstants.cfgBank, "Bank",
			getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.esrIdentity, "ESR-Teilnehmernummer",
			getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.esrUser, "ESR-Kundennummer",
			getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
	// TODO Auto-generated method stub
	
	}
	
	@Override
	public boolean performOk(){
		if (super.performOk()) {
			Hub.globalCfg.flush();
			return true;
		}
		return false;
	}
	
}
