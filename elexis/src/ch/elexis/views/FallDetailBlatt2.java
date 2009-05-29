/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FallDetailBlatt2.java 5322 2009-05-29 10:59:45Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.DayDateCombo;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Display detail data of a Fall
 */
public class FallDetailBlatt2 extends Composite {
	private static final String SELECT_CONTACT_BODY = Messages.getString("FallDetailBlatt2.PleaseSelectContactFor"); //$NON-NLS-1$
	private static final String SELECT_CONTACT_CAPTION = Messages.getString("FallDetailBlatt2.PleaseSelectCpntactCaption"); //$NON-NLS-1$
	private static final String LABEL = Messages.getString("FallDetailBlatt2.Labek"); //$NON-NLS-1$
	private static final String RECHNUNGSEMPFAENGER = Messages.getString("FallDetailBlatt2.BillAdressee"); //$NON-NLS-1$
	private static final String VERSICHERUNGSNUMMER = Messages.getString("FallDetailBlatt2.InsuranceNumber"); //$NON-NLS-1$
	private static final String KOSTENTRAEGER = Messages.getString("FallDetailBlatt2.Guarantor"); //$NON-NLS-1$
	private static final String ABRECHNUNGSMETHODE = Messages.getString("FallDetailBlatt2.BillingMethod"); //$NON-NLS-1$
	private final FormToolkit tk;
	private final ScrolledForm form;
	String[] Abrechnungstypen = Fall.getAbrechnungsSysteme();
	private Fall actFall;
	DayDateCombo ddc;
	
	public static final String[] Reasons =
		{
			Fall.TYPE_DISEASE, Fall.TYPE_ACCIDENT, Fall.TYPE_MATERNITY, Fall.TYPE_PREVENTION,
			Fall.TYPE_BIRTHDEFECT, Fall.TYPE_OTHER
		};
	public static final String[] dgsys = null;
	Combo cAbrechnung, cReason;
	DatePickerCombo dpVon, dpBis;
	Text tBezeichnung, tGarant;
	Hyperlink autoFill;
	List<Control> lReqs = new ArrayList<Control>();
	
	public FallDetailBlatt2(final Composite parent){
		super(parent, SWT.NONE);
		tk = Desk.getToolkit();
		form = tk.createScrolledForm(this);
		Composite top = form.getBody();
		setLayout(new FillLayout());
		top.setLayout(new GridLayout(2, false));
		tk.createLabel(top, ABRECHNUNGSMETHODE);
		Composite cpAbrechnung = new Composite(top, SWT.NONE);
		cpAbrechnung.setLayout(new GridLayout(2, false));
		cAbrechnung = new Combo(cpAbrechnung, SWT.READ_ONLY);
		autoFill = tk.createHyperlink(cpAbrechnung, Messages.getString("FallDetailBlatt2.ApplyData"), SWT.NONE); //$NON-NLS-1$
		autoFill.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(final HyperlinkEvent e){
				Fall f = getFall();
				if (f == null) {
					return;
				}
				String abr = f.getAbrechnungsSystem();
				// make sure compatibility methods are called
				
				String ktNew = f.getInfoString(KOSTENTRAEGER);
				String ktOld = f.get(Messages.getString("FallDetailBlatt2.GuarantorNoSpecialChars")); //$NON-NLS-1$
				
				if (StringTool.isNothing(ktNew)) {
					Kontakt k = Kontakt.load(ktOld);
					if (k.isValid()) {
						f.setRequiredContact(KOSTENTRAEGER, k);
					}
				}
				String vnNew = f.getInfoString(VERSICHERUNGSNUMMER);
				// String vnOld=f.getVersNummer();
				String vnOld = f.get(Messages.getString("FallDetailBlatt2.InsNumber")); //$NON-NLS-1$
				if (StringTool.isNothing(vnNew)) {
					f.setRequiredString(VERSICHERUNGSNUMMER, vnOld);
				}
				
				Fall[] faelle = f.getPatient().getFaelle();
				for (Fall f0 : faelle) {
					if (f0.getId().equals(f.getId())) {
						// ignore current Fall
						continue;
					}
					
					if (f0.getAbrechnungsSystem().equals(abr)) {
						if (f.getInfoString(RECHNUNGSEMPFAENGER).equals("")) {
							f.setInfoString(RECHNUNGSEMPFAENGER, f0.get("GarantID")); //$NON-NLS-1$
						}
						if (f.getInfoString(KOSTENTRAEGER).equals("")) {
							f.setInfoString(KOSTENTRAEGER, f0.get(Messages.getString("FallDetailBlatt2.GuarantorNoSpecialChars"))); //$NON-NLS-1$
						}
						if (f.getInfoString(VERSICHERUNGSNUMMER).equals("")) { //$NON-NLS-1$
							f.setInfoString(VERSICHERUNGSNUMMER, f0
								.getInfoString(VERSICHERUNGSNUMMER));
						}
						// TODO break? or looking for the most current Fall?
						break;
					}
				}
				setFall(f);
			}
		});
		cAbrechnung.setItems(Abrechnungstypen);
		
		cAbrechnung.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				int i = cAbrechnung.getSelectionIndex();
				Fall fall = getFall();
				if (fall != null) {
					if (fall.getBehandlungen(false).length > 0) {
						if (Hub.acl.request(AccessControlDefaults.CASE_MODIFY)) {
							if (SWTHelper
								.askYesNo(
									Messages.getString("FallDetailBlatt2.DontChangeBillingSystemCaption"), //$NON-NLS-1$
									Messages.getString("FallDetailBlatt2.DontChangeBillingSystemBody"))) { //$NON-NLS-1$
								fall.setAbrechnungsSystem(cAbrechnung.getItem(i));
								setFall(fall);
								GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
								return;
							}
						} else {
							SWTHelper
								.alert(Messages.getString("FallDetailBlatt2.CantChangeBillingSystemCaption"), //$NON-NLS-1$
									Messages.getString("FallDetailBlatt2.CantChangeBillingSystemBody")); //$NON-NLS-1$
						}
						String gesetz = fall.getAbrechnungsSystem();
						if (ch.rgw.tools.StringTool.isNothing(gesetz)) {
							gesetz = Messages.getString("FallDetailBlatt2.free"); //$NON-NLS-1$
						}
						cAbrechnung.select(cAbrechnung.indexOf(gesetz));
						
					} else {
						fall.setAbrechnungsSystem(Abrechnungstypen[i]);
						setFall(fall);
						GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
						// Falls noch kein Garant gesetzt ist: Garanten des letzten Falles zum
						// selben Gesetz nehmen
					}
					
				}
			}
			
		});
		tk.createLabel(top, LABEL);
		tBezeichnung = tk.createText(top, StringTool.leer);
		tBezeichnung.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e){
				String newval = ((Text) e.getSource()).getText();
				Fall fall = getFall();
				if (fall != null) {
					fall.set(LABEL, newval);
					GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
				}
				super.focusLost(e);
			}
		});
		tBezeichnung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(top, Messages.getString("FallDetailBlatt2.ReasonForInsurance")); //$NON-NLS-1$
		cReason = new Combo(top, SWT.READ_ONLY);
		cReason.setItems(Reasons);
		cReason.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				int i = cReason.getSelectionIndex();
				Fall fall = getFall();
				if (fall != null) {
					fall.setGrund(Reasons[i]);
					GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
				}
			}
		});
		cReason.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(top, Messages.getString("FallDetailBlatt2.StartDate")); //$NON-NLS-1$
		dpVon = new DatePickerCombo(top, SWT.NONE);
		dpVon.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				Fall fall = getFall();
				fall.setBeginnDatum(new TimeTool(dpVon.getDate().getTime())
					.toString(TimeTool.DATE_GER));
				GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
			}
			
		});
		tk.createLabel(top, Messages.getString("FallDetailBlatt2.EndDate")); //$NON-NLS-1$
		dpBis = new DatePickerCombo(top, SWT.NONE);
		dpBis.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				Fall fall = getFall();
				fall.setEndDatum(new TimeTool(dpBis.getDate().getTime())
					.toString(TimeTool.DATE_GER));
				GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
			}
			
		});
		ddc = new DayDateCombo(top, Messages.getString("FallDetailBlatt2.ProposeForBillingIn"), Messages.getString("FallDetailBlatt2.DaysOrAfter")); //$NON-NLS-1$ //$NON-NLS-2$
		ddc.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		ddc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TimeTool nDate = ddc.getDate();
				Fall fall = getFall();
				if (fall != null) {
					fall.setBillingDate(nDate);
				}
			}
		});
		tk.adapt(ddc);
		tk.createSeparator(top, SWT.HORIZONTAL).setLayoutData(
			SWTHelper.getFillGridData(2, true, 1, false));
		
		Hyperlink hlGarant = tk.createHyperlink(top, RECHNUNGSEMPFAENGER, SWT.NONE);
		hlGarant.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				KontaktSelektor ksl =
					new KontaktSelektor(getShell(), Kontakt.class, Messages.getString("FallDetailBlatt2.SelectGuarantorCaption"), //$NON-NLS-1$
						Messages.getString("FallDetailBlatt2.SelectGuarantorBody"), true); //$NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					Kontakt sel = (Kontakt) ksl.getSelection();
					Fall fall = getFall();
					if (fall != null) {
						fall.setGarant(sel);
						setFall(fall);
						GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
					}
				}
			}
		});
		
		tGarant = tk.createText(top, "");
		tGarant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.paintBordersFor(top);
		setFall(getFall());
		
	}
	
	class Focusreact implements FocusListener {
		private final String field;
		
		Focusreact(final String dbField){
			field = dbField;
		}
		
		public void focusGained(final FocusEvent e){ /* nichts */}
		
		public void focusLost(final FocusEvent e){
			String newval = ((Text) e.getSource()).getText();
			Fall fall = getFall();
			if (fall != null) {
				fall.setInfoString(field, newval);
				GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
			}
		}
		
	}
	
	public void setFall(final Fall f){
		actFall = f;
		for (Control c : lReqs) {
			c.dispose();
		}
		lReqs.clear();
		cAbrechnung.setItems(Fall.getAbrechnungsSysteme());
		if (f == null) {
			form.setText(Messages.getString("FallDetailBlatt2.NoCaseSelected")); //$NON-NLS-1$
			tBezeichnung.setText(Messages.getString("FallDetailBlatt2.29")); //$NON-NLS-1$
			cReason.select(0);
			return;
		}
		
		form.setText(f.getLabel());
		tBezeichnung.setText(f.getBezeichnung());
		String grund = f.getGrund();
		int ix = cReason.indexOf(grund);
		if (ix == -1) {
			ix = 0;
		}
		cReason.select(ix);
		String abr = f.getAbrechnungsSystem();
		cAbrechnung.setText(abr);
		TimeTool tt = new TimeTool();
		if (tt.set(f.getBeginnDatum()) == true) {
			dpVon.setDate(tt.getTime());
		} else {
			dpVon.setDate(null);
		}
		if (tt.set(f.getEndDatum()) == true) {
			dpBis.setDate(tt.getTime());
		} else {
			dpBis.setDate(null);
		}
		tGarant.setText(f.getGarant().getLabel());
		String reqs = f.getRequirements();
		if (reqs != null) {
			for (String req : reqs.split(";")) {
				final String[] r = req.split(":"); //$NON-NLS-1$
				if (r.length < 2) {
					continue;
				}
				if (r[1].equals("T")) { //$NON-NLS-1$
					lReqs.add(tk.createLabel(form.getBody(), r[0]));
					String val = f.getInfoString(r[0]);
					Text tx = tk.createText(form.getBody(), val);
					tx.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					tx.addFocusListener(new Focusreact(r[0]));
					lReqs.add(tx);
				} else if (r[1].equals("D")) { //$NON-NLS-1$
					lReqs.add(tk.createLabel(form.getBody(), r[0]));
					final DatePickerCombo dp = new DatePickerCombo(form.getBody(), SWT.NONE);
					String dat = f.getInfoString(r[0]);
					if (tt.set(dat)) {
						dp.setDate(tt.getTime());
					}
					dp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					dp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e){
							TimeTool tt = new TimeTool(dp.getDate().getTime());
							f.setInfoString(r[0], tt.toString(TimeTool.DATE_GER));
						}
					});
					lReqs.add(dp);
				} else if (r[1].equals("K")) { //$NON-NLS-1$
					Hyperlink hl = tk.createHyperlink(form.getBody(), r[0], SWT.NONE);
					String val = f.getInfoString(r[0]);
					if (val.startsWith("**ERROR")) { //$NON-NLS-1$
						val = "";
					} else {
						Kontakt k = Kontakt.load(val);
						val = k.getLabel();
					}
					Text tx = tk.createText(form.getBody(), val);
					hl.addHyperlinkListener(new HyperlinkAdapter() {
						@Override
						public void linkActivated(final HyperlinkEvent e){
							KontaktSelektor ksl =
								new KontaktSelektor(getShell(), Kontakt.class, SELECT_CONTACT_CAPTION,
										MessageFormat.format(SELECT_CONTACT_BODY, new Object[]{r[0]}),true);
									//"Bitte wählen Sie den Kontakt für " + r[0] + " aus", true);
							if (ksl.open() == Dialog.OK) {
								Kontakt sel = (Kontakt) ksl.getSelection();
								Fall fall = getFall();
								if (fall != null) {
									if (sel != null) {
										fall.setInfoString(r[0], sel.getId());
									} else {
										fall.setInfoString(r[0], StringTool.leer);
									}
									setFall(fall);
									GlobalEvents.getInstance()
										.fireSelectionEvent(fall.getPatient());
								}
							}
						}
					});
					lReqs.add(hl);
					tx.setEditable(false);
					tx.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					lReqs.add(tx);
				}
			}
			TimeTool bt = f.getBillingDate();
			ddc.setDates(bt);
			form.reflow(true);
			form.redraw();
		}
		
	}
	
	private Fall getFall(){
		if (actFall == null) {
			Fall ret = GlobalEvents.getSelectedFall();
			if (ret != null) {
				actFall = ret;
			}
		}
		return actFall;
	}
}
