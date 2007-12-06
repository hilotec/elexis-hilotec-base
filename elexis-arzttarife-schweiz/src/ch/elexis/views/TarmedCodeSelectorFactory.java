/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: TarmedCodeSelectorFactory.java 3420 2007-12-06 20:36:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.swt.SWT;

import ch.elexis.data.TarmedCodeProvider;
import ch.elexis.data.TarmedLeistung;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class TarmedCodeSelectorFactory extends CodeSelectorFactory {
	
	public TarmedCodeSelectorFactory() {
		
	}

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		ViewerConfigurer vc=new ViewerConfigurer(
				new TarmedCodeProvider(cv),
				new ViewerConfigurer.TreeLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"Code","Text"}), //$NON-NLS-1$
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE,null)
				);
		return vc;
	}

	@Override
	public Class getElementClass() {
		return TarmedLeistung.class;
	}

	@Override
	public void dispose() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	@Override
	public String getCodeSystemName() {
		return "Tarmed"; //$NON-NLS-1$
	}

}
