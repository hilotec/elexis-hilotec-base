/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: PatientPerspektive.java 2947 2007-08-03 10:16:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.views.AUF2;
import ch.elexis.views.FaelleView;
import ch.elexis.views.FallDetailView;
import ch.elexis.views.KompendiumView;
import ch.elexis.views.KonsDetailView;
import ch.elexis.views.KonsListe;
import ch.elexis.views.LaborView;
import ch.elexis.views.PatHeuteView;
import ch.elexis.views.PatientDetailView;
import ch.elexis.views.PatientenListeView;
import ch.elexis.views.RezepteView;
import ch.elexis.views.Starter;
import ch.elexis.views.TextView;
import ch.elexis.views.codesystems.DiagnosenView;
import ch.elexis.views.codesystems.LeistungenView;

/**
 * Aufbau des initalen Layouts der "Patient"-Seite
 */
public class PatientPerspektive implements IPerspectiveFactory {
	public static final String ID="ch.elexis.PatientPerspective"; //$NON-NLS-1$
	public void createInitialLayout(final IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		if(Hub.localCfg.get(PreferenceConstants.SHOWSIDEBAR,"true").equals("true")){ //$NON-NLS-1$ //$NON-NLS-2$
			layout.addStandaloneView(Starter.ID,false,SWT.LEFT,0.1f,editorArea);
		}
        IFolderLayout left=layout.createFolder("Links.folder",IPageLayout.LEFT,0.4f,editorArea); //$NON-NLS-1$
        IFolderLayout main=layout.createFolder("Haupt.Folder",IPageLayout.RIGHT,0.3f,"Links.folder"); //$NON-NLS-1$
		IFolderLayout leftbottom=layout.createFolder("links.unten", IPageLayout.BOTTOM, 0.7f, "Links.folder");
		IFolderLayout right=layout.createFolder("Rechts.folder",IPageLayout.RIGHT, 0.7f, "Haupt.Folder");
		
		main.addView(PatientDetailView.ID);
		left.addView(PatientenListeView.ID);
		//left.addView(KonsListe.ID);
		//left.addView(FallListeView.ID);
		leftbottom.addView(FaelleView.ID);
		left.addView(PatHeuteView.ID);
		main.addView(KonsDetailView.ID);
		
		main.addView(LaborView.ID);
		main.addView(RezepteView.ID);
		main.addView(AUF2.ID);
		
		right.addView(KonsListe.ID);
		
		main.addPlaceholder(FallDetailView.ID);
		main.addPlaceholder(TextView.ID);
		main.addPlaceholder(KompendiumView.ID);
		layout.addFastView(LeistungenView.ID,0.5f);
		layout.addFastView(DiagnosenView.ID,0.5f);
		layout.addPerspectiveShortcut(ID);
		layout.addShowViewShortcut(PatientDetailView.ID);
		layout.addShowViewShortcut(PatientenListeView.ID);
        //layout.addShowViewShortcut(FallListeView.ID);
		layout.addPerspectiveShortcut(FaelleView.ID);
		layout.addShowViewShortcut(KonsListe.ID);
        layout.addShowViewShortcut(PatHeuteView.ID);
        layout.addShowViewShortcut(KonsDetailView.ID);
        layout.addShowViewShortcut(RezepteView.ID);
        layout.addShowViewShortcut(FallDetailView.ID);
	}

}
