/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ErsterMandantDialog.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Person;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class ErsterMandantDialog extends TitleAreaDialog {
	Text tUsername,tPwd1,tPwd2,tTitle,tFirstname,tLastname,tEmail,tStreet,tZip,tPlace,tPhone, tFax;
	String[] anreden={"Herr","Frau","Firma"};
	Combo cbAnrede;
	
	public ErsterMandantDialog(Shell parent){
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite rx=(Composite)super.createDialogArea(parent);
		Composite ret=new Composite(rx,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2,false));
		new Label(ret,SWT.NONE).setText("Username");
		tUsername=new Text(ret,SWT.BORDER);
		tUsername.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Passwort");
		tPwd1=new Text(ret,SWT.BORDER|SWT.PASSWORD);
		tPwd1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Passwort wdh.");
		tPwd2=new Text(ret,SWT.BORDER|SWT.PASSWORD);
		tPwd2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Anrede");
		cbAnrede=new Combo(ret,SWT.SIMPLE|SWT.SINGLE);
		cbAnrede.setItems(anreden);

		new Label(ret,SWT.NONE).setText("Titel");
		tTitle=new Text(ret,SWT.BORDER);
		tTitle.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Vorname");
		tFirstname=new Text(ret,SWT.BORDER);
		tFirstname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Name");
		tLastname=new Text(ret,SWT.BORDER);
		tLastname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("E-Mail");
		tEmail=new Text(ret,SWT.BORDER);
		tEmail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Strasse");
		tStreet=new Text(ret,SWT.BORDER);
		tStreet.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Plz");
		tZip=new Text(ret,SWT.BORDER);
		tZip.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Ort");
		tPlace=new Text(ret,SWT.BORDER);
		tPlace.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Telefon");
		tPhone=new Text(ret,SWT.BORDER);
		tPhone.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Telefax");
		tFax=new Text(ret,SWT.BORDER);
		tFax.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return rx;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Ersten Mandanten erstellen");
		setMessage("Bitte geben Sie die Daten für den Hauptmandanten/Praxisbesitzer ein.\nWenn Sie jetzt abbrechen, können Sie später den Mandanten manuell erstellen.");
		setTitleImage(Desk.getImage(Desk.IMG_LOGO48));
	}

	@Override
	protected void okPressed() {
		String pwd=tPwd1.getText();
		if(!pwd.equals(tPwd2.getText())){
			SWTHelper.showError("Passwortfehler", "Die beiden Passwörter sind nicht identisch");
			return;
		}
		String email=tEmail.getText();
		if(StringTool.isMailAddress(email)){
			SWTHelper.showError("E-Mail ungültig", "Es muss eine gültige E-Mail-Adresse angegeben werden");
			return;
		}
		String username=tUsername.getText();
		if(username.equals("")){
			SWTHelper.showError("Kein username angebenen", "Es muss ein username angegeben werden");
			return;
		}
		Mandant m=new Mandant(username,pwd);
		String g=Person.MALE;
		if(cbAnrede.getText().startsWith("F")){
			g=Person.FEMALE;
		}
		m.set(new String[]{Person.NAME,Person.FIRSTNAME,"Titel",Person.SEX,
				"E-Mail",Person.PHONE1,"Fax",Kontakt.STREET,Kontakt.ZIP,Kontakt.PLACE}, 
				tLastname.getText(),tFirstname.getText(), tTitle.getText(),g,
				email,tPhone.getText(),tFax.getText(),tStreet.getText(),tZip.getText(),
				tStreet.getText());
		String gprs=m.getInfoString("groups");
		gprs="Admin,Anwender";
		m.setInfoElement("Groups", gprs);
		super.okPressed();
	}
	
}
