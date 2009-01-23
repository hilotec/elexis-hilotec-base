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
 * $Id: ArtikelLoader.java 5004 2009-01-23 05:18:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.LinkedList;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;

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
	private LinkedList<FilterProvider> filters = new LinkedList<FilterProvider>();
	
	public PersistentObjectLoader(CommonViewer cv, Query<? extends PersistentObject> qbe){
		this.cv = cv;
		this.qbe = qbe;
	}
	
	public Query<? extends PersistentObject> getQuery(){
		return qbe;
	}
	
	public void startListening(){
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
		
	}
	
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
	
	public void changed(String[] fields, String[] values){
		reload();
		
	}
	
	public void reorder(String field){
		reload();
		
	}
	
	public void selected(){

	}
	
	public void addFilterProvider(FilterProvider fp){
		filters.add(fp);
	}
	
	public interface FilterProvider {
		public void applyFilter(Query<? extends PersistentObject> qbe);
	}
	
	protected abstract void reload();
}
