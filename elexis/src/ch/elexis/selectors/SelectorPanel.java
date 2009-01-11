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
 * $Id: SelectorPanel.java 4930 2009-01-11 17:33:49Z rgw_ch $
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

public class SelectorPanel extends Composite implements SelectorListener {
	boolean bCeaseFire;
	private LinkedList<SelectorListener> listeners = new LinkedList<SelectorListener>();
	
	public SelectorPanel(Composite parent){
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.fill = true;
		layout.pack = true;
		setLayout(layout);
		ImageHyperlink hClr = Desk.getToolkit().createImageHyperlink(this, SWT.NONE);
		hClr.setImage(Desk.getImage(Desk.IMG_CLEAR));
		hClr.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				clearValues();
			}
		});
		hClr.setBackground(parent.getBackground());
		
	}
	
	public void addFields(String... fields){
		for (String s : fields) {
			new SelectorField(this, s).addSelectorListener(this);
		}
	}
	
	public void removeField(String field){
		for (Control c : getChildren()) {
			if (c instanceof SelectorField) {
				if (((SelectorField) c).getLabel().equalsIgnoreCase(field)) {
					((SelectorField) c).removeSelectorListener(this);
					c.dispose();
				}
			}
		}
	}
	
	public void clearValues(){
		bCeaseFire = true;
		for (Control c : getChildren()) {
			if (c instanceof SelectorField) {
				((SelectorField) c).clear();
			}
		}
		bCeaseFire = false;
	}
	
	public HashMap<String, String> getValues(){
		HashMap<String, String> ret = new HashMap<String, String>();
		for (Control c : getChildren()) {
			if (c instanceof SelectorField) {
				SelectorField sf = (SelectorField) c;
				ret.put(sf.getLabel(), sf.getText());
			}
		}
		return ret;
	}
	
	public void selectionChanged(SelectorField field){
		if (!bCeaseFire) {
			for (SelectorListener lis : listeners) {
				lis.selectionChanged(field);
			}
		}
	}
	
	public void addSelectorListener(SelectorListener l){
		listeners.add(l);
	}
	
	public void removeSelectorListener(SelectorListener l){
		listeners.remove(l);
	}
}
