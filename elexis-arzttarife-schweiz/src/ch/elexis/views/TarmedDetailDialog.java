/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: TarmedDetailDialog.java 4401 2008-09-08 20:27:47Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.TarmedLeistung;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;

public class TarmedDetailDialog extends Dialog {
	Verrechnet v;
	TarmedDetailDisplay td;
	Combo cSide;
	Button bPflicht;
	
	public TarmedDetailDialog(Shell shell, Verrechnet tl){
		super(shell);
		v=tl;
		td=new TarmedDetailDisplay();
		
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		//Composite ret=td.createDisplay(parent, null);
		//td.display(tl);
		TarmedLeistung tl=(TarmedLeistung)v.getVerrechenbar();
		Composite ret=(Composite)super.createDialogArea(parent);
		ret.setLayout(new GridLayout(6,false));
		
		double tpAL=tl.getAL()/100.0;
		double tpTL=tl.getTL()/100.0;
		double tpw=v.getTPW();
		Money mAL=new Money(tpAL*tpw);
		Money mTL=new Money(tpTL*tpw);
		double tpAll=Math.round((tpAL+tpTL)*100.0)/100.0;
		Money mAll=new Money(tpAll*tpw);
		
		new Label(ret,SWT.NONE).setText("TP AL");
		new Label(ret,SWT.NONE).setText(Double.toString(tpAL));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF AL");
		new Label(ret,SWT.NONE).setText(mAL.getAmountAsString());
		
		new Label(ret,SWT.NONE).setText("TP TL");
		new Label(ret,SWT.NONE).setText(Double.toString(tpTL));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF TL");
		new Label(ret,SWT.NONE).setText(mTL.getAmountAsString());
		
		new Label(ret,SWT.NONE).setText("TP Total");
		new Label(ret,SWT.NONE).setText(Double.toString(tpAll));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF Total");
		new Label(ret,SWT.NONE).setText(mAll.getAmountAsString());
		
		String mins=Integer.toString(tl.getMinutes());
		new Label(ret,SWT.NONE).setText("Zeit:");
		new Label(ret,SWT.NONE).setText(mins+" min.");
		
		new Label(ret,SWT.NONE).setText("Seite");
		cSide=new Combo(ret,SWT.SINGLE);
		cSide.setItems(new String[]{"egal","links","rechts"});
		
		new Label(ret,SWT.NONE).setText("Pflichtleist.");
		bPflicht=new Button(ret,SWT.CHECK);
		String sPflicht=v.getDetail(TarmedLeistung.PFLICHTLEISTUNG);
		if((sPflicht==null) || (Boolean.parseBoolean(sPflicht))){
			bPflicht.setSelection(true);
		}
		String side=v.getDetail(TarmedLeistung.SIDE);
		if(side==null){
			cSide.select(0);
		}else if(side.equalsIgnoreCase("l")){
			cSide.select(1);
		}else{
			cSide.select(2);
		}
		return ret;
	}
	@Override
	public void create(){
		super.create();
		getShell().setText("Tarmed-Details");
	}
	@Override
	protected void okPressed(){
		int idx=cSide.getSelectionIndex();
		if(idx<1){
			v.setDetail(TarmedLeistung.SIDE, null);
		}else if(idx==1){
			v.setDetail(TarmedLeistung.SIDE, "l");
		}else{
			v.setDetail(TarmedLeistung.SIDE, "r");
		}
		v.setDetail(TarmedLeistung.PFLICHTLEISTUNG, Boolean.toString(bPflicht.getSelection()));
		super.okPressed();
	}

	
	
}

