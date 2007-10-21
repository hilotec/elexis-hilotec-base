/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: RnOutputDialog.java 3280 2007-10-21 15:12:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Rechnung;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Extensions;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;

public class RnOutputDialog extends TitleAreaDialog {
	private Collection<Rechnung> rnn;
	private List<IRnOutputter> lo;
	private Combo cbLo;
	private Button bCopy;
	private List<Control> ctls=new ArrayList<Control>();
	private StackLayout stack=new StackLayout();
	public RnOutputDialog(Shell shell, Collection<Rechnung> rnn) {
		super(shell);
		this.rnn=rnn;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		lo=Extensions.getClasses("ch.elexis.RechnungsManager", "outputter");
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cbLo=new Combo(ret,SWT.SINGLE|SWT.READ_ONLY);
		cbLo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bCopy=new Button(ret,SWT.CHECK);
		bCopy.setText("Als Kopie markieren");
		bCopy.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		final Composite bottom=new Composite(ret,SWT.NONE);
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottom.setLayout(stack);
		for(IRnOutputter ro:lo){
			cbLo.add(ro.getDescription());
			ctls.add(ro.createSettingsControl(bottom));
		}
		cbLo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx=cbLo.getSelectionIndex();
				if(idx!=-1){
					stack.topControl=ctls.get(idx);
					bottom.layout();
					Hub.localCfg.set(PreferenceConstants.RNN_DEFAULTEXPORTMODE, idx);
				}
			}
			
		});
		int lastSelected=Hub.localCfg.get(PreferenceConstants.RNN_DEFAULTEXPORTMODE, 0);
		if((lastSelected<0) || (lastSelected>=cbLo.getItemCount())){
			lastSelected=0;
			Hub.localCfg.set(PreferenceConstants.RNN_DEFAULTEXPORTMODE, 0);
		}
		cbLo.select(lastSelected);
		stack.topControl=ctls.get(cbLo.getSelectionIndex());
		bottom.layout();
		return ret;
	}

	@Override
	public void create() {
		super.create();
		int num=rnn.size();
		if(num>1){
			getShell().setText("Rechnungen ausgeben");
			setTitle(num+" Rechnungen ausgeben");
			setMessage("Wählen Sie bitte das Ausgabeziel für diese "+num+" Rechnungen aus.");

		}else{
			getShell().setText("Rechnung ausgeben");
			setTitle("Rechnung ausgeben");
			setMessage("Wählen Sie bitte das Ausgabeziel für diese Rechnung aus");
		}
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}

	@Override
	protected void okPressed() {
		int idx=cbLo.getSelectionIndex();
		if(idx!=-1){
			IRnOutputter rop=lo.get(idx);
			Result<Rechnung> result=rop.doOutput(bCopy.getSelection() ? IRnOutputter.TYPE.COPY : IRnOutputter.TYPE.ORIG, rnn);
		}
		super.okPressed();
	}
	
}
