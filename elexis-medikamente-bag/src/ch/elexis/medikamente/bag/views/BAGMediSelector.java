/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BAGMediSelector.java 5030 2009-01-24 16:36:00Z rgw_ch $
 *******************************************************************************/
package ch.elexis.medikamente.bag.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;

import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.BAGMediFactory;
import ch.elexis.selectors.SelectorPanelProvider;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.views.artikel.ArtikelContextMenu;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class BAGMediSelector extends CodeSelectorFactory {
	private IAction sameOfGroupAction;
	CommonViewer cv;
	SelectorPanelProvider slp;
	
	public BAGMediSelector(){
		makeActions();
	}
	
	@Override
	public ViewerConfigurer createViewerConfigurer(final CommonViewer cv){
		ArtikelContextMenu menu =
			new ArtikelContextMenu((BAGMedi) new BAGMediFactory().createTemplate(BAGMedi.class), cv);
		menu.addAction(sameOfGroupAction);
		//
		this.cv = cv;
		return new ViewerConfigurer(new ContentProvider(cv), new BAGMediLabelProvider(),
			new ControlFieldProvider(cv), new ViewerConfigurer.DefaultButtonProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
	}
	
	@Override
	public void dispose(){
	// TODO Auto-generated method stub
	
	}
	
	@Override
	public String getCodeSystemName(){
		return BAGMedi.CODESYSTEMNAME;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class getElementClass(){
		return BAGMedi.class;
	}
	
	private void makeActions(){
		sameOfGroupAction = new Action("Selbe therap. Gruppe") {
			{
				setToolTipText("Zeige alle Medikamente derselben therapeutischen Gruppe");
			}
			
			@Override
			public void run(){
				ContentProvider cp = (ContentProvider) cv.getConfigurer().getContentProvider();
				BAGMedi selected = (BAGMedi) cv.getSelection()[0];
				cp.group = selected.get("Gruppe");
				ControlFieldProvider cfp =
					(ControlFieldProvider) cv.getConfigurer().getControlFieldProvider();
				if (cfp.isEmpty()) {
					cv.notify(CommonViewer.Message.update_keeplabels);
				} else {
					cfp.clearValues();
				}
				
			}
			
		};
	}
	
}
