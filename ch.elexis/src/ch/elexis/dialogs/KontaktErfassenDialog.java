/*******************************************************************************
 * Copyright (c) 2007-2010, medshare and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Imhof - initial implementation
 *    G. Weirich - added Anschrift
 * 
 * $Id: KontaktErfassenDialog.java 6238 2010-03-19 15:50:41Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;
import ch.elexis.data.Mandant;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.TimeTool.TimeFormatException;

public class KontaktErfassenDialog extends TitleAreaDialog {
	private static final int KED_NAME = 0;
	private static final int KED_FIRSTNAME = 1;
	private static final int KED_ADDITIONAL = 7;
	
	private Button bOrganisation, bLabor, bPerson, bPatient, bAnwender, bMandant;
	
	Kontakt newKontakt = null;
	
	String[] fld;
	Text tName, tVorname, tZusatz, tGebDat, tStrasse, tPlz, tOrt, tTel, tFax, tEmail;
	Combo cbSex;
	Label lName, lVorname, lZusatz;
	Hyperlink hlAnschrift;
	
	public KontaktErfassenDialog(final Shell parent, final String[] fields){
		super(parent);
		fld = fields;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite typeComp = new Composite(parent, SWT.NONE);
		typeComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		typeComp.setLayout(new GridLayout(1, false));
		
		Composite cTypes = Desk.getToolkit().createComposite(typeComp, SWT.BORDER);
		bOrganisation =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.organization"), //$NON-NLS-1$
				SWT.CHECK);
		bOrganisation.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bOrganisationChanged(bOrganisation.getSelection());
			}
		});
		bLabor =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.labor"), SWT.CHECK); //$NON-NLS-1$
		bLabor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bLaborChanged(bLabor.getSelection());
			}
		});
		bPerson =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.person"), SWT.CHECK); //$NON-NLS-1$
		bPerson.setSelection(true);
		bPerson.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bPersonChanged(bPerson.getSelection());
			}
		});
		bPatient =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.patient"), SWT.CHECK); //$NON-NLS-1$
		bPatient.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bPatientChanged(bPatient.getSelection());
			}
		});
		if (fld.length > KontaktSelektor.HINT_PATIENT) {
			if (!StringTool.isNothing(fld[KontaktSelektor.HINT_PATIENT])) {
				bPatient.setSelection(true);
			}
		}
		bAnwender =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.user"), SWT.CHECK); //$NON-NLS-1$
		bAnwender.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bAnwenderChanged(bAnwender.getSelection());
			}
		});
		bMandant =
			Desk.getToolkit().createButton(cTypes,
				Messages.getString("KontaktErfassenDialog.mandant"), SWT.CHECK); //$NON-NLS-1$
		bMandant.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				bMandantChanged(bMandant.getSelection());
			}
		});
		// Not everybody may create users and mandators
		if (!Hub.acl.request(AccessControlDefaults.ACL_USERS)) {
			bMandant.setEnabled(false);
			bAnwender.setEnabled(false);
		}
		cTypes.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		cTypes.setLayout(new FillLayout());
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		
		lName = new Label(ret, SWT.NONE);
		lName.setText(Messages.getString("KontaktErfassenDialog.name")); //$NON-NLS-1$
		tName = new Text(ret, SWT.BORDER);
		tName.setText(fld[KontaktSelektor.HINT_NAME]);
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tName.setTextLimit(80);
		
		lVorname = new Label(ret, SWT.NONE);
		lVorname.setText(Messages.getString("KontaktErfassenDialog.firstName")); //$NON-NLS-1$
		tVorname = new Text(ret, SWT.BORDER);
		tVorname
			.setText(fld[KontaktSelektor.HINT_FIRSTNAME] == null ? "" : fld[KontaktSelektor.HINT_FIRSTNAME]); //$NON-NLS-1$
		tVorname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tVorname.setTextLimit(80);
		
		lZusatz = new Label(ret, SWT.NONE);
		lZusatz.setText(Messages.getString("KontaktErfassenDialog.zusatz")); //$NON-NLS-1$
		tZusatz = new Text(ret, SWT.BORDER);
		tZusatz.setText(fld.length > KontaktSelektor.HINT_ADD ? fld[KontaktSelektor.HINT_ADD] : ""); //$NON-NLS-1$
		tZusatz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		new Label(ret, SWT.NONE).setText(Messages.getString("PatientErfassenDialog.sex"));//$NON-NLS-1$
		cbSex = new Combo(ret, SWT.SINGLE);
		cbSex
			.setItems(new String[] {
				Messages.getString("KontaktErfassenDialog.male"), Messages.getString("KontaktErfassenDialog.female")}); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (fld.length <= KontaktSelektor.HINT_SEX || fld[KontaktSelektor.HINT_SEX].length() == 0) {
			if (StringTool.isNothing(fld[KontaktSelektor.HINT_FIRSTNAME])) {
				cbSex.select(0);
			} else {
				cbSex.select(StringTool.isFemale(fld[KontaktSelektor.HINT_FIRSTNAME]) ? 1 : 0);
			}
		} else {
			cbSex.select(fld[KontaktSelektor.HINT_SEX].equals(Person.MALE) ? 0 : 1);
		}
		
		new Label(ret, SWT.NONE).setText(Messages.getString("KontaktErfassenDialog.birthDate")); //$NON-NLS-1$
		tGebDat = new Text(ret, SWT.BORDER);
		tGebDat
			.setText(fld[KontaktSelektor.HINT_BIRTHDATE] == null ? "" : fld[KontaktSelektor.HINT_BIRTHDATE]); //$NON-NLS-1$
		tGebDat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tGebDat.setTextLimit(8);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("PatientErfassenDialog.street")); //$NON-NLS-1$
		tStrasse = new Text(ret, SWT.BORDER);
		tStrasse
			.setText(fld.length > KontaktSelektor.HINT_STREET ? fld[KontaktSelektor.HINT_STREET]
					: ""); //$NON-NLS-1$
		tStrasse.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tStrasse.setTextLimit(80);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("PatientErfassenDialog.zip")); //$NON-NLS-1$
		tPlz = new Text(ret, SWT.BORDER);
		tPlz.setText(fld.length > KontaktSelektor.HINT_ZIP ? fld[KontaktSelektor.HINT_ZIP] : ""); //$NON-NLS-1$
		tPlz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tPlz.setTextLimit(6);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("PatientErfassenDialog.city")); //$NON-NLS-1$
		tOrt = new Text(ret, SWT.BORDER);
		tOrt
			.setText(fld.length > KontaktSelektor.HINT_PLACE ? fld[KontaktSelektor.HINT_PLACE] : ""); //$NON-NLS-1$
		tOrt.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tOrt.setTextLimit(50);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("PatientErfassenDialog.phone")); //$NON-NLS-1$
		tTel = new Text(ret, SWT.BORDER);
		tTel.setText(fld.length > 6 ? fld[6] : ""); //$NON-NLS-1$
		tTel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTel.setTextLimit(30);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("KontaktErfassenDialog.fax")); //$NON-NLS-1$
		tFax = new Text(ret, SWT.BORDER);
		tFax.setText(fld.length > 8 ? fld[8] : ""); //$NON-NLS-1$
		tFax.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tFax.setTextLimit(30);
		
		new Label(ret, SWT.NONE).setText(Messages.getString("KontaktErfassenDialog.email")); //$NON-NLS-1$
		tEmail = new Text(ret, SWT.BORDER);
		tEmail.setText(fld.length > 9 ? fld[9] : ""); //$NON-NLS-1$
		tEmail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tEmail.setTextLimit(80);
		new Label(ret, SWT.NONE).setText(Messages.getString("KontaktErfassenDialog.postanschrift")); //$NON-NLS-1$
		hlAnschrift =
			Desk.getToolkit().createHyperlink(ret,
				Messages.getString("KontaktErfassenDialog.postalempty"), SWT.NONE); //$NON-NLS-1$
		hlAnschrift.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				createKontakt();
				AnschriftEingabeDialog aed = new AnschriftEingabeDialog(getShell(), newKontakt);
				aed.create();
				SWTHelper.center(getShell(), aed.getShell());
				aed.open();
				hlAnschrift.setText(newKontakt.getPostAnschrift(false));
			}
			
		});
		hlAnschrift.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setMessage(Messages.getString("KontaktErfassenDialog.message")); //$NON-NLS-1$
		setTitle(Messages.getString("KontaktErfassenDialog.subTitle")); //$NON-NLS-1$
		getShell().setText(Messages.getString("KontaktErfassenDialog.title")); //$NON-NLS-1$
		setTitleImage(Desk.getImage(Desk.IMG_LOGO48));
	}
	
	protected void bOrganisationChanged(boolean isSelected){
		bOrganisation.setSelection(isSelected);
		if (isSelected) {
			bPersonChanged(false);
			lName.setText(Messages.getString("KontaktErfassenDialog.bezeichnung"));//$NON-NLS-1$
			lVorname.setText(Messages.getString("KontaktErfassenDialog.zusatz")); //$NON-NLS-1$
			lZusatz.setText(Messages.getString("KontaktErfassenDialog.ansprechperson")); //$NON-NLS-1$
			cbSex.setEnabled(false);
			lName.getParent().layout();
		} else {
			bLaborChanged(false);
		}
	}
	
	protected void bLaborChanged(boolean isSelected){
		bLabor.setSelection(isSelected);
		if (isSelected) {
			bOrganisationChanged(true);
			lZusatz.setText(Messages.getString("KontaktErfassenDialog.laborleiter")); //$NON-NLS-1$
			lName.getParent().layout();
		}
	}
	
	protected void bPersonChanged(boolean isSelected){
		bPerson.setSelection(isSelected);
		if (isSelected) {
			bOrganisationChanged(false);
			lName.setText(Messages.getString("KontaktErfassenDialog.name"));//$NON-NLS-1$
			lVorname.setText(Messages.getString("KontaktErfassenDialog.firstName")); //$NON-NLS-1$
			lZusatz.setText(Messages.getString("KontaktErfassenDialog.zusatz")); //$NON-NLS-1$
			cbSex.setEnabled(true);
			lName.getParent().layout();
		} else {
			bAnwenderChanged(false);
			bMandantChanged(false);
			bPatientChanged(false);
		}
	}
	
	protected void bAnwenderChanged(boolean isSelected){
		bAnwender.setSelection(isSelected);
		if (isSelected) {
			bPatientChanged(false);
			bPersonChanged(true);
		} else {
			bMandantChanged(false);
		}
	}
	
	protected void bMandantChanged(boolean isSelected){
		bMandant.setSelection(isSelected);
		if (isSelected) {
			bAnwenderChanged(true);
		}
	}
	
	protected void bPatientChanged(boolean isSelected){
		bPatient.setSelection(isSelected);
		if (isSelected) {
			bAnwenderChanged(false);
			bPersonChanged(true);
		}
	}
	
	private void createKontakt(){
		String[] ret = new String[8];
		ret[0] = tName.getText();
		ret[1] = tVorname.getText();
		int idx = cbSex.getSelectionIndex();
		if (idx == -1) {
			SWTHelper.showError(Messages.getString("KontaktErfassenDialog.geschlechtFehlt.title"), //$NON-NLS-1$
				Messages.getString("KontaktErfassenDialog.geschlechtFehlt.msg")); //$NON-NLS-1$
			return;
		}
		ret[2] = cbSex.getItem(idx);
		ret[3] = tGebDat.getText();
		try {
			if (!StringTool.isNothing(ret[3])) {
				new TimeTool(ret[3], true);
			}
			ret[4] = tStrasse.getText();
			ret[5] = tPlz.getText();
			ret[6] = tOrt.getText();
			ret[7] = tTel.getText();
			if (newKontakt == null) {
				Query<Kontakt> qbe = new Query<Kontakt>(Kontakt.class);
				qbe.add("Bezeichnung1", "=", ret[0]); //$NON-NLS-1$ //$NON-NLS-2$
				qbe.add("Bezeichnung2", "=", ret[1]); //$NON-NLS-1$ //$NON-NLS-2$
				List<Kontakt> list = qbe.execute();
				if ((list != null) && (!list.isEmpty())) {
					Kontakt k = list.get(0);
					if (bOrganisation.getSelection() && k.istOrganisation()) {
						if (bLabor.getSelection()) {
							k.set("istOrganisation", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (MessageDialog.openConfirm(getShell(), Messages
							.getString("KontaktErfassenDialog.organisationExistiert.title"), //$NON-NLS-1$
							Messages.getString("KontaktErfassenDialog.organisationExistiert.msg")) == false) { //$NON-NLS-1$
							super.okPressed();
							return;
						}
					}
					if (k.istPerson()) {
						if (bAnwender.getSelection()) {
							k.set("istAnwender", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (bMandant.getSelection()) {
							k.set("istMandant", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (bPatient.getSelection()) {
							k.set("istPatient", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (MessageDialog.openConfirm(getShell(), Messages
							.getString("KontaktErfassenDialog.personExisitiert.title"), //$NON-NLS-1$
							Messages.getString("KontaktErfassenDialog.personExisitiert.msg")) == false) { //$NON-NLS-1$
							super.okPressed();
							return;
						}
					}
				}
				
				/**
				 * Neuer Kontakt erstellen. Reihenfolge der Abfrage ist Wichtig, da ein Anwender
				 * auch ein Mandant sein kann. "Organisation", - "Labor", "Person" - "Patient" -
				 * "Anwender" - "Mandant"
				 */
				if (bMandant.getSelection()) {
					newKontakt = new Mandant(ret[0], ret[1], ret[3], ret[2]);
					newKontakt.set("Zusatz", tZusatz.getText()); //$NON-NLS-1$
				} else if (bAnwender.getSelection()) {
					newKontakt = new Anwender(ret[0], ret[1], ret[3], ret[2]);
					newKontakt.set("Zusatz", tZusatz.getText()); //$NON-NLS-1$
				} else if (bPatient.getSelection()) {
					newKontakt = new Patient(ret[0], ret[1], ret[3], ret[2]);
					newKontakt.set("Zusatz", tZusatz.getText()); //$NON-NLS-1$
				} else if (bPerson.getSelection()) {
					newKontakt = new Person(ret[0], ret[1], ret[3], ret[2]);
					newKontakt.set("Zusatz", tZusatz.getText()); //$NON-NLS-1$
				} else if (bLabor.getSelection()) {
					newKontakt = new Labor(ret[0], ret[0]);
					newKontakt.set("Zusatz1", ret[1]); //$NON-NLS-1$
					newKontakt.set("Ansprechperson", tZusatz.getText()); //$NON-NLS-1$
				} else if (bOrganisation.getSelection()) {
					newKontakt = new Organisation(ret[0], ret[1]);
					newKontakt.set("Ansprechperson", tZusatz.getText()); //$NON-NLS-1$
				} else {
					MessageDialog.openInformation(getShell(), Messages
						.getString("KontaktErfassenDialog.unbekannterTyp.title"), //$NON-NLS-1$
						Messages.getString("KontaktErfassenDialog.unbekannterTyp.msg")); //$NON-NLS-1$
					return;
				}
			}
			newKontakt.set(new String[] {
				"Strasse", "Plz", "Ort", "Telefon1", "Fax", "E-Mail"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				new String[] {
					ret[4], ret[5], ret[6], ret[7], tFax.getText(), tEmail.getText()
				});
			
			ElexisEventDispatcher.fireSelectionEvent(newKontakt);
			
		} catch (TimeFormatException e) {
			ExHandler.handle(e);
			SWTHelper.showError(Messages.getString("KontaktErfassenDialog.falschesDatum.title"), //$NON-NLS-1$
				Messages.getString("KontaktErfassenDialog.falschesDatum.msg")); //$NON-NLS-1$
			return;
		}
		
	}
	
	@Override
	protected void okPressed(){
		createKontakt();
		super.okPressed();
	}
	
	public Kontakt getResult(){
		return newKontakt;
	}
}
