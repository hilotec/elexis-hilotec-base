/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: RechnungsDrucker.java 2835 2007-07-18 16:55:27Z rgw_ch $
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.data.Fall;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.RnPrintView;
import ch.rgw.tools.ExHandler;

public class RechnungsDrucker implements IRnOutputter{
	Mandant actMandant;
	TarmedACL ta=TarmedACL.getInstance();
	RnPrintView rnp;
	IWorkbenchPage rnPage;
	IProgressMonitor monitor;
	private Button bESR, bForms, bIgnoreFaults;
	
	public Result<Rechnung> doOutput(final IRnOutputter.TYPE type, final Collection<Rechnung> rechnungen) {
		
		rnPage=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res=new Result<Rechnung>();
		
		try{
			rnp=(RnPrintView)rnPage.showView(RnPrintView.ID);
			progressService.runInUI(
			      PlatformUI.getWorkbench().getProgressService(),
			      new IRunnableWithProgress() {
			         public void run(final IProgressMonitor monitor) {
			        	 monitor.beginTask(Messages.RechnungsDrucker_PrintingBills,rechnungen.size()*10);
			        	 int errors=0;
			        	 for(Rechnung rn:rechnungen){
			        		 
			 				if(rnp.doPrint(rn,type,bESR.getSelection(),bForms.getSelection(), !bIgnoreFaults.getSelection(),monitor)==false){
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
			 				rn.addTrace(Rechnung.OUTPUT,getDescription()+": "+RnStatus.Text[rn.getStatus()]);
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
