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
 * $Id$
 *******************************************************************************/

package ch.elexis.importers;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.matchers.KontaktMatcher;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Some statically defined import methods (all from Excel-files)
 * @author gerry
 *
 */
public class Presets {
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID="elexis.ch/importPresets";
	private final static String KONTAKTID=IMPORT_XID+"/KID";
	private static Log log=Log.get("Preset import");
	
	static{
		Xid.localRegisterXIDDomainIfNotExists(KONTAKTID, "Frühere ID",Xid.ASSIGNMENT_LOCAL);
	}
	public static final boolean importUniversal(final ExcelWrapper exw, final IProgressMonitor moni){
		exw.setFieldTypes(new Class[]{Integer.class,Integer.class,String.class,String.class,String.class,String.class,
				TimeTool.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,
				String.class,String.class,Integer.class});
		int first=exw.getFirstRow();
		int last=exw.getLastRow();
		moni.beginTask("Import Kontaktdaten (Universalimporter)", last-first);
		int counter=0;
		for(int i=exw.getFirstRow()+1;i<=exw.getLastRow();i++){
			String[] row=exw.getRow(i).toArray(new String[0]);
			if(row==null){
				continue;
			}
			if(row.length!=17){
				continue;
			}
			
			String ID=StringTool.getSafe(row,0);
			String EAN=StringTool.getSafe(row, 17);
			if(StringTool.isNothing(ID)){
				ID=EAN; 		//EAN
			}
			if(StringTool.isNothing(ID)){
				SWTHelper.showError("Bad line format","ungültiger Eintrag", "ID oder EAN muss angegeben sein");
				continue;
			}
			if(Xid.findObject(KONTAKTID, ID)!=null){	// avoid duplicate import
				continue;
			}
			String typ=StringTool.getSafe(row,1);
			String titel=StringTool.getSafe(row, 2);
			String bez1=StringTool.getSafe(row,3);
			String bez2=StringTool.getSafe(row,4);
			String zusatz=StringTool.getSafe(row, 5);
			String strasse=StringTool.getSafe(row, 13);
			String plz=StringTool.getSafe(row, 14);
			String ort=StringTool.getSafe(row, 15);
			String natel=StringTool.getSafe(row, 12);
			Kontakt k=null;
			if(StringTool.isNothing(typ)|| typ.equals("0")){
				k=KontaktMatcher.findOrganisation(bez1, strasse, plz, ort, true);
				k.set("Zusatz1", bez2);
				k.set("Bezeichnung3", zusatz);
			}else{
				String sex=StringTool.getSafe(row, 7);
				String gebdat=StringTool.getSafe(row, 6);
				k=KontaktMatcher.findPerson(bez1, bez2, gebdat, sex, strasse, plz, ort, natel, true);
				k.set("Titel", titel);
				k.set("Zusatz", zusatz);
			}
			moni.subTask(k.getLabel());
			k.set(new String[]{"E-Mail","Website","Telefon1","Telefon2","Natel","Strasse","Plz","Ort","Anschrift"},
					StringTool.getSafe(row, 8),
					StringTool.getSafe(row, 9),
					StringTool.getSafe(row, 10),
					StringTool.getSafe(row, 11),
					natel,
					strasse,
					plz,
					ort,
					StringTool.getSafe(row, 16));
			k.addXid(Xid.DOMAIN_EAN, EAN, true);
			k.addXid(KONTAKTID, ID, false);
			moni.worked(1);
			if(moni.isCanceled()){
				return false;
			}
			if(counter++>200){
				PersistentObject.clearCache();
				System.gc();
				try{
					Thread.sleep(100);
				}catch(Exception ex){
					// no worries
				}
				counter=0;
			}
		}
		moni.done();
		return true;
	}
	public static boolean importRussi(final ExcelWrapper exw, final IProgressMonitor moni){
		exw.setFieldTypes(new Class[]{
				Integer.class,String.class,TimeTool.class,String.class,
				Integer.class,String.class,String.class,String.class,
				String.class,String.class,String.class,String.class,String.class
		});
		int first=exw.getFirstRow();
		int last=exw.getLastRow();
		moni.beginTask("Import Patientendaten Russi", last-first);
		for(int i=first+1;i<last;i++){
			String[] row=exw.getRow(i).toArray(new String[0]);
			if(Xid.findObject(KONTAKTID, row[0])!=null){	// avoid duplicate import
				continue;
			}
			String[] name=StringTool.getSafe(row,1).split("\\s",2);
			String gdraw=StringTool.getSafe(row, 2);
			String gebdat=new TimeTool(gdraw).toString(TimeTool.DATE_GER);
			String gender=StringTool.getSafe(row,9).startsWith("W") ? "w" : "m";
			Patient pat=new Patient(name[0],name.length>1 ? name[1] : "-",gebdat,gender);
			String patcode=new StringBuilder().append(pat.getLabel()).append(pat.getPatCode()).toString();
			moni.subTask(patcode);
			log.log(patcode, Log.INFOS);
			pat.addXid(KONTAKTID, row[0], false);
			Anschrift an=pat.getAnschrift();
			an.setStrasse(StringTool.getSafe(row,3));
			an.setPlz(StringTool.getSafe(row,4));
			an.setOrt(StringTool.getSafe(row,5));
			pat.setAnschrift(an);
			pat.set("Telefon1", StringTool.getSafe(row,6));
			pat.set("Natel", StringTool.getSafe(row,7));
			pat.set("Telefon2", StringTool.getSafe(row,8));
			if(!StringTool.isNothing(StringTool.getSafe(row,10))){
				Organisation org=KontaktMatcher.findOrganisation(row[10], "", "", "", true);
				Fall fall=pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), "KVG");
				fall.setRequiredContact("Kostenträger", org);
				fall.setGarant(pat);
			}
			if(!StringTool.isNothing(StringTool.getSafe(row,11))){
				Organisation org=KontaktMatcher.findOrganisation(row[11], "", "", "", true);
				Fall fall=pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), "UVG");
				fall.setRequiredContact("Kostenträger", org);
				fall.setGarant(org);
			}
			moni.worked(1);
		}
		moni.done();
		return true;
	}
	
	
}
