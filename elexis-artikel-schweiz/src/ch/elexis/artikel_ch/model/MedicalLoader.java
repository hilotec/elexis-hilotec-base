/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: MedicalLoader.java 5001 2009-01-22 15:50:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_ch.model;

import ch.elexis.actions.ArtikelLoader;
import ch.elexis.artikel_ch.data.Medical;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;

public class MedicalLoader extends ArtikelLoader {

	public MedicalLoader(CommonViewer cv){
		super(cv);
		qbe=new Query<Medical>(Medical.class);
		orderField=Medical.NAME;
	}
}
