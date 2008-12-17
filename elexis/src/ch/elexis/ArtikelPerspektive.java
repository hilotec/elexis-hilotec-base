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
 *  $Id: ArtikelPerspektive.java 4828 2008-12-17 16:43:33Z rgw_ch $
 *******************************************************************************/
package ch.elexis;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.views.*;
import ch.elexis.views.artikel.ArtikelView;

/**
 * Diese Klasse erzeugt das Anfangslayout für die Funktion "Artikel"
 */
public class ArtikelPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.ArtikelPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		if (Hub.localCfg.get(PreferenceConstants.SHOWSIDEBAR, "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
			layout.addStandaloneView(Starter.ID, false, SWT.LEFT, 0.1f, editorArea);
		}
		IFolderLayout ifr = layout.createFolder("rechts", SWT.RIGHT, 1.0f, editorArea); //$NON-NLS-1$
		ifr.addView(ArtikelView.ID);
		ifr.addView(LagerView.ID);
		ifr.addView(KompendiumView.ID);
		
		// layout.addView(Artikelliste.ID,SWT.RIGHT,0.5f,editorArea);
		// layout.addView(Artikeldetail.ID,SWT.RIGHT,0.5f,editorArea);
		
	}
	
}
