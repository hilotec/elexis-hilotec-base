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
 * $Id: AssignEtiketteDialog.java 3800 2008-04-20 12:44:30Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

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

import ch.elexis.data.Etikette;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;

public class AssignEtiketteDialog extends TitleAreaDialog {
	PersistentObject mine;
	Table table;
	List<Etikette> alleEtiketten;
	List<Etikette> mineEtiketten;
	
	public AssignEtiketteDialog(Shell shell, PersistentObject obj){
		super(shell);
		mine=obj;
		mineEtiketten=mine.getEtiketten();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		Label lbl=new Label(ret,SWT.WRAP);
		lbl.setText("Bitte bestätigen Sie alle benötigten Etiketten mit Häkchen und "+
				"markieren Sie die Haupt-Etikette ");
		table=new Table(ret,SWT.CHECK|SWT.SINGLE);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Query<Etikette> qbe=new Query<Etikette>(Etikette.class);
		alleEtiketten=qbe.execute();
		for(Etikette et:alleEtiketten){
			TableItem it=new TableItem(table,SWT.NONE);
			if(mineEtiketten.contains(et)){
				it.setChecked(true);
			}
			it.setText(et.getLabel());
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
		setTitle("Etiketten");
		setMessage("Geben Sie bitte die Etiketten für "+mine.getLabel()+" ein.");
		getShell().setText("Elexis Etiketten");
	}

	@Override
	protected void okPressed() {
		
		for(TableItem it:table.getItems()){
			Etikette et=(Etikette)it.getData();
			if(it.getChecked()){
				if(!mineEtiketten.contains(et)){
					mine.addEtikette(et);
				}
			}else{
				if(mineEtiketten.contains(et)){
					mine.removeEtikette(et);
				}
			}
		}
		super.okPressed();
	}
	
}
