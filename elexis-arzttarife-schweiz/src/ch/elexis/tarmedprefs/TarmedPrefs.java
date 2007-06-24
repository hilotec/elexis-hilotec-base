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
 * $Id: TarmedPrefs.java 1625 2007-01-19 20:01:59Z rgw_ch $
 *******************************************************************************/
package ch.elexis.tarmedprefs;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ch.elexis.data.TarmedLeistung;

public class TarmedPrefs extends PreferencePage implements
		IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret,SWT.NONE).setText(Messages.getString("TarmedPrefs.TPKVG")); //$NON-NLS-1$
		new MultiplikatorEditor(ret,TarmedLeistung.class,"KVG"); //$NON-NLS-1$
		new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL);
		new Label(ret,SWT.NONE).setText(Messages.getString("TarmedPrefs.TPUVG")); //$NON-NLS-1$
		new MultiplikatorEditor(ret,TarmedLeistung.class,"UVG"); //$NON-NLS-1$
		new Label(ret,SWT.NONE).setText(Messages.getString("TarmedPrefs.TPIV")); //$NON-NLS-1$
		new MultiplikatorEditor(ret,TarmedLeistung.class,"IV"); //$NON-NLS-1$
		new Label(ret,SWT.NONE).setText(Messages.getString("TarmedPrefs.TPMV")); //$NON-NLS-1$
		new MultiplikatorEditor(ret,TarmedLeistung.class,"MV"); //$NON-NLS-1$
		return ret;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
	
}
