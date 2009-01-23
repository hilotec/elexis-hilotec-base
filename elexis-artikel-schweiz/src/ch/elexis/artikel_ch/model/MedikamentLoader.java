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
 * $Id: BackgroundJob.java 4695 2008-11-22 05:58:01Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_ch.model;

import ch.elexis.actions.FlatDataLoader;
import ch.elexis.artikel_ch.data.Medikament;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;

public class MedikamentLoader extends FlatDataLoader {
	
	public MedikamentLoader(CommonViewer cv){
		super(cv,new Query<Medikament>(Medikament.class));
		orderField=Medikament.NAME;
	}
}
