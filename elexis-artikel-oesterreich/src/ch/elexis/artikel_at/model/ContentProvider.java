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
 * $Id: ContentProvider.java 5062 2009-01-28 18:46:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_at.model;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.artikel_at.views.MedikamentSelector2;
import ch.elexis.data.Query;
import ch.elexis.selectors.SelectorPanel;
import ch.elexis.util.viewers.SelectorPanelProvider;
import ch.elexis.util.viewers.ViewerConfigurer.CommonContentProvider;

public class ContentProvider implements CommonContentProvider {
	MedikamentSelector2 msl;
	Query<Medikament> qMedi = new Query<Medikament>(Medikament.class);
	
	public ContentProvider(MedikamentSelector2 mine){
		msl = mine;
	}
	
	public void startListening(){
		msl.getConfigurer().getControlFieldProvider().addChangeListener(this);
		
	}
	
	public void stopListening(){
		msl.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}
	
	public Object[] getElements(Object inputElement){
		SelectorPanelProvider slp =
			(SelectorPanelProvider) msl.getConfigurer().getControlFieldProvider();
		SelectorPanel panel = slp.getPanel();
		HashMap<String, String> values = panel.getValues();
		qMedi.clear();
		String m = values.get(msl.SELECT_NAME);
		if (m.length() > 1) {
			qMedi.add("Name", "LIKE", m + "%");
		}
		List<Medikament> list = qMedi.execute();
		return list.toArray();
	}
	
	public void dispose(){
		stopListening();
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	// TODO Auto-generated method stub
	
	}
	
	public void changed(HashMap<String, String> vars){
		msl.reload();
	}
	
	public void reorder(String field){
		msl.reload();
		
	}
	
	public void selected(){
	// TODO Auto-generated method stub
	
	}
}
