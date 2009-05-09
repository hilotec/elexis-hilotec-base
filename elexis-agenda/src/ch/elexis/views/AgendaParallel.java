/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Sponsoring:
 * 	 mediX Notfallpaxis, diepraxen Stauffacher AG, Zürich
 * 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AgendaParallel.java 5280 2009-05-09 10:46:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.agenda.data.Termin;
import ch.elexis.data.PersistentObject;

/**
 * A View to display ressources side by side in the same view. 
 * @author gerry
 *
 */
public class AgendaParallel extends BaseAgendaView {
	DayBar[] bereiche;
		
	public AgendaParallel() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void create(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTermin(Termin t) {
		// TODO Auto-generated method stub
		
	}

	public void reloadContents(Class<? extends PersistentObject> clazz) {
		// TODO Auto-generated method stub
		
	}

}
