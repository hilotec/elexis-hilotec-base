/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: OmnivoreView.java 1835 2007-02-18 09:12:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.omnivore.views;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.omnivore.data.DocHandle;
import ch.elexis.util.SWTHelper;


/**
 	A class do receive documents by drag&drop. Documents are imported into the database and linked
 	to the selected patient. On double-click they are opened with their associated application.
 */

public class OmnivoreView extends ViewPart implements ActivationListener, SelectionListener {
	private TableViewer viewer;
	private Table table;
	private Action importAction, editAction,deleteAction;
	private Action doubleClickAction;
	private String[] colLabels={"Datum","Titel","Stichwörter"};
	private int[] colWidth={80,150,500};

	/*
	 * 
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			Query<DocHandle> qbe=new Query<DocHandle>(DocHandle.class);
			Patient pat=GlobalEvents.getSelectedPatient();
			if(pat!=null){
				qbe.add("PatID", "=", pat.getId());
				return qbe.execute().toArray();
			}else{
				return new Object[0];
			}
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			switch(index){
			case 0: return ((DocHandle)obj).get("Datum"); 
			case 1: return ((DocHandle)obj).get("Titel");
			case 2: return ((DocHandle)obj).get("Keywords");
			default: return "?";
			}
		}
		public Image getColumnImage(Object obj, int index) {
			return null; //getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public OmnivoreView() {
		DocHandle.load("1"); // make sure the table is created
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		table=new Table(parent,SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		TableColumn[] cols=new TableColumn[colLabels.length];
		for(int i=0;i<colLabels.length;i++){
			cols[i]=new TableColumn(table,SWT.NONE);
			cols[i].setWidth(colWidth[i]);
			cols[i].setText(colLabels[i]);
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setUseHashlookup(true);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		Transfer[] transferTypes=new Transfer[]{FileTransfer.getInstance()};
		viewer.addDropSupport(DND.DROP_COPY, transferTypes, new DropTargetAdapter(){

			
			@Override
			public void dragEnter(DropTargetEvent event) {
				event.detail=DND.DROP_COPY;
			}

			@Override
			public void drop(DropTargetEvent event) {
				String[] files=(String[])event.data;
				for(String file:files){
					DocHandle.assimilate(file);
					viewer.refresh();
				}
				
			}
			
		});
		GlobalEvents.getInstance().addActivationListener(this, this);
		viewer.setInput(getViewSite());

	}
	

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this, this);
		super.dispose();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				OmnivoreView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(importAction);
		//manager.add(new Separator());
		//manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(editAction);
		manager.add(deleteAction);
		//manager.add(action2);
		// Other plug-ins can contribute there actions here
		//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(importAction);
		//manager.add(action2);
	}

	private void makeActions() {
		importAction = new Action("Importiere") {
			{
				setToolTipText("Externes Dokument importieren");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_IMPORT));
			}
			public void run() {
				FileDialog fd=new FileDialog(getViewSite().getShell(),SWT.OPEN);
				String filename=fd.open();
				if(filename!=null){
					DocHandle.assimilate(filename);
					viewer.refresh();
				}
			}
		};
		
		deleteAction=new Action("Löschen"){
			{
				setToolTipText("Dokument löschen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
			}
			public void run(){
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				DocHandle dh=(DocHandle)obj;
				if(SWTHelper.askYesNo("Wirklich löschen?", "Möchten Sie "+dh.get("Titel")+" wirklich löschen?")){
					dh.delete();
					viewer.refresh();
				}
			}
		};
		editAction=new Action("Bearbeiten"){
			{
				setToolTipText("Dokumentbeschreibung bearbeiten");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
			}
			public void run(){
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				FileImportDialog fid=new FileImportDialog((DocHandle)obj);
				if(fid.open()==Dialog.OK){
					viewer.refresh(true);
				}
				
			}
		};
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				DocHandle dh=(DocHandle)obj;
				dh.execute();
				
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
			viewer.refresh();
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(PersistentObject obj) {
		viewer.refresh();
	}
}
