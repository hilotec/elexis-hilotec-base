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
 * $Id: FlatDataLoader.java 5117 2009-02-09 17:47:19Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;

/**
 * A PersistentObjectLoader for flat tables
 * 
 * @author Gerry
 * 
 */
public class FlatDataLoader extends PersistentObjectLoader implements ILazyContentProvider {
	private static final String LOADMESSAGE = "Lade Daten...";
	private List<? extends PersistentObject> raw = null;
	private List<? extends PersistentObject> filtered = null;
	protected String orderField;
	
	public FlatDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe){
		super(cv, qbe);
	}
	
	/*
	 * protected void reload(){ Desk.syncExec(new Runnable() {
	 * 
	 * public void run(){ TableViewer tv = (TableViewer) cv.getViewerWidget(); tv.setItemCount(1);
	 * tv.replace(LOADMESSAGE, 0);
	 * 
	 * qbe.clear(); cv.getConfigurer().getControlFieldProvider().setQuery(qbe); applyQueryFilters();
	 * if (orderField != null) { qbe.orderBy(false, orderField); }
	 * 
	 * raw = qbe.execute(); filtered = raw; tv.remove(LOADMESSAGE); tv.setItemCount(raw.size()); }
	 * });
	 * 
	 * }
	 */

	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params){
		final TableViewer tv = (TableViewer) cv.getViewerWidget();
		// tv.setItemCount(1);
		// tv.replace(LOADMESSAGE, 0);
		if (filtered != null) {
			filtered.clear();
		}
		filtered = null;
		qbe.clear();
		cv.getConfigurer().getControlFieldProvider().setQuery(qbe);
		applyQueryFilters();
		if (orderField != null) {
			qbe.orderBy(false, orderField);
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		raw = qbe.execute();
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		Desk.asyncExec(new Runnable() {
			public void run(){
				tv.setItemCount(0);
				filtered = raw;
				// tv.remove(LOADMESSAGE);
				tv.setItemCount(raw.size());
			}
		});
		
		return Status.OK_STATUS;
	}
	
	/*
	 * public void applyViewerFilter(){ LinkedList<PersistentObject> dest = new
	 * LinkedList<PersistentObject>(); for (PersistentObject po : raw) { if
	 * (viewerFilter.select(po)) { dest.add(po); } } filtered = dest; }
	 */

	public void updateElement(int index){
		if (index > 0 && index < filtered.size()) {
			Object o = filtered.get(index);
			if (o != null) {
				TableViewer tv = (TableViewer) cv.getViewerWidget();
				tv.replace(filtered.get(index), index);
			}
		}
	}
	
	public void setOrderField(String name){
		orderField = name;
	}
}
