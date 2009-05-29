/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: FixMediDisplay.java 5322 2009-05-29 10:59:45Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;

import ch.elexis.Desk;
import ch.elexis.StringConstants;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.elexis.dialogs.MediDetailDialog;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Display and let the user modify the medication of the currently selected patient This is a
 * pop-in-Replacement for DauerMediDisplay. To calculate the daily cost wie accept the forms 1-1-1-1
 * and 1x1, 2x3 and so on
 * 
 * @author gerry
 * 
 */
public class FixMediDisplay extends ListDisplay<Prescription> {
	private static final String TTCOST = Messages.getString("FixMediDisplay.DailyCost"); //$NON-NLS-1$
	private LDListener dlisten;
	private IAction stopMedicationAction, changeMedicationAction, removeMedicationAction;
	FixMediDisplay self;
	Label lCost;
	PersistentObjectDropTarget target;
	static final String REZEPT = Messages.getString("FixMediDisplay.Prescription"); //$NON-NLS-1$
	static final String LISTE = Messages.getString("FixMediDisplay.UsageList"); //$NON-NLS-1$
	static final String HINZU = Messages.getString("FixMediDisplay.AddItem"); //$NON-NLS-1$
	static final String KOPIEREN = Messages.getString("FixMediDisplay.Copy"); //$NON-NLS-1$
	
	public FixMediDisplay(Composite parent, IViewSite s){
		super(parent, SWT.NONE, null);
		lCost = new Label(this, SWT.NONE);
		lCost.setText(TTCOST);
		lCost.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		dlisten = new DauerMediListener(s);
		self = this;
		addHyperlinks(HINZU, LISTE, REZEPT);
		makeActions();
		ViewMenus menu = new ViewMenus(s);
		menu.createControlContextMenu(list, stopMedicationAction, changeMedicationAction, null,
			removeMedicationAction);
		setDLDListener(dlisten);
		target =
			new PersistentObjectDropTarget(Messages.getString("FixMediDisplay.FixMedikation"), this, //$NON-NLS-1$
				new PersistentObjectDropTarget.Receiver() {
					
					public boolean accept(PersistentObject o){
						if (o instanceof Prescription) {
							return true;
						}
						if (o instanceof Artikel) {
							return true;
						}
						return false;
					}
					
					public void dropped(PersistentObject o, DropTargetEvent e){
						if (o instanceof Artikel) {
							Prescription pre =
								new Prescription((Artikel) o, GlobalEvents.getSelectedPatient(),
									StringTool.leer, StringTool.leer);
							pre.set(Prescription.DATE_FROM, new TimeTool().toString(TimeTool.DATE_GER));
							MediDetailDialog dlg = new MediDetailDialog(getShell(), pre);
							if (dlg.open() == Window.OK) {
								// self.add(pre);
								reload();
							}
							
						} else if (o instanceof Prescription) {
							Prescription pre = (Prescription) o;
							Prescription now =
								new Prescription(pre.getArtikel(), GlobalEvents
									.getSelectedPatient(), pre.getDosis(), pre.getBemerkung());
							now.set(Prescription.DATE_FROM, new TimeTool().toString(TimeTool.DATE_GER));
							// self.add(now);
							reload();
						}
					}
				});
		new PersistentObjectDragSource2(list, new PersistentObjectDragSource2.Draggable() {
			
			public List<PersistentObject> getSelection(){
				Prescription pr = FixMediDisplay.this.getSelection();
				ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>(1);
				if (pr != null) {
					ret.add(pr);
				}
				return ret;
			}
		});
		
	}
	
	public void reload(){
		clear();
		Patient act = GlobalEvents.getSelectedPatient();
		double cost = 0.0;
		boolean canCalculate = true;
		if (act != null) {
			Prescription[] pre = act.getFixmedikation();
			for (Prescription pr : pre) {
				float num = 0;
				try {
					String dosis = pr.getDosis();
					if (dosis.matches("[0-9]+[xX][0-9]+(/[0-9]+)?")) { //$NON-NLS-1$
						String[] dose = dosis.split("[xX]"); //$NON-NLS-1$
						int count = Integer.parseInt(dose[0]);
						num = getNum(dose[1]) * count;
					} else if (dosis.indexOf('-') != -1) {
						String[] dos = dosis.split("-"); //$NON-NLS-1$
						if (dos.length > 2) {
							for (String d : dos) {
								num += getNum(d);
							}
						} else {
							num = getNum(dos[1]);
						}
					} else {
						canCalculate = false;
					}
					Artikel art = pr.getArtikel();
					int ve = art.guessVE();
					if (ve != 0) {
						Money price = pr.getArtikel().getVKPreis();
						cost += num * price.getAmount() / ve;
					} else {
						canCalculate = false;
					}
				} catch (Exception ex) {
					ExHandler.handle(ex);
					canCalculate = false;
				}
				add(pr);
			}
			double rounded = Math.round(100.0 * cost) / 100.0;
			if (canCalculate) {
				lCost.setText(TTCOST + Double.toString(rounded));
			} else {
				if (rounded == 0.0) {
					lCost.setText(TTCOST + "?"); //$NON-NLS-1$
				} else {
					lCost.setText(TTCOST + ">" + Double.toString(rounded)); //$NON-NLS-1$
				}
			}
		}
	}
	
	private float getNum(String n){
		if (n.indexOf('/') != -1) {
			String[] bruch = n.split(StringConstants.SLASH);
			float zaehler = Float.parseFloat(bruch[0]);
			float nenner = Float.parseFloat(bruch[1]);
			return zaehler / nenner;
		} else {
			return Float.parseFloat(n);
		}
	}
	
	class DauerMediListener implements LDListener {
		IViewSite site;
		
		DauerMediListener(IViewSite s){
			site = s;
		}
		
		public void hyperlinkActivated(String l){
			try {
				if (l.equals(HINZU)) {
					site.getPage().showView(LeistungenView.ID);
					GlobalEvents.getInstance().setCodeSelectorTarget(target);
				} else if (l.equals(LISTE)) {
					
					RezeptBlatt rpb = (RezeptBlatt) site.getPage().showView(RezeptBlatt.ID);
					rpb.createEinnahmeliste(GlobalEvents.getSelectedPatient(), getAll().toArray(
						new Prescription[0]));
				} else if (l.equals(REZEPT)) {
					Rezept rp = new Rezept(GlobalEvents.getSelectedPatient());
					for (Prescription p : getAll().toArray(new Prescription[0])) {
						/*
						 * rp.addLine(new RpZeile("1",p.getArtikel().getLabel(),"",
						 * p.getDosis(),p.getBemerkung()));
						 */
						rp.addPrescription(new Prescription(p));
					}
					RezeptBlatt rpb = (RezeptBlatt) site.getPage().showView(RezeptBlatt.ID);
					rpb.createRezept(rp);
				} else if (l.equals(KOPIEREN)) {
					toClipBoard(true);
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
			
		}
		
		public String getLabel(Object o){
			if (o instanceof Prescription) {
				return ((Prescription) o).getLabel();
			}
			return o.toString();
		}
	}
	
	private void makeActions(){
		
		changeMedicationAction =
			new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY, Messages.getString("FixMediDisplay.Change")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
					setToolTipText(Messages.getString("FixMediDisplay.Modify")); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = (Prescription) getSelection();
					if (pr != null) {
						new MediDetailDialog(getShell(), pr).open();
						reload();
						redraw();
					}
				}
			};
		
		stopMedicationAction =
			new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY, Messages.getString("FixMediDisplay.Stop")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REMOVEITEM));
					setToolTipText(Messages.getString("FixMediDisplay.StopThisMedicament")); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = (Prescription) getSelection();
					if (pr != null) {
						remove(pr);
						pr.delete(); // this does not delete but stop the Medication. Sorry for
						// that
						reload();
					}
				}
			};
		
		removeMedicationAction =
			new RestrictedAction(AccessControlDefaults.DELETE_MEDICATION, Messages.getString("FixMediDisplay.Delete")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
					setToolTipText(Messages.getString("FixMediDisplay.DeleteUnrecoverable")); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = (Prescription) getSelection();
					if (pr != null) {
						remove(pr);
						pr.remove(); // this does, in fact, remove the medication from the
						// database
						reload();
					}
				}
			};
		
	}
	
}
