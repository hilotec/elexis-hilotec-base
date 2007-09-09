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
 * $Id: InteraktionsDialog.java 3125 2007-09-09 16:11:30Z rgw_ch $
 *****************************************************************************/

package ch.elexis.medikamente.bag.views;


import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.SWTHelper;

public class InteraktionsDialog extends TitleAreaDialog {
	BAGMedi medi;
	List<Substance> substances;
	
	public InteraktionsDialog(Shell shell, BAGMedi medi){
		super(shell);
		this.medi=medi;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		substances=medi.getSubstances();
		new Label(ret,SWT.NONE).setText("Inhaltsstoffe");
		org.eclipse.swt.widgets.List lSubst=new org.eclipse.swt.widgets.List(ret,SWT.SINGLE);
		lSubst.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		for(Substance s:substances){
			lSubst.add(s.getLabel());
		}
		new Label(ret,SWT.NONE).setText("Interaktion mit:");
		ListDisplay<Substance> ldInter=new ListDisplay<Substance>(ret,SWT.NONE,
				new ListDisplay.LDListener(){

					public String getLabel(Object o) {
						if(o instanceof Substance){
							Substance subst = (Substance) o;
							return subst.getLabel();
						}
						return "?";
					}

					public void hyperlinkActivated(String l) {
						// TODO Auto-generated method stub
						
					}});
		ldInter.addHyperlinks("Substanz Hinzufügen...");
		ldInter.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Typ der Interaktion");
		Combo cbTyp=new Combo(ret,SWT.SINGLE|SWT.READ_ONLY);
		cbTyp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText("Beschreibung der Interaktion");
		Text text=SWTHelper.createText(ret, 4, SWT.BORDER);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle(medi.getLabel());
		//setMessage("Interaktionen für "+medi.getLabel());
		getShell().setText("Interaktionen");
	}
	
	
}
