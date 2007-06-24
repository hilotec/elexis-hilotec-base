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
 * $Id: LaborleistungPrefs.java 1625 2007-01-19 20:01:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.tarmedprefs;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.data.LaborLeistung;

public class LaborleistungPrefs extends PreferencePage implements
		IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		return new MultiplikatorEditor(parent,LaborLeistung.class,""); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		
	}

}
