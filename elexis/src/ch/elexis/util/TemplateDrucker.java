/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation, based on RechnungsDrucker
 * 
 * $Id: TemplateDrucker.java 6043 2010-02-01 14:34:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.Hub;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.data.Patient;
import ch.elexis.status.ElexisStatus;
import ch.elexis.views.TemplatePrintView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class TemplateDrucker {
	TemplatePrintView tpw;
	IWorkbenchPage page;
	// IProgressMonitor monitor;
	Patient patient;
	String template;
	String printer;
	String tray;
	
	public TemplateDrucker(String template, String printer, String tray){
		this.template = template;
		this.printer = null;
		this.tray = null;
		
		if (!StringTool.isNothing(printer)) {
			this.printer = printer;
		}
		if (!StringTool.isNothing(tray)) {
			this.tray = tray;
		}
	}
	
	public void doPrint(Patient pat){
		this.patient = pat;
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		
		try {
			tpw = (TemplatePrintView) page.showView(TemplatePrintView.ID);
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor){
					monitor.beginTask(
						Messages.getString("TemplateDrucker.printing") + template + "...", 1); //$NON-NLS-1$
					
					Patient actPatient =
						(Patient) ElexisEventDispatcher.getSelected(Patient.class);
					if (tpw.doPrint(actPatient, template, printer, tray, monitor) == false) {
						Status status =
							new Status(Status.ERROR, "ch.elexis", Status.ERROR, Messages
								.getString("TemplateDrucker.errorPrinting"), null);
						ErrorDialog
						.openError(
							null,
							Messages.getString("TemplateDrucker.errorPrinting"), Messages.getString("TemplateDrucker.docname") + template + Messages.getString("TemplateDrucker.couldntPrint"), status); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						
					}
					
					monitor.done();
				}
			}, null);
			
			page.hideView(tpw);
			
		} catch (Exception ex) {
			ElexisStatus status = new ElexisStatus(IStatus.ERROR, Hub.PLUGIN_ID, IStatus.ERROR,
					Messages.getString("TemplateDrucker.errorPrinting") + ": " +  Messages.getString("TemplateDrucker.couldntOpen"),
					ex);
			StatusManager.getManager().handle(status);
		}
	}
}
