/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ErstelleRnnCommand.java 4428 2008-09-22 11:25:23Z rgw_ch $
 *******************************************************************************/
package ch.elexis.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.util.ResultAdapter;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.Tree;
import ch.elexis.views.rechnung.Messages;
import ch.rgw.tools.Result;

/**
 * Command um Rechnungen aus einer Liste von Patienten, Fällen und Konsultationen zu erstellen
 * Der Parameter des Commands muss ein Tree sein, welcher in der ersten Ebene die Patienten,
 * in der zweiten die Fälle und in der Dritten die zu verrechnenden Konsultationen
 * enthält. 
 * @author gerry
 * 
 */
public class ErstelleRnnCommand extends AbstractHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent eev) throws ExecutionException{

		Tree tSelection = (Tree) Handler.getParam(eev);
		IProgressMonitor monitor=Handler.getMonitor(eev);
		Result<Rechnung> res=null;
		for (Tree tPat = tSelection.getFirstChild(); tPat != null; tPat = tPat.getNextSibling()) {
			int rejected = 0;
			for (Tree tFall = tPat.getFirstChild(); tFall != null; tFall = tFall.getNextSibling()) {
				Fall fall = (Fall) tFall.contents;
				if (Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true)) {
					if (!fall.isValid()) {
						rejected++;
						continue;
					}
				}
				Collection<Tree> lt = tFall.getChildren();
				ArrayList<Konsultation> lb = new ArrayList<Konsultation>(lt.size() + 1);
				for (Tree t : lt) {
					lb.add((Konsultation) t.contents);
				}
				res = Rechnung.build(lb);
				if(monitor!=null){
					monitor.worked(1);
				}
				if (!res.isOK()) {
					ErrorDialog.openError(HandlerUtil.getActiveShell(eev), Messages
						.getString("KonsZumVerrechnenView.errorInInvoice"), //$NON-NLS-1$
						Messages.getString("KonsZumVerrechnenView.invoiceForCase", //$NON-NLS-1$
							new Object[] {
								fall.getLabel()
							}), ResultAdapter.getResultAsStatus(res));
				} else {
					tPat.remove(tFall);
				}
			}
			if (rejected != 0) {
				SWTHelper
					.showError(
						"Fehlerhafte Falldefinitionen",
						Integer.toString(rejected)
							+ " Rechnungen wurden nicht erstellt, weil die Fälle nicht alle notwendigen Angaben enthalten. "
							+ "Bitte kontrollieren Sie die Fall-Details");
			} else {
				tSelection.remove(tPat);
			}
		}
		return res;
	}
	
}
