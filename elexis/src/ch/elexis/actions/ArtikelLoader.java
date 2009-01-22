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
 * $Id: ArtikelLoader.java 5001 2009-01-22 15:50:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;
import ch.rgw.tools.ExHandler;

public class ArtikelLoader implements CommonContentProvider, ILazyContentProvider {
	
	CommonViewer cv;
	private static final String LOADMESSAGE = "Lade Daten...";
	private Object[] data = null;
	protected Query<?> qbe;
	protected String orderField;
	
	public ArtikelLoader(CommonViewer cv){
		this.cv = cv;
		
	}
	
	public void startListening(){
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}
	
	public void stopListening(){
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
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
		orderField = field;
		reload();
		
	}
	
	public void selected(){
	// TODO Auto-generated method stub
	
	}
	
	public void updateElement(int index){
		((TableViewer) cv.getViewerWidget()).replace(data[index], index);
	}
	
	public Object[] getElements(Object inputElement){
		
		return null;
	}
	
	/*
	 * public void reload(){ TableViewer tv = (TableViewer) cv.getViewerWidget();
	 * tv.setItemCount(1); tv.replace(LOADMESSAGE, 0);
	 * 
	 * try { PlatformUI.getWorkbench().getProgressService().run(false, false, new
	 * IRunnableWithProgress() {
	 * 
	 * public void run(IProgressMonitor monitor) throws InvocationTargetException,
	 * InterruptedException{ monitor.beginTask(LOADMESSAGE, IProgressMonitor.UNKNOWN); qbe.clear();
	 * cv.getConfigurer().getControlFieldProvider().setQuery(qbe); if (orderField != null) {
	 * qbe.orderBy(false, orderField); } data = qbe.execute().toArray(); monitor.done(); } }); }
	 * catch (Exception e) { ExHandler.handle(e); } tv.remove(LOADMESSAGE);
	 * tv.setItemCount(data.length); }
	 */
	public void reload(){
		Desk.syncExec(new Runnable(){

			public void run(){
				TableViewer tv = (TableViewer) cv.getViewerWidget();
				tv.setItemCount(1);
				tv.replace(LOADMESSAGE, 0);

				qbe.clear();
				cv.getConfigurer().getControlFieldProvider().setQuery(qbe);
				if (orderField != null) {
					qbe.orderBy(false, orderField);
				}
				
				data = qbe.execute().toArray();
				tv.remove(LOADMESSAGE);
				tv.setItemCount(data.length);
				
			}});
	}
}
