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
 * $Id: SelectorPanel.java 5070 2009-01-30 17:49:34Z rgw_ch $
 *******************************************************************************/

package ch.elexis.selectors;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

import ch.elexis.Desk;
import ch.rgw.tools.LimitSizeStack;

public class SelectorPanel extends Composite implements ActiveControlListener {
	boolean bCeaseFire, bExclusive;
	private LinkedList<ActiveControlListener> listeners = new LinkedList<ActiveControlListener>();
	private LimitSizeStack<TraceElement> undoList = new LimitSizeStack<TraceElement>(50);
	private Composite cFields;
	private ToolBarManager tActions;
	private ToolBar tb;
	private IAction aClr;
	
	public SelectorPanel(Composite parent, IAction... actions){
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		/*
		 * RowLayout layout = new RowLayout(SWT.HORIZONTAL); layout.fill = true; layout.pack = true;
		 */
		FormLayout layout = new FormLayout();
		layout.marginLeft = 0;
		layout.marginRight = 0;
		setLayout(layout);
		tActions = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		
		aClr = new Action("Felder leeren") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_CLEAR));
			}
			
			@Override
			public void run(){
				clearValues();
			}
		};
		tActions.add(aClr);
		for (IAction ac : actions) {
			if (ac != null) {
				tActions.add(ac);
			} else {
				tActions.add(new Separator());
			}
		}
		tb = tActions.createControl(this);
		FormData fdActions = new FormData();
		fdActions.top = new FormAttachment(0, 0);
		fdActions.right = new FormAttachment(100, 0);
		tb.setLayoutData(fdActions);
		cFields = new Composite(this, SWT.NONE);
		FormData fd = new FormData();
		// fd.right = new FormAttachment(tb);
		// fd.bottom = new FormAttachment(0, 0);
		// fd.bottom = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		cFields.setLayoutData(fd);
		cFields.setLayout(new FillLayout());
		pack();
	}
	
	public Composite getFieldParent(){
		return cFields;
	}
	
	public void setExclusive(boolean excl){
		bExclusive = excl;
	}
	
	public void addActions(final IAction... actions){
		for (IAction ac : actions) {
			if (ac != null) {
				tActions.add(ac);
			} else {
				tActions.add(new Separator());
			}
		}
	}
	
	/*
	 * private static class ActionAdapter extends HyperlinkAdapter implements
	 * IPropertyChangeListener { IAction ac; ImageHyperlink hp;
	 * 
	 * ActionAdapter(final IAction action, final ImageHyperlink hp){ ac = action; this.hp = hp; }
	 * 
	 * @Override public void linkActivated(HyperlinkEvent e){ ac.run(); }
	 * 
	 * public void propertyChange(PropertyChangeEvent event){ hp.getImage().dispose();
	 * hp.setImage(ac.getImageDescriptor().createImage()); }
	 * 
	 * }
	 */

	public void addField(ActiveControl ac){
		ac.addListener(this);
	}
	
	public void addFields(ActiveControl... activeControls){
		ActiveControl last = null;
		for (ActiveControl ac : activeControls) {
			ac.addListener(this);
			last = ac;
		}
		if (tb.isReparentable() && last != null) {
			tb.setParent(last.getControllerComposite());
		}
		layout();
	}
	
	public void removeField(String field){
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				if (((ActiveControl) c).getLabel().equalsIgnoreCase(field)) {
					((ActiveControl) c).removeSelectorListener(this);
					c.dispose();
				}
			}
		}
	}
	
	public void clearValues(){
		
		Desk.asyncExec(new Runnable() {
			public void run(){
				bCeaseFire = true;
				for (Control c : cFields.getChildren()) {
					if (c instanceof ActiveControl) {
						((ActiveControl) c).clear();
					}
				}
				bCeaseFire = false;
				contentsChanged(null);
			}
		});
	}
	
	public HashMap<String, String> getValues(){
		HashMap<String, String> ret = new HashMap<String, String>();
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ret.put(ac.getLabel(), ac.getText());
			}
		}
		return ret;
	}
	
	public void contentsChanged(ActiveControl field){
		if (bExclusive && (field != null)) {
			for (Control c : cFields.getChildren()) {
				if (c instanceof ActiveControl) {
					ActiveControl ac = (ActiveControl) c;
					if (!ac.getLabel().equals(field.getLabel())) {
						String t = ac.getText();
						if (t.length() > 0) {
							new TraceElement(ac);
							ac.clear();
						}
					}
				}
			}
		}
		if (field != null) {
			new TraceElement(field);
		}
		if (!bCeaseFire) {
			bCeaseFire = true;
			for (ActiveControlListener lis : listeners) {
				lis.contentsChanged(field);
			}
			bCeaseFire = false;
		}
	}
	
	public void addSelectorListener(ActiveControlListener l){
		listeners.add(l);
	}
	
	public void removeSelectorListener(ActiveControlListener l){
		listeners.remove(l);
	}
	
	public void titleClicked(final ActiveControl field){
		if (!bCeaseFire) {
			bCeaseFire = true;
			for (ActiveControlListener lis : listeners) {
				lis.titleClicked(field);
			}
			bCeaseFire = true;
		}
		
	}
	
	private class TraceElement {
		ActiveControl control;
		String value;
		
		TraceElement(ActiveControl ac){
			control = ac;
			value = ac.getText();
			undoList.push(this);
		}
	}
	
	public void invalidContents(ActiveControl field){
		aClr.setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ACHTUNG));
		aClr.setToolTipText((String) field.getData(ActiveControl.POP_ERRMSG));
		
	}
}
