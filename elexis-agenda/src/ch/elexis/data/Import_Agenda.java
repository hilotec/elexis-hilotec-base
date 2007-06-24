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
 * $Id: Import_Agenda.java 1545 2007-01-06 14:20:57Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.Hub;
import ch.elexis.actions.Synchronizer;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;


public class Import_Agenda extends ImporterPage{
	public static final String drop=
	"DROP INDEX it ON AGNTERMINE;"+
	"DROP INDEX pattern ON AGNTERMINE;"+
	"DROP INDEX mandterm ON AGNTERMINE;"+
	"DROP TABLE AGNTERMINE;";
	ImporterPage.DBBasedImporter importer=null;
	Combo cbBereich;
	Button bSyncEnable,bDoDelete;
	Text tMandant;
	String orig_mandant;
	String dest_bereich;
	boolean bDelete=false;
	Hashtable<String,String> map;
	public Import_Agenda() {
		map=Synchronizer.getBereichMapping();
	}
	
	@Override
	public String getTitle() {
		return "JavaAgenda";
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		Result<JdbcLink> res=importer.getConnection();
		if(!res.isOK()){
			return res.asStatus();
		}
		JdbcLink j=res.get();
		int size=j.queryInt("SELECT COUNT(0) FROM agnTermine WHERE BEIWEM='"+orig_mandant+"' AND deleted<>'1'");
		monitor.beginTask("Importiere Agenda", size+100);
		Stm stm=j.getStatement();
		//Activator.getDefault().pinger.pause(true);
		Synchronizer.pause(true);
		if(bDelete){
			monitor.subTask("Lege Tabellen an");
			Termin.getConnection().execScript(new ByteArrayInputStream(drop.getBytes("UTF-8")), true, false);
			Termin.init();
		}
		monitor.worked(10);
		
		try{
			monitor.subTask("Importiere Datensätze");
			ResultSet rs=stm.query("SELECT * FROM agnTermine WHERE BEIWEM='"+orig_mandant+"' AND deleted<>'1'");
			Query<Patient> qPat=new Query<Patient>(Patient.class);
			int loop=0;
			while(rs.next()){
				if(++loop>100){
					loop=0;
					PersistentObject.clearCache();
					Thread.sleep(10);
				}
				int von=rs.getInt("Beginn");
				int dauer=rs.getInt("Dauer");
				int bis=von+dauer;
				Termin t=new Termin(rs.getString("ID"),dest_bereich,rs.getString("Tag"),von,bis,rs.getString("TerminTyp"),rs.getString("TerminStatus"));
				t.set(new String[]{"Grund","ErstelltWann","ErstelltVon"},
						rs.getString("Grund"), rs.getString("Angelegt"), 
						rs.getString("ErstelltVon"));
				
				String pers=rs.getString("Personalien");
				String[] px=Termin.findID(pers);
				List<Patient> list=qPat.queryFields(new String[]{"Name","Vorname","Geburtsdatum"}, px, true);
				if((list==null) || (list.size()!=1)){
					t.set("Wer", pers);
				}else{
					t.set("Wer", ((PersistentObject)list.get(0)).getId());
				}
				if(monitor.isCanceled()){
					monitor.done();
					Termin.getConnection().execScript(new ByteArrayInputStream(drop.getBytes("UTF-8")), true, false);
					SWTHelper.showError("Import abgebrochen", "Der Import wurde nicht durchgeführt");
					return Status.CANCEL_STATUS;
				}
				monitor.worked(1);
			}
			return Status.OK_STATUS;
		}catch(Exception ex){
			SWTHelper.showError("Fehler beim Import", ex.getMessage());
			ExHandler.handle(ex);
		}finally{
			j.releaseStatement(stm);
			monitor.done();
			//Activator.getDefault().pinger.pause(false);
			Synchronizer.pause(false);
		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public String getDescription() {
		return "Daten aus Java-Agenda importieren.";
	}
	

	@Override
	public void collect() {
		orig_mandant=tMandant.getText();
		dest_bereich=cbBereich.getText();
		map.put(dest_bereich, orig_mandant);
		Synchronizer.setBereichMapping(map);
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_ENABLED, bSyncEnable.getSelection());
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_TYPE, results[0]);
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_HOST, results[1]);
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_CONNECTOR, results[2]);
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_DBUSER, results[3]);
		Hub.globalCfg.set(PreferenceConstants.AG_SYNC_DBPWD, results[4]);
		bDelete=bDoDelete.getSelection();
	}

	@Override
	public Composite createPage(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		importer=new ImporterPage.DBBasedImporter(ret,this);
		importer.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Composite cMandant=new Composite(ret,SWT.BORDER);
		cMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cMandant.setLayout(new GridLayout(3,false));
		Composite cl=new Composite(cMandant,SWT.NONE);
		cl.setLayout(new GridLayout());
		new Label(cl,SWT.NONE).setText("Bereich in Elexis");
		cbBereich=new Combo(cl,SWT.SINGLE|SWT.READ_ONLY);
		cbBereich.setItems(Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, "Praxis").split(","));
		dest_bereich=cbBereich.getItem(0);
		cbBereich.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				map.put(dest_bereich, tMandant.getText());
				dest_bereich=cbBereich.getText();
				orig_mandant=map.get(dest_bereich);
				tMandant.setText(orig_mandant==null ? "" : orig_mandant);
			}
			
		});
		cbBereich.select(0);
		orig_mandant=map.get(dest_bereich);
		if(orig_mandant==null){
			orig_mandant="";
		}
		new Label(cMandant,SWT.NONE).setText("Entspricht");
		Composite cr=new Composite(cMandant,SWT.NONE);
		cr.setLayout(new GridLayout());
		new Label(cr,SWT.NONE).setText("Name in Agenda");
		tMandant=new Text(cr,SWT.BORDER);
		tMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tMandant.setText(orig_mandant);
		bDoDelete=new Button(ret,SWT.CHECK);
		bDoDelete.setText("Agenda-Daten löschen und neu anlegen");
		bSyncEnable=new Button(ret,SWT.CHECK);
		bSyncEnable.setText("Kontinuierlich synchronisieren");
		bSyncEnable.setSelection(Hub.globalCfg.get(PreferenceConstants.AG_SYNC_ENABLED, false));
		return ret;
	}


}
