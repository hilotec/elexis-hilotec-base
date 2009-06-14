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
 *  $Id: TextField.java 5355 2009-06-14 10:35:19Z rgw_ch $
 *******************************************************************************/
package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TextField extends ActiveControl {
	
	public TextField(Composite parent, int displayBits, String displayName){
		super(parent, displayBits, displayName);
		setControl(new Text(this,SWT.BORDER));
		getTextControl().addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e){
				fireChangedEvent();
			}});
	}
	
	public Text getTextControl(){
		return (Text)ctl;
	}

	@Override
	public String getText(){
		return getTextControl().getText();
	}

	@Override
	public void setText(String text){
		getTextControl().setText(text);
	}

	@Override
	public boolean isValid(){

		return false;
	}

	@Override
	public void clear(){
		getTextControl().setText("");
	}
}
