/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: RnActions.java 5024 2009-01-23 16:36:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.commands.Handler;
import ch.elexis.commands.MahnlaufCommand;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.FallDetailView;
import ch.elexis.views.PatientDetailView2;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Tree;

/**
 * Collection of bill-related actions
 * 
 * @author gerry
 * 
 */
public class RnActions {
	Action rnExportAction, editCaseAction, delRnAction, reactivateRnAction, patDetailAction;
	Action expandAllAction, collapseAllAction, reloadAction, mahnWizardAction;
	Action addPaymentAction, addExpenseAction, changeStatusAction, stornoAction;
	Action increaseLevelAction, printListeAction, rnFilterAction;
	Action addAccountExcessAction;
	
	RnActions(final RechnungsListeView view){
		
		printListeAction = new Action("Liste drucken") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Die angezeigte Liste ausdrucken");
			}
			
			@Override
			public void run(){
				Object[] sel = view.cv.getSelection();
				new RnListeDruckDialog(view.getViewSite().getShell(), sel).open();
			}
		};
		mahnWizardAction = new Action("Mahnungen-Automatik") {
			{
				setToolTipText("Automatischer Mahnlauf gem. untenstehenden Angaben");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_WIZARD));
			}
			
			@Override
			public void run(){
				if (!MessageDialog
					.openConfirm(
						view.getViewSite().getShell(),
						"Mahnlauf durchführen",
						"Möchten Sie wirklich einen automatischen Mahnlauf durchführen?\n(lässt sich nicht rückgängig machen)")) {
					return;
				}
				Handler.execute(view.getViewSite(), MahnlaufCommand.ID, null);
				view.cfp.clearValues();
				view.cfp.cbStat
					.setText(RnControlFieldProvider.stats[RnControlFieldProvider.stats.length - 2]);
				view.cfp.fireChangedEvent();
			}
		};
		rnExportAction = new Action(Messages.getString("RechnungsListeView.printAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RechnungsListeView.printToolTip")); //$NON-NLS-1$
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_GOFURTHER));
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					new RnOutputDialog(view.getViewSite().getShell(), list).open();
				}
			};
		
		patDetailAction = new Action("Patient details") {
			@Override
			public void run(){
				IWorkbenchPage rnPage =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					/* PatientDetailView fdv=(PatientDetailView) */rnPage
						.showView(PatientDetailView2.ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
			
		};
		editCaseAction = new Action("Fall bearbeiten") {
			
			@Override
			public void run(){
				IWorkbenchPage rnPage =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					/* FallDetailView fdv=(FallDetailView) */rnPage.showView(FallDetailView.ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
			
		};
		delRnAction = new Action("Rechnung löschen") {
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				for (Rechnung rn : list) {
					rn.storno(true);
				}
			}
		};
		reactivateRnAction = new Action("Rechnung wieder Aktivieren") {
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				for (Rechnung rn : list) {
					rn.setStatus(RnStatus.OFFEN);
				}
			}
		};
		expandAllAction = new Action("Alle expandieren") {
			@Override
			public void run(){
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer) view.cv.getViewerWidget()).expandAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		collapseAllAction = new Action("Alle einklappen") {
			@Override
			public void run(){
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer) view.cv.getViewerWidget()).collapseAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		reloadAction = new Action("Neu einlesen") {
			{
				setToolTipText("Liste neu einlesen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REFRESH));
			}
			
			@Override
			public void run(){
				view.cfp.fireChangedEvent();
			}
		};
		
		addPaymentAction = new Action("Buchung/Zahlung hinzufügen") {
			{
				setToolTipText("Einen Betrag als Zahlung eingeben");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
			}
			
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.BuchungHinzuDialog(view.getViewSite().getShell(), actRn)
						.open() == Dialog.OK) {
						GlobalEvents.getInstance().fireObjectEvent(actRn,
							GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		
		addExpenseAction = new Action("Gebühr zuschlagen") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REMOVEITEM));
			}
			
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.GebuehrHinzuDialog(view.getViewSite().getShell(), actRn)
						.open() == Dialog.OK) {
						GlobalEvents.getInstance().fireObjectEvent(actRn,
							GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		
		changeStatusAction =
			new RestrictedAction(AccessControlDefaults.ADMIN_CHANGE_BILLSTATUS_MANUALLY,
				"Status ändern") {
				{
					setToolTipText("Manuelle Statusänderung (Vorsicht!");
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				}
				
				@Override
				public void doRun(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
						if (new RnDialogs.StatusAendernDialog(view.getViewSite().getShell(), actRn)
							.open() == Dialog.OK) {
							GlobalEvents.getInstance().fireObjectEvent(actRn,
								GlobalEvents.CHANGETYPE.update);
						}
					}
				}
			};
		stornoAction = new Action("Stornieren") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText("Rechnung für ungültig erklären");
			}
			
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.StornoDialog(view.getViewSite().getShell(), actRn).open() == Dialog.OK) {
						GlobalEvents.getInstance().fireObjectEvent(actRn,
							GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		increaseLevelAction = new Action("Mahnstufe erhöhen") {
			{
				setToolTipText("Die Mahnstufe dieser Rechnung erhöhen");
			}
			
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					switch (actRn.getStatus()) {
					case RnStatus.OFFEN_UND_GEDRUCKT:
						actRn.setStatus(RnStatus.MAHNUNG_1);
						break;
					case RnStatus.MAHNUNG_1_GEDRUCKT:
						actRn.setStatus(RnStatus.MAHNUNG_2);
						break;
					case RnStatus.MAHNUNG_2_GEDRUCKT:
						actRn.setStatus(RnStatus.MAHNUNG_3);
						break;
					default:
						SWTHelper.showInfo("Konnte Status nicht ändern",
							"Diese Rechnung hatte keinen automatisch erhöhbaren Status");
					}
				}
				
			}
		};
		addAccountExcessAction = new Action("Positives Kontoguthaben zuweisen") {
			{
				setToolTipText("Dieser Rechnung ein positives Kontoguthaben zuweisen");
			}
			
			@Override
			public void run(){
				List<Rechnung> list = view.createList();
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					
					// Allfaelliges Guthaben des Patienten der Rechnung als Anzahlung hinzufuegen
					Fall fall = actRn.getFall();
					Patient patient = fall.getPatient();
					Money prepayment = patient.getAccountExcess();
					if (prepayment.getCents() > 0) {
						// make sure prepayment is not bigger than amount of bill
						Money amount;
						if (prepayment.getCents() > actRn.getBetrag().getCents()) {
							amount = new Money(actRn.getBetrag());
						} else {
							amount = new Money(prepayment);
						}
						
						if (SWTHelper
							.askYesNo(
								"Positives Kontoguthaben",
								"Das Konto von Patient \""
									+ patient.getLabel()
									+ "\" weist ein positives Kontoguthaben auf. Wollen Sie den Betrag von "
									+ amount.toString() + " dieser Rechnung \"" + actRn.getNr()
									+ ": " + fall.getLabel() + "\" zuweisen?")) {
							
							// remove amount from account and transfer it to the bill
							Money accountAmount = new Money(amount);
							accountAmount.negate();
							new AccountTransaction(patient, null, accountAmount, null,
								"Anzahlung von Kontoguthaben auf Rechnung " + actRn.getNr());
							actRn.addZahlung(amount, "Anzahlung von Kontoguthaben");
						}
					}
				}
			}
		};
		rnFilterAction = new Action("Liste filtern", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
				setToolTipText("Bedingungen für die Anzeige eingeben");
			}
			
			@Override
			public void run(){
				if (isChecked()) {
					RnFilterDialog rfd = new RnFilterDialog(view.getViewSite().getShell());
					if (rfd.open() == Dialog.OK) {
						view.cntp.setConstraints(rfd.ret);
						view.cfp.fireChangedEvent();
					}
				} else {
					view.cntp.setConstraints(null);
					view.cfp.fireChangedEvent();
				}
				
			}
		};
	}
	
	static class RnListeDruckDialog extends TitleAreaDialog implements ICallback {
		ArrayList<Rechnung> rnn;
		
		public RnListeDruckDialog(final Shell shell, final Object[] tree){
			super(shell);
			rnn = new ArrayList<Rechnung>(tree.length);
			for (Object o : tree) {
				if (o instanceof Tree) {
					Tree tr = (Tree) o;
					if (tr.contents instanceof Rechnung) {
						tr = tr.getParent();
					}
					if (tr.contents instanceof Fall) {
						tr = tr.getParent();
					}
					if (tr.contents instanceof Patient) {
						for (Tree tFall : (Tree[]) tr.getChildren().toArray(new Tree[0])) {
							Fall fall = (Fall) tFall.contents;
							for (Tree tRn : (Tree[]) tFall.getChildren().toArray(new Tree[0])) {
								Rechnung rn = (Rechnung) tRn.contents;
								rnn.add(rn);
							}
						}
					}
				}
			}
			
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			TextContainer text = new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			text.createFromTemplateName(null, "Liste", Brief.UNKNOWN, Hub.actUser, "Rechnungen");
			text.getPlugin().insertText("[Titel]",
				"Rechnungsliste gedruckt am " + new TimeTool().toString(TimeTool.DATE_GER) + "\n",
				SWT.CENTER);
			String[][] table = new String[rnn.size() + 1][];
			Money sum = new Money();
			int i;
			for (i = 0; i < rnn.size(); i++) {
				Rechnung rn = rnn.get(i);
				table[i] = new String[3];
				StringBuilder sb = new StringBuilder();
				Fall fall = rn.getFall();
				Patient p = fall.getPatient();
				table[i][0] = rn.getNr();
				sb.append(p.getLabel()).append(" - ").append(fall.getLabel());
				table[i][1] = sb.toString();
				Money betrag = rn.getBetrag();
				sum.addMoney(betrag);
				table[i][2] = betrag.getAmountAsString();
			}
			table[i] = new String[3];
			table[i][0] = "";
			table[i][1] = "Summe";
			table[i][2] = sum.getAmountAsString();
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
			text.getPlugin().insertTable("[Liste]", 0, table, new int[] {
				10, 80, 10
			});
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText("Rechnungsliste");
			setTitle("Liste drucken");
			setMessage("Dies druckt alle aufgelisteten Patienten");
			getShell().setSize(900, 700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getShell());
		}
		
		@Override
		protected void okPressed(){
			super.okPressed();
		}
		
		public void save(){
		// TODO Auto-generated method stub
		
		}
		
		public boolean saveAs(){
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
