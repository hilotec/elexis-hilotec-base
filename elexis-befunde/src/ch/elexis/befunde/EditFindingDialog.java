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
 *    $Id: EditFindingDialog.java 1405 2006-12-10 17:48:59Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde;

import java.util.Date;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Patient;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class EditFindingDialog extends TitleAreaDialog {
	Messwert mw;
	String name;
	DatePickerCombo dp;
	Hashtable names;
	String[] flds;
	boolean[] multiline;
	String[] values;
	Text[] inputs;
	
	EditFindingDialog(Shell parent, Messwert m, String n){
		super(parent);
		mw=m;
		name=n;
		names=Messwert.getSetup().getHashtable("Befunde");
		flds=((String)names.get(n+"_FIELDS")).split(Messwert.SETUP_SEPARATOR);
		multiline=new boolean[flds.length];
		values=new String[flds.length];
		inputs=new Text[flds.length];
		for(int i=0;i<flds.length;i++){
			String[] line=flds[i].split(Messwert.SETUP_CHECKSEPARATOR);
			flds[i]=line[0];
			multiline[i]=line[1].equals("m") ? true : false;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		Patient pat=GlobalEvents.getSelectedPatient();
		if(pat!=null){
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			dp=new DatePickerCombo(ret,SWT.NONE);
			dp.setDate(new Date());
			if(mw!=null){
				Hashtable vals=mw.getHashtable("Befunde");
				for(int i=0;i<flds.length;i++){
					values[i]=(String)vals.get(flds[i]);
				}
			}
			for(int i=0;i<flds.length;i++){
				new Label(ret,SWT.NONE).setText(flds[i]);
				inputs[i]=SWTHelper.createText(ret, multiline[i] ? 4 : 1 , SWT.NONE);
				inputs[i].setText(values[i]==null ? "" : values[i]);
			}
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Befund");
		setTitle(GlobalEvents.getSelectedPatient().getLabel());
		setMessage("Geben Sie bitte den Text für "+name+" ein");
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		Hashtable hash;
		if(mw==null){
			hash=new Hashtable();
			mw=new Messwert(GlobalEvents.getSelectedPatient(),name,dp.getText(),hash);
		}else{
			hash=mw.getHashtable("Befunde");
		}
		for(int i=0;i<flds.length;i++){
			String val=inputs[i].getText();
			if(StringTool.isNothing(val)){
				hash.remove(flds[i]);
			}else{
				hash.put(flds[i], val);
			}
			mw.setHashtable("Befunde", hash);
		}
		super.okPressed();
	}
	
}
