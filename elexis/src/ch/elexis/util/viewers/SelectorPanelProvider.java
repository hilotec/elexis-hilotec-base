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

package ch.elexis.util.viewers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.selectors.ActiveControl;
import ch.elexis.selectors.ActiveControlListener;
import ch.elexis.selectors.ComboField;
import ch.elexis.selectors.DateField;
import ch.elexis.selectors.IntegerField;
import ch.elexis.selectors.MoneyField;
import ch.elexis.selectors.SelectorPanel;
import ch.elexis.selectors.TextField;
import ch.elexis.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.rgw.tools.IFilter;

public class SelectorPanelProvider implements ControlFieldProvider {
	private LinkedList<ControlFieldListener> listeners = new LinkedList<ControlFieldListener>();
	private SelectorPanel panel;
	private FieldDescriptor<? extends PersistentObject>[] fields;
	private boolean bExclusive = false;
	
	public SelectorPanelProvider(FieldDescriptor<? extends PersistentObject>[] fields,
		boolean bExlusive){
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
		for (FieldDescriptor<? extends PersistentObject> field : fields) {
			ActiveControl ac = null;
			switch (field.tFeldTyp) {
			case STRING:
				ac = new TextField(panel, 0, field.sAnzeige);
				break;
			case CURRENCY:
				ac = new MoneyField(panel, 0, field.sAnzeige);
				break;
			case DATE:
				ac = new DateField(panel, 0, field.sAnzeige);
				break;
			
			case COMBO:
				ac = new ComboField(panel, 0, field.sAnzeige, (String[])field.ext);
				break;
			case INT:
				ac=new IntegerField(panel,0,field.sAnzeige);
			}
			 ac.setData(ActiveControl.PROP_FIELDNAME, field.sFeldname);
			 ac.setData(ActiveControl.PROP_HASHNAME, field.sHashname);
			panel.addField(ac);
		}
		
		panel.setExclusive(bExclusive);
		panel.addSelectorListener(new ActiveControlListener() {
			
			public void contentsChanged(ActiveControl field){
				fireChangedEvent();
			}
			
			public void titleClicked(ActiveControl field){
				fireClickedEvent(field.getLabel());
			}

			public void invalidContents(ActiveControl field){
				// TODO Auto-generated method stub
				
			}
		});
		return panel;
	}
	
	public IFilter createFilter(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void fireClickedEvent(final String fieldname){
		for (ControlFieldListener cl : listeners) {
			cl.reorder(fieldname);
		}
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
		for (FieldDescriptor<? extends PersistentObject> fd : fields) {
			if (vals.get(fd.sAnzeige).length() > 0) {
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
	
	public void setQuery(Query<? extends PersistentObject> q){
	// TODO Auto-generated method stub
	
	}
	
	public SelectorPanel getPanel(){
		return panel;
	}
}
