/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ComboField.java 5320 2009-05-27 16:51:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ComboField extends ActiveControl {
	Combo combo;
	
	public ComboField(Composite parent, int displayBits, String displayName, String... values){
		super(parent, displayBits, displayName);
		combo=new Combo(parent,SWT.READ_ONLY|SWT.SINGLE);
		combo.setItems(values);
		setControl(combo);
	}

	@Override
	public void clear(){
		combo.select(0);
	}

	@Override
	public String getText(){
		return combo.getText();
	}

	@Override
	public boolean isValid(){
		int idx=combo.getSelectionIndex();
		return idx!=-1;
	}

	@Override
	public void setText(String text){
		combo.setText(text);		
	}

}
