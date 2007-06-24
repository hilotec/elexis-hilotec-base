/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation, adapted from JavaAgenda
 *    
 *  $Id: TerminStatusDialog.java 1251 2006-11-07 22:07:11Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Termin;
import ch.elexis.util.SWTHelper;
import ch.elexis.agenda.Messages;

public class TerminStatusDialog extends TitleAreaDialog {
	Button[] bStatus;
	String[] status;
	Termin termin;
	public TerminStatusDialog(Shell shell){
		super(shell);
		termin=(Termin)GlobalEvents.getInstance().getSelectedObject(Termin.class);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		status=Termin.TerminStatus;
		bStatus=new Button[status.length];
		String orig=termin.getStatus();
		for(int i=0;i<status.length;i++){
			bStatus[i]=new Button(ret,SWT.RADIO);
			bStatus[i].setText(status[i]);
			bStatus[i].setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			if(status[i].equals(orig)){
				bStatus[i].setSelection(true);
			}
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.TerminStatusDialog_terminState); 
		setMessage(Messages.TerminStatusDialog_enterState); 
		setTitle(Messages.TerminStatusDialog_terminState); 
	}

	@Override
	protected void okPressed() {
		for(int i=0;i<status.length;i++){
			if(bStatus[i].getSelection()){
				termin.setStatus(status[i]);
				break;
			}
		}
		GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
		super.okPressed();
	}
	
}
