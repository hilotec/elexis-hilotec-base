/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

public class UserSettings2 extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String EXPANDABLE_COMPOSITES_BASE="view/expandableComposites";
	public static final String EXPANDABLE_COMPOSITES=EXPANDABLE_COMPOSITES_BASE+"/setting";
	public static final String STATES=EXPANDABLE_COMPOSITES_BASE+"/states/";
	public static final String OPEN="1";
	public static final String CLOSED="2";
	public static final String REMEMBER_STATE="3";
	
	private SettingsPreferenceStore prefs=new SettingsPreferenceStore(Hub.userCfg);
	public UserSettings2(){
		super(GRID);
		setPreferenceStore(prefs);
		prefs.setDefault(EXPANDABLE_COMPOSITES, REMEMBER_STATE);
		System.out.println(getPreferenceStore().getString(EXPANDABLE_COMPOSITES));
	}
	@Override
	protected void createFieldEditors() {
		addField(new RadioGroupFieldEditor(EXPANDABLE_COMPOSITES,
				"Erweiterbare Felder",1,new String[][]{
				{"Immer geöffnet",OPEN},
				{"Immer geschlossen",CLOSED},
				{"Letzten Zustand merken",REMEMBER_STATE}	
				
		},getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean performOk() {
		prefs.flush();
		return super.performOk();
	}
	

	
}
