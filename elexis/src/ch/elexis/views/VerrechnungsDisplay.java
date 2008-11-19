/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: VerrechnungsDisplay.java 4687 2008-11-19 16:00:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.text.ParseException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite {
	Table tVerr;
	private Hyperlink hVer;
	private PersistentObjectDropTarget dropTarget;
	private Log log = Log.get("VerrechnungsDisplay");
	private IAction chPriceAction, chCountAction, chTextAction, removeAction;
	private static final String CHPRICE = "Preis ändern";
	private static final String CHCOUNT = "Zahl ändern";
	private static final String REMOVE = "Position entfernen";
	private static final String CHTEXT = "Text ändern";
	
	VerrechnungsDisplay(final IWorkbenchPage page, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout());
		hVer = Desk.getToolkit().createHyperlink(this, "Verrechnung", SWT.NONE);
		hVer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		hVer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e){
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper.alert("Fehler", "LeistungenView.ID");
					}
					page.showView(LeistungenView.ID);
					GlobalEvents.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ExHandler.handle(ex);
					log
						.log("Fehler beim Starten des Leistungscodes " + ex.getMessage(),
							Log.ERRORS);
				}
			}
		});
		makeActions();
		tVerr = Desk.getToolkit().createTable(this, SWT.SINGLE);
		tVerr.setLayoutData(new GridData(GridData.FILL_BOTH));
		tVerr.setMenu(createVerrMenu());
		dropTarget = new PersistentObjectDropTarget("Verrechnen", tVerr, new DropReceiver());
	}
	
	public void clear(){
		tVerr.removeAll();
	}
	
	public void addPersistentObject(PersistentObject o){
		Konsultation actKons = GlobalEvents.getSelectedKons();
		if (actKons != null) {
			if (o instanceof IVerrechenbar) {
				if (Hub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper.alert("Fehlende Rechte",
						"Sie haben nicht die Berechtigung, Leistungen zu verrechnen");
				} else {
					Result<IVerrechenbar> result = actKons.addLeistung((IVerrechenbar) o);
					
					if (!result.isOK()) {
						SWTHelper.alert("Diese Verrechnung ist ungültig", result.toString());
					}
					setLeistungen(actKons);
				}
			}
		}
	}
	
	private final class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(PersistentObject o, DropTargetEvent ev){
			addPersistentObject(o);
		}
		
		public boolean accept(PersistentObject o){
			if (GlobalEvents.getSelectedPatient() != null) {
				if (o instanceof IVerrechenbar) {
					return true;
				}
				if (o instanceof Leistungsblock) {
					return true;
				}
			}
			return false;
		}
	}
	
	void setLeistungen(Konsultation b){
		List<Verrechnet> lgl = b.getLeistungen();
		// DecimalFormat df=new DecimalFormat("0.00");
		// Collections.sort(lgl,TarmedLeistung.tarmedComparator);
		tVerr.setRedraw(false);
		tVerr.removeAll();
		StringBuilder sdg = new StringBuilder();
		Money sum = new Money(0);
		// double sum=0;
		for (Verrechnet lst : lgl) {
			sdg.setLength(0);
			int z = lst.getZahl();
			// double preis=(z*lst.getEffPreisInRappen())/100.0;
			Money preis = lst.getNettoPreis().multiply(z);
			sum.addMoney(preis);
			sdg.append(z).append(" ").append(lst.getCode()).append(" ").append(lst.getText())
				.append(" (").append(preis.getAmountAsString()).append(")");
			TableItem ti = new TableItem(tVerr, SWT.WRAP);
			ti.setText(sdg.toString());
			ti.setData(lst);
		}
		tVerr.setRedraw(true);
		sdg.setLength(0);
		sdg.append("Verrechnung (").append(sum.getAmountAsString()).append(")");
		hVer.setText(sdg.toString());
	}
	
	/*
	 * class delVerrListener extends SelectionAdapter { public void widgetSelected(SelectionEvent e)
	 * { int sel = tVerr.getSelectionIndex(); TableItem ti = tVerr.getItem(sel); Result<Verrechnet>
	 * result = GlobalEvents.getSelectedKons().removeLeistung( (Verrechnet) ti.getData()); if
	 * (!result.isOK()) { SWTHelper.alert("Leistungsposition kann nicht entfernt werden",
	 * result.toString()); } setLeistungen(GlobalEvents.getSelectedKons()); } }
	 */
	private Menu createVerrMenu(){
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				manager.add(chPriceAction);
				manager.add(chCountAction);
				List<IAction> itemActions = v.getVerrechenbar().getActions(v);
				if ((itemActions != null) && (itemActions.size() > 0)) {
					manager.add(new Separator());
					for (IAction a : itemActions) {
						if (a != null) {
							manager.add(a);
						}
					}
				}
				manager.add(new Separator());
				manager.add(chTextAction);
				// manager.add(detailsAction);
				manager.add(removeAction);
				
			}
		});
		return mgr.createContextMenu(tVerr);
	}
	
	private void makeActions(){
		removeAction = new Action(REMOVE) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Result<Verrechnet> result =
					GlobalEvents.getSelectedKons().removeLeistung((Verrechnet) ti.getData());
				if (!result.isOK()) {
					SWTHelper.alert("Leistungsposition kann nicht entfernt werden", result
						.toString());
				}
				setLeistungen(GlobalEvents.getSelectedKons());
			}
		};
		chPriceAction = new Action(CHPRICE) {
			
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				// String ext=actBehandlung.getFall().getGesetz();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				// String
				// p=Rechnung.geldFormat.format(v.getEffPreisInRappen()/100.0);
				Money oldPrice = v.getBruttoPreis();
				String p = oldPrice.getAmountAsString();
				InputDialog dlg =
					new InputDialog(Desk.getTopShell(), "Preis für Leistung ändern",
						"Geben Sie bitte den neuen Preis für die Leistung ein (x.xx oder -x%)", p,
						null);
				if (dlg.open() == Dialog.OK) {
					// v.setPreisInRappen(Integer.parseInt(dlg.getValue().replaceAll("\\.","")));
					try {
						String val = dlg.getValue().trim();
						Money newPrice = new Money(oldPrice);
						if (val.endsWith("%") && val.length() > 1) {
							val = val.substring(0, val.length() - 1);
							double percent = Double.parseDouble(val);
							double factor = 1.0 + (percent / 100.0);
							// double amount = newPrice.getAmount();
							// amount += amount * percent / 100.0;
							// newPrice = new Money(amount);
							// double factor = (newPrice.getAmount() * 100.0) /
							// oldPrice.getAmount();
							v.setSecondaryScaleFactor(factor);
						} else {
							newPrice = new Money(val);
							v.setTP(newPrice.getCents());
							v.setSecondaryScaleFactor(1);
						}
						// v.setPreis(newPrice);
						setLeistungen(GlobalEvents.getSelectedKons());
					} catch (ParseException ex) {
						SWTHelper.showError("Falsche Betragseingabe",
							"Der eingegebene Betrag konnte nicht interpretiert werden");
					}
				}
			}
			
		};
		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String p = Integer.toString(v.getZahl());
				InputDialog dlg =
					new InputDialog(
						Desk.getTopShell(),
						"Zahl der Leistung ändern",
						"Geben Sie bitte die neue Anwendungszahl (oder Bruchzahl wie 1/3) für die Leistung bzw. den Artikel ein",
						p, null);
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue();
						if (!StringTool.isNothing(val)) {
							String[] frac = val.split("/");
							if (frac.length > 1) {
								v.changeAnzahl(1);
								double scale =
									Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
								// Money price = v.getBruttoPreis();
								// price.multiply(Double.parseDouble(frac[0])
								// / Double.parseDouble(frac[1]));
								// v.setPreis(price);
								v.setSecondaryScaleFactor(scale);
								v.setText(v.getText() + " (" + val + " OP)");
							} else {
								int neu = Integer.parseInt(dlg.getValue());
								v.changeAnzahl(neu);
								v.setSecondaryScaleFactor(1.0);
								v.setText(v.getVerrechenbar().getText());
							}
						}
						setLeistungen(GlobalEvents.getSelectedKons());
					} catch (NumberFormatException ne) {
						SWTHelper.showError("Ungültige Eingabe",
							"Bitte geben Sie eine ganze Zahl oder einen Bruch der Form x/y ein");
					}
				}
			}
		};
		
		chTextAction = new Action(CHTEXT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String oldText = v.getText();
				InputDialog dlg =
					new InputDialog(
						Desk.getTopShell(),
						"Text der Leistung ändern",
						"Geben Sie bitte die neue Beschreibung für die Leistung bzw. den Artikel ein",
						oldText, null);
				if (dlg.open() == Dialog.OK) {
					String input = dlg.getValue();
					if (input.matches("[0-9\\.,]+")) {
						if (!SWTHelper
							.askYesNo("Wirklich Text ändern?",
								"Sie haben eine Zahl eingegeben. Soll dies wirklich der neue Name für die Leistung sein?")) {
							return;
						}
					}
					v.setText(input);
					setLeistungen(GlobalEvents.getSelectedKons());
				}
			}
		};
	}
}
