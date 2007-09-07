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
 *  $Id: BAGMediSelector.java 3107 2007-09-07 11:03:26Z rgw_ch $
 *******************************************************************************/
package ch.elexis.medikamente.bag.views;

import org.eclipse.swt.SWT;

import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class BAGMediSelector extends CodeSelectorFactory {
	
	CommonViewer cv;
	
	@Override
	public ViewerConfigurer createViewerConfigurer(final CommonViewer cv) {
		//new ArtikelContextMenu((Medikament)new ch.elexis.artikel_at.data.ArtikelFactory().createTemplate(Medikament.class),cv,this);
		this.cv=cv;
		return new ViewerConfigurer(
				new ContentProvider(cv),
				new BAGMediLabelProvider(),
				new ControlFieldProvider(cv),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,null)
		);
	}


	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCodeSystemName() {
		return BAGMedi.CODESYSTEMNAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getElementClass() {
		return BAGMedi.class;
	}

}
