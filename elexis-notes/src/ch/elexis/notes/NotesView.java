/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: NotesView.java 1853 2007-02-19 16:17:02Z rgw_ch $
 *******************************************************************************/
package ch.elexis.notes;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;

public class NotesView extends ViewPart implements ActivationListener, SelectionListener {
	ScrolledForm fMaster;
	NotesList master;
	NotesDetail detail;
	private IAction newCategoryAction,newNoteAction,delNoteAction;
	FormToolkit tk=Desk.theToolkit;
	
	@Override
	public void createPartControl(Composite parent) {
		SashForm sash=new SashForm(parent,SWT.HORIZONTAL);
		fMaster=tk.createScrolledForm(sash);
		fMaster.getBody().setLayout(new GridLayout());
		master=new NotesList(fMaster.getBody());
		master.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		detail=new NotesDetail(sash);
		//detail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		fMaster.setText("Kategorien");
		fMaster.getToolBarManager().add(newCategoryAction);
		fMaster.getToolBarManager().add(newNoteAction);
		fMaster.getToolBarManager().add(delNoteAction);
		newNoteAction.setEnabled(false);
		detail.setEnabled(false);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		fMaster.updateToolBar();
		sash.setWeights(new int[]{3,7});
		//fDetail.updateToolBar();
		//fDetail.reflow(true);
	}

	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	public void activation(boolean mode) {
	}

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(Class template) {
		if(template.equals(Note.class)){
			newNoteAction.setEnabled(false);
		}
		
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Note){
			Note note=(Note)obj;
			detail.setEnabled(true);
			detail.setNote(note);
			newNoteAction.setEnabled(true);
		}
	}
	private void makeActions(){
		newCategoryAction=new Action("Neue Kategorie"){
			{
				setToolTipText("Eine neue Haupt-Kategorie erstellen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
			}
			public void run(){
				InputDialog id=new InputDialog(getViewSite().getShell(),"Neue Hauptkategorie erstellen",
						"Bitte geben Sie einen namen für die neue Kategorie ein","",null);
				if(id.open()==Dialog.OK){
					/*Note note=*/new Note(null,id.getValue(),"");
					master.tv.refresh();
				}
			}
		};
		newNoteAction=new Action("Neue Notiz"){
			{
				setToolTipText("Neue Notiz oder Unterkategorie erstellen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_ADDITEM));
			}
			public void run(){
				Note act=(Note)GlobalEvents.getInstance().getSelectedObject(Note.class);
				if(act!=null){
					InputDialog id=new InputDialog(getViewSite().getShell(),"Neue Notiz erstellen",
							"Bitte geben Sie einen namen für die neue Notiz oder Unterkategorie ein","",null);
					if(id.open()==Dialog.OK){
						/*Note note=*/new Note(act,id.getValue(),"");
						master.tv.refresh();
					}
				}
			}
			
		};
		delNoteAction=new Action("Löschen..."){
			{
				setToolTipText("Notiz und alle Unterkategorien löschen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
			}
			public void run(){
				Note act=(Note)GlobalEvents.getInstance().getSelectedObject(Note.class);
				if(act!=null){
					if(SWTHelper.askYesNo("Notiz(en) löschen", "Wirklich diesen Eintrag und alle Untereinträge löschen?")){
						act.delete();
						master.tv.refresh();
					}
				}
			}
			
		};
	}
}
