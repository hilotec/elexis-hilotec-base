/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TrustXTransmit.java 2522 2007-06-14 19:53:08Z rgw_ch $
 *******************************************************************************/

package ch.elexis.trustx;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.Hub;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.TrustCenters;
import ch.elexis.tarmedprefs.PreferenceConstants;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Transmit bills directly to a 'TrustCenter' via TrustX-Module. 
 * Note: This works only on Windows, because TrustX and ASAS are Closed-Source 
 * COM-DLL's
 * Prerequisites: ASAS installed and configured, Trustx installed and configured.
 * Dependencies: elexis-arzttarife-schweiz
 * @author Gerry
 *
 */
public class TrustXTransmit implements IRnOutputter{
	Combo cbTC, cbASAS;
	ITrustx trustx;
	ICode icode;
	String inputdir;
	TrustXLog xlog;
	//private Log log=Log.get("TrustX");
	
	/**
	 * Export and transmit all bills contained in rnn
	 * @param asCopy true to mar bills as "copy" or "resend"
	 * @param rnn Collection of bills to transmit
	 * @return Result containing all erroneous bills.
	 */
	public Result<Rechnung> doOutput(final IRnOutputter.TYPE type, final Collection<Rechnung> rnn) {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res=new Result<Rechnung>();
		final String tc=cbTC.getText();
		if(StringTool.isNothing(tc)){
			SWTHelper.alert("Kein Truscenter", "Bitte wählen Sie ein TrustCenter aus der Liste (TC Test für Tests)");
			return res;
		}
		trustx.trustCenter(tc);
		trustx.asasLogin(cbASAS.getText());
		Hub.mandantCfg.set(PreferenceCostants.TRUSTX_ASASLOGIN, cbASAS.getText());
		
		try{
			progressService.runInUI(
			      PlatformUI.getWorkbench().getProgressService(),
			      new IRunnableWithProgress() {
			         public void run(IProgressMonitor monitor) {
		        	 monitor.beginTask("Exportiere Rechnungen...",rnn.size()*10);
		        	 int errors=0;
		     		 for(Rechnung rn:rnn){
		     			xlog.init();
		     			monitor.worked(1);
		    			XMLExporter ex=new XMLExporter();
		    			ex.doExport(rn, inputdir+File.separator+rn.getNr()+".xml", type,true);
		    			monitor.worked(2);
		    			if(rn.getStatus()==RnStatus.FEHLERHAFT){
		    				errors++;
		    				continue;
		    			}
		    			trustx.auto();
		    			monitor.worked(5);
		    			boolean status=xlog.read();
		    			monitor.worked(2);
		    			if(status){
		    				rn.addTrace(Rechnung.OUTPUT,"An TrustCenter "+trustx.trustCenter());
    						continue;
		    			}
		    			rn.reject(RnStatus.REJECTCODE.REJECTED_BY_PEER, xlog.getLastErrorString());
		    			res.add(Log.ERRORS, 1, "Fehler beim Übertragen: "+xlog.getLastErrorString(), rn, true);
		    			errors++;
		    		 }
		        	monitor.done();
		        	if(errors>0){
		        		SWTHelper.alert("Fehler bei der Übermittlung", Integer.toString(errors)+" Rechnungen waren fehlerhaft. Sie können diese unter Rechnungen mit dem Status fehlerhaft aufsuchen und korrigieren");
		        	}else{
		        		SWTHelper.showInfo("Übermittlung beendet", "Es sind keine Fehler aufgetreten");
		        	}
		         }
		      },
		      null);
		}catch(Exception ex){
			ExHandler.handle(ex);
			res.add(Log.ERRORS,2,ex.getMessage(),null,true);
			ErrorDialog.openError(null,"Fehler beim Drucken","Konnte Drucker-View nicht starten",
					res.asStatus());
			return res;
		}
		return res;
	}

		
	
		public String getDescription() {
		return "Übermittlung via TrustX";
	}

	public Control createSettingsControl(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		Label trustxver=new Label(ret, SWT.NONE);
		Label asasver=new Label(ret,SWT.NONE);
		trustxver.setText("TrustX nicht gefunden oder falsch konfiguriert");
		asasver.setText("ASAS nicht vorhanden oder nicht gestartet");
		cbASAS=new Combo(ret,SWT.READ_ONLY);
		try{
			trustx=ClassFactory.createCTrustx();
			String tv=trustx.trustxVersion();
			trustxver.setText("TrustX Version "+tv);
			inputdir=trustx.inputDirectory();
			String base=trustx.workDirectory();
			String session=trustx.session();
			if(StringTool.isNothing(session)){
				session=new TimeTool().toString(TimeTool.DATE_ISO);
			}
			asasver.setText(trustx.asasVersion());
			IAsasCollection asasLogins=trustx.asasLogins();
			int iLogins=asasLogins.count();
			for(int i=0;i<iLogins;i++){
				Object o=asasLogins.item(i+1);
				cbASAS.add(o.toString());
			}
			String def="";
			if(iLogins>0){
				def=cbASAS.getItem(0);
			}
			cbASAS.setText(Hub.mandantCfg.get(PreferenceCostants.TRUSTX_ASASLOGIN,def));
			xlog=new TrustXLog(base+File.separator+"logs"+File.separator+session+".log");

		}catch(Throwable ex){
			ExHandler.handle(ex);
		}
		cbTC=new Combo(ret,SWT.READ_ONLY);
		cbTC.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		for(String s:TrustCenters.getTCList()){
			cbTC.add(s);
		}
		cbTC.setText(Hub.localCfg.get(PreferenceConstants.TARMEDTC,"TC test"));
		cbTC.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Hub.localCfg.set(PreferenceConstants.TARMEDTC, cbTC.getText());
			}
			
		});
		return ret;
	}

}
