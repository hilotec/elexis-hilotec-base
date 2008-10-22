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
 *  $Id: NotesView.java 4624 2008-10-22 13:32:17Z rgw_ch $
 *******************************************************************************/
package ch.elexis.notes;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.ImageData;
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
import ch.elexis.exchange.IScannerAccess;
import ch.elexis.exchange.IScannerAccess.ISource;
import ch.elexis.util.Extensions;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class NotesView extends ViewPart implements ActivationListener, SelectionListener {
	ScrolledForm fMaster;
	NotesList master;
	NotesDetail detail;
	boolean hasScanner = false;
	private IAction newCategoryAction, newNoteAction, delNoteAction, scanAction;
	FormToolkit tk = Desk.getToolkit();
	
	@Override
	public void createPartControl(Composite parent){
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		fMaster = tk.createScrolledForm(sash);
		fMaster.getBody().setLayout(new GridLayout());
		master = new NotesList(fMaster.getBody());
		master.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		detail = new NotesDetail(sash);
		// detail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		fMaster.setText("Kategorien");
		if (hasScanner) {
			fMaster.getToolBarManager().add(scanAction);
			fMaster.getToolBarManager().add(new Separator());
		}
		fMaster.getToolBarManager().add(newCategoryAction);
		fMaster.getToolBarManager().add(newNoteAction);
		fMaster.getToolBarManager().add(delNoteAction);
		newNoteAction.setEnabled(false);
		detail.setEnabled(false);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		fMaster.updateToolBar();
		sash.setWeights(new int[] {
			3, 7
		});
		// fDetail.updateToolBar();
		// fDetail.reflow(true);
	}
	
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	public void activation(boolean mode){}
	
	public void visible(boolean mode){
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}
	
	public void clearEvent(Class template){
		if (template.equals(Note.class)) {
			newNoteAction.setEnabled(false);
		}
		
	}
	
	public void selectionEvent(PersistentObject obj){
		if (obj instanceof Note) {
			Note note = (Note) obj;
			detail.setEnabled(true);
			detail.setNote(note);
			newNoteAction.setEnabled(true);
		}
	}
	
	private void makeActions(){
		newCategoryAction = new Action("Neue Kategorie") {
			{
				setToolTipText("Eine neue Haupt-Kategorie erstellen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
			}
			
			public void run(){
				InputDialog id =
					new InputDialog(getViewSite().getShell(), "Neue Hauptkategorie erstellen",
						"Bitte geben Sie einen namen für die neue Kategorie ein", "", null);
				if (id.open() == Dialog.OK) {
					/* Note note= */new Note(null, id.getValue(), "");
					master.tv.refresh();
				}
			}
		};
		newNoteAction = new Action("Neue Notiz") {
			{
				setToolTipText("Neue Notiz oder Unterkategorie erstellen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
			}
			
			public void run(){
				Note act = (Note) GlobalEvents.getInstance().getSelectedObject(Note.class);
				if (act != null) {
					InputDialog id =
						new InputDialog(
							getViewSite().getShell(),
							"Neue Notiz erstellen",
							"Bitte geben Sie einen namen für die neue Notiz oder Unterkategorie ein",
							"", null);
					if (id.open() == Dialog.OK) {
						/* Note note= */new Note(act, id.getValue(), "");
						master.tv.refresh();
					}
				}
			}
			
		};
		delNoteAction = new Action("Löschen...") {
			{
				setToolTipText("Notiz und alle Unterkategorien löschen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
			}
			
			public void run(){
				Note act = (Note) GlobalEvents.getInstance().getSelectedObject(Note.class);
				if (act != null) {
					if (SWTHelper.askYesNo("Notiz(en) löschen",
						"Wirklich diesen Eintrag und alle Untereinträge löschen?")) {
						act.delete();
						master.tv.refresh();
					}
				}
			}
			
		};
		if (Extensions.isServiceAvailable("ScannerService")) {
			hasScanner = true;
			scanAction = new Action("Scannen...") {
				{
					setToolTipText("Document mit dem Scanner einlesen");
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_IMPORT));
				}
				
				public void run(){
					try {
						IScannerAccess scanner =
							(IScannerAccess) Extensions.findBestService("ScannerService");
						if (scanner != null) {
							ISource defsrc = scanner.getDefaultSource();
							if (defsrc != null) {
								ArrayList<ImageData> images = new ArrayList<ImageData>();
								do {
									ImageData imageData = scanner.aquire(defsrc);
									images.add(imageData);
								} while (SWTHelper.askYesNo("Bild gelesen",
									"Weitere Seiten hinzufügen?"));
								Note act = (Note) GlobalEvents.getInstance().getSelectedObject(Note.class);
								InputDialog id =
									new InputDialog(
										getViewSite().getShell(),
										"Neues Dokument einbinden",
										"Bitte geben Sie einen Namen für das eben gescannte Dokument ein.",
										"", null);
								if (id.open() == Dialog.OK) {
									/* Note note= */new Note(act, id.getValue(), "");
											master.tv.refresh();
								}
							}
						}		
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
			};
		}
		
	}
}
