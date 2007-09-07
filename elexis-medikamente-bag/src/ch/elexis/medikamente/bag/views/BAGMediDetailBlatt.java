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
 * $Id: BAGMediDetailBlatt.java 3107 2007-09-07 11:03:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.InputData;

public class BAGMediDetailBlatt extends Composite {
	LabeledInputField.AutoForm fld;
	Text fullName;
	Text tLagerung;
	Text tUnit;
	Text tIndikation, tRules, tRemarks;
	Group gRsigns, gSsigns;
	Button[] bRsigns, bSsigns;
	Composite texte;
	Composite parent;
	ScrolledForm form;
	InputData[] fields=new InputData[]{
			new InputData("Hersteller"),
			new InputData("Generika"),
			new InputData("Pharmacode"),
			new InputData("BAG-Dossier"),
			new InputData("Swissmedic-Nr"),
			new InputData("Swissmedic-Liste")
	};

	
	public BAGMediDetailBlatt(final Composite pr){
		super(pr,SWT.NONE);
		parent=pr;
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form=Desk.theToolkit.createScrolledForm(this);
		Composite ret=form.getBody();
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		fullName=SWTHelper.createText(Desk.theToolkit, ret, 3, SWT.BORDER|SWT.READ_ONLY|SWT.WRAP);
		fullName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		fld=new LabeledInputField.AutoForm(ret,fields);
		fld.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		fld.setEnabled(false);
		Desk.theToolkit.adapt(fld);

		texte=Desk.theToolkit.createComposite(ret);
		texte.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		texte.setLayout(new GridLayout());
		
	}
	public void display(final BAGMedi m){
		form.setText(m.getLabel());
	}
}
