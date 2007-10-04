/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: RnActions.java 3237 2007-10-04 10:25:25Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.text.ParseException;
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
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.Tree;
import ch.elexis.views.FallDetailView;
import ch.elexis.views.PatientDetailView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Collection of bill-related actions
 * @author gerry
 *
 */
public class RnActions {
    Action rnExportAction, editCaseAction, delRnAction, reactivateRnAction, patDetailAction;
	Action expandAllAction,collapseAllAction, reloadAction, mahnWizardAction;
	Action addPaymentAction, addExpenseAction, changeStatusAction, stornoAction;
	Action increaseLevelAction, printListeAction;
	
    RnActions(final RechnungsListeView view){
    	
    	printListeAction=new Action("Liste drucken"){
    		{
    			setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINT));
    			setToolTipText("Die angezeigte Liste ausdrucken");
    		}
    		@Override
			public void run(){
    			Object[] sel=view.cv.getSelection();
    			//Tree[] elems=(Tree[])view.cv.getConfigurer().getContentProvider().getElements(null);
    			new RnListeDruckDialog(view.getViewSite().getShell(),sel).open();
    		}
    	};
    	mahnWizardAction=new Action("Mahnungen-Automatik"){
    		{
    			setToolTipText("Automatischer Mahnlauf gem. untenstehenden Angaben");
    			setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_WIZARD));
    		}
    		@Override
			public void run(){
    			if(!MessageDialog.openConfirm(view.getViewSite().getShell(), "Mahnlauf durchführen", 
    					"Möchten Sie wirklich einen automatischen Mahnlauf durchführen?\n(lässt sich nicht rückgängig machen)")){
    				return;
    			}
				Query<Rechnung> qbe=new Query<Rechnung>(Rechnung.class);
				qbe.add("RnStatus", "=", Integer.toString(RnStatus.OFFEN_UND_GEDRUCKT));
				qbe.add("MandantID", "=", Hub.actMandant.getId());
				TimeTool tt=new TimeTool();
				// Rechnung zu 1. Mahnung
				int days=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL1ST, 30);
				Money betrag=new Money();
				try{
					betrag=new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT1ST,"0.00"));
				}catch(ParseException ex){
					ExHandler.handle(ex);
					
				}
				tt.addHours(days*24*-1);
				qbe.add("RnDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
				List<Rechnung> list=qbe.execute();
				for(Rechnung rn:list){
					rn.setStatus(RnStatus.MAHNUNG_1);
					if(!betrag.isZero()){
						rn.addZahlung(betrag.multiply(-1.0), "Mahngebühr 1. Mahnung");
					}
				}
				// 1. Mahnung zu 2. Mahnung
				qbe.clear();
				qbe.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_1_GEDRUCKT));
				qbe.add("MandantID", "=", Hub.actMandant.getId());
				tt=new TimeTool();
				days=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL2ND, 10);
				try{
					betrag=new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT2ND,"0.00"));
				}catch(ParseException ex){
					ExHandler.handle(ex);
					betrag=new Money();
				}
				tt.addHours(days*24*-1);
				qbe.add("StatusDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
				list=qbe.execute();
				for(Rechnung rn:list){
					rn.setStatus(RnStatus.MAHNUNG_2);
					if(!betrag.isZero()){
						rn.addZahlung(betrag.multiply(-1.0), "Mahngebühr 2. Mahnung");
					}
				}
				// 2. Mahnung zu 3. Mahnung
				qbe.clear();
				qbe.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_2_GEDRUCKT));
				qbe.add("MandantID", "=", Hub.actMandant.getId());
				tt=new TimeTool();
				days=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL3RD, 10);
				try{
					betrag=new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT3RD,"0.00"));
				}catch(ParseException ex){
					ExHandler.handle(ex);
					betrag=new Money();
				}
				tt.addHours(days*24*-1);
				qbe.add("StatusDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
				list=qbe.execute();
				for(Rechnung rn:list){
					rn.setStatus(RnStatus.MAHNUNG_3);
					if(!betrag.isZero()){
						rn.addZahlung(betrag.multiply(-1.0), "Mahngebühr 2. Mahnung");
					}
				}
				view.cfp.clearValues();
				view.cfp.cbStat.setText(RnControlFieldProvider.stats[RnControlFieldProvider.stats.length-1]);
				view.cfp.fireChangedEvent();
			}
    	};
		rnExportAction=new Action(Messages.getString("RechnungsListeView.printAction")){ //$NON-NLS-1$
			{
				setToolTipText(Messages.getString("RechnungsListeView.printToolTip")); //$NON-NLS-1$
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EXPORT));
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				new RnOutputDialog(view.getViewSite().getShell(),list).open();
			}
		};

		patDetailAction=new Action("Patient details"){
			@Override
			public void run() {
				IWorkbenchPage rnPage=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try{
					/* PatientDetailView fdv=(PatientDetailView)*/rnPage.showView(PatientDetailView.ID);
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}
			
		};
		editCaseAction=new Action("Fall bearbeiten"){

			@Override
			public void run() {
				IWorkbenchPage rnPage=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try{
					/* FallDetailView fdv=(FallDetailView) */ rnPage.showView(FallDetailView.ID);
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}
			
		};
		delRnAction=new Action("Rechnung löschen"){
			@Override
			public void run() {
				List<Rechnung> list=view.createList();    				
				for(Rechnung rn:list){
					rn.storno(true);
				}
			}
		};
		reactivateRnAction=new Action("Rechnung wieder Aktivieren"){
			@Override
			public void run() {
				List<Rechnung> list=view.createList();    				
				for(Rechnung rn:list){
					rn.setStatus(RnStatus.OFFEN);
				}
			}
		};
		expandAllAction=new Action("Alle expandieren"){
			@Override
			public void run() {
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer)view.cv.getViewerWidget()).expandAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		collapseAllAction=new Action("Alle einklappen"){
			@Override
			public void run() {
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer)view.cv.getViewerWidget()).collapseAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		reloadAction=new Action("Neu einlesen"){
			{
				setToolTipText("Liste neu einlesen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_REFRESH));
			}
			@Override
			public void run() {
				view.cfp.fireChangedEvent();
			}
		};
		
		addPaymentAction=new Action("Buchung/Zahlung hinzufügen"){
			{
				setToolTipText("Einen Betrag als Zahlung eingeben");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_ADDITEM));
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				if(list.size()>0){
					Rechnung actRn=list.get(0);
					if(new RnDialogs.BuchungHinzuDialog(view.getViewSite().getShell(),actRn).open()==Dialog.OK){
						GlobalEvents.getInstance().fireObjectEvent(actRn, GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		
		addExpenseAction=new Action("Gebühr zuschlagen"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_REMOVEITEM));
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				if(list.size()>0){
					Rechnung actRn=list.get(0);
					if(new RnDialogs.GebuehrHinzuDialog(view.getViewSite().getShell(),actRn).open()==Dialog.OK){
						GlobalEvents.getInstance().fireObjectEvent(actRn, GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		
		changeStatusAction=new Action("Status ändern"){
			{
				setToolTipText("Manuelle Statusänderung (Vorsicht!");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				if(list.size()>0){
					Rechnung actRn=list.get(0);
					if(new RnDialogs.StatusAendernDialog(view.getViewSite().getShell(),actRn).open()==Dialog.OK){
						GlobalEvents.getInstance().fireObjectEvent(actRn, GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		stornoAction=new Action("Stornieren"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
				setToolTipText("Rechnung für ungültig erklären");
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				if(list.size()>0){
					Rechnung actRn=list.get(0);
					if(new RnDialogs.StornoDialog(view.getViewSite().getShell(),actRn).open()==Dialog.OK){
						GlobalEvents.getInstance().fireObjectEvent(actRn, GlobalEvents.CHANGETYPE.update);
					}
				}
			}
		};
		increaseLevelAction=new Action("Mahnstufe erhöhen"){
			{
				setToolTipText("Die Mahnstufe dieser Rechnung erhöhen");
			}
			@Override
			public void run(){
				List<Rechnung> list=view.createList();
				if(list.size()>0){
					Rechnung actRn=list.get(0);
					switch(actRn.getStatus()){
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
						SWTHelper.showInfo("Konnte Status nicht ändern", "Diese Rechnung hatte keinen automatisch erhöhbaren Status");
					}
				}
			
			}	
		};
	}
   
    class RnListeDruckDialog extends TitleAreaDialog implements ICallback{
		Object[] tree;		
		
		public RnListeDruckDialog(final Shell shell, final Object[] tree) {
			super(shell);
			this.tree=tree;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			TextContainer text=new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			text.createFromTemplateName(null, "Liste", Brief.UNKNOWN, Hub.actUser, "Rechnungen");
			String[][] table=new String[tree.length][];
			
			for(int i=0;i<tree.length;i++){
				table[i]=new String[2];
				Tree tr=(Tree)tree[i];
				if(tr.contents instanceof Rechnung){
					tr=tr.getParent();
				}
				if(tr.contents instanceof Fall){
					tr=tr.getParent();
				}
				Patient p=(Patient)tr.contents;
				StringBuilder sb=new StringBuilder();
				sb.append(p.getLabel());
				for(Tree tFall:(Tree[])tr.getChildren().toArray(new Tree[0])){
					Fall fall=(Fall)tFall.contents;
					sb.append("\n -- Fall: ").append(fall.getLabel());
					for(Tree tRn:(Tree[])tFall.getChildren().toArray(new Tree[0])){
						Rechnung rn=(Rechnung)tRn.contents;
						sb.append("\n -- -- Rechnung: ").append(rn.getLabel());
					}
				}
				table[i][0]=sb.toString();
			}
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
			text.getPlugin().insertTable("[Liste]", 0, table, new int[]{90,10});
			return ret;
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("Rechnungsliste");
			setTitle("Liste drucken");
			setMessage("Dies druckt alle aufgelisteten Patienten");
			getShell().setSize(900,700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), getShell());
		}

		@Override
		protected void okPressed() {
			super.okPressed();
		}

		public void save() {
			// TODO Auto-generated method stub
			
		}

		public boolean saveAs() {
			// TODO Auto-generated method stub
			return false;
		}
		
		
	}
}
