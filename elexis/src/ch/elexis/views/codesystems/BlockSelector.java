/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BlockSelector.java 5039 2009-01-25 19:49:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.codesystems;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridLayout;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultControlFieldProvider;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.util.viewers.CommonViewer.DoubleClickListener;

public class BlockSelector extends CodeSelectorFactory {
	IAction deleteAction, renameAction;
	CommonViewer cv;
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		this.cv = cv;
		makeActions();
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			
			public void menuAboutToShow(IMenuManager manager){
				manager.add(renameAction);
				manager.add(deleteAction);
				
			}
		});
		cv.setContextMenu(mgr);
		return new ViewerConfigurer(new BlockContentProvider(cv), new DefaultLabelProvider(),
			new DefaultControlFieldProvider(cv, new String[] {
				"Name"
			}), new ViewerConfigurer.DefaultButtonProvider(cv, Leistungsblock.class),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null));
		
	}
	
	@Override
	public Class getElementClass(){
		return Leistungsblock.class;
	}
	
	@Override
	public void dispose(){

	}
	
	private void makeActions(){
		deleteAction = new Action("Block löschen") {
			@Override
			public void run(){
				Object o = cv.getSelection()[0];
				if (o instanceof Leistungsblock) {
					((Leistungsblock) o).delete();
					cv.notify(CommonViewer.Message.update);
				}
			}
		};
		renameAction = new Action("umbenennen") {
			@Override
			public void run(){
				Object o = cv.getSelection()[0];
				if (o instanceof Leistungsblock) {
					Leistungsblock lb = (Leistungsblock) o;
					InputDialog dlg =
						new InputDialog(Desk.getTopShell(), "Block umbenennen",
							"Geben Sie bitte einen neuen Namen für den Block ein", lb.get("Name"),
							null);
					if (dlg.open() == Dialog.OK) {
						lb.set("Name", dlg.getValue());
						cv.notify(CommonViewer.Message.update);
					}
					
				}
			}
		};
	}
	
	public static class BlockContentProvider implements ViewerConfigurer.CommonContentProvider,
			ITreeContentProvider, BackingStoreListener {
		CommonViewer cv;
		ViewerFilter filter;
		
		BlockContentProvider(CommonViewer c){
			cv = c;
		}
		
		public void startListening(){
			/*
			 * Menu menu=new Menu(cv.getViewerWidget().getControl()); MenuItem mi=new
			 * MenuItem(menu,SWT.NONE); mi.setText("Löschen"); mi.addSelectionListener(new
			 * SelectionAdapter(){
			 * 
			 * @Override public void widgetSelected(SelectionEvent e) { Object
			 * o=cv.getSelection()[0]; if(o instanceof Leistungsblock){
			 * ((Leistungsblock)o).delete(); cv.notify(CommonViewer.Message.update); } }
			 * 
			 * }); cv.getViewerWidget().getControl().setMenu(menu);
			 */
			cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
			GlobalEvents.getInstance().addBackingStoreListener(this);
			
		}
		
		public void reloadContents(Class clazz){
			if (clazz.equals(Leistungsblock.class)) {
				cv.notify(CommonViewer.Message.update);
			}
			
		}
		
		public void stopListening(){
			cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
		}
		
		public Object[] getElements(Object inputElement){
			Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
			qbe.orderBy(false, new String[] {
				"Name"
			});
			return qbe.execute().toArray();
		}
		
		public void dispose(){
			stopListening();
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
		
		/** Vom ControlFieldProvider */
		public void changed(HashMap<String, String> vals){
			TreeViewer tv = (TreeViewer) cv.getViewerWidget();
			if (filter != null) {
				tv.removeFilter(filter);
				filter = null;
			}
			cv.notify(CommonViewer.Message.update);
			if (cv.getConfigurer().getControlFieldProvider().isEmpty()) {
				cv.notify(CommonViewer.Message.empty);
			} else {
				cv.notify(CommonViewer.Message.notempty);
				filter = (ViewerFilter) cv.getConfigurer().getControlFieldProvider().createFilter();
				tv.addFilter(filter);
				
			}
			
		}
		
		/** Vom ControlFieldProvider */
		public void reorder(String field){

		}
		
		/** Vom ControlFieldProvider */
		public void selected(){
		// nothing to do
		}
		
		public Object[] getChildren(Object parentElement){
			if (parentElement instanceof Leistungsblock) {
				Leistungsblock lb = (Leistungsblock) parentElement;
				return lb.getElements().toArray();
				
			}
			return new Object[0];
		}
		
		public Object getParent(Object element){
			return null;
		}
		
		public boolean hasChildren(Object element){
			if (element instanceof Leistungsblock) {
				return !(((Leistungsblock) element).isEmpty());
			}
			return false;
		}
		
	};
	
	public static class bsPage extends cPage {
		bsPage(CTabFolder ctab, CodeSelectorFactory cs){
			super(ctab);
			setLayout(new GridLayout());
			cv = new CommonViewer();
			vc = cs.createViewerConfigurer(cv);
			cv.create(vc, this, SWT.NONE, this);
			cv.getViewerWidget().getControl().setLayoutData(
				SWTHelper.getFillGridData(1, true, 1, true));
			vc.getContentProvider().startListening();
			
			// add double click listener for CodeSelectorTarget
			cv.addDoubleClickListener(new DoubleClickListener() {
				public void doubleClicked(PersistentObject obj, CommonViewer cv){
					ICodeSelectorTarget target = GlobalEvents.getInstance().getCodeSelectorTarget();
					if (target != null) {
						/*
						 * String title = "Element hinzufügen"; String message =
						 * "Wollen Sie das ausgewählte Element " + "'" + obj.getLabel() + "' zu " +
						 * "'" + target.getName() + "' hinzufügen?"; if (SWTHelper.askYesNo(title,
						 * message)) {
						 */
						if (obj instanceof Leistungsblock) {
							Leistungsblock block = (Leistungsblock) obj;
							List<ICodeElement> elements = block.getElements();
							for (ICodeElement codeElement : elements) {
								if (codeElement instanceof PersistentObject) {
									PersistentObject po = (PersistentObject) codeElement;
									target.codeSelected(po);
								}
							}
						} else {
							// PersistentObject
							target.codeSelected(obj);
						}
						/*
						 * }
						 */
					}
				}
			});
		}
		
		public void refresh(){
			cv.notify(CommonViewer.Message.update);
		}
		
	}
	
	@Override
	public String getCodeSystemName(){
		return "Block";
	}
	
	public void reloadContents(Class clazz){

	}
}
