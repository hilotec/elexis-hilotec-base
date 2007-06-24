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
 * $Id: PrinterPreferencePage.java 2511 2007-06-11 11:45:13Z danlutz $
 *******************************************************************************/

package ch.elexis.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.util.SWTHelper;

public class PrinterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	Text tEtiketten,tEtikettenschacht,tA5,tA5Schacht,tA4ESR,tA4ESRSchacht,tA4,tA4Schacht;
	Text tEinzelblatt;
	Text tEinzelblattSchacht;
	Button bEtiketten;
	Button cEtiketten;
	PrinterSelector psel;
	
	@Override
	protected Control createContents(Composite parent) {
		psel=new PrinterSelector();
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(3,false));
		new Label(ret,SWT.NONE).setText("Drucker mit Etiketten");
		tEtiketten=new Text(ret,SWT.BORDER|SWT.READ_ONLY);
		tEtiketten.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tEtiketten.setData("EtikettenDrucker");
		bEtiketten=new Button(ret,SWT.PUSH);
		bEtiketten.setText(" ->");
		bEtiketten.setData(tEtiketten);
		bEtiketten.addSelectionListener(psel);
		new Label(ret,SWT.NONE).setText("Schacht für Etiketten");
		tEtikettenschacht=new Text(ret,SWT.BORDER);
		tEtikettenschacht.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		new Label(ret,SWT.NONE);
		new Label(ret, SWT.NONE);  // placeholder
		cEtiketten = new Button(ret, SWT.CHECK);
		cEtiketten.setText("Drucker jedes Mal auswählen");
		cEtiketten.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cEtiketten.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setEtikettenSelection();
			}
		});
		
		new Label(ret,SWT.NONE).setText("Drucker mit A4-Papier mit ESR");
		tA4ESR=new Text(ret,SWT.BORDER|SWT.READ_ONLY);
		tA4ESR.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		Button b=new Button(ret,SWT.PUSH);
		b.setData(tA4ESR);
		b.addSelectionListener(psel);
		b.setText("->");
		new Label(ret,SWT.NONE).setText("Schacht mit A4-Papier mit ESR");
		tA4ESRSchacht=new Text(ret,SWT.BORDER);
		tA4ESRSchacht.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		new Label(ret,SWT.NONE);
		
		new Label(ret,SWT.NONE).setText("Drucker mit weissen A4-Papier");
		tA4=new Text(ret,SWT.BORDER|SWT.READ_ONLY);
		tA4.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		b=new Button(ret,SWT.PUSH);
		b.setData(tA4);
		b.addSelectionListener(psel);
		b.setText("->");
		new Label(ret,SWT.NONE).setText("Schacht mit weissem A4-Papier");
		tA4Schacht=new Text(ret,SWT.BORDER);
		tA4Schacht.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		new Label(ret,SWT.NONE);
		
		new Label(ret,SWT.NONE).setText("Drucker mit A5-Papier");
		tA5=new Text(ret,SWT.BORDER|SWT.READ_ONLY);
		tA5.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		b=new Button(ret,SWT.PUSH);
		b.setData(tA5);
		b.addSelectionListener(psel);
		b.setText("->");
		new Label(ret,SWT.NONE).setText("Schacht mit A5-Papier");
		tA5Schacht=new Text(ret,SWT.BORDER);
		tA5Schacht.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		new Label(ret,SWT.NONE);
		
		new Label(ret,SWT.NONE).setText("Drucker mit Einzelblatteinzug");
		tEinzelblatt=new Text(ret,SWT.BORDER|SWT.READ_ONLY);
		tEinzelblatt.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		b=new Button(ret,SWT.PUSH);
		b.setData(tEinzelblatt);
		b.addSelectionListener(psel);
		b.setText("->");
		new Label(ret,SWT.NONE).setText("Schacht für Einzelblatteinzug");
		tEinzelblattSchacht=new Text(ret,SWT.BORDER);
		tEinzelblattSchacht.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		new Label(ret,SWT.NONE);
		
		tEtiketten.setText(Hub.localCfg.get("Drucker/Etiketten/Name",""));
		tEtikettenschacht.setText(Hub.localCfg.get("Drucker/Etiketten/Schacht",""));
		cEtiketten.setSelection(Hub.localCfg.get("Drucker/Etiketten/Choose", false));
		setEtikettenSelection();
		tA4ESR.setText(Hub.localCfg.get("Drucker/A4ESR/Name",""));
		tA4ESRSchacht.setText(Hub.localCfg.get("Drucker/A4ESR/Schacht",""));
		tA4.setText(Hub.localCfg.get("Drucker/A4/Name",""));
		tA4Schacht.setText(Hub.localCfg.get("Drucker/A4/Schacht",""));
		tA5.setText(Hub.localCfg.get("Drucker/A5/Name",""));
		tA5Schacht.setText(Hub.localCfg.get("Drucker/A5/Schacht",""));
		tEinzelblatt.setText(Hub.localCfg.get("Drucker/Einzelblatt/Name",""));
		tEinzelblattSchacht.setText(Hub.localCfg.get("Drucker/Einzelblatt/Schacht",""));
		return ret;
	}
	class PrinterSelector extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			PrintDialog pd=new PrintDialog(getShell());
			PrinterData pdata=pd.open();
			if(pdata!=null){
				Text tx=(Text) ((Button)e.getSource()).getData();
				tx.setText(pdata.name);
				tx.setData(pdata);
			}
		}
		
		
	};
	
	private void setEtikettenSelection() {
		boolean selection = cEtiketten.getSelection();
		
		if (selection) {
			tEtiketten.setText("");
			tEtiketten.setData(null);
			tEtikettenschacht.setText("");
		}
		
		tEtiketten.setEnabled(!selection);
		tEtikettenschacht.setEnabled(!selection);
		bEtiketten.setEnabled(!selection);
	}
	
	public void init(IWorkbench workbench) {
		
	}
	@Override
	protected void performApply() {
		Hub.localCfg.set("Drucker/Etiketten/Name",tEtiketten.getText());
		Hub.localCfg.set("Drucker/Etiketten/Schacht",tEtikettenschacht.getText());
		Object data = tEtiketten.getData();
		if (data instanceof PrinterData) {
			PrinterData pdata = (PrinterData) data;
			Hub.localCfg.set("Drucker/Etiketten/Driver", pdata.driver);
		} else {
			Hub.localCfg.set("Drucker/Etiketten/Driver", "");
		}
		
		Hub.localCfg.set("Drucker/A4ESR/Name",tA4ESR.getText());
		Hub.localCfg.set("Drucker/A4ESR/Schacht",tA4ESRSchacht.getText());
		Hub.localCfg.set("Drucker/A4/Name",tA4.getText());
		Hub.localCfg.set("Drucker/A4/Schacht",tA4Schacht.getText());
		Hub.localCfg.set("Drucker/A5/Name",tA5.getText());
		Hub.localCfg.set("Drucker/A5/Schacht",tA5Schacht.getText());
		Hub.localCfg.set("Drucker/Einzelblatt/Name",tEinzelblatt.getText());
		Hub.localCfg.set("Drucker/Einzelblatt/Schacht",tEinzelblattSchacht.getText());
	}
	
}
