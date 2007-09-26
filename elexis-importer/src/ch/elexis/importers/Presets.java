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
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.Xid;
import ch.elexis.util.Log;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Some statically defined import methods (all from Excel-files)
 * @author gerry
 *
 */
public class Presets {
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID="elexis.ch/importRussi";
	private final static String PATID=IMPORT_XID+"/PatID";
	private static Log log=Log.get("Preset import");
	
	static{
		Xid.localRegisterXIDDomainIfNotExists(PATID, Xid.ASSIGNMENT_LOCAL);
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
			if(Xid.findObject(PATID, row[0])!=null){	// avoid duplicate import
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
			pat.addXid(PATID, row[0], false);
			Anschrift an=pat.getAnschrift();
			an.setStrasse(StringTool.getSafe(row,3));
			an.setPlz(StringTool.getSafe(row,4));
			an.setOrt(StringTool.getSafe(row,5));
			pat.setAnschrift(an);
			pat.set("Telefon1", StringTool.getSafe(row,6));
			pat.set("Natel", StringTool.getSafe(row,7));
			pat.set("Telefon2", StringTool.getSafe(row,8));
			if(!StringTool.isNothing(StringTool.getSafe(row,10))){
				Organisation org=new Organisation(row[10],"KK");
				Fall fall=pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), "KVG");
				fall.setRequiredContact("Kostenträger", org);
				fall.setGarant(pat);
			}
			if(!StringTool.isNothing(StringTool.getSafe(row,11))){
				Organisation org=new Organisation(row[11],"UVG");
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
