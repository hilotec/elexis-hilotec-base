/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: RechnungsBlatt.java 4708 2008-12-02 16:44:44Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.rechnung;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.ObjectListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.Zahlung;
import ch.elexis.preferences.UserSettings2;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.WidgetFactory;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

public class RechnungsBlatt extends Composite implements ActivationListener, SelectionListener,
		ObjectListener {
	
	IViewSite site;
	ListViewer buchungen;
	org.eclipse.swt.widgets.List lbJournal;
	org.eclipse.swt.widgets.List lbOutputs;
	Rechnung actRn;
	ScrolledForm form;
	FormToolkit tk = Desk.getToolkit();
	// Button bBuchung,bPrint,bStorno,bGebuehr,bGutschrift;
	Text tRejects, tBemerkungen;
	Label rnAdressat;
	ListViewer konsultationenViewer;
	
	private ExpandableComposite ecBuchungen;
	private ExpandableComposite ecBemerkungen;
	private ExpandableComposite ecStatus;
	private ExpandableComposite ecFehler;
	private ExpandableComposite ecAusgaben;
	private ExpandableComposite ecKons;
	
	static final InputData[] rndata =
		{
			new InputData("RnNummer"),
			new InputData("RnDatum"),
			new InputData("Rechnungsstatus", "RnStatus", new LabeledInputField.IContentProvider() {
				
				public void displayContent(PersistentObject po, InputData ltf){
					Rechnung r = (Rechnung) po;
					ltf.setText(RnStatus.getStatusText(r.getStatus()));
					
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					if (new RnDialogs.StatusAendernDialog(Hub.plugin.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), (Rechnung) po).open() == Dialog.OK) {
						GlobalEvents.getInstance().fireObjectEvent((Rechnung) po,
							GlobalEvents.CHANGETYPE.update);
					}
				}
				
			}), new InputData("Behdl von", "RnDatumVon", Typ.STRING, null),
			new InputData("Behdl bis", "RnDatumBis", Typ.STRING, null),
			new InputData("Betrag total", "Betragx100", Typ.CURRENCY, null),
			new InputData("Betrag offen", "Betragx100", new LabeledInputField.IContentProvider() {
				
				public void displayContent(PersistentObject po, InputData ltf){
					Rechnung rn = (Rechnung) po;
					Money offen = rn.getOffenerBetrag();
					ltf.setText(offen.getAmountAsString());
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					if (new RnDialogs.BuchungHinzuDialog(Hub.plugin.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), (Rechnung) po).open() == Dialog.OK) {
						GlobalEvents.getInstance().fireObjectEvent((Rechnung) po,
							GlobalEvents.CHANGETYPE.update);
					}
				}
				
			})
		};
	LabeledInputField.AutoForm rnform;
	
	public RechnungsBlatt(Composite parent, IViewSite site){
		super(parent, SWT.NONE);
		this.site = site;
		setLayout(new GridLayout());
		form = tk.createScrolledForm(this);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		// TableWrapLayout twl=new TableWrapLayout();
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		// body.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		rnform = new LabeledInputField.AutoForm(body, rndata, 2, 3);
		// rnform.setEnabled(false);
		for (InputData li : rndata) {
			li.setEditable(false);
		}
		rnform.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		rnAdressat = new Label(body, SWT.NONE);
		rnAdressat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		IExpansionListener ecExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanging(final ExpansionEvent e){
				ExpandableComposite src = (ExpandableComposite) e.getSource();
				saveExpandedState("RechnungsBlatt/" + src.getText(), e.getState());
			}
			
		};
		
		ecBuchungen = WidgetFactory.createExpandableComposite(tk, form, "Buchungen");
		ecBuchungen.addExpansionListener(ecExpansionListener);
		// tk.createLabel(body, "Buchungen");
		buchungen = new ListViewer(ecBuchungen, SWT.V_SCROLL | SWT.BORDER);
		// TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
		SWTHelper.setGridDataHeight(buchungen.getControl(), 4, true);
		buchungen.setContentProvider(new IStructuredContentProvider() {
			public void dispose(){}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
			
			public Object[] getElements(Object inputElement){
				Rechnung actRn =
					(Rechnung) GlobalEvents.getInstance().getSelectedObject(Rechnung.class);
				if (actRn == null) {
					return new String[] {
						"Keine Rechnung ausgewählt"
					};
				}
				List<Zahlung> lz = actRn.getZahlungen();
				return lz.toArray();
			}
			
		});
		tk.adapt(buchungen.getControl(), true, true);
		ecBuchungen.setClient(buchungen.getControl());
		buchungen.setLabelProvider(new DefaultLabelProvider() {
			public String getColumnText(Object element, int columnIndex){
				return getText(element);
			}
			
			@Override
			public String getText(Object element){
				if (element instanceof Zahlung) {
					Zahlung zahlung = (Zahlung) element;
					
					StringBuilder sb = new StringBuilder();
					sb.append(zahlung.getLabel());
					String bemerkung = zahlung.getBemerkung();
					if (!StringTool.isNothing(bemerkung)) {
						sb.append(" (");
						sb.append(bemerkung);
						sb.append(")");
					}
					return sb.toString();
				} else {
					return element.toString();
				}
			}
		});
		// new Label(body,SWT.SEPARATOR|SWT.HORIZONTAL);
		ecBemerkungen = WidgetFactory.createExpandableComposite(tk, form, "Bemerkungen");
		ecBemerkungen.addExpansionListener(ecExpansionListener);
		tBemerkungen = SWTHelper.createText(tk, ecBemerkungen, 5, SWT.BORDER);
		tBemerkungen.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
				actRn.setBemerkung(tBemerkungen.getText());
			}
			
		});
		ecBemerkungen.setClient(tBemerkungen);
		// tk.createLabel(body, "Statusänderungen");
		ecStatus = WidgetFactory.createExpandableComposite(tk, form, "Statusänderungen");
		ecStatus.addExpansionListener(ecExpansionListener);
		lbJournal = new org.eclipse.swt.widgets.List(ecStatus, SWT.V_SCROLL | SWT.BORDER);
		SWTHelper.setGridDataHeight(lbJournal, 4, true);
		tk.adapt(lbJournal, true, true);
		ecStatus.setClient(lbJournal);
		// tk.createLabel(body, "Fehlermeldungen");
		ecFehler = WidgetFactory.createExpandableComposite(tk, form, "Fehlermeldungen");
		ecFehler.addExpansionListener(ecExpansionListener);
		tRejects = SWTHelper.createText(tk, ecFehler, 4, SWT.READ_ONLY | SWT.V_SCROLL);
		ecFehler.setClient(tRejects);
		// tk.createLabel(body, "Ausgaben");
		ecAusgaben = WidgetFactory.createExpandableComposite(tk, form, "Ausgaben");
		ecAusgaben.addExpansionListener(ecExpansionListener);
		lbOutputs = new org.eclipse.swt.widgets.List(ecAusgaben, SWT.V_SCROLL | SWT.BORDER);
		ecAusgaben.setClient(lbOutputs);
		SWTHelper.setGridDataHeight(lbOutputs, 4, true);
		tk.adapt(lbOutputs, true, true);
		
		// tk.createLabel(body, "Konsultationen");
		ecKons = WidgetFactory.createExpandableComposite(tk, form, "Konsultationen");
		ecKons.addExpansionListener(ecExpansionListener);
		konsultationenViewer = new ListViewer(ecKons, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecKons.setClient(konsultationenViewer.getList());
		
		konsultationenViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement){
				List<Object> elements = new ArrayList<Object>();
				if (actRn != null) {
					List<Konsultation> konsultationen = actRn.getKonsultationen();
					if (konsultationen != null) {
						for (Konsultation konsultation : konsultationen) {
							elements.add(konsultation);
							
							List<IDiagnose> diagnosen = konsultation.getDiagnosen();
							if (diagnosen != null) {
								for (IDiagnose diagnose : diagnosen) {
									elements.add(diagnose);
								}
							}
							
							List<Verrechnet> leistungen = konsultation.getLeistungen();
							if (leistungen != null) {
								for (Verrechnet verrechnet : leistungen) {
									elements.add(verrechnet);
								}
							}
						}
					}
				}
				
				return elements.toArray();
			}
			
			public void dispose(){
			// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			// nothing to do
			}
		});
		konsultationenViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element){
				if (element instanceof Konsultation) {
					Konsultation konsultation = (Konsultation) element;
					
					Money sum = new Money(0);
					List<Verrechnet> leistungen = konsultation.getLeistungen();
					if (leistungen != null) {
						for (Verrechnet verrechnet : leistungen) {
							int zahl = verrechnet.getZahl();
							Money preis = verrechnet.getNettoPreis();
							preis.multiply(zahl);
							sum.addMoney(preis);
						}
					}
					return konsultation.getLabel() + " (" + sum.toString() + ")";
				} else if (element instanceof IDiagnose) {
					IDiagnose diagnose = (IDiagnose) element;
					return "  - " + diagnose.getLabel();
				} else if (element instanceof Verrechnet) {
					Verrechnet verrechnet = (Verrechnet) element;
					int zahl = verrechnet.getZahl();
					Money preis = verrechnet.getNettoPreis();
					preis.multiply(zahl);
					return "  - " + zahl + " " + verrechnet.getLabel() + " (" + preis.toString()
						+ ")";
				} else {
					return element.toString();
				}
			}
		});
		konsultationenViewer.setInput(this);
		// form.getToolBarManager().add()
		GlobalEvents.getInstance().addSelectionListener(this);
		buchungen.setInput(site);
		GlobalEvents.getInstance().addActivationListener(this, site.getPart());
		GlobalEvents.getInstance().addObjectListener(this);
	}
	
	private void saveExpandedState(String field, boolean state){
		if (state) {
			Hub.userCfg.set(UserSettings2.STATES + field, UserSettings2.OPEN);
		} else {
			Hub.userCfg.set(UserSettings2.STATES + field, UserSettings2.CLOSED);
		}
	}
	
	private void setExpandedState(ExpandableComposite ec, String field){
		String mode =
			Hub.userCfg.get(UserSettings2.EXPANDABLE_COMPOSITES, UserSettings2.REMEMBER_STATE);
		if (mode.equals(UserSettings2.OPEN)) {
			ec.setExpanded(true);
		} else if (mode.equals(UserSettings2.CLOSED)) {
			ec.setExpanded(false);
		} else {
			String state = Hub.userCfg.get(UserSettings2.STATES + field, UserSettings2.CLOSED);
			if (state.equals(UserSettings2.CLOSED)) {
				ec.setExpanded(false);
			} else {
				ec.setExpanded(true);
			}
		}
	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, site.getPart());
		GlobalEvents.getInstance().removeObjectListener(this);
		super.dispose();
	}
	
	public void activation(boolean mode){
	/* egal */
	}
	
	public void visible(boolean mode){
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
			selectionEvent(GlobalEvents.getInstance().getSelectedObject(Rechnung.class));
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}
	
	public void clearEvent(Class template){
		if (template.equals(Rechnung.class)) {
			actRn = null;
			display();
		}
	}
	
	public void selectionEvent(PersistentObject obj){
		if (obj instanceof Rechnung) {
			actRn = (Rechnung) obj;
			Desk.getDisplay().syncExec(new Runnable() {
				public void run(){
					display();
				}
			});
		} else if (obj instanceof Anwender) {
			Desk.getDisplay().syncExec(new Runnable() {
				public void run(){
					display();
				}
			});
		}
	}
	
	public void display(){
		rnform.reload(actRn);
		rnAdressat.setText("");
		buchungen.refresh(true);
		lbJournal.removeAll();
		tRejects.setText("");
		lbOutputs.removeAll();
		if (actRn != null) {
			rnAdressat.setText("Adressat: " + actRn.getFall().getGarant().getLabel());
			form.setText(actRn.getLabel());
			List<String> trace = actRn.getTrace(Rechnung.STATUS_CHANGED);
			for (String s : trace) {
				String[] stm = s.split("\\s*:\\s");
				StringBuilder sb = new StringBuilder();
				sb.append(stm[0]).append(" : ").append(
					RnStatus.getStatusText(Integer.parseInt(stm[1])));
				lbJournal.add(sb.toString());
			}
			if (actRn.getStatus() == RnStatus.FEHLERHAFT) {
				List<String> rejects = actRn.getTrace(Rechnung.REJECTED);
				StringBuilder rjj = new StringBuilder();
				for (String r : rejects) {
					rjj.append(r).append("\n------\n");
				}
				tRejects.setText(rjj.toString());
			}
			List<String> outputs = actRn.getTrace(Rechnung.OUTPUT);
			for (String o : outputs) {
				lbOutputs.add(o);
			}
			tBemerkungen.setText(actRn.getBemerkung());
		}
		
		konsultationenViewer.refresh();
		
		setExpandedState(ecBuchungen, "RechnungsBlatt/" + ecBuchungen.getText());
		setExpandedState(ecBemerkungen, "RechnungsBlatt/" + ecBemerkungen.getText());
		setExpandedState(ecStatus, "RechnungsBlatt/" + ecStatus.getText());
		setExpandedState(ecFehler, "RechnungsBlatt/" + ecFehler.getText());
		setExpandedState(ecAusgaben, "RechnungsBlatt/" + ecAusgaben.getText());
		setExpandedState(ecKons, "RechnungsBlatt/" + ecKons.getText());
		
		form.reflow(true);
	}
	
	public void objectChanged(PersistentObject o){
		if (o instanceof Rechnung) {
			selectionEvent(o);
		}
		
	}
	
	public void objectCreated(PersistentObject o){
	// TODO Auto-generated method stub
	
	}
	
	public void objectDeleted(PersistentObject o){
		if ((actRn != null) && (o.getId().equals(actRn.getId()))) {
			clearEvent(o.getClass());
		}
		
	}
}
