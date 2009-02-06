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
 * $Id: TreeDataLoader.java 5096 2009-02-06 11:58:52Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.views.codesystems.CodeSelectorFactory;
import ch.rgw.tools.Tree;

public class TreeDataLoader extends PersistentObjectLoader implements ILazyTreeContentProvider {
	protected String parentColumn;
	protected Tree<PersistentObject> root;
	protected CodeSelectorFactory home;
	
	public TreeDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe, String parentField){
		super(cv, qbe);
		parentColumn = parentField;
		root = new Tree<PersistentObject>(null, null);
	}
	
	public TreeDataLoader(CodeSelectorFactory csf, CommonViewer cv,
		Query<? extends PersistentObject> qbe, String parentField){
		super(cv, qbe);
		parentColumn = parentField;
		root = new Tree<PersistentObject>(null, null);
		home = csf;
	}
	
	/*
	 * @Override protected void reload(){ qbe.clear(); qbe.add(parentColumn, "=", "NIL");
	 * applyQueryFilters(); for (PersistentObject po : qbe.execute()) { new
	 * Tree<PersistentObject>(root, po); } }
	 */
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params){
		monitor.beginTask("Lade Daten", IProgressMonitor.UNKNOWN);
		root.clear();
		qbe.clear();
		qbe.add(parentColumn, "=", "NIL");
		applyQueryFilters();
		for (PersistentObject po : qbe.execute()) {
			new Tree<PersistentObject>(root, po);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.worked(1);
		}
		monitor.done();
		
		Desk.asyncExec(new Runnable() {
			public void run(){
				((TreeViewer) cv.getViewerWidget()).setChildCount(home, root.getChildren().size());
			}
		});
		
		return Status.OK_STATUS;
	}
	
	public Object getParent(Object element){
		if (element instanceof Tree) {
			return ((Tree) element).getParent();
		}
		return null;
	}
	
	public void updateChildCount(Object element, int currentChildCount){
		int num = 0;
		if (element instanceof Tree) {
			Tree<PersistentObject> t = (Tree<PersistentObject>) element;
			if (!t.hasChildren()) {
				qbe.clear();
				qbe.add(parentColumn, "=", t.contents.getId());
				applyQueryFilters();
				for (PersistentObject po : qbe.execute()) {
					new Tree<PersistentObject>(t, po);
				}
			}
			num = t.getChildren().size();
		} else {
			num = root.getChildren().size();
		}
		((TreeViewer) cv.getViewerWidget()).setChildCount(element, num);
	}
	
	public void updateElement(Object parent, int index){
		Tree<PersistentObject> t;
		if (parent instanceof Tree) {
			t = (Tree<PersistentObject>) parent;
		} else {
			t = root;
		}
		Tree elem = t.getChildren().toArray(new Tree[0])[index];
		((TreeViewer) cv.getViewerWidget()).replace(parent, index, elem);
		updateChildCount(elem, 0);
	}
	
}
