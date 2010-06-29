/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: PatientPerspektive.java 5194 2009-02-24 16:31:36Z rgw_ch $
 *******************************************************************************/

package ch.medshare.ebm;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.views.KonsDetailView;
import ch.elexis.views.KonsListe;

public class SearchPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.medshare.ebm.SearchPerspektive";
	
	public SearchPerspektive() {
		
	}
	
	public void createInitialLayout(final IPageLayout layout){
		String editorArea = layout.getEditorArea();
		layout.setFixed(false);
		IFolderLayout left =
			layout.createFolder("Links.folder", IPageLayout.LEFT, 0.4f, editorArea);
		IFolderLayout leftbottom =
			layout.createFolder("links.unten", IPageLayout.BOTTOM, 0.7f, "Links.folder");
		
		left.addView(KonsDetailView.ID);
		leftbottom.addView(KonsListe.ID);
	}
	
		
}
