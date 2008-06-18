/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PatFilterImpl.java 4049 2008-06-18 17:36:53Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import com.sun.org.apache.regexp.internal.RE;

import ch.elexis.data.Artikel;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Etikette;
import ch.elexis.data.Fall;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.NamedBlob2;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.Script;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.PatListFilterBox.IPatFilter;
import ch.rgw.tools.ExHandler;

/**
 * Default implementation of IPatFilter. Will be called after all other filters
 * returned DONT_HANDLE
 * @author Gerry
 
 */
public class PatFilterImpl implements IPatFilter {

	public int accept(Patient p, PersistentObject o){
		if(o instanceof Kontakt){
			Query<BezugsKontakt> qbe=new Query<BezugsKontakt>(BezugsKontakt.class);
			qbe.add("myID", "=", p.getId());
			qbe.add("otherID", "=", o.getId());
			if(qbe.execute().size()>0){
				return ACCEPT;
			}
			return REJECT;
		}else if(o instanceof IVerrechenbar){
			IVerrechenbar iv=(IVerrechenbar)o;
			Fall[] faelle=p.getFaelle();
			for(Fall fall:faelle){
				Konsultation[] konsen=fall.getBehandlungen(false);
				for(Konsultation k:konsen){
					List<Verrechnet> lv=k.getLeistungen();
					for(Verrechnet v:lv){
						if(v.getVerrechenbar().equals(iv)){
							return ACCEPT;
						}
					}
				}
			}
			return REJECT;

		}else if(o instanceof IDiagnose){
			IDiagnose diag=(IDiagnose)o;
			Fall[] faelle=p.getFaelle();
			for(Fall fall:faelle){
				Konsultation[] konsen=fall.getBehandlungen(false);
				for(Konsultation k:konsen){
					List<IDiagnose> id=k.getDiagnosen();
					if(id.contains(diag)){
						return ACCEPT;
					}
				}
			}
			return REJECT;
		}else if(o instanceof Artikel){
			Query<Prescription> qbe=new Query<Prescription>(Prescription.class);
			qbe.add("PatientID", "=", p.getId());
			qbe.add("ArtikelID", "=",o.getId());
			if(qbe.execute().size()>0){
				return ACCEPT;
			}
			return REJECT;
		}else if(o instanceof Prescription){
			Artikel art=((Prescription)o).getArtikel();
			Query<Prescription> qbe=new Query<Prescription>(Prescription.class);
			qbe.add("PatientID", "=", p.getId());
			qbe.add("ArtikelID", "=", art.getId());
			if(qbe.execute().size()>0){
				return ACCEPT;
			}
			return REJECT;
		}else if(o instanceof Etikette){
			List<Etikette> etis=p.getEtiketten();
			Etikette e=(Etikette)o;
			if(etis.contains(e)){
				return ACCEPT;
			}
			return REJECT;
		}else if (o instanceof NamedBlob){
			NamedBlob nb=(NamedBlob)o;
			String[] val=nb.getString().split("::");
			String test=p.get(val[0]);
			if(test==null){
				return DONT_HANDLE;
			}
			String op=val[1];
			if(op.equals("=")){
				return test.equalsIgnoreCase(val[2]) ? ACCEPT : REJECT;
			}else if(op.equals("LIKE")){
				return test.toLowerCase().contains(val[2].toLowerCase()) ? ACCEPT : REJECT;
			}else if(op.equals("Regexp")){
				return test.matches(val[2]) ? ACCEPT : REJECT;
			}
		}else if(o instanceof Script){
			Object ret;
			try {
				Script script=(Script)o;
				script.setVariable("patient", p);
				ret = script.execute(p);
				if(ret instanceof Integer){
					return (Integer)ret;
				}

			} catch (Exception e) {
				return FILTER_FAULT;
			}
		}
		return DONT_HANDLE;
	}

	public boolean aboutToStart(PersistentObject filter) {
		if(filter instanceof Script){
			try {
				((Script)filter).init();
				return true;
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Initialisieren des Scripts", e.getMessage());
			}
		}
		return false;

	}

	public boolean finished(PersistentObject filter) {
		if(filter instanceof Script){
			try {
				((Script)filter).finished();
				return true;
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Abschluss des Scripts", e.getMessage());
			}
		}
		return false;
	}

	
}
