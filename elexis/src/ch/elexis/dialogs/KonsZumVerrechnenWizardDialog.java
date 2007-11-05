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
 *  $Id$
 *******************************************************************************/
package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.util.Money;
import ch.elexis.util.MoneyInput;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class KonsZumVerrechnenWizardDialog extends TitleAreaDialog {
	Button cbBefore, cbAmount, cbTime, cbQuartal;
	DatePickerCombo dp1,dp2;
	MoneyInput mi1;
	
	public TimeTool ttFirstBefore, ttLastBefore;
	public Money mAmount;
	public boolean bQuartal;
	
	public KonsZumVerrechnenWizardDialog(final Shell parentShell) {
		super(parentShell);
		
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2,false));
		cbBefore=new Button(ret,SWT.CHECK);
		cbBefore.setText("Alle Behandlungsserien verrechnen, welche angefangen haben vor:");
		dp1=new DatePickerCombo(ret,SWT.NONE);
		dp1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		((GridData)dp1.getLayoutData()).widthHint=50;
		cbTime=new Button(ret,SWT.CHECK);
		cbTime.setText("Alle Behandlungsserien verrechnen, die geendet haben vor:");
		dp2=new DatePickerCombo(ret,SWT.NONE);
		dp2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		cbAmount=new Button(ret,SWT.CHECK);
		cbAmount.setText("Alle Behandlungsserien verrechnen, deren Betrag höher ist als:");
		mi1=new MoneyInput(ret);
		mi1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		cbQuartal=new Button(ret,SWT.CHECK);
		cbQuartal.setText("Alle Behandlungen des vergangenen Quartals verrechnen");
		new Label(ret,SWT.NONE);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Rechnungs-Automatik");
		setMessage("Behandlungen zum Verrechnen automatisch auswählen");
		getShell().setText("Rechnungen erstellen");
	}

	@Override
	protected void okPressed() {
		if(cbBefore.getSelection()){
			ttFirstBefore=new TimeTool(dp1.getDate().getTime());
		}
		if(cbTime.getSelection()){
			ttLastBefore=new TimeTool(dp2.getDate().getTime());
		}
		if(cbAmount.getSelection()){
			mAmount=mi1.getMoney(false);
		}
		bQuartal=cbQuartal.getSelection();
		super.okPressed();
	}
	

}
