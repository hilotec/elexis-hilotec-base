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
 * $Id: RnFilterDialog.java 3319 2007-11-06 18:44:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.PersistentObject;
import ch.elexis.util.DateInput;
import ch.elexis.util.Money;
import ch.elexis.util.MoneyInput;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class RnFilterDialog extends TitleAreaDialog {
	static final String FROM="von";
	static final String UNTIL="bis";
	String[] ret;
	MoneyInput miVon,miBis;
	DateInput diRnVon,diRnBis,diStatVon,diStatBis;
	public RnFilterDialog(final Shell parentShell){
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(3,false));
		new Label(ret,SWT.NONE).setText("Betrag");
		miVon=new MoneyInput(ret,FROM); 
		miBis=new MoneyInput(ret,UNTIL);
		new Label(ret,SWT.NONE).setText("Rechnungsdatum");
		diRnVon=new DateInput(ret,FROM);
		diRnBis=new DateInput(ret,UNTIL);
		new Label(ret,SWT.NONE).setText("Satusdatum");
		diStatVon=new DateInput(ret,FROM);
		diStatBis=new DateInput(ret,UNTIL);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Rechnungsliste-Filter");
		setMessage("Bitte geben Sie die Bedingungenn für die Rechnungsanzeige ein");
		getShell().setText("Rechnungsliste");
	}

	@Override
	protected void okPressed() {
		ArrayList<String> al=new ArrayList<String>();
		Money mFrom=miVon.getMoney(true);
		Money mUntil=miBis.getMoney(true);
		
		if(mFrom!=null){
			//String sFrom=StringTool.pad(SWT.LEFT, '0', mFrom.getCentsAsString(), 9);
			al.add(PersistentObject.getConnection().translateFlavor("cast(Betrag as SIGNED) >="+mFrom.getCentsAsString()));
		}
		if(mUntil!=null){
			//String sUntil=StringTool.pad(SWT.LEFT, '0', mUntil.getCentsAsString(), 9);
			al.add(PersistentObject.getConnection().translateFlavor("cast(Betrag as SIGNED) <="+mUntil.getCentsAsString()));
		}
		TimeTool tt=diRnVon.getDate();
		if(tt!=null){
			al.add("RnDatum >="+tt.toString(TimeTool.DATE_COMPACT));
		}
		tt=diRnBis.getDate();
		if(tt!=null){
			al.add("RnDatum <="+tt.toString(TimeTool.DATE_COMPACT));
		}
		tt=diStatVon.getDate();
		if(tt!=null){
			al.add("StatusDatum >="+tt.toString(TimeTool.DATE_COMPACT));
		}
		tt=diStatBis.getDate();
		if(tt!=null){
			al.add("StatusDatum <="+tt.toString(TimeTool.DATE_COMPACT));
		}
		if(al.size()>0){
			ret=al.toArray(new String[0]);
		}else{
			ret=null;
		}
		
		super.okPressed();
	}
	
}
