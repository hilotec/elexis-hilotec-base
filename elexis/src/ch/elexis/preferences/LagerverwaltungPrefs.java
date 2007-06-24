/*******************************************************************************
 * Copyright (c) 2005-2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/
package ch.elexis.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

/**
 * Einstellungen für die Lagerverwaltung
 * @author Daniel Lutz <danlutz@watz.ch>
 */
public class LagerverwaltungPrefs  extends FieldEditorPreferencePage
					implements IWorkbenchPreferencePage {

	public LagerverwaltungPrefs(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.globalCfg));
		setDescription("Lagerverwaltung");
	}
	
	@Override
	protected void createFieldEditors() {
		addField(new RadioGroupFieldEditor(
				PreferenceConstants.INVENTORY_ORDER_TRIGGER, "Bestellkriterium",
				1, new String[][] {
						{
							"Bestellen, wenn Minbestand unterschritten",
							PreferenceConstants.INVENTORY_ORDER_TRIGGER_BELOW_VALUE
						}, {
							"Bestellen, wenn Minbestand erreicht",
							PreferenceConstants.INVENTORY_ORDER_TRIGGER_EQUAL_VALUE
						},
		}, getFieldEditorParent()));
	}

	public void init(final IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		if(super.performOk()){
			Hub.globalCfg.flush();
			return true;
		}
		return false;
	}
}
