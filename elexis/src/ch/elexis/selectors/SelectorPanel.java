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
 * $Id: SelectorPanel.java 5355 2009-06-14 10:35:19Z rgw_ch $
 *******************************************************************************/

package ch.elexis.selectors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
	private LimitSizeStack<TraceElement> undoList = new LimitSizeStack<TraceElement>(
			50);
	private Composite cFields;
	private ToolBarManager tActions;
	private ToolBar tb;
	private IAction aClr;

	public SelectorPanel(Composite parent, IAction... actions) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		/*
		 * RowLayout layout = new RowLayout(SWT.HORIZONTAL); layout.fill = true;
		 * layout.pack = true;
		 */
		FormLayout layout = new FormLayout();
		layout.marginLeft = 0;
		layout.marginRight = 0;
		setLayout(layout);
		tActions = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);

		aClr = new Action(Messages.SelectorPanel_clearFields) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_CLEAR));
			}

			@Override
			public void run() {
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
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		cFields.setLayoutData(fd);
		cFields.setLayout(new FillLayout());
		pack();
	}

	public Composite getFieldParent() {
		return cFields;
	}

	/**
	 * If set, writing in one field will clear all oder fields in the panel
	 * 
	 * @param excl
	 *            true if there can only one field with text
	 */
	public void setExclusive(boolean excl) {
		bExclusive = excl;
	}

	/**
	 * Add Actions to display in the uper right corner of the panel
	 * 
	 * @param actions
	 */
	public void addActions(final IAction... actions) {
		for (IAction ac : actions) {
			if (ac != null) {
				tActions.add(ac);
			} else {
				tActions.add(new Separator());
			}
		}
	}

	/**
	 * Add a field to the panel
	 * 
	 * @param ac
	 */
	public void addField(ActiveControl ac) {
		ac.addListener(this);
	}

	/**
	 * Add a number of fields to the Panel
	 * 
	 * @param activeControls
	 */
	public void addFields(ActiveControl... activeControls) {
		ActiveControl last = null;
		for (ActiveControl ac : activeControls) {
			ac.addListener(this);
			last = ac;
		}
		if (tb.isReparentable() && last != null) {
			Composite ctl = last.getControllerComposite();
			ctl.setLayout(new FormLayout());
			tb.setParent(ctl);
			// last.setLayout(new FormLayout());
		}
		layout();
	}

	/**
	 * Remove a field from the panel
	 * 
	 * @param field
	 */
	public void removeField(String field) {
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				if (((ActiveControl) c).getLabel().equalsIgnoreCase(field)) {
					((ActiveControl) c).removeSelectorListener(this);
					c.dispose();
				}
			}
		}
	}

	/**
	 * Clear all fields to their default "empty" value
	 */
	public void clearValues() {

		Desk.asyncExec(new Runnable() {
			public void run() {
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

	/**
	 * Return the values of all fields.
	 * 
	 * @return A HashMap with the label of each field as keys and the respective
	 *         field contents as values
	 */
	public HashMap<String, String> getValues() {
		HashMap<String, String> ret = new HashMap<String, String>();
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ret.put(ac.getLabel(), ac.getText());
			}
		}
		return ret;
	}
	/**
	 * Return all ActiveControls attached to this panel
	 * @return al List that might be empty but is never null
	 */
	public List<ActiveControl> getControls(){
		LinkedList<ActiveControl> ret=new LinkedList<ActiveControl>();
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ret.add(ac);
			}
		}
		return ret;
	}
	/**
	 * From ActiveControlListener: Notify that the contents of a field has changed
	 * This will in turn notify the SelectorListeners attached to this panel
	 */
	public void contentsChanged(ActiveControl field) {
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

	/**
	 * Add a listener to the list of listeners that will be notified, if one of the fields has been changed
	 * @param l
	 */
	public void addSelectorListener(ActiveControlListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a listener from the list of SelectorListeners
	 * @param l
	 */
	public void removeSelectorListener(ActiveControlListener l) {
		listeners.remove(l);
	}
	/**
	 * From ActiveControlListener: Notify that the user clicked the label of a field
	 * This will in turn notify the SelectorListeners attached to this panel
	 */

	public void titleClicked(final ActiveControl field) {
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

		TraceElement(ActiveControl ac) {
			control = ac;
			value = ac.getText();
			undoList.push(this);
		}
	}

	/**
	 * inform the user, that a field has invalid content
	 */
	public void invalidContents(ActiveControl field) {
		aClr.setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ACHTUNG));
		aClr.setToolTipText((String) field.getData(ActiveControl.PROP_ERRMSG));

	}
	
	public void setLock(boolean bLocked){
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ac.setEnabled(bLocked);
			}
		}

	}
}
