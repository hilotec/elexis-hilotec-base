/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: SelectorPanel.java 5034 2009-01-25 16:36:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.selectors;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.Desk;
import ch.rgw.tools.LimitSizeStack;

public class SelectorPanel extends Composite implements ActiveControlListener {
	boolean bCeaseFire, bExclusive;
	private LinkedList<ActiveControlListener> listeners = new LinkedList<ActiveControlListener>();
	private LimitSizeStack<TraceElement> undoList=new LimitSizeStack<TraceElement>(50);
	private ImageHyperlink hClr;
	
	public SelectorPanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.fill = true;
		layout.pack = true;
		setLayout(layout);
		hClr = Desk.getToolkit().createImageHyperlink(this,
				SWT.NONE);
		hClr.setImage(Desk.getImage(Desk.IMG_CLEAR));
		hClr.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				clearValues();
			}
		});
		hClr.setBackground(parent.getBackground());

	}

	
	public void setExclusive(boolean excl) {
		bExclusive = excl;
	}

	/*
	public void addTextFields(String... fields) {
		for (String s : fields) {
			new TextField(this, s).addSelectorListener(this);
		}
	}
	 */
	
	public void addField(ActiveControl ac){
		ac.addListener(this);
	}
	public void addFields(ActiveControl...activeControls){
		for(ActiveControl ac:activeControls){
			ac.addListener(this);
		}
	}
	public void removeField(String field) {
		for (Control c : getChildren()) {
			if (c instanceof ActiveControl) {
				if (((ActiveControl) c).getLabel().equalsIgnoreCase(field)) {
					((ActiveControl) c).removeSelectorListener(this);
					c.dispose();
				}
			}
		}
	}

	public void clearValues() {
		bCeaseFire = true;
		for (Control c : getChildren()) {
			if (c instanceof ActiveControl) {
				((ActiveControl) c).clear();
			}
		}
		bCeaseFire = false;
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> ret = new HashMap<String, String>();
		for (Control c : getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ret.put(ac.getLabel(), ac.getText());
			}
		}
		return ret;
	}

	public void contentsChanged(ActiveControl field) {
		if (bExclusive) {
			for (Control c : getChildren()) {
				if (c instanceof ActiveControl) {
					ActiveControl ac = (ActiveControl) c;
					if (!ac.getLabel().equals(field.getLabel())) {
						String t=ac.getText();
						if(t.length()>0){
							new TraceElement(ac);
							ac.clear();
						}
					}
				}
			}
		}
		new TraceElement(field);
		if (!bCeaseFire) {
			bCeaseFire = true;
			for (ActiveControlListener lis : listeners) {
				lis.contentsChanged(field);
			}
			bCeaseFire = false;
		}
	}

	public void addSelectorListener(ActiveControlListener l) {
		listeners.add(l);
	}

	public void removeSelectorListener(ActiveControlListener l) {
		listeners.remove(l);
	}

	public void titleClicked(final ActiveControl field){
		if(!bCeaseFire){
			bCeaseFire=true;
			for (ActiveControlListener lis : listeners) {
				lis.titleClicked(field);
			}
			bCeaseFire=true;
		}
		
	}
	
	private class TraceElement{
		ActiveControl control;
		String value;
		TraceElement(ActiveControl ac){
			control=ac;
			value=ac.getText();
			undoList.push(this);
		}
	}

	public void invalidContents(ActiveControl field){
		hClr.setImage(Desk.getImage(Desk.IMG_ACHTUNG));
		hClr.setToolTipText((String)field.getData(ActiveControl.POP_ERRMSG));
	}
}
