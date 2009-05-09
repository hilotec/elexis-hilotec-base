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

package ch.elexis.agenda.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.agenda.data.IPlannable;
import ch.elexis.util.SWTHelper;

/**
 * A View to display ressources side by side in the same view.
 * 
 * @author gerry
 * 
 */
public class AgendaParallel extends BaseView {
	private double pixelPerMinute;
	private Label[] labels;
	private ProportionalSheet sheet;
	
	public AgendaParallel(){
	// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void create(Composite parent){
		ScrolledComposite bounding = new ScrolledComposite(parent, SWT.V_SCROLL);
		bounding.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		sheet=new ProportionalSheet(bounding,this);
		bounding.setContent(sheet);
		
	}
	
	@Override
	protected IPlannable getSelection(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void refresh(){
	// TODO Auto-generated method stub
	
	}
	
}
