/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: AssignStickerDialog.java 5328 2009-05-30 06:53:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.data.Sticker;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;

public class AssignStickerDialog extends TitleAreaDialog {
	PersistentObject mine;
	Table table;
	List<Sticker> alleEtiketten;
	List<Sticker> mineEtiketten;

	public AssignStickerDialog(Shell shell, PersistentObject obj) {
		super(shell);
		mine = obj;
		mineEtiketten = mine.getStickers();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		Label lbl = new Label(ret, SWT.WRAP);
		lbl.setText(Messages.getString("AssignStickerDialog.PleaseConfirm")); //$NON-NLS-1$
		table = new Table(ret, SWT.CHECK | SWT.SINGLE);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Query<Sticker> qbe = new Query<Sticker>(Sticker.class);
		alleEtiketten = qbe.execute();
		for (Sticker et : alleEtiketten) {
			TableItem it = new TableItem(table, SWT.NONE);
			if (mineEtiketten.contains(et)) {
				it.setChecked(true);
			}
			it.setText(et.getLabel() + "(" + et.getWert() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			it.setImage(et.getImage());
			it.setForeground(et.getForeground());
			it.setBackground(et.getBackground());
			it.setData(et);
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Sticker"); //$NON-NLS-1$
		setMessage(MessageFormat.format(Messages.getString("AssignStickerDialog.enterStickers"),mine.getLabel())); //$NON-NLS-1$
		getShell().setText("Elexis Sticker"); //$NON-NLS-1$
	}

	@Override
	protected void okPressed() {

		for (TableItem it : table.getItems()) {
			Sticker et = (Sticker) it.getData();
			if (it.getChecked()) {
				if (!mineEtiketten.contains(et)) {
					mine.addSticker(et);
				}
			} else {
				if (mineEtiketten.contains(et)) {
					mine.removeSticker(et);
				}
			}
		}
		super.okPressed();
	}

}
