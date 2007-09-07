/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ContentProvider.java 3111 2007-09-07 19:45:29Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.Query;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;

public class ContentProvider implements CommonContentProvider {
	CommonViewer cv;
	Query<BAGMedi> qbm=new Query<BAGMedi>(BAGMedi.class);
	Query<Substance> qbs=new Query<Substance>(Substance.class);
	
	
	public ContentProvider(final CommonViewer mine){
		cv=mine;
	}
	public void startListening() {
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);

	}

	public void stopListening() {
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}

	public Object[] getElements(final Object inputElement) {
		qbm.clear();
		qbs.clear();
		List<BAGMedi> lMedi=null;
		List<Substance> lSubst=null;
		ControlFieldProvider cfp=(ControlFieldProvider)cv.getConfigurer().getControlFieldProvider();
		String[] vals=cfp.getValues();
		qbm.add("Typ", "=", "Medikament");
		SortedSet<BAGMedi> ret=new TreeSet<BAGMedi>();
		if(vals[0].length()>1){
			qbm.add("Name", "LIKE", vals[0]+"%");
			qbm.orderBy(false,new String[]{"Name"});
			lMedi=qbm.execute();
		}
		if(vals[1].length()>1){
			qbs.add("name", "LIKE", vals[1]+"%");
			qbs.orderBy(false, new String[]{"name"});
			lSubst=qbs.execute();
			for(Substance subst:lSubst){
				ret=subst.findMedis(ret);
			}
			if(lMedi!=null){
				Iterator<BAGMedi> it=ret.iterator();
				while(it.hasNext()){
					BAGMedi bm=it.next();
					if(!lMedi.contains(bm)){
						it.remove();
					}
				}
			}
		}else{
			if(lMedi==null){
				return qbm.execute().toArray();
			}else{
				return lMedi.toArray();
			}
		}
		return ret.toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	public void changed(final String[] fields, final String[] values) {
		cv.notify(CommonViewer.Message.update_keeplabels);
	}

	public void reorder(final String field) {
		cv.notify(CommonViewer.Message.update_keeplabels);
	}

	public void selected() {

	}

}
