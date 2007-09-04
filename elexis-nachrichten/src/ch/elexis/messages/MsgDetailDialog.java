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
 * $Id: MsgDetailDialog.java 3092 2007-09-04 05:06:50Z rgw_ch $
 *******************************************************************************/

package ch.elexis.messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class MsgDetailDialog extends Dialog {

	Label lbFrom;
	Combo cbTo;
	Text text;
	Message msg;
	Anwender[] users;
	Button bOK, bRecall,bAsReminder,bAnswer, bCancel;
	
	MsgDetailDialog(final Shell shell, final Message msg){
		super(shell);
		this.msg=msg;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2,false));
		new Label(ret,SWT.NONE).setText("Nachricht vom ");
		new Label(ret,SWT.NONE).setText(msg==null ? 
				new TimeTool().toString(TimeTool.FULL_GER) :
				new TimeTool(msg.get("time")).toString(TimeTool.FULL_GER));
		new Label(ret,SWT.NONE).setText("Von:");
		lbFrom=new Label(ret,SWT.NONE);
		
		new Label(ret,SWT.NONE).setText("An: ");
		cbTo=new Combo(ret,SWT.SINGLE|SWT.READ_ONLY);
		text=SWTHelper.createText(ret, 4, SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		if(msg==null){
			users=Hub.getUserList().toArray(new Anwender[0]);
			for(Anwender a:users){
				cbTo.add(a.getLabel());
			}
			lbFrom.setText(Hub.actMandant.getLabel());
		}else{
			lbFrom.setText(msg.getSender().getLabel());
			cbTo.add(msg.getDest().getLabel());
			cbTo.select(0);
			cbTo.setEnabled(false);
			text.setText(msg.get("Text"));
		}
		
		return ret;
	}

	@Override
	public void create() {
		super.create();
		if(msg==null){
			getShell().setText("Nachricht erstellen");
		}else{
			getShell().setText("Nachricht lesen");
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		String sOK;
		if(msg==null){
			sOK="Senden";
		}else{
			sOK="Löschen";
		}
		bOK=createButton(parent, IDialogConstants.OK_ID, sOK, false);
		bRecall=createButton(parent, IDialogConstants.CLIENT_ID, "Merken",false);
		bAnswer=createButton(parent, IDialogConstants.CLIENT_ID+1, "Anworten",false);
		bAsReminder=createButton(parent, IDialogConstants.CLIENT_ID+2,"Als Reminder",false);
		bCancel=createButton(parent, IDialogConstants.CANCEL_ID,"Abbruch", false);
	}

	@Override
	protected void okPressed() {
		if(msg==null){
			int idx=cbTo.getSelectionIndex();
			if(idx!=-1){
				msg=new Message(users[idx],text.getText());
			}
		}
		super.okPressed();
	}
	
}
