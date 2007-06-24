/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DatabaseCleaner.java 2327 2007-05-04 16:34:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.elexis.data.*;
import ch.rgw.tools.ExHandler;

/**
 * Die Datenbank aufräumen, ungültige Datensätze finden, ggf. Indices aufbauen
 * @author gerry
 *
 */
public class DatabaseCleaner {

	OutputStream osw;
	ArrayList<PersistentObject> purgeList=new ArrayList<PersistentObject>(200);	
	boolean purge;
	
	/**
	 * Neuen DatabaseCleaner erstellen
	 * @param os	Outputstream, in den die Reports geschrieben werden
	 * @param withPurge true, wenn fehlerhafte Einträge gelöscht werden sollen
	 */
	public DatabaseCleaner(OutputStream os, boolean withPurge){
		osw=os;
		purge=withPurge;
	}
	public void checkAll(){
		checkKonsultationen();
		checkRechnungen();
	}
	
	public void checkKonsultationen(){
		Query<Konsultation> qbe=new Query<Konsultation>(Konsultation.class);
		List<Konsultation> list=qbe.execute();
		
		for(Konsultation k:list){
			Fall fall=k.getFall();
			if(fall==null){
				blame(k,"Kein Fall für Konsultation");
				continue;
			}
			Mandant m=k.getMandant();
			if(m==null){
				blame(k,"Kein Mandant für Konsultation");
				continue;
			}
		}
		
	}
	public void checkRechnungen(){
		Query<Rechnung> qbe=new Query<Rechnung>(Rechnung.class);
		List<Rechnung> list=(List<Rechnung>)qbe.queryExpression("SELECT ID FROM RECHNUNGEN WHERE FallID is null", new LinkedList<Rechnung>());
		for(Rechnung rn:list){
			if(true){
				blame(rn,"Kein Fall für die Rechnung");
				Query<Konsultation> qk=new Query<Konsultation>(Konsultation.class);
				qk.add("RechnungsID", "=", rn.getId());
				List<Konsultation> lk=qk.execute();
				for(Konsultation k:lk){
					Fall f=k.getFall();
					Patient pat=f.getPatient();
					note("betrifft "+pat.getLabel()+", "+f.getLabel()+", "+k.getLabel());
				}
				if(purge){
					PersistentObject.getConnection().exec("UPDATE BEHANDLUNGEN SET RECHNUNGSID=NULL WHERE RECHNUNGSID="+rn.getWrappedId());
				}
			}
			
		}
	}
	void blame(PersistentObject o, String msg){
		try{
			osw.write(("\r\n"+msg+": "+o.getId()+", "+o.getLabel()+"\r\n").getBytes("iso-8859-1"));
			purgeList.add(o);
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
	}
	void note(String msg){
		try{
			osw.write(("  -- "+msg+"\r\n").getBytes("iso-8859-1"));
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
	}
	void doPurge(){
		if(purge){
			for(PersistentObject o:purgeList){
				o.delete();
			}
		}

	}
}
