/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Sgam.informatics
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Transporter.java 1143 2006-10-21 19:06:51Z rgw_ch $
 *******************************************************************************/

package ch.sgam.informatics.exchange.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.Desk;
import ch.elexis.util.SWTHelper;

public class SendMailDialog extends TitleAreaDialog {
	String subject,text;
	Text tSubject, tMessage;
	SendMailDialog(Shell shell){
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret,SWT.NONE).setText(Messages.SendMailDialog_msgTitlle);
		tSubject=new Text(ret,SWT.BORDER|SWT.SINGLE);
		tSubject.setText(Messages.SendMailDialog_medicalData);
		tSubject.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.SendMailDialog_msgBody);
		tMessage=new Text(ret,SWT.BORDER|SWT.MULTI);
		tMessage.setText(Messages.SendMailDialog_dearCollegue);
		tMessage.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("E-Mail"); //$NON-NLS-1$
		setTitle(Messages.SendMailDialog_sendMail);
		setMessage(Messages.SendMailDialog_enterMail);
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}

	@Override
	protected void okPressed() {
		subject=tSubject.getText();
		text=tMessage.getText();
		super.okPressed();
	}
	
	
}
