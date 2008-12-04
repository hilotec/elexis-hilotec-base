/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FieldDisplayView.java 4722 2008-12-04 10:11:09Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * This view displays the content of an arbitrary field.
 * 
 * @author gerry
 * 
 */
public class FieldDisplayView extends ViewPart implements ActivationListener, SelectionListener,
		HeartListener {
	public static final String ID = "ch.elexis.dbfielddisplay";
	private IAction newViewAction, editDataAction;
	Text text;
	Class<? extends PersistentObject> myClass;
	String myField;
	boolean bCanEdit;
	ScrolledForm form;
	FormToolkit tk = Desk.getToolkit();
	String subid;
	String NODE = "FeldAnzeige";
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		form = tk.createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getBody().setLayout(new GridLayout());
		text = tk.createText(form.getBody(), "", SWT.MULTI | SWT.V_SCROLL);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent arg0){
				if (bCanEdit) {
					PersistentObject mine = GlobalEvents.getInstance().getSelectedObject(myClass);
					if (mine != null) {
						mine.set(myField, text.getText());
					}
				}
			}
			
		});
		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0){
				arg0.doit = bCanEdit;
				
			}
			
			public void keyReleased(KeyEvent arg0){}
		});
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newViewAction, editDataAction);
		String nx = "Patient.Diagnosen";
		Integer canEdit = null;
		subid = getViewSite().getSecondaryId();
		if (subid == null) {
			subid = "defaultData";
		}
		nx = Hub.userCfg.get("FieldDisplayViewData/" + subid, null);
		canEdit = Hub.userCfg.get("FieldDisplayViewCanEdit/" + subid, 0);
		setField(nx == null ? "Patient.Diagnosen" : nx, canEdit == null ? false : (canEdit != 0));
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
	}
	
	/*
	 * @Override public void init(IViewSite site, IMemento memento) throws PartInitException {
	 * super.init(site, memento); String nx="Patient.Diagnosen"; Integer canEdit=null;
	 * if(memento!=null){ subid=site.getSecondaryId(); if(subid!=null){
	 * nx=memento.getString(NODE+":"+subid); canEdit=memento.getInteger(NODE+":"+subid+":canEdit");
	 * } } setField(nx==null ? "Patient.Diagnosen" : nx, canEdit==null ? false : (canEdit!=0)); }
	 * 
	 * 
	 * 
	 * @Override public void saveState(IMemento memento) { if(memento!=null){ if(subid!=null){
	 * memento.putString(NODE+":"+subid, myClass.getSimpleName()+"."+myField);
	 * memento.putInteger(NODE+":"+subid+":canEdit", bCanEdit ? 1 : 0); } }
	 * super.saveState(memento); }
	 */

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	
	@Override
	public void setFocus(){
		text.setFocus();
	}
	
	public void activation(boolean mode){

	}
	
	public void visible(boolean mode){
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
			Hub.heart.addListener(this);
			heartbeat();
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
			Hub.heart.removeListener(this);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void clearEvent(Class template){
		if (template.equals(myClass)) {
			text.setText("");
		}
	}
	
	public void selectionEvent(PersistentObject obj){
		if (myClass.isInstance(obj)) {
			String val = obj.get(myField);
			if (val == null) {
				SWTHelper.showError("Fehler bei der Felddefinition", "Das Feld " + myField
					+ " kann nicht aufgelöst werden");
				text.setText("");
			} else {
				text.setText(obj.get(myField));
			}
		} else if (obj instanceof Anwender) {
			String nx = Hub.userCfg.get("FieldDisplayViewData/" + subid, null);
			Integer canEdit = Hub.userCfg.get("FieldDisplayViewCanEdit/" + subid, 0);
			setField(nx == null ? "Patient.Diagnosen" : nx, canEdit == null ? false
					: (canEdit != 0));
		}
	}
	
	public void heartbeat(){
		PersistentObject mine = GlobalEvents.getInstance().getSelectedObject(myClass);
		if (mine == null) {
			clearEvent(myClass);
		} else {
			selectionEvent(mine);
		}
	}
	
	private void setField(String field, boolean canEdit){
		String[] def = field.split("\\.");
		if (def.length != 2) {
			SWTHelper.showError("Falsche Felddefinition",
				"Bitte Feld in der Form 'Datentyp.Feldname' angeben");
		} else {
			myClass = resolveName(def[0]);
			if (myClass != null) {
				myField = def[1];
				bCanEdit = canEdit;
				setPartName(myField);
				Hub.userCfg.set("FieldDisplayViewData/" + subid, myClass.getSimpleName() + "."
					+ myField);
				Hub.userCfg.set("FieldDisplayViewCanEdit/" + subid, canEdit);
				
			}
		}
	}
	
	private Class resolveName(String k){
		Class ret = null;
		if (k.equalsIgnoreCase("Mandant")) {
			ret = Mandant.class;
		} else if (k.equalsIgnoreCase("Anwender")) {
			ret = Anwender.class;
		} else {
			try {
				String fqname = "ch.elexis.data." + k;
				ret = Class.forName(fqname);
			} catch (java.lang.Exception ex) {
				SWTHelper.showError("Feldtyp falsch", "Der gewünschte Datentyp " + k
					+ " konnte nicht erkannt werden");
				ret = null;
			}
		}
		return ret;
	}
	
	private void makeActions(){
		newViewAction = new Action("Neues Fenster") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
				setToolTipText("Ein neues Fenster öffnen");
			}
			
			@Override
			public void run(){
				try {
					String fieldtype = new SelectDataDialog().run();
					FieldDisplayView n =
						(FieldDisplayView) getViewSite().getPage().showView(ID,
							StringTool.unique("DataDisplay"), IWorkbenchPage.VIEW_VISIBLE);
					n.setField(fieldtype, false);
					heartbeat();
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		};
		editDataAction = new Action("Datentyp...") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText("Den angezeigten Datentyp einstellen");
			}
			
			public void run(){
				SelectDataDialog sdd = new SelectDataDialog();
				if (sdd.open() == Dialog.OK) {
					setField(sdd.result, sdd.bEditable);
					heartbeat();
				}
			}
		};
	}
	
	class SelectDataDialog extends TitleAreaDialog {
		String[] nodes;
		Combo cbNodes;
		Button btEditable;
		String result;
		boolean bEditable;
		
		SelectDataDialog(){
			super(getViewSite().getShell());
		}
		
		String run(){
			create();
			if (nodes.length > 1) {
				if (open() == Dialog.OK) {
					return result;
				}
				
			}
			return nodes[0];
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cbNodes = new Combo(ret, SWT.SINGLE);
			cbNodes.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			nodes = Hub.localCfg.get(NODE, "Patient.Diagnosen").split(",");
			cbNodes.setItems(nodes);
			btEditable = new Button(ret, SWT.CHECK);
			btEditable.setText("Feld kann direkt geändert werden");
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle("Datentyp");
			setMessage(
				"Geben Sie bitte einen Ausdruck der Form Objekttyp.Feldname ein (z.B. Patient.Diagnosen)",
				IMessageProvider.INFORMATION);
		}
		
		@Override
		protected void okPressed(){
			String tx = cbNodes.getText();
			if (StringTool.getIndex(nodes, tx) == -1) {
				String tm = StringTool.join(nodes, ",") + "," + tx;
				Hub.localCfg.set(NODE, tm);
			}
			result = tx;
			bEditable = btEditable.getSelection();
			super.okPressed();
		}
		
	}
}
