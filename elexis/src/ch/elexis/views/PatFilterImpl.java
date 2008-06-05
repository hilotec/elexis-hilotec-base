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
 * $Id: PatFilterImpl.java 4005 2008-06-05 12:14:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import ch.elexis.data.Artikel;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Script;
import ch.elexis.views.PatListFilterBox.IPatFilter;

/**
 * Default implementation of IPatFilter. Will be called after all other filters
 * returned DONT_HANDLE
 * @author Gerry
 
 */
public class PatFilterImpl implements IPatFilter {

	public int accept(Patient p, PersistentObject o){
		if(o instanceof Kontakt){
			// 
		}else if(o instanceof IVerrechenbar){
			
		}else if(o instanceof IDiagnose){
			
		}else if(o instanceof Prescription){
			
		}else if(o instanceof Artikel){
			
		}else if(o instanceof Script){
			Object ret;
			try {
				Script script=(Script)o;
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

}
