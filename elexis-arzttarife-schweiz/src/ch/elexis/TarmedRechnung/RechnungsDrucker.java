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
 * $Id: RechnungsDrucker.java 3942 2008-05-21 08:09:41Z rgw_ch $
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.tarmedprefs.PreferenceConstants;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.RnPrintView2;
import ch.rgw.tools.ExHandler;

public class RechnungsDrucker implements IRnOutputter{
	//Mandant actMandant;
	TarmedACL ta=TarmedACL.getInstance();
	RnPrintView2 rnp;
	IWorkbenchPage rnPage;
	//IProgressMonitor monitor;
	private Button bESR, bForms, bIgnoreFaults, bSaveFileAs;
	String dirname=Hub.localCfg.get(PreferenceConstants.RNN_EXPORTDIR, null);
	Text tName;
	
	public Result<Rechnung> doOutput(final IRnOutputter.TYPE type, final Collection<Rechnung> rechnungen) {
		
		rnPage=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res=new Result<Rechnung>();
		
		try{
			rnp=(RnPrintView2)rnPage.showView(RnPrintView2.ID);
			progressService.runInUI(
			      PlatformUI.getWorkbench().getProgressService(),
			      new IRunnableWithProgress() {
			         public void run(final IProgressMonitor monitor) {
			        	 monitor.beginTask(Messages.RechnungsDrucker_PrintingBills,rechnungen.size()*10);
			        	 int errors=0;
			        	 for(Rechnung rn:rechnungen){
			        		try{
				 				if(rnp.doPrint(rn,type, bSaveFileAs.getSelection() ? dirname+File.separator+rn.getNr()+".xml"
				 						: null, bESR.getSelection(),bForms.getSelection(), !bIgnoreFaults.getSelection(),monitor)==false){
				 					String errms=Messages.RechnungsDrucker_TheBill+rn.getNr()+Messages.RechnungsDrucker_Couldntbeprintef;
				 					res.add(Log.ERRORS, 1, errms, rn, true);
				 					errors++;
				 					continue;
				 				}
								int status_vorher=rn.getStatus();
				 				if( (status_vorher==RnStatus.OFFEN) ||
				 						(status_vorher==RnStatus.MAHNUNG_1) ||
				 						(status_vorher==RnStatus.MAHNUNG_2) ||
				 						(status_vorher==RnStatus.MAHNUNG_3)){
				 					rn.setStatus(status_vorher+1);
				 				}
				 				rn.addTrace(Rechnung.OUTPUT,getDescription()+": "+RnStatus.getStatusText(rn.getStatus()));
			        		}catch(Exception ex){
			        			String msg=ex.getMessage();
			        			if(msg==null){
			        				msg="interner Fehler";
			        			}
			        			SWTHelper.showError("Fehler beim Drucken der Rechnung "+rn.getNr(), msg);
			        			errors++;
			        		}
			 			}
			        	monitor.done();
			        	if(errors==0){
			        		SWTHelper.showInfo(Messages.RechnungsDrucker_PrintingFinished, Messages.RechnungsDrucker_AllFinishedNoErrors);
			        	}else{
			        		SWTHelper.showError(Messages.RechnungsDrucker_ErrorsWhilePrinting, Integer.toString(errors)+Messages.RechnungsDrucker_ErrorsWhiilePrintingAdvice);
			        	}
			         }
			      },
			      null);

			rnPage.hideView(rnp);

		}catch(Exception ex){
			ExHandler.handle(ex);
			res.add(Log.ERRORS,2,ex.getMessage(),null,true);
			ErrorDialog.openError(null,Messages.RechnungsDrucker_ErrorsWhilePrinting,Messages.RechnungsDrucker_CouldntOpenPrintView,
					res.asStatus());
			return res;
		}
		return res;
	}

	public String getDescription() {
		return Messages.RechnungsDrucker_PrintAsTarmed;
	}

	public Control createSettingsControl(final Composite parent){
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		bESR=new Button(ret,SWT.CHECK);
		bForms=new Button(ret,SWT.CHECK);
		bESR.setText(Messages.RechnungsDrucker_WithESR);
		bESR.setSelection(true);
		bForms.setText(Messages.RechnungsDrucker_WithForm);
		bForms.setSelection(true);
		bIgnoreFaults=new Button(ret,SWT.CHECK);
		bIgnoreFaults.setText(Messages.RechnungsDrucker_IgnoreFaults);
		bIgnoreFaults.setSelection(Hub.localCfg.get(PreferenceConstants.RNN_RELAXED, true));
		bIgnoreFaults.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Hub.localCfg.set(PreferenceConstants.RNN_RELAXED, bIgnoreFaults.getSelection());
			}
			
		});
		Group cSaveCopy=new Group(ret,SWT.NONE);
		cSaveCopy.setText("Datei für TrustCenter");
		cSaveCopy.setLayout(new GridLayout(2,false));
		bSaveFileAs=new Button(cSaveCopy,SWT.CHECK);
		bSaveFileAs.setText("auch als XML für TrustCenter speichern");
		bSaveFileAs.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		bSaveFileAs.setSelection(Hub.localCfg.get(PreferenceConstants.RNN_SAVECOPY, false));
		bSaveFileAs.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Hub.localCfg.set(PreferenceConstants.RNN_SAVECOPY, bSaveFileAs.getSelection());
			}
			
		});

		Button bSelectFile=new Button(cSaveCopy,SWT.PUSH);
		bSelectFile.setText("Verzeichnis:");
		bSelectFile.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog ddlg=new DirectoryDialog(parent.getShell());
				dirname=ddlg.open();
				if(dirname==null){
					SWTHelper.alert("Verzeichnisname fehlr", "Sie müssen ein existierendes Verzeichnis auswählen");
				}else{
					Hub.localCfg.set(PreferenceConstants.RNN_EXPORTDIR, dirname);
					tName.setText(dirname);
				}
			}
		});
	    tName=new Text(cSaveCopy,SWT.BORDER|SWT.READ_ONLY);
		tName.setText(Hub.localCfg.get(PreferenceConstants.RNN_EXPORTDIR, ""));
		return ret;
	}

	public boolean canStorno(final Rechnung rn) {
		// We do not need to react on cancel messages
		return false;
	}

	public boolean canBill(final Fall fall) {
		return true;
	}
}
