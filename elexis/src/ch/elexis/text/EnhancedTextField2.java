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

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.StringConstants;
import ch.elexis.text.model.SSDRange;
import ch.elexis.text.model.SimpleStructuredDocument;
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
	private HashMap<String,IKonsExtension> xRefHandlers=new HashMap<String,IKonsExtension>();
	
	public EnhancedTextField2(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	public void addXrefHandler(String id, IKonsExtension ike) {
		xRefHandlers.put(id, ike);
	}

	@Override
	public void insertXRef(int pos, String textToDisplay, String providerId,
			String itemID) {
		
	}

	@Override
	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension) {
		// TODO Auto-generated method stub

	}

	/**
	 * Contents will always be saved as SimpleStructuredDocument
	 */
	@Override
	public String getContentsAsXML() {
		SimpleStructuredDocument sd = new SimpleStructuredDocument();
		sd.insertText(st.getText(), 0);
		StyleRange[] ranges = st.getStyleRanges(true);
		for (StyleRange sr : ranges) {
			StringBuilder id = new StringBuilder();
			if (sr.underline) {
				id.append(SSDRange.STYLE_UNDERLINE).append(
						StringConstants.COMMA);
			}
			if ((sr.fontStyle & SWT.BOLD) != 0) {
				id.append(SSDRange.STYLE_BOLD).append(StringConstants.COMMA);
			}
			if ((sr.fontStyle & SWT.ITALIC) != 0) {
				id.append(SSDRange.STYLE_ITALIC).append(StringConstants.COMMA);
			}
			if (id.length() > 1) {
				id.deleteCharAt(id.length() - 1);
			}
			SSDRange r = new SSDRange(sr.start, sr.length,
					SSDRange.TYPE_MARKUP, id.toString());
			sd.addRange(r);
		}
		return sd.toXML(false);
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
