/*******************************************************************************
 * Copyright (c) 2005-2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;

public class AgendaDruck extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	Combo cTerminTemplate;
	Text tTerminPrinter;
	Button bTerminPrinterButton;
	Text tTerminTray;
	
	Composite cPrinterArea;
	
	Button bDirectPrint;
	
	PrinterSelector psel;
	 
    public AgendaDruck() {
        setDescription("Einstellungen für Agenda-Ausdruck");
    }

    @Override
	protected Control createContents(Composite parent) {
		psel=new PrinterSelector();
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(3,false));
		new Label(ret, SWT.NONE).setText("Systemvorlage für Terminkarten");
		cTerminTemplate = new Combo(ret, SWT.READ_ONLY);
		cTerminTemplate.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		bDirectPrint = new Button(ret, SWT.CHECK);
		bDirectPrint.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		bDirectPrint.setText("Direkt drucken, ohne Bestätigung");
		bDirectPrint.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				refreshDirectPrint();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		cPrinterArea = new Composite(ret, SWT.NONE);
		cPrinterArea.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		cPrinterArea.setLayout(new GridLayout(3, false));
		
		new Label(cPrinterArea,SWT.NONE).setText("Drucker für Terminkarten");
		tTerminPrinter=new Text(cPrinterArea,SWT.BORDER|SWT.READ_ONLY);
		tTerminPrinter.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tTerminPrinter.setData("TerminPrinter");
		bTerminPrinterButton=new Button(cPrinterArea,SWT.PUSH);
		bTerminPrinterButton.setText(" ->");
		bTerminPrinterButton.setData(tTerminPrinter);
		bTerminPrinterButton.addSelectionListener(psel);
		
		new Label(cPrinterArea,SWT.NONE).setText("Schacht für Terminkarten");
		tTerminTray=new Text(cPrinterArea,SWT.BORDER);
		tTerminTray.setLayoutData(SWTHelper.getFillGridData(2,true,1,false));
		
		setInitialValues();
		
		return ret;
	}
    
    private void refreshDirectPrint() {
    	boolean directPrint = bDirectPrint.getSelection();

    	if (directPrint) {
    		cPrinterArea.setVisible(true);
    	} else {
    		cPrinterArea.setVisible(false);
    	}
    }
    
    /* fill combo box with available templates */
    private void setTemplates() {
    	cTerminTemplate.removeAll();
    	
    	String currentTemplate = Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE,
    			PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE_DEFAULT);

    	Brief[] templates = getSystemTemplates();
    	for (int i = 0; i < templates.length; i++) {
    		Brief brief = templates[i];
    		String name = brief.getBetreff();
    		cTerminTemplate.add(name);
    	}
    	
    	cTerminTemplate.setText(currentTemplate);
    }
    
    private void setInitialValues() {
        setTemplates();
        
        tTerminPrinter.setText(Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_NAME, ""));
        tTerminTray.setText(Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_TRAY, ""));
        
        boolean directPrint = Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT,
        		PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT);
        bDirectPrint.setSelection(directPrint);
        refreshDirectPrint();
    }
    
    @Override
	public boolean performOk() {
    	Hub.localCfg.set(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE, cTerminTemplate.getText());
    	Hub.localCfg.set(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_NAME, tTerminPrinter.getText());
		Hub.localCfg.set(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_TRAY, tTerminTray.getText());
		Hub.localCfg.set(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT, bDirectPrint.getSelection());
		
		Hub.localCfg.flush();
		
    	return super.performOk();
	}

	public void init(IWorkbench workbench) {
        // nothing to do
    }
	
	private Brief[] getSystemTemplates() {
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add("Typ","=", Brief.TEMPLATE);
		qbe.add("BehandlungsID", "=", "SYS");
		qbe.startGroup();
		qbe.add("DestID", "=", Hub.actMandant.getId());
		qbe.or();
		qbe.add("DestID", "=", "");
		qbe.endGroup();
		qbe.and();
		qbe.add("geloescht", "<>", "1");

		qbe.orderBy(false, "Datum");
		List<Brief> l = qbe.execute();
		if (l != null) {
			return l.toArray(new Brief[0]);
		} else {
			return new Brief[0];
		}
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


}
