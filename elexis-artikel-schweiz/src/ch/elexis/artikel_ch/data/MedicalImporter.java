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
 *  $Id: MedicalImporter.java 1742 2007-02-06 20:48:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_ch.data;


import java.io.*;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;

public class MedicalImporter extends ImporterPage {
	Button bClear;
	boolean bDelete;
	
	public MedicalImporter() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		File file=new File(results[0]);
		long l=file.length();
		InputStreamReader ir=new InputStreamReader(new FileInputStream(file),"iso-8859-1");
		BufferedReader br=new BufferedReader(ir);
		
		String in;
		monitor.beginTask("Medical Import",(int)(l/100));
		String mode=" (Modus: Daten ergänzen/update)";
		if(bDelete==true){
			if(MessageDialog.openConfirm(null,"Wirklich Daten löschen","Achtung: Wenn die alten Daten gelöscht werden, kann es\nsein, dass bestehende Bezüge ungültig werden.")==true){
				PersistentObject.getConnection().exec("DELETE FROM ARTIKEL WHERE TYP='Medical'");
				mode=" (Modus: Alles neu erstellen)";
			}
		}
		monitor.beginTask("Medical Import"+mode,(int)(l/100));
		
		Query<Artikel> qbe=new Query<Artikel>(Artikel.class);
		while((in=br.readLine())!=null){
			/*String s1=in.substring(0,3);		*/	// ??
			String pk=in.substring(3,10);			// Pharmacode
			String titel=in.substring(10,60).trim();// Text
			String ek=in.substring(60,66).trim();	// EK-Preis
			String vk=in.substring(66,72).trim();	// VK-Preis
			String kasse=in.substring(72,75);		// Kassentyp
			String rp=in.substring(75,76);			// Rezeptpflicht
			String hix=in.substring(76,83);			// Hersteller-Index
			String ean=in.substring(83,96);			// EAN
			String mwst=in.substring(96,97);		// MWSt-Typ
			monitor.worked(1);
			if(mwst.equals("2")){
				continue;
			}
			String id=qbe.findSingle("SubID", "=", pk);
			Artikel a=Artikel.load(id);
			if(a==null){
				id=qbe.findSingle("Name","=",titel);
				a=Artikel.load(id);
				if(a==null){
					a=new Artikel(titel,"Medical",pk);
				}else{
					a.set("SubID", pk);
				}
			}
			Hashtable ext=a.getHashtable("ExtInfo");
			String[] fields={"EK_Preis","VK_Preis"};
			a.set(fields,ek,vk);
			ext.put("Pharmacode",pk);
			ext.put("Kassentyp",kasse);
			ext.put("Rezeptpflicht",rp);
			ext.put("Hersteller",hix);
			ext.put("EAN",ean);
			ext.put("MWSt-Typ",mwst);
			a.setHashtable("ExtInfo",ext);
			if(monitor.isCanceled()){
				monitor.done();
				return Status.CANCEL_STATUS;
			}
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public String getTitle() {
		return "Medical Import";
	}
	@Override
	public String getDescription(){
		return "Bitte wählen Sie die Datei (RA-11-Format) aus, aus der die Artikel importiert werden sollen";
	}

	@Override
	public Composite createPage(final Composite parent) {
		Composite ret=new ImporterPage.FileBasedImporter(parent,this);
		ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		bClear=new Button(parent,SWT.CHECK|SWT.WRAP);
		bClear.setText("Alle Daten vorher löschen (VORSICHT! Bitte Anleitung beachten)");
		bClear.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		return ret;
	}
	@Override
	public void collect() {
		bDelete=bClear.getSelection();
		super.collect();
	}
	
}
