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
 * $Id: Leistungscodes.java 134 2006-04-08 09:11:18Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Leistungscodes extends PreferencePage implements
		IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new FillLayout());
		StyledText text=new StyledText(ret,SWT.NONE);
		text.setWordWrap(true);
		text.setText("Unter dieser Rubrik können Einstellungen für verschiedene\n"+
				"Leistungscode-Plugins verwaltet werden.\n"
				+"Die Einstellungsseiten erscheinen nur, falls entsprechende Plugins\n"+
				"installiert sind.");
		return ret;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
