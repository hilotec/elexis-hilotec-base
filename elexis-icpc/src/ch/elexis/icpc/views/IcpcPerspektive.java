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
 *  $Id: IcpcPerspektive.java 1781 2007-02-10 16:15:02Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.views.*;
import ch.elexis.views.codesystems.DiagnosenView;
import ch.elexis.views.codesystems.LeistungenView;

public class IcpcPerspektive implements IPerspectiveFactory {
	public static final String ID="ch.elexis.icpc.perspective";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		if(Hub.localCfg.get(PreferenceConstants.SHOWSIDEBAR,"true").equals("true")){ //$NON-NLS-1$ //$NON-NLS-2$
			layout.addStandaloneView(Starter.ID,false,IPageLayout.LEFT,0.1f,editorArea);
		}
		layout.addView(EpisodesView.ID, IPageLayout.LEFT, 0.2f, editorArea);
		IFolderLayout ifr=layout.createFolder("zentrum",IPageLayout.RIGHT,1.0f,editorArea); //$NON-NLS-1$
		IFolderLayout obenrechts=layout.createFolder("obenrechts", IPageLayout.RIGHT, 0.7f, "zentrum");
		obenrechts.addView(EncounterView.ID);
		obenrechts.addView(ReminderView.ID);
		IFolderLayout untenlinks=layout.createFolder("untenlinks", IPageLayout.BOTTOM, 0.4f, EpisodesView.ID);
		untenlinks.addView(FaelleView.ID);
		untenlinks.addView(DauerMediView.ID);
		//layout.addView(FaelleView.ID, IPageLayout.BOTTOM, 0.5f, EpisodesView.ID);
		IFolderLayout untenrechts=layout.createFolder("untenrechts", IPageLayout.BOTTOM, 0.3f, "obenrechts");
		//layout.addView(KonsListe.ID,IPageLayout.BOTTOM,0.3f,EncounterView.ID);
		untenrechts.addView(KonsListe.ID);
		untenrechts.addView(PatHeuteView.ID);
		ifr.addView(KonsDetailView.ID);
		ifr.addView(KompendiumView.ID);
		ifr.addView(LaborView.ID);
		ifr.addPlaceholder(RezeptBlatt.ID);
		ifr.addPlaceholder(AUFZeugnis.ID);
		ifr.addPlaceholder(TextView.ID);
		ifr.addPlaceholder(FallDetailView.ID);

		IFolderLayout bfr=layout.createFolder("unten", IPageLayout.BOTTOM, 0.7f, "zentrum");
		bfr.addView(AUF2.ID);
		bfr.addView(RezepteView.ID);
		
		layout.addFastView(PatientenListeView.ID);
		layout.addFastView(DiagnosenView.ID);
		layout.addFastView(LeistungenView.ID);
		
		layout.addShowViewShortcut(EpisodesView.ID);
		layout.addShowViewShortcut(EncounterView.ID);
		layout.addShowViewShortcut(KonsDetailView.ID);
		layout.addShowViewShortcut(LaborView.ID);
		layout.addShowViewShortcut(KonsListe.ID);
		layout.addShowViewShortcut(ReminderView.ID);
		layout.addShowViewShortcut(DauerMediView.ID);
	}

}
