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
 *  $Id: VerrechnungsDisplay.java 5361 2009-06-18 12:07:37Z rgw_ch $
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
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite {
	Table tVerr;
	private Hyperlink hVer;
	private PersistentObjectDropTarget dropTarget;
	private Log log = Log.get("VerrechnungsDisplay"); //$NON-NLS-1$
	private IAction chPriceAction, chCountAction, chTextAction, removeAction,
			removeAllAction;
	private static final String CHPRICE = Messages
			.getString("VerrechnungsDisplay.changePrice"); //$NON-NLS-1$
	private static final String CHCOUNT = Messages
			.getString("VerrechnungsDisplay.changeNumber"); //$NON-NLS-1$
	private static final String REMOVE = Messages
			.getString("VerrechnungsDisplay.removeElement"); //$NON-NLS-1$
	private static final String CHTEXT = Messages
			.getString("VerrechnungsDisplay.changeText"); //$NON-NLS-1$
	private static final String REMOVEALL = Messages.getString("VerrechnungsDisplay.removeAll"); //$NON-NLS-1$

	VerrechnungsDisplay(final IWorkbenchPage page, Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
		hVer = Desk.getToolkit().createHyperlink(this,
				Messages.getString("VerrechnungsDisplay.billing"), SWT.NONE); //$NON-NLS-1$
		hVer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));
		hVer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper
								.alert(
										Messages
												.getString("VerrechnungsDisplay.error"), "LeistungenView.ID"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					page.showView(LeistungenView.ID);
					GlobalEvents.getInstance()
							.setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ExHandler.handle(ex);
					log
							.log(
									Messages
											.getString("VerrechnungsDisplay.errorStartingCodeWindow") + ex.getMessage(), //$NON-NLS-1$
									Log.ERRORS);
				}
			}
		});
		makeActions();
		tVerr = Desk.getToolkit().createTable(this, SWT.SINGLE);
		tVerr.setLayoutData(new GridData(GridData.FILL_BOTH));
		tVerr.setMenu(createVerrMenu());
		dropTarget = new PersistentObjectDropTarget(
				Messages.getString("VerrechnungsDisplay.doBill"), tVerr, new DropReceiver()); //$NON-NLS-1$
	}

	public void clear() {
		tVerr.removeAll();
	}

	public void addPersistentObject(PersistentObject o) {
		Konsultation actKons = GlobalEvents.getSelectedKons();
		if (actKons != null) {
			if (o instanceof IVerrechenbar) {
				if (Hub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper
							.alert(
									Messages
											.getString("VerrechnungsDisplay.missingRightsCaption"), //$NON-NLS-1$
									Messages
											.getString("VerrechnungsDisplay.missingRightsBody")); //$NON-NLS-1$
				} else {
					Result<IVerrechenbar> result = actKons
							.addLeistung((IVerrechenbar) o);

					if (!result.isOK()) {
						SWTHelper
								.alert(
										Messages
												.getString("VerrechnungsDisplay.imvalidBilling"), result.toString()); //$NON-NLS-1$
					}
					setLeistungen(actKons);
				}
			}
		}
	}

	private final class DropReceiver implements
			PersistentObjectDropTarget.Receiver {
		public void dropped(PersistentObject o, DropTargetEvent ev) {
			addPersistentObject(o);
		}

		public boolean accept(PersistentObject o) {
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

	void setLeistungen(Konsultation b) {
		List<Verrechnet> lgl = b.getLeistungen();
		tVerr.setRedraw(false);
		tVerr.removeAll();
		StringBuilder sdg = new StringBuilder();
		Money sum = new Money(0);
		for (Verrechnet lst : lgl) {
			sdg.setLength(0);
			int z = lst.getZahl();
			Money preis = lst.getNettoPreis().multiply(z);
			sum.addMoney(preis);
			sdg
					.append(z)
					.append(" ").append(lst.getCode()).append(" ").append(lst.getText()) //$NON-NLS-1$ //$NON-NLS-2$
					.append(" (").append(preis.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			TableItem ti = new TableItem(tVerr, SWT.WRAP);
			ti.setText(sdg.toString());
			ti.setData(lst);
		}
		tVerr.setRedraw(true);
		sdg.setLength(0);
		sdg
				.append(Messages.getString("VerrechnungsDisplay.billed")).append(sum.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		hVer.setText(sdg.toString());
	}

	private Menu createVerrMenu() {
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
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
				manager.add(removeAction);
				manager.add(new Separator());
				manager.add(removeAllAction);
			}
		});
		return mgr.createContextMenu(tVerr);
	}

	private void makeActions() {
		removeAction = new Action(REMOVE) {
			@Override
			public void run() {
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Result<Verrechnet> result = GlobalEvents.getSelectedKons()
						.removeLeistung((Verrechnet) ti.getData());
				if (!result.isOK()) {
					SWTHelper
							.alert(
									Messages
											.getString("VerrechnungsDisplay.PositionCanootBeRemoved"), result //$NON-NLS-1$
											.toString());
				}
				setLeistungen(GlobalEvents.getSelectedKons());
			}
		};
		removeAllAction = new Action(REMOVEALL) {
			@Override
			public void run() {
				TableItem[] items = tVerr.getItems();
				for (TableItem ti : items) {
					Result<Verrechnet> result = GlobalEvents.getSelectedKons()
							.removeLeistung((Verrechnet) ti.getData());
					if (!result.isOK()) {
						SWTHelper
								.alert(
										Messages
												.getString("VerrechnungsDisplay.PositionCanootBeRemoved"), result //$NON-NLS-1$
												.toString());
					}
				}
				setLeistungen(GlobalEvents.getSelectedKons());
			}
		};
		chPriceAction = new Action(CHPRICE) {

			@Override
			public void run() {
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				Money oldPrice = v.getBruttoPreis();
				String p = oldPrice.getAmountAsString();
				InputDialog dlg = new InputDialog(
						Desk.getTopShell(),
						Messages
								.getString("VerrechnungsDisplay.changePriceForService"), //$NON-NLS-1$
						Messages.getString("VerrechnungsDisplay.enterNewPrice"), p, //$NON-NLS-1$
						null);
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue().trim();
						Money newPrice = new Money(oldPrice);
						if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
							val = val.substring(0, val.length() - 1);
							double percent = Double.parseDouble(val);
							double factor = 1.0 + (percent / 100.0);
							v.setSecondaryScaleFactor(factor);
						} else {
							newPrice = new Money(val);
							v.setTP(newPrice.getCents());
							v.setSecondaryScaleFactor(1);
						}
						// v.setPreis(newPrice);
						setLeistungen(GlobalEvents.getSelectedKons());
					} catch (ParseException ex) {
						SWTHelper
								.showError(
										Messages
												.getString("VerrechnungsDisplay.badAmountCaption"), //$NON-NLS-1$
										Messages
												.getString("VerrechnungsDisplay.badAmountBody")); //$NON-NLS-1$
					}
				}
			}

		};
		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run() {
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String p = Integer.toString(v.getZahl());
				InputDialog dlg = new InputDialog(
						Desk.getTopShell(),
						Messages
								.getString("VerrechnungsDisplay.changeNumberCaption"), //$NON-NLS-1$
						Messages
								.getString("VerrechnungsDisplay.changeNumberBody"), //$NON-NLS-1$
						p, null);
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue();
						if (!StringTool.isNothing(val)) {
							String[] frac = val.split("/"); //$NON-NLS-1$
							if (frac.length > 1) {
								v.changeAnzahl(1);
								double scale = Double.parseDouble(frac[0])
										/ Double.parseDouble(frac[1]);
								v.setSecondaryScaleFactor(scale);
								v
										.setText(v.getText()
												+ " (" + val + Messages.getString("VerrechnungsDisplay.Orininalpackungen")); //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								int neu = Integer.parseInt(dlg.getValue());
								v.changeAnzahl(neu);
								v.setSecondaryScaleFactor(1.0);
								v.setText(v.getVerrechenbar().getText());
							}
						}
						setLeistungen(GlobalEvents.getSelectedKons());
					} catch (NumberFormatException ne) {
						SWTHelper
								.showError(
										Messages
												.getString("VerrechnungsDisplay.invalidEntryCaption"), //$NON-NLS-1$
										Messages
												.getString("VerrechnungsDisplay.invalidEntryBody")); //$NON-NLS-1$
					}
				}
			}
		};

		chTextAction = new Action(CHTEXT) {
			@Override
			public void run() {
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String oldText = v.getText();
				InputDialog dlg = new InputDialog(
						Desk.getTopShell(),
						Messages
								.getString("VerrechnungsDisplay.changeTextCaption"), //$NON-NLS-1$
						Messages
								.getString("VerrechnungsDisplay.changeTextBody"), //$NON-NLS-1$
						oldText, null);
				if (dlg.open() == Dialog.OK) {
					String input = dlg.getValue();
					if (input.matches("[0-9\\.,]+")) { //$NON-NLS-1$
						if (!SWTHelper
								.askYesNo(
										Messages
												.getString("VerrechnungsDisplay.confirmChangeTextCaption"), //$NON-NLS-1$
										Messages
												.getString("VerrechnungsDisplay.confirmChangeTextBody"))) { //$NON-NLS-1$
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
