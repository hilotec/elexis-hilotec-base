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
 * $Id: MigelLoader.java 5005 2009-01-23 05:48:01Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_ch.model;

import ch.elexis.actions.FlatDataLoader;
import ch.elexis.artikel_ch.data.MiGelArtikel;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;

public class MigelLoader extends FlatDataLoader {
	public MigelLoader(CommonViewer cv){
		super(cv,new Query<MiGelArtikel>(MiGelArtikel.class));
		orderField=MiGelArtikel.NAME;
	}
}
