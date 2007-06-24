/*******************************************************************************
 * Copyright (c) 2006, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *  $Id: ExterneDokumente.java 2391 2007-05-18 12:46:08Z danlutz $
 *******************************************************************************/
package ch.elexis.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;

/**
 * Einstellungen zur Verknüpfung externen Dokumenten
 * @author Daniel Lutz
 */
public class ExterneDokumente extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public ExterneDokumente() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
		setDescription("Externe Dokumente");
	}

	@Override
	protected void createFieldEditors() {
		DirectoryFieldEditor dfe;
		
		dfe=new DirectoryFieldEditor(PreferenceConstants.BASIS_PFAD,
				"Verzeichnis für externe Dokumente", getFieldEditorParent());
		addField(dfe);
		
		dfe=new DirectoryFieldEditor(PreferenceConstants.BASIS_PFAD2,
				"Verzeichnis für externe Dokumente", getFieldEditorParent());
		addField(dfe);

		dfe=new DirectoryFieldEditor(PreferenceConstants.BASIS_PFAD3,
				"Verzeichnis für externe Dokumente", getFieldEditorParent());
		addField(dfe);
	}

	public void init(IWorkbench workbench) {
	}
}
