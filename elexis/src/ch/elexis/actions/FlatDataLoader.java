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
 * $Id: FlatDataLoader.java 5008 2009-01-23 11:19:49Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;

/**
 * A PersistentObjectLoader for flat tables
 * 
 * @author Gerry
 * 
 */
public class FlatDataLoader extends PersistentObjectLoader implements ILazyContentProvider {
	private static final String LOADMESSAGE = "Lade Daten...";
	private Object[] data = null;
	protected String orderField;
	
	public FlatDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe){
		super(cv, qbe);
	}
	
	@Override
	protected void reload(){
		Desk.syncExec(new Runnable() {
			
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
				// tv.refresh(true);
			}
		});
		
	}
	
	public void updateElement(int index){
		TableViewer tv = (TableViewer) cv.getViewerWidget();
		tv.replace(data[index], index);
	}
	
	public void setOrderField(String name){
		orderField = name;
	}
}
