/*******************************************************************************
 * Copyright (c) 2005, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package org.iatrix.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.iatrix.widgets.KonsListDisplay;

import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

/**
 * View for showing Konsultationen
 * @author danlutz
 */

public class KonsListView extends ViewPart implements GlobalEvents.SelectionListener, ActivationListener, ISaveablePart2 {
    public static final String ID="org.iatrix.views.KonsListView";

    KonsListDisplay konsListDisplay;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        konsListDisplay = new KonsListDisplay(parent);
        GlobalEvents.getInstance().addActivationListener(this, this);
    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }
    
	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
    	GlobalEvents.getInstance().removeSelectionListener(this);
    	GlobalEvents.getInstance().removeActivationListener(this, this);
        super.dispose();
    }

    public void selectionEvent(PersistentObject obj) {
        if ((obj instanceof Patient)) {
        	Patient patient = (Patient) obj;
            konsListDisplay.setPatient(patient);
        }
    }

	public void clearEvent(Class template) {
		if (template.equals(Patient.class)) {
			konsListDisplay.setPatient(null);
		}
	}
	
	public void activation(boolean mode) {
		// do nothing
	}

	public void visible(boolean mode) {
		if (mode == true) {
			selectionEvent(GlobalEvents.getSelectedPatient());
			GlobalEvents.getInstance().addSelectionListener(this);
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}


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
