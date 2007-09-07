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
 * $Id: ContentProvider.java 3107 2007-09-07 11:03:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.Query;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;

public class ContentProvider implements CommonContentProvider {
	CommonViewer cv;
	Query<BAGMedi> qbe;
	
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
		qbe=new Query<BAGMedi>(BAGMedi.class);
		return qbe.execute().toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	public void changed(final String[] fields, final String[] values) {

	}

	public void reorder(final String field) {

	}

	public void selected() {

	}

}
