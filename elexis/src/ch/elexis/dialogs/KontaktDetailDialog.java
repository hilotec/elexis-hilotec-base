package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.data.*;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;

/**
 * Edit Kontakt details. Can be called either with an existing Kontakt or with a String[2] that define
 * a contact (may all be null)
 * @author gerry
 *
 */
public class KontaktDetailDialog extends TitleAreaDialog {
	Kontakt k;
	LabeledInputField liName,liVorname,liGebDat,liSex,liStrasse,liPlz, liOrt,liTel,liFax,liMail;
	String[] vals;
	int type=0;
	ButtonAdapter ba=new ButtonAdapter();
	
	public KontaktDetailDialog(Shell parentShell, Kontakt kt) {
		super(parentShell);
		k=kt;
	}

	public KontaktDetailDialog(Shell parentShell, String[] v) {
		super(parentShell);
		vals=v;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(3,true));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		if(k==null){
			Composite cType=new Composite(ret,SWT.BORDER);
			cType.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
			cType.setLayout(new FillLayout());
			Button bPerson=new Button(cType,SWT.RADIO);
			bPerson.setText("Person");
			Button bOrg=new Button(cType,SWT.RADIO);
			bOrg.setText("Organisation");
			bPerson.addSelectionListener(ba);
			bOrg.addSelectionListener(ba);
			liName=SWTHelper.createLabeledField(ret, "Name", LabeledInputField.Typ.TEXT);
			liVorname=SWTHelper.createLabeledField(ret, "Vorname", LabeledInputField.Typ.TEXT);
			liGebDat=SWTHelper.createLabeledField(ret, "Geburtsdatum", LabeledInputField.Typ.TEXT);
			liSex=SWTHelper.createLabeledField(ret, "Geschlecht", LabeledInputField.Typ.TEXT);
			if(vals!=null){
				/*
				liGebDat.setText(vals[2]==null ? "" : vals[2]);
				liSex.setText(vals[3]==null ? "" : vals[3]);
				liStrasse.setText(vals[4]);
				liPlz.setText(vals[5]);
				*/
			liName.setText(vals[0]==null ? "" : vals[0]);
			liVorname.setText(vals[1]==null ? "" : vals[1]);
			}
		}else{
			if(k.istPerson()){
				Person p=Person.load(k.getId());
				liName=SWTHelper.createLabeledField(ret, "Name", LabeledInputField.Typ.TEXT);
				liVorname=SWTHelper.createLabeledField(ret, "Vorname", LabeledInputField.Typ.TEXT);
				liGebDat=SWTHelper.createLabeledField(ret, "Geburtsdatum", LabeledInputField.Typ.TEXT);
				liSex=SWTHelper.createLabeledField(ret, "Geschlecht", LabeledInputField.Typ.TEXT);
				liName.setText(p.getName());
				liVorname.setText(p.getVorname());
				liGebDat.setText(p.getGeburtsdatum());
				liSex.setText(p.getGeschlecht());
			}else{
				liName=SWTHelper.createLabeledField(ret, "Name", LabeledInputField.Typ.TEXT);
				liVorname=SWTHelper.createLabeledField(ret, "Zusatz", LabeledInputField.Typ.TEXT);
				liName.setText(k.get("Bezeichnung1"));
				liVorname.setText(k.get("Bezeichnung2"));
			}
		}
		liStrasse=SWTHelper.createLabeledField(ret, "Strasse", LabeledInputField.Typ.TEXT);
		liPlz=SWTHelper.createLabeledField(ret, "Plz", LabeledInputField.Typ.TEXT);
		liOrt=SWTHelper.createLabeledField(ret, "Ort", LabeledInputField.Typ.TEXT);
		liTel=SWTHelper.createLabeledField(ret, "Tel.", LabeledInputField.Typ.TEXT);
		liFax=SWTHelper.createLabeledField(ret, "Fax", LabeledInputField.Typ.TEXT);
		liMail=SWTHelper.createLabeledField(ret, "E-Mail", LabeledInputField.Typ.TEXT);
		if(k!=null){
			Anschrift an=k.getAnschrift();
			liStrasse.setText(an.getStrasse());
			liPlz.setText(an.getPlz());
			liOrt.setText(an.getOrt());
			liTel.setText(k.get("Telefon1"));
			liFax.setText(k.get("Fax"));
			liMail.setText(k.get("E-Mail"));
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Kontaktdetails anzeigen");
		if(k!=null){
			setTitle(k.getLabel());
		}else{
			setTitle("Neuer Kontakt");
		}
		setMessage("Bitte geben Sie soweit bekannt die korrekten Daten ein");
	}

	@Override
	protected void okPressed() {
		if(k==null){
			if(type==0){
				SWTHelper.showError("Typ des Kontakts", "Bitte geben Sie (mindestens) an, ob es sich um eine Person oder eine Organisation handelt");
				return;
			}else if(type==1){
				k=new Person(liName.getText(),liVorname.getText(),liGebDat.getText(),liSex.getText());
			}else{
				k=new Organisation(liName.getText(),liVorname.getText());
			}
		}else{
			if(k.istPerson()){
				Person p=Person.load(k.getId());
				p.set("Name",liName.getText());
				p.set("Vorname", liVorname.getText());
				p.set("Geburtsdatum", liGebDat.getText());
				p.set("Geschlecht", liSex.getText());
			}else{
				Organisation o=Organisation.load(k.getId());
				o.set("Name", liName.getText());
				o.set("Zusatz",liVorname.getText());
			}
		}
		Anschrift an=k.getAnschrift();
		an.setStrasse(liStrasse.getText());
		an.setPlz(liPlz.getText());
		an.setOrt(liOrt.getText());
		k.setAnschrift(an);
		k.set("Telefon1", liTel.getText());
		k.set("E-Mail", liMail.getText());
		super.okPressed();
	}
	class ButtonAdapter extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(((Button)e.getSource()).getText().equals("Person")){
				type=1;
				liGebDat.setEnabled(true);
				liSex.setEnabled(true);
				liVorname.setLabel("Vorname");
			}else{
				type=2;
				liGebDat.setEnabled(false);
				liSex.setEnabled(false);
				liVorname.setLabel("Zusatz");
			}
		}
		
	}
	
}
