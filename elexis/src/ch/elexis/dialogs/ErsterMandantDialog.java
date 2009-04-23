package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.data.Mandant;
import ch.elexis.data.Person;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class ErsterMandantDialog extends TitleAreaDialog {
	Text tUsername,tPwd1,tPwd2,tTitle,tFirstname,tLastname,tEmail,tStreet,tZip,tPlace,tPhone, tFax;
	
	
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
		new Label(ret,SWT.NONE).setText("Passwort wdh.");
		tPwd2=new Text(ret,SWT.BORDER|SWT.PASSWORD);
		new Label(ret,SWT.NONE).setText("Titel");
		tTitle=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Vorname");
		tFirstname=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Name");
		tLastname=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("E-Mail");
		tEmail=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Strasse");
		tStreet=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Plz");
		tZip=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Ort");
		tPlace=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Telefon");
		tPhone=new Text(ret,SWT.BORDER);
		new Label(ret,SWT.NONE).setText("Telefax");
		tFax=new Text(ret,SWT.BORDER);
		return rx;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Ersten Mandanten erstellen");
		setMessage("Bitte geben Sie die Daten für den Hauptmandanten/Praxisbesitzer ein.\nWenn Sie abbrechen können Sie später den Mandanten manuell erstellen.");
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
		m.set(new String[]{"Name","Vorname","Titel","Geschlecht","E-Mail","Telefon1","Fax","Strasse","Plz","Ort"}, 
				tLastname.getText(),tFirstname.getText(), tTitle.getText(),Person.MALE,
				email,tPhone.getText(),tFax.getText(),tStreet.getText(),tZip.getText(),
				tStreet.getText());
		super.okPressed();
	}
	
}
