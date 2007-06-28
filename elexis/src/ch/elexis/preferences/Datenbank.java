/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Datenbank.java 2650 2007-06-28 19:45:43Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.util.DatabaseCleaner;
import ch.elexis.util.SWTHelper;
import ch.rgw.IO.Settings;
import ch.rgw.tools.ExHandler;

/**
 * Datenbankspezifische Einstellungen. Datenbanktyp, Connect-String, Jdbc-Klasse usw.
 */
public class Datenbank extends PreferencePage implements IWorkbenchPreferencePage {

	Button bKons, bRn, bRepair;
	Label lOutputFile;
	Button bOutputFile, bCheck;
	Settings cfg;
	public Datenbank() {

		noDefaultAndApplyButton();
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
		cfg=Hub.localCfg;
		setDescription("Angaben zur Datenbankverbindung");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(2,false));
		new Label(ret,SWT.NONE).setText("Datenbankverbindung:");
		new Text(ret,SWT.READ_ONLY).setText(cfg.get(PreferenceConstants.DB_CLASS,""));
		new Label(ret,SWT.NONE).setText("Verbindungsstring:");
		new Text(ret,SWT.READ_ONLY).setText(cfg.get(PreferenceConstants.DB_CONNECT,""));
		new Label(ret,SWT.NONE).setText("Username für Datenbank:");
		new Text(ret,SWT.READ_ONLY).setText(cfg.get(PreferenceConstants.DB_USERNAME,""));
		new Label(ret,SWT.NONE).setText("Passwort für Datenbank:");
		new Text(ret,SWT.READ_ONLY).setText(cfg.get(PreferenceConstants.DB_PWD,""));
		new Label(ret,SWT.NONE).setText("Datenbanktyp");
		new Text(ret,SWT.READ_ONLY).setText(cfg.get(PreferenceConstants.DB_TYP,""));
		
		new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		if(false){ // TODO 
		new Label(ret,SWT.NONE).setText("Datenbankreorganisation");
		bRepair=new Button(ret,SWT.CHECK);
		bRepair.setText("Fehler gleich reparieren");
		bOutputFile=new Button(ret,SWT.PUSH);
		bOutputFile.setText("Analyselog nach...");
		bOutputFile.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd=new FileDialog(ret.getShell(),SWT.SAVE);
				String f=fd.open();
				if(f!=null){
					lOutputFile.setText(f);
				}
			}});
		lOutputFile=new Label(ret,SWT.NONE);
		lOutputFile.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bKons=new Button(ret,SWT.CHECK);
		bKons.setText("Konsultationen prüfen");
		bRn=new Button(ret,SWT.CHECK);
		bRn.setText("Rechnungen prüfen");
		bCheck=new Button(ret,SWT.PUSH);
		bCheck.setText("Prüfen!");
		bCheck.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				try{
					File fo=new File(lOutputFile.getText());
					fo.createNewFile();
					FileOutputStream fos=new FileOutputStream(fo);
					DatabaseCleaner dc=new DatabaseCleaner(fos,bRepair.getSelection());
					if(bKons.getSelection()){
						dc.checkKonsultationen();
					}
					if(bRn.getSelection()){
						dc.checkRechnungen();
					}
					fos.close();
				}catch(Exception ex){
					ExHandler.handle(ex);
					MessageDialog.openError(getShell(), "Fehler beim Log erstellen", "Konnter Logdatei nicht erstellen");
				}
			}});
		} // false
		return ret;
	}
    

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {/* leer */	}

	

	
}