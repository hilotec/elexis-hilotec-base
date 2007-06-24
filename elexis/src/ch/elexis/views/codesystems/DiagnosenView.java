/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.views.codesystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.views.codesystems.CodeSelectorFactory.cPage;

public class DiagnosenView extends ViewPart implements ActivationListener, ISaveablePart2 {
	public final static String ID="ch.elexis.DiagnosenView";
	CTabFolder ctab;
	CTabItem selected;
	
	public DiagnosenView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ctab=new CTabFolder(parent,SWT.BOTTOM);
		ctab.setSimple(false);
		ctab.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				if(selected!=null){
					cPage page=(cPage)selected.getControl();
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
				}
				selected=ctab.getSelection();
				((cPage)selected.getControl()).refresh();
				setFocus();
			}
			
		});

		CodeSelectorFactory.makeTabs(ctab, getViewSite(),"ch.elexis.Diagnosecode");

		GlobalEvents.getInstance().addActivationListener(this,this);
	}

	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this,this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		if((ctab!=null) && (ctab.getSelection()!=null)){
			ctab.setFocus();
			((CodeSelectorFactory.cPage)ctab.getSelection().getControl()).refresh();
		}
	}

	public void activation(boolean mode) {
		if(mode==false){
			if(selected!=null){
				cPage page=(cPage)selected.getControl();
				page.cv.getConfigurer().getControlFieldProvider().clearValues();
			}
			
			// remove any ICodeSelectiorTarget, since it's no more needed
			GlobalEvents.getInstance().removeCodeSelectorTarget();
		}
		
	}
	public void visible(boolean mode){}
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
}
