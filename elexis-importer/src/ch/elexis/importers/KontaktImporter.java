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
 *  $Id: KontaktImporter.java 2158 2007-03-22 15:10:35Z rgw_ch $
 *******************************************************************************/
package ch.elexis.importers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;

public class KontaktImporter extends ImporterPage {
	KontaktImporterBlatt importer;
	
	public KontaktImporter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		importer = new KontaktImporterBlatt(parent);
		importer.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return importer;
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		if(importer.doImport()){
			return Status.OK_STATUS;
		}
		return new Status(Status.ERROR,"ch.elexis.import.div",1,"Fehler beim Import",null);
	}

	@Override
	public String getDescription() {
		return "Import von Kontaktdaten aus verschiedener Quelle";
	}

	@Override
	public String getTitle() {
		return "Kontakte";
	}

	static Kontakt queryKontakt(String name,String vorname, String strasse, String plz, String ort, boolean createIfMissing){
		Query<Kontakt> qbe=new Query<Kontakt>(Kontakt.class);
		List<Kontakt> res=qbe.queryFields(new String[]{"Bezeichnung1","Bezeichnung2","Strasse","Plz","Ort"}, 
				new String[]{name,vorname,strasse,plz,ort}, false);
		if((res!=null) && (res.size()>0)) 			
		{
			Kontakt found=res.get(0);
			StringBuilder s1=new StringBuilder();
			StringBuilder s2=new StringBuilder();
			s1.append(found.get("Bezeichnung1")).append(", ").append(found.get("Bezeichnung2"))
				.append(" - ").append(found.get("Strasse")).append(" ")
				.append(found.get("Plz")).append(" ").append(found.get("Ort"));
			
			s2.append(name).append(", ").append(vorname).append(" - ")
				.append(strasse).append(" ").append(plz).append(" ").append(ort);
			
			if(SWTHelper.askYesNo("Kontakte identisch", "Bezeichnen \n"+s1.toString()+" und\n"+s1.toString()+"\ndenselben Kontakt?")){
				return found;
			}
		}
		return null;
	}
}
