/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: LazyTree.java 4702 2008-11-30 07:17:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jface.viewers.IFilter;


/** 
 * Ein Tree, der seine Children erst bei Bedarf lädt. Dazu muss ein
 * LazyTreeListener übergeben werden, der die Children liefern muss. 
 * @author gerry
 *
 */
public class LazyTree<T> extends Tree<T> {
	LazyTreeListener listen;
	
	@SuppressWarnings("unchecked")
	public LazyTree(Tree<T>p, T elem, LazyTreeListener l, Comparator<T> comp){
		super(p,elem,comp);
		listen=l;
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public LazyTree(Tree<T>p, T elem, LazyTreeListener l){
		super(p,elem);
		listen=l;
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public LazyTree(Tree<T> p, T elem, IFilter f, LazyTreeListener l){
		super(p,elem,f);
		listen=l;
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Collection<Tree<T>>getChildren(){
		loadChildren();
		return super.getChildren();
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public boolean hasChildren(){
		if(first==null){
			return (listen==null ? false : listen.hasChildren(this));
		}
		return true;
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public LazyTree<T> add(T elem, LazyTreeListener l){
		LazyTree<T> ret=new LazyTree<T>(this,elem,filter,l);
		return ret;
	}
	
	// Stack Overflow?? //TODO
	private void loadChildren(){
		if((first==null) && (listen!=null)){
			listen.fetchChildren(this);
		}
	}

	public Tree<T> getFirstChild() {
		loadChildren();
		return first;
	}

	public interface LazyTreeListener{
		public void fetchChildren(LazyTree l);
		public boolean hasChildren(LazyTree l);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public synchronized Tree move(Tree newParent) {
		if(!(newParent instanceof LazyTree)){
			preload();
		
		}
		return super.move(newParent);
	}
	
	public Tree preload(){
		loadChildren();
		for(Tree child=first;child!=null;child=child.next){
			if(child instanceof LazyTree){
				((LazyTree)child).preload();
			}
		}
		return this;
	}
	
}
