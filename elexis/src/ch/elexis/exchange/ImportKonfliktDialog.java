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
 *  $Id: ImportKonfliktDialog.java 1221 2006-11-03 14:55:54Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jdom.Element;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;

public class ImportKonfliktDialog extends TitleAreaDialog {
	PersistentObject[] choices;
	String def;
	Button[] bChoice;
	Result<PersistentObject> result=null;
	
	public ImportKonfliktDialog(Shell parentShell, PersistentObject[] choice, String prop) {
		super(parentShell);
		this.choices=choice;
		def=prop;

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bChoice=new Button[choices.length+1];
		int i=0;
		for(i=0;i<choices.length;i++){
			bChoice[i]=new Button(ret,SWT.RADIO);
			bChoice[i].setText(choices[i].getLabel());
		}
		bChoice[i]=new Button(ret,SWT.RADIO);
		bChoice[i].setText("Nicht zuordnen, sondern neu erstellen");
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Importkonflikt auflösen");
		setTitle("Import des Datensatzes "+def);
		setMessage("Wo soll dieser Datensatz zugeordnet werden?" );
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}

	@Override
	protected void okPressed() {
		result=null;
		for(int i=0;i<choices.length;i++){
			if(bChoice[i].getSelection()==true){
				result=new Result<PersistentObject>(choices[i]);
				break;
			}
		}
		if(result==null){
			result=new Result<PersistentObject>(null);
		}
		super.okPressed();
	}
}
