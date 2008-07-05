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
 * $Id: ConflictResolveDialog.java 4104 2008-07-05 19:23:25Z rgw_ch $
 *******************************************************************************/

package ch.elexis.matchers;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;

public class ConflictResolveDialog extends TitleAreaDialog {
	private Kontakt res;
	private Kontakt mine;
	
	
	public ConflictResolveDialog(Shell shell, Kontakt k) {
		super(shell);
		mine=k;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret= (Composite)super.createDialogArea(parent);
		Label lTell=new Label(ret,SWT.WRAP);
		lTell.setText(resolve1);
		
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Nähere Angaben zum Import");
		setMessage(mine.getLabel());
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}
	
	final static String resolve1="Es kann nicht automatisch entschieden werden, "+
	"ob # in der Datenbank enthalten ist, bzw. welchem existierenden Kontakt dies entspricht.\n"+
	"Bitte wählen Sie unten aus, welchem Kontakt dieser neue Eintrag entspricht oder ob ein Kontakt "+
	"für diesen Eintrag neu erstellt werden soll.";

	public Kontakt getResult() {
		return res;
	}
}
