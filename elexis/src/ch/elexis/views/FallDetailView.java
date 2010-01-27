/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FallDetailView.java 5970 2010-01-27 16:43:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.actions.GlobalActions;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.util.SWTHelper;

public class FallDetailView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.FallDetailView"; //$NON-NLS-1$
	FallDetailBlatt2 fdb;
	private ElexisEventListenerImpl eeli_fall = new ElexisEventListenerImpl(
			Fall.class) {

		public void runInUi(final ElexisEvent ev) {
			fdb.setFall((Fall) ev.getObject());
		}
	};
	private ElexisEventListenerImpl eeli_pat = new ElexisEventListenerImpl(
			Patient.class) {

		public void runInUi(final ElexisEvent ev) {
			Patient patient = (Patient) ev.getObject();
			Fall selectedFall = (Fall) ElexisEventDispatcher
					.getSelected(Fall.class);
			if (selectedFall == null
					|| !selectedFall.getPatient().equals(patient)) {

				Konsultation letzteKons = patient.getLetzteKons(false);
				if (letzteKons != null) {
					fdb.setFall(letzteKons.getFall());
				} else {
					fdb.setFall(null);
				}
			}
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		fdb = new FallDetailBlatt2(parent);
		fdb.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_pat);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		ElexisEventDispatcher.getInstance()
				.removeListeners(eeli_fall, eeli_pat);
		super.dispose();
	}

	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir
	 * benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist. Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}

	public void doSave(IProgressMonitor monitor) { /* leer */
	}

	public void doSaveAs() { /* leer */
	}

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
