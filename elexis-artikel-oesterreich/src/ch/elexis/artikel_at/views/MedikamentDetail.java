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
 *  $Id: MedikamentDetail.java 2189 2007-03-30 15:51:10Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_at.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.views.IDetailDisplay;

public class MedikamentDetail implements IDetailDisplay {
	
	MedikamentDetailBlatt detail;
	
	public Composite createDisplay(Composite parent, IViewSite site) {
		detail=new MedikamentDetailBlatt(parent);
		return detail;
		
	}

	public Class getElementClass() {
		return Medikament.class;
	}

	public String getTitle() {
		return "Medikamente";
	}

	public void display(Object obj) {
		detail.display((Medikament)obj);
	}

}
