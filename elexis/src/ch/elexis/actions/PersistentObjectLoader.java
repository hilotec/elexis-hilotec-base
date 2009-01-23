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
 * $Id: PersistentObjectLoader.java 5026 2009-01-23 17:32:50Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.LinkedList;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer.CommonContentProvider;
import ch.rgw.tools.IFilter;

/**
 * This is a replacement for the former BackgroundJob-System. Since it became clear that the
 * database access takes less than 10% of the total time needed for reload of a CommonViewer, the
 * BackgroundJobs were not adequate for this task. Furthermore, there werde several issues with
 * those widely used jobs.
 * 
 * PersistentObjectLoader is a much simpler replacement and does not load in background.
 * 
 * @author Gerry
 * 
 */
public abstract class PersistentObjectLoader implements CommonContentProvider {
	protected CommonViewer cv;
	protected Query<? extends PersistentObject> qbe;
	private LinkedList<QueryFilter> queryFilters = new LinkedList<QueryFilter>();
	protected IFilter viewerFilter;
	
	public PersistentObjectLoader(CommonViewer cv, Query<? extends PersistentObject> qbe){
		this.cv = cv;
		this.qbe = qbe;
	}
	
	public Query<? extends PersistentObject> getQuery(){
		return qbe;
	}
	
	/**
	 * start listening the selector fields of the ControlField of the loader's CommonViewer. If the
	 * user enters text or clicks the headings, a changed() or reorder() event will be fired
	 */
	public void startListening(){
		viewerFilter = cv.getConfigurer().getControlFieldProvider().createFilter();
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}
	
	/**
	 * stop listening the selector fields
	 */
	public void stopListening(){
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}
	
	public Object[] getElements(Object inputElement){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void dispose(){
		stopListening();
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		reload();
		
	}
	
	/**
	 * One or more of the ControlField's selectors habe been changed
	 * 
	 * @param fields
	 *            the field names
	 * @param values
	 *            the new values
	 */
	public void changed(String[] fields, String[] values){
		reload();
		// applyViewerFilter();
	}
	
	/**
	 * The user request reordering of the table
	 * 
	 * @param field
	 *            the field name after which the table should e reordered
	 */
	public void reorder(String field){
		reload();
		
	}
	
	public void selected(){

	}
	
	public void addQueryFilter(QueryFilter fp){
		synchronized (queryFilters) {
			queryFilters.add(fp);
		}
	}
	
	public void removeQueryFilter(QueryFilter fp){
		synchronized (queryFilters) {
			queryFilters.remove(fp);
		}
	}
	
	public void applyQueryFilters(){
		synchronized (queryFilters) {
			for (QueryFilter fp : queryFilters) {
				fp.apply(qbe);
			}
		}
	}
	
	/**
	 * a FilterProvider can modify the Query of this Loader. It will be called before each reload.
	 * 
	 * @author Gerry
	 * 
	 */
	public interface QueryFilter {
		public void apply(Query<? extends PersistentObject> qbe);
	}
	
	protected abstract void reload();
	
	protected abstract void applyViewerFilter();
}
