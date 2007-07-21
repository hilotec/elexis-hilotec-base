/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: LazyTreeLoader.java 2860 2007-07-21 18:32:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IFilter;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.LazyTree;
import ch.elexis.util.Tree;
import ch.elexis.util.LazyTree.LazyTreeListener;

/**
 * Ein Job, der eine Baumstruktur "Lazy" aus der Datenbank lädt. D.h. es werden
 * immer nur die gerade benötigten Elemente geladen.
 * Die Baumstruktur muss so in einer Tabelle abgelegt sein, dass eine Spalte auf
 * das Elternelement verweist. 
 * @author gerry
 *
 * @param <T>
 */
public class LazyTreeLoader<T> extends AbstractDataLoaderJob implements LazyTreeListener{
	String parentColumn;
	String parentField;
	IFilter filter;
	IProgressMonitor monitor;
	
	public LazyTreeLoader(String Jobname, Query q, String parent, String[] orderBy){
		super(Jobname,q,orderBy);
        setReverseOrder(true);
		parentColumn=parent;
	}
	public void setFilter(IFilter f){
	    filter=f;
	    if(isValid()==true){
	        ((Tree)result).setFilter(f);
        }
	}
	public void setParentField(String f){
		parentField=f;
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public IStatus execute(IProgressMonitor moni) {
		monitor=moni;
    	if(monitor!=null){
        	monitor.subTask(getJobname());
        }
	    result=new LazyTree<T>(null,null,filter,this);
        qbe.clear();
	    qbe.add(parentColumn,"=","NIL"); //$NON-NLS-1$ //$NON-NLS-2$
        List<T> list=load();
	    for(T t:list){
	    	((LazyTree)result).add(t,this);
	        if(monitor!=null){
	        	monitor.worked(1);
	        }
	    }
        return Status.OK_STATUS;
	}

	@Override
	public int getSize() {
		return qbe.size();
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void fetchChildren(LazyTree l) {
		 qbe.clear();
		 PersistentObject obj=(PersistentObject) l.contents;
		 if(obj!=null){
			 qbe.add(parentColumn,"=",parentField==null ? obj.getId() : obj.get(parentField)); //$NON-NLS-1$
			 List ret=load();
			 for(PersistentObject o:(List<PersistentObject>)ret){
				 l.add(o,this);
			 }
		 }
	}
	public boolean hasChildren(LazyTree l) {
		fetchChildren(l);
		return (l.getFirstChild()==null);
	}

}
