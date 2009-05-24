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
 *  $Id: SelectFallDialog.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Fall;
import ch.elexis.util.SWTHelper;

public class SelectFallDialog extends TitleAreaDialog {
	Fall[] faelle;
	public Fall result;
	org.eclipse.swt.widgets.List list;

	public SelectFallDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		list = new org.eclipse.swt.widgets.List(parent, SWT.BORDER);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Fall actFall = (Fall) GlobalEvents.getInstance().getSelectedObject(
				Fall.class);
		faelle = actFall.getPatient().getFaelle();
		for (Fall f : faelle) {
			list.add(f.getLabel());
		}
		return list;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("SelectFallDialog.selectFall")); //$NON-NLS-1$
		setMessage(Messages.getString("SelectFallDialog.pleaseSelectCase")); //$NON-NLS-1$
		getShell().setText(Messages.getString("SelectFallDialog.Cases")); //$NON-NLS-1$
	}

	@Override
	public void okPressed() {
		int sel = list.getSelectionIndex();
		if (sel == -1) {
			result = null;
		}
		result = faelle[sel];
		super.okPressed();
	}

}