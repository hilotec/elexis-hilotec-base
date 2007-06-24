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
 *    $Id: BriefePerspektive.java 1232 2006-11-06 14:15:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis;


import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.views.*;

public class BriefePerspektive implements IPerspectiveFactory {
	public static final String ID="ch.elexis.BriefePerspektive"; //$NON-NLS-1$
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		if(Hub.localCfg.get(PreferenceConstants.SHOWSIDEBAR,"true").equals("true")){ //$NON-NLS-1$ //$NON-NLS-2$
			layout.addStandaloneView(Starter.ID,false,SWT.LEFT,0.1f,editorArea);
		}
        IFolderLayout left=layout.createFolder("Links.folder",SWT.LEFT,0.3f,editorArea); //$NON-NLS-1$
		IFolderLayout main=layout.createFolder("Haupt.Folder",SWT.RIGHT,0.7f,editorArea); //$NON-NLS-1$
        left.addView(BriefAuswahl.ID);
		main.addView(TextView.ID);
		// layout.addFastView(BriefErstellen.ID);
		layout.addFastView(PatientDetailView.ID);
		layout.addFastView(KonsDetailView.ID);
		layout.addPerspectiveShortcut(ID);
        layout.addShowViewShortcut(PatientDetailView.ID);
        layout.addShowViewShortcut(KonsDetailView.ID);
        layout.addShowViewShortcut(BriefAuswahl.ID);
        layout.addShowViewShortcut(TextView.ID);

	}

}
