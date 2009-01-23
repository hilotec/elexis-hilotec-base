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
 * $Id: KontaktLoader.java 5004 2009-01-23 05:18:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.LinkedList;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.Desk;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;

public class KontaktLoader implements CommonContentProvider, ILazyContentProvider {
	CommonViewer cv;
	private static final String LOADMESSAGE = "Lade Daten...";
	private Object[] data = null;
	protected Query<Kontakt> qbe;
	protected String orderField;
	private LinkedList<FilterProvider> filters=new LinkedList<FilterProvider>();
	public Query<Kontakt> getQuery(){
		return qbe;
	}
	
	public KontaktLoader(CommonViewer cv){
		this.cv = cv;
		qbe=new Query<Kontakt>(Kontakt.class);
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
	// TODO Auto-generated method stub
	
	}
	
	public void updateElement(int index){
		((TableViewer) cv.getViewerWidget()).replace(data[index], index);
	}

	public void reload(){
		Desk.syncExec(new Runnable(){

			public void run(){
				TableViewer tv = (TableViewer) cv.getViewerWidget();
				tv.setItemCount(1);
				tv.replace(LOADMESSAGE, 0);

				qbe.clear();
				cv.getConfigurer().getControlFieldProvider().setQuery(qbe);
				for(FilterProvider fp:filters){
					fp.applyFilter(qbe);
				}
				if (orderField != null) {
					qbe.orderBy(false, orderField);
				}
				
				data = qbe.execute().toArray();
				tv.remove(LOADMESSAGE);
				tv.setItemCount(data.length);
				
			}});
	}
	public void addFilterProvider(FilterProvider fp){
		filters.add(fp);
	}
	
	public interface FilterProvider{
	    public void applyFilter(Query<? extends PersistentObject> qbe);
	}
}
