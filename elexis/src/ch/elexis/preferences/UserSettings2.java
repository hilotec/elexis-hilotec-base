/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: UserSettings2.java 4061 2008-06-21 23:22:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import ch.elexis.Hub;
import ch.elexis.preferences.inputs.MultilineFieldEditor;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.Patientenblatt2;

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
		prefs.setDefault(PreferenceConstants.USR_PATLIST_SHOWPATNR, false);
		prefs.setDefault(PreferenceConstants.USR_PATLIST_SHOWNAME, true);
		prefs.setDefault(PreferenceConstants.USR_PATLIST_SHOWFIRSTNAME, true);
		prefs.setDefault(PreferenceConstants.USR_PATLIST_SHOWDOB, true);
	}
	@Override
	protected void createFieldEditors() {
		addField(new RadioGroupFieldEditor(EXPANDABLE_COMPOSITES,
				"Erweiterbare Felder",1,new String[][]{
				{"Immer geöffnet",OPEN},
				{"Immer geschlossen",CLOSED},
				{"Letzten Zustand merken",REMEMBER_STATE}	
				
		},getFieldEditorParent()));
		new Label(getFieldEditorParent(),SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(getFieldEditorParent(),SWT.NONE).setText("Anzuzeigende Felder in Patientenliste");
		addField(new BooleanFieldEditor(PreferenceConstants.USR_PATLIST_SHOWPATNR,"Patient-Nr", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.USR_PATLIST_SHOWNAME,"Name",getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.USR_PATLIST_SHOWFIRSTNAME,"Vorname",getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.USR_PATLIST_SHOWDOB,"Geburtsdatum",getFieldEditorParent()));
		new Label(getFieldEditorParent(),SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(getFieldEditorParent(),SWT.NONE).setText("Zusatzfelder im Patient-Detail-Blatt");
		addField(new MultilineFieldEditor(Patientenblatt2.CFG_EXTRAFIELDS,"",5,SWT.NONE, 
				true,getFieldEditorParent()));
		
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean performOk() {
		Hub.userCfg.flush();
		return super.performOk();
	}
	
	/**
	 * save the state of an expandable composite
	 * @param field name of the composite (any unique string, preferably derived from view name)
	 * @param state the state to save
	 */
	public static void saveExpandedState(final String field, final boolean state){
		if(state){
			Hub.userCfg.set(UserSettings2.STATES+field, UserSettings2.OPEN);
		}else{
			Hub.userCfg.set(UserSettings2.STATES+field, UserSettings2.CLOSED);
		}
	}
	/**
	 * Set the state of an expandable Composite to the previously saved state.
	 * @param ec the expandable Composite to expand or collapse
	 * @param field the unique name
	 */
	public static void setExpandedState(final ExpandableComposite ec,final String field){
		String mode=Hub.userCfg.get(UserSettings2.EXPANDABLE_COMPOSITES,UserSettings2.REMEMBER_STATE);
		if(mode.equals(UserSettings2.OPEN)){
			ec.setExpanded(true);
		}else if(mode.equals(UserSettings2.CLOSED)){
			ec.setExpanded(false);
		}else{
			String state=Hub.userCfg.get(UserSettings2.STATES+field,UserSettings2.CLOSED);
			if(state.equals(UserSettings2.CLOSED)){
				ec.setExpanded(false);
			}else{
				ec.setExpanded(true);
			}
		}
	}
}
