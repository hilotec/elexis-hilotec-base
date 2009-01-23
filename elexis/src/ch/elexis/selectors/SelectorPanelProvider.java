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
 * $Id: CommonViewer.java 4799 2008-12-10 17:40:59Z psiska $
 *******************************************************************************/

package ch.elexis.selectors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Query;
import ch.elexis.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.util.viewers.ViewerConfigurer.ControlFieldProvider;

public class SelectorPanelProvider implements ControlFieldProvider {
	private LinkedList<ControlFieldListener> listeners = new LinkedList<ControlFieldListener>();
	private SelectorPanel panel;
	private String[] fields;
	private boolean bExclusive = false;
	
	public SelectorPanelProvider(String[] fields, boolean bExlusive){
		this.fields = fields;
		this.bExclusive = bExlusive;
	}
	
	public void addChangeListener(ControlFieldListener cl){
		listeners.add(cl);
	}
	
	public void clearValues(){
		if (panel != null) {
			panel.clearValues();
		}
	}
	
	public Composite createControl(Composite parent){
		panel = new SelectorPanel(parent);
		panel.addFields(fields);
		panel.setExclusive(bExclusive);
		panel.addSelectorListener(new SelectorListener() {
			
			public void selectionChanged(SelectorField field){
				fireChangedEvent();
			}
		});
		return panel;
	}
	
	public ViewerFilter createFilter(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void fireChangedEvent(){
		HashMap<String, String> hv = panel.getValues();
		String[] fld = new String[hv.size()];
		String[] val = new String[hv.size()];
		int i = 0;
		for (Entry<String, String> e : hv.entrySet()) {
			fld[i] = e.getKey();
			val[i++] = e.getValue();
		}
		for (ControlFieldListener cl : listeners) {
			cl.changed(fld, val);
		}
	}
	
	public void fireSortEvent(String text){
		for (ControlFieldListener cl : listeners) {
			cl.reorder(text);
		}
	}
	
	public String[] getValues(){
		HashMap<String, String> vals = panel.getValues();
		String[] ret = new String[fields.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = vals.get(fields[i]);
		}
		return ret;
	}
	
	public boolean isEmpty(){
		HashMap<String, String> vals = panel.getValues();
		for (String s : fields) {
			if (vals.get(s).length() > 0) {
				return false;
			}
		}
		return true;
	}
	
	public void removeChangeListener(ControlFieldListener cl){
		listeners.remove(cl);
	}
	
	public void setFocus(){
		panel.setFocus();
	}
	
	public void setQuery(Query q){
	// TODO Auto-generated method stub
	
	}
	
	public SelectorPanel getPanel(){
		return panel;
	}
}
