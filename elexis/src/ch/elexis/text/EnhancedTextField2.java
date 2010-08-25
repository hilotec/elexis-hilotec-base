/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: EnhancedTextField.java 6247 2010-03-21 06:36:34Z rgw_ch $
 *******************************************************************************/
package ch.elexis.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.util.IKonsExtension;
import ch.rgw.tools.GenericRange;
import ch.rgw.tools.StringTool;

/**
 * This is a pop-in replacement for EnhancedTextField that can handle SimpleStructuredDocument
 * contents and for backwards compatibility also Samdas
 * @author Gerry Weirich
 */

public class EnhancedTextField2 extends Composite implements IRichTextDisplay {
	private StyledText st;
	
	public EnhancedTextField2(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	public void addXrefHandler(String id, IKonsExtension ike) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertXRef(int pos, String textToDisplay, String providerId,
			String itemID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getContentsAsXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentsPlaintext() {
		return st.getText();
	}

	@Override
	public GenericRange getSelectedRange() {
		Point pt=st.getSelection();
		return new GenericRange(pt.x,pt.y);
	}

	@Override
	public String getWordUnderCursor() {
		return StringTool.getWordAtIndex(st.getText(), st.getCaretOffset());
	}

}
