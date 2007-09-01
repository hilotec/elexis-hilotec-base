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
 * $Id: RnActions.java 3054 2007-09-01 16:36:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.text.ParseException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
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
	Action increaseLevelAction;
	
    RnActions(final RechnungsListeView view){
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
   
}
