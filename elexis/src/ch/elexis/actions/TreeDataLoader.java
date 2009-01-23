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
 * $Id: TreeDataLoader.java 5024 2009-01-23 16:36:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.rgw.tools.Tree;

public class TreeDataLoader extends PersistentObjectLoader implements ILazyTreeContentProvider {
	String parentColumn;
	Tree<PersistentObject> root;
	
	public TreeDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe, String parentField){
		super(cv, qbe);
		parentColumn = parentField;
		root = new Tree<PersistentObject>(null, null);
	}
	
	@Override
	protected void reload(){
		qbe.clear();
		qbe.add(parentColumn, "=", "NIL");
		applyFilters();
		for (PersistentObject po : qbe.execute()) {
			new Tree<PersistentObject>(root, po);
		}
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
				applyFilters();
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
		
	}
	
}
