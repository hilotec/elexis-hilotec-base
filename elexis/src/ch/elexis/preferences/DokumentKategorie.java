/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: DokumentKategorie.java 602 2006-07-26 21:21:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

public class DokumentKategorie extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public DokumentKategorie() {
		super(GRID);
		 setPreferenceStore(new SettingsPreferenceStore(Hub.globalCfg));
        setDescription("Dokument-Kategorien");

	}

	@Override
	public void createFieldEditors() {
		/*
		addField(new Agenda.StringInput(
				PreferenceConstants.DOC_CATEGORY,
				"Dokumentkategorien",
				getFieldEditorParent()
				));
				*/
	}

	public void init(IWorkbench workbench) {
		// TODO Automatisch erstellter Methoden-Stub

	}

}
