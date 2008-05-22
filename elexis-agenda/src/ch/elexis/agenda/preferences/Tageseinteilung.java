/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TagesView.java 916 2006-09-13 08:33:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.agenda.preferences;

import java.util.Hashtable;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ch.elexis.agenda.Messages;

import ch.elexis.Hub;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;

public class Tageseinteilung extends PreferencePage implements
		IWorkbenchPreferencePage {
	Text tMo,tDi,tMi,tDo,tFr,tSa,tSo;
	int actBereich;
	String[] bereiche;
	
	public Tageseinteilung() {
		super(Messages.Tageseinteilung_dayPlanning); 
		bereiche=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.Tageseinteilung_praxis).split(","); 
		actBereich=0;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret,SWT.None).setText(Messages.Tageseinteilung_enterPeriods); 
		final Combo cbBereich=new Combo(ret,SWT.READ_ONLY|SWT.SINGLE);
		cbBereich.setItems(bereiche);
		Composite grid=new Composite(ret,SWT.BORDER);
		grid.setLayout(new GridLayout(7,true));
		grid.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(grid,SWT.CENTER).setText(Messages.Tageseinteilung_mo); 
		new Label(grid,SWT.CENTER).setText(Messages.Tageseinteilung_tu); 
		new Label(grid,SWT.CENTER).setText(Messages.Tageseinteilung_we); 
		new Label(grid,SWT.CENTER).setText(Messages.Tageseinteilung_th); 
		new Label(grid,SWT.NONE).setText(Messages.Tageseinteilung_fr); 
		new Label(grid,SWT.NONE).setText(Messages.Tageseinteilung_sa); 
		new Label(grid,SWT.NONE).setText(Messages.Tageseinteilung_so); 
		tMo=new Text(grid,SWT.BORDER|SWT.MULTI);
		tMo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tDi=new Text(grid,SWT.BORDER|SWT.MULTI);
		tDi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tMi=new Text(grid,SWT.BORDER|SWT.MULTI);
		tMi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tDo=new Text(grid,SWT.BORDER|SWT.MULTI);
		tDo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tFr=new Text(grid,SWT.BORDER|SWT.MULTI);
		tFr.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tSa=new Text(grid,SWT.BORDER|SWT.MULTI);
		tSa.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tSo=new Text(grid,SWT.BORDER|SWT.MULTI);
		tSo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cbBereich.select(actBereich);
		reload();
		cbBereich.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx=cbBereich.getSelectionIndex();
				if(idx!=-1){
					save();
					actBereich=idx;
					reload();
				}
			}
			
		});
		return ret;
	}

	void reload(){
		Hashtable<String, String>map=Plannables.getDayPrefFor(bereiche[actBereich]);
		String p=map.get(Messages.Tageseinteilung_mo); 
		tMo.setText(p==null ? "0000-0800\n1800-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_tu); 
		tDi.setText(p==null ? "0000-0800\n1800-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_we); 
		tMi.setText(p==null ? "0000-0800\n1800-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_th); 
		tDo.setText(p==null ? "0000-0800\n1800-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_fr); 
		tFr.setText(p==null ? "0000-0800\n1800-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_sa); 
		tSa.setText(p==null ? "0000-0800\n1200-2359" : p); //$NON-NLS-1$
		p=map.get(Messages.Tageseinteilung_su); 
		tSo.setText(p==null ? "0000-2359" : p); //$NON-NLS-1$
	}
	
	void save(){
		Hashtable<String,String>map=new Hashtable<String,String>();
		map.put(Messages.Tageseinteilung_mo, tMo.getText()); 
		map.put(Messages.Tageseinteilung_tu, tDi.getText()); 
		map.put(Messages.Tageseinteilung_we, tMi.getText()); 
		map.put(Messages.Tageseinteilung_th, tDo.getText()); 
		map.put(Messages.Tageseinteilung_fr, tFr.getText()); 
		map.put(Messages.Tageseinteilung_sa, tSa.getText()); 
		map.put(Messages.Tageseinteilung_su, tSo.getText()); 
		Plannables.setDayPrefFor(bereiche[actBereich], map);
	}
	public void init(IWorkbench workbench) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void performApply() {
		save();
		super.performApply();
	}
	
}
