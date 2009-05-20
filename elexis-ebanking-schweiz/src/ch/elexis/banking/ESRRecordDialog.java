/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ESRRecordDialog.java 5316 2009-05-20 11:34:51Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.Desk;
import ch.elexis.data.Fall;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.InputData;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Change an ESR record manually
 * 
 * @author gerry
 * 
 */
public class ESRRecordDialog extends TitleAreaDialog {
	private ESRRecord rec;
	private boolean bBooked;
	private Button bKeep, bBook, bUnbook, bDelete;
	private Label lFile;
	private LabeledInputField.AutoForm af;
	
	private InputData[] fields =
		{
			new InputData("Eingelesen", "Eingelesen", InputData.Typ.DATE, null),
			new InputData("ESR-Typ", "ESRCode", InputData.Typ.STRING, null),
			new InputData("Verarbeitet", "Verarbeitet", InputData.Typ.DATE, null),
			new InputData("Gutgeschrieben", "Gutgeschrieben", InputData.Typ.DATE, null),
			new InputData("Angekommen", "Datum", InputData.Typ.DATE, null),
			new InputData("Betrag", "BetragInRp", InputData.Typ.CURRENCY, null),
			new InputData("Rechnung", "RechnungsID", new LabeledInputField.IContentProvider() {
				public void displayContent(PersistentObject po, InputData ltf){
					Rechnung rn = rec.getRechnung();
					if (rn == null) {
						ltf.setText("??");
					} else {
						ltf.setText(rn.getNr());
					}
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					InputDialog id =
						new InputDialog(getShell(), "Rechnungsnummer ändern",
							"Bitte geben Sie die neue Rechnungsnummer ein", ltf.getText(), null);
					if (id.open() == Dialog.OK) {
						String rnid =
							new Query<Rechnung>(Rechnung.class).findSingle("RnNummer", "=", id
								.getValue());
						int err = 0;
						if (rnid != null) {
							Rechnung r = Rechnung.load(rnid);
							if (r.isAvailable()) {
								Fall fall = r.getFall();
								if (fall.isAvailable()) {
									Patient pat = fall.getPatient();
									Mandant mn = r.getMandant();
									if (pat.isAvailable()) {
										rec.set("RechnungsID", r.getId());
										// ltf.setText(r.getNr());
										rec.set("PatientID", pat.getId());
										if (mn != null && mn.isValid()) {
											rec.set("MandantID", mn.getId());
										}
										af.reload(rec);
									} else {
										err = 4;
									}
								} else {
									err = 3;
								}
								
							} else {
								err = 2;
							}
							
						} else {
							err = 1;
						}
						if (err != 0) {
							SWTHelper.showError("Rechnung nicht gefunden",
								"Es wurde keine gültige Rechnung mit der Nummer " + id.getValue()
									+ " gefunden.");
						}
					}
				}
				
			}), new InputData("Patient", "PatientID", new LabeledInputField.IContentProvider() {
				
				public void displayContent(PersistentObject po, InputData ltf){
					ltf.setText(rec.getPatient().getLabel());
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					KontaktSelektor ksl =
						new KontaktSelektor(getShell(), Patient.class, "Patient auswählen",
							"Bitte wählen Sie einen Patienteneintrag");
					if (ksl.open() == Dialog.OK) {
						Patient actPatient = (Patient) ksl.getSelection();
						rec.set("PatientID", actPatient.getId());
						ltf.setText(actPatient.getLabel());
					}
				}
				
			})
		
		};
	
	ESRRecordDialog(Shell shell, ESRRecord record){
		super(shell);
		rec = record;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		TableWrapLayout twl = new TableWrapLayout();
		ret.setLayout(twl);
		af = new LabeledInputField.AutoForm(ret, fields);
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		af.setLayoutData(twd);
		lFile = new Label(ret, SWT.NONE);
		lFile.setText("File: " + rec.getFile());
		TableWrapData tw3 = new TableWrapData();
		tw3.grabHorizontal = true;
		lFile.setLayoutData(tw3);
		Composite cChoices = new Composite(ret, SWT.BORDER);
		TableWrapData tw2 = new TableWrapData();
		tw2.grabHorizontal = true;
		cChoices.setLayoutData(tw2);
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		cChoices.setLayout(rl);
		bKeep = new Button(cChoices, SWT.RADIO);
		bKeep.setText("Nichts ändern");
		bBook = new Button(cChoices, SWT.RADIO);
		bBook.setText("Record verbuchen");
		bUnbook = new Button(cChoices, SWT.RADIO);
		bUnbook.setText("Record nicht verbuchen");
		bDelete = new Button(cChoices, SWT.RADIO);
		bDelete.setText("Record löschen");
		bBooked = !StringTool.isNothing(rec.getGebucht());
		bKeep.setSelection(true);
		
		af.reload(rec);
		ret.pack();
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("ESR Record anpassen");
		setMessage("Warnung: Manuelle Änderungen von ESR-Records können die Buchhaltung verfälschen!");
		setTitleImage(Desk.getImage(Desk.IMG_LOGO48));
		getShell().setText("Details für ESR-Record");
		
	}
	
	@Override
	protected void okPressed(){
		if (bBook.getSelection()) {
			if (!bBooked) {
				Money zahlung = rec.getBetrag();
				Rechnung rn = rec.getRechnung();
				rn.addZahlung(zahlung, "VESR für rn " + rn.getNr() + " / "
					+ rec.getPatient().getPatCode(), new TimeTool(rec.getValuta()));
				rec.setGebucht(null);
			}
		} else if (bUnbook.getSelection()) {
			if (bBooked) {
				Money zahlung = rec.getBetrag();
				Rechnung rn = rec.getRechnung();
				rn.addZahlung(zahlung.negate(), "stornierter VESR für rn " + rn.getNr() + " / "
					+ rec.getPatient().getPatCode(),null);
				rec.set("Gebucht", "");
			}
		}
		super.okPressed();
	}
	
}
