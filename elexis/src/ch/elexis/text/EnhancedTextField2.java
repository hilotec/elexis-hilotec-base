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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.ElexisException;
import ch.elexis.StringConstants;
import ch.elexis.services.GlobalServiceDescriptors;
import ch.elexis.text.IRangeRenderer.OUTPUT;
import ch.elexis.text.model.SSDRange;
import ch.elexis.text.model.SimpleStructuredDocument;
import ch.elexis.util.Extensions;
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
	private List<SSDRange> ranges;
	private HashMap<String, IRangeRenderer> renderers = new HashMap<String, IRangeRenderer>();

	public EnhancedTextField2(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	public void addXrefHandler(String id, IKonsExtension ike) {
		renderers.put(id, adapt(ike));
	}

	@Override
	public void insertXRef(int pos, String textToDisplay, String providerId,
			String itemID) {
		if(ranges==null){
			ranges=new LinkedList<SSDRange>();
		}
		SSDRange sdr=new SSDRange(pos, textToDisplay.length(), providerId, itemID);
		ranges.add(sdr);
		StyleRange sr=new StyleRange();
		sr.start=pos;
		sr.length=textToDisplay.length();
		sr.data=sdr;
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
		return getContents().toXML(false);
	}

	@Override
	public String getContentsPlaintext() {
		return st.getText();
	}

	public SimpleStructuredDocument getContents(){
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
		return sd;
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

	@Override
	public void setXrefHandlers(Map<String, IKonsExtension> handlers) {
		// we don't need xrefhandlers but some clients send them, so convert to renderers
		for(String key:handlers.keySet()){
			renderers.put(key, adapt(handlers.get(key)));
		}
	}

	void doFormat(SimpleStructuredDocument ssd) throws ElexisException{
		st.setText(ssd.getPlaintext());
		for (SSDRange r : ssd.getRanges()) {
			IRangeRenderer renderer = renderers.get(r.getType());
			if (renderer == null) {
				renderer = (IRangeRenderer) Extensions.findBestService(
						GlobalServiceDescriptors.TEXT_CONTENTS_EXTENSION,
						r.getType());
				if (renderer != null) {
					renderers.put(r.getType(), renderer);
				}
			}
			if (renderer == null
					|| (!renderer.canRender(r.getType(),
							IRangeRenderer.OUTPUT.STYLED_TEXT))) {
				String hint = r.getHint();
			} else {
				Object rendered = renderer
						.doRender(r, OUTPUT.STYLED_TEXT, this);
				if (rendered instanceof StyleRange) {
					StyleRange sr = (StyleRange) rendered;
					st.setStyleRange(sr);
		
				} 
			}
		}

	}
	
	/**
	 * Adapter for existing code. DO NOT use this in new code
	 * convert an IKonsExtension to an IRangeRenderer
	 * @param ik an iKonsExtention
	 * @return an IRangeRenderer with the same properties as the input
	 * @deprecated only for compatibility reasons
	 */
	IRangeRenderer adapt(final IKonsExtension ik){
		return new IRangeRenderer(){

			@Override
			public boolean canRender(String rangeType, OUTPUT outputType) {
				return outputType.equals(OUTPUT.STYLED_TEXT);
			}

			@Override
			public Object doRender(SSDRange range, OUTPUT outputType,
					IRichTextDisplay display) throws ElexisException {
				StyleRange sr=new StyleRange();
				sr.start=range.getPosition();
				sr.length=range.getLength();
				ik.doLayout(sr, range.getHint(), range.getID());
				return sr;
			}

			@Override
			public IAction[] getActions(String rangeType) {
				return ik.getActions();
			}

			@Override
			public boolean onSelection(SSDRange range) {
				return ik.doXRef(range.getContents(), range.getID());
				
			}

			@Override
			public void inserted(SSDRange range, Object context) {
				ik.insert(range, 0);
			}

			@Override
			public void removed(SSDRange range, Object context) {
				ik.removeXRef(range.getContents(), range.getID());
			}};
	}
}
