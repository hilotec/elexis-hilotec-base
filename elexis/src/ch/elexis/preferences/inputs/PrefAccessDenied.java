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
 *  $Id: PrefAccessDenied.java 1849 2007-02-19 07:52:43Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences.inputs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.util.SWTHelper;

public class PrefAccessDenied extends Composite {
	public PrefAccessDenied(Composite parent){
		super(parent,SWT.NONE);
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		setLayout(new FillLayout());
		new Label(this,SWT.WRAP).setText("Diese Einstellungsseite ist gesperrt");
	}
}
