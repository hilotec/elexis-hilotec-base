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
 * $Id: TreeDataLoader.java 5008 2009-01-23 11:19:49Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;

public class TreeDataLoader extends PersistentObjectLoader implements ILazyTreeContentProvider {
	String parentColumn;
	
	public TreeDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe, String parentField){
		super(cv, qbe);
		parentColumn = parentField;
	}
	
	@Override
	protected void reload(){
	// TODO Auto-generated method stub
	
	}
	
	public Object getParent(Object element){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void updateChildCount(Object element, int currentChildCount){
	// TODO Auto-generated method stub
	
	}
	
	public void updateElement(Object parent, int index){
	// TODO Auto-generated method stub
	
	}
	
}
