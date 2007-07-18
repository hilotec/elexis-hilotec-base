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
 * $Id: Validator.java 2838 2007-07-18 17:44:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.StringTool;

public class Validator {
	
	public Result<Rechnung> checkBill(final XMLExporter xp, final Result<Rechnung> res){
		Rechnung rn=xp.rn;
		Kontakt m=rn.getMandant();
		if(rn.getStatus()>RnStatus.OFFEN){
			return res;	// Wenn sie eh schon gedrcukt war machen wir kein Büro mehr auf
		}
		
		if((m==null) || (!m.isValid()) ){
			rn.reject(RnStatus.REJECTCODE.NO_MANDATOR, Messages.Validator_NoMandator);
			res.add(Log.ERRORS,2,Messages.Validator_NoMandator,rn,true);
	
		}
		Fall fall=rn.getFall();
		
		if((fall==null) || (!fall.isValid())){
			rn.reject(RnStatus.REJECTCODE.NO_CASE, Messages.Validator_NoCase);
			res.add(Log.ERRORS,4,Messages.Validator_NoCase,rn,true);
		}
		/*
		String g=fall.getGesetz();
		if(g.equalsIgnoreCase(Fall.LAW_OTHER)){
			return res;
		}
		*/
		String ean=m.getInfoString("EAN"); //$NON-NLS-1$
		if(StringTool.isNothing(ean)){
			rn.reject(RnStatus.REJECTCODE.NO_MANDATOR, Messages.Validator_NoEAN);
			res.add(Log.ERRORS,3,Messages.Validator_NoEAN,rn,true);
		}
		Kontakt kostentraeger=fall.getKostentraeger();
		
		/*	wird schon von Fall#isValid gesichert
		Kontakt garant=fall.getGarant();
		if((garant==null) || (!garant.isValid())){
			rn.reject(RnStatus.REJECTCODE.NO_DEBITOR, "Kein korrekter Garant zugeordnet");
			res.add(Log.ERRORS,5,"Kein korrekter Garant zugeordet",rn,true);
		}
		
		
		if((kostentraeger==null) || (!kostentraeger.isValid())){
			rn.reject(RnStatus.REJECTCODE.NO_GUARANTOR, "Kein korrekter Kostenträger zugeordnet");
			res.add(Log.ERRORS,5,"Kein korrekter Kostenträger zugeordet",rn,true);
		}
		*/
		ean=kostentraeger.getInfoString("EAN"); //$NON-NLS-1$
		if(StringTool.isNothing(ean) || (!ean.matches("[0-9]{13}"))){ //$NON-NLS-1$
			rn.reject(RnStatus.REJECTCODE.NO_GUARANTOR, Messages.Validator_NoEAN2);
			res.add(Log.ERRORS,6,Messages.Validator_NoEAN2,rn,true);
		}
		String bez=kostentraeger.get("Bezeichnung1"); //$NON-NLS-1$
		if(StringTool.isNothing(bez)){
			rn.reject(RnStatus.REJECTCODE.NO_GUARANTOR, Messages.Validator_NoName);
			res.add(Log.ERRORS,7,Messages.Validator_NoName,rn,true);
		}
		if(StringTool.isNothing(xp.diagnosen)){
			rn.reject(RnStatus.REJECTCODE.NO_DIAG, Messages.Validator_NoDiagnosis);
			res.add(Log.ERRORS,8,Messages.Validator_NoDiagnosis,rn,true);
		}
		return res;
	}
}
