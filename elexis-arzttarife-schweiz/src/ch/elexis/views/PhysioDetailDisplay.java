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
 * $Id: PhysioDetailDisplay.java 5139 2009-02-16 21:10:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PhysioLeistung;
import ch.elexis.selectors.ActiveControl;
import ch.elexis.selectors.SelectorPanel;
import ch.elexis.selectors.TextField;
import ch.elexis.util.SWTHelper;

public class PhysioDetailDisplay implements IDetailDisplay {
	Form form;
	SelectorPanel slp;
	ActiveControl[] ctls;
	
	public Composite createDisplay(Composite parent, IViewSite site){
		form = Desk.getToolkit().createForm(parent);
		TableWrapLayout twl = new TableWrapLayout();
		form.getBody().setLayout(twl);
		slp = new SelectorPanel(form.getBody());
		ctls = new ActiveControl[] {
			new TextField(slp, 0, "Ziffer"), new TextField(slp, 0, "Titel")
		};
		slp.addFields(ctls);
		
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		slp.setLayoutData(twd);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getHead().setBackground(Desk.getColor(Desk.COL_BLUE));
		form.getBody().setBackground(Desk.getColor(Desk.COL_GREEN));
		slp.setBackground(Desk.getColor(Desk.COL_RED));
		return form;
		
	}
	
	public void display(Object obj){
		if (ctls == null || ctls.length < 2) {
			return;
		}
		if (obj instanceof PhysioLeistung) {
			PhysioLeistung pl = (PhysioLeistung) obj;
			ctls[0].setText(pl.get("Ziffer"));
			ctls[1].setText(pl.get("Titel"));
		} else {
			ctls[0].setText("?");
			ctls[1].setText("?");
		}
		
	}
	
	public Class<? extends PersistentObject> getElementClass(){
		return PhysioLeistung.class;
	}
	
	public String getTitle(){
		return "Physiotherapie";
	}
	
}
