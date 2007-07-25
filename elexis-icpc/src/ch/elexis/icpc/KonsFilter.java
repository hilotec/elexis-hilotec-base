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
 * $Id: KonsFilter.java 2905 2007-07-25 10:53:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc;

import java.util.List;

import org.eclipse.jface.viewers.IFilter;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.IObjectFilterProvider;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.icpc.views.EpisodesView;

public class KonsFilter implements IObjectFilterProvider, IFilter {
	Episode mine;
	EpisodesView home;
	
	public KonsFilter(final EpisodesView home){
		this.home=home;
	}
	public void setProblem(final Episode problem){
		mine=problem;
		GlobalEvents.getInstance().fireUpdateEvent(Konsultation.class);
	}
	
	public void activate() {
		home.activateKonsFilterAction(true);
	}
	public void changed() {
		// should we mind?
	}
	public void deactivate() {
		home.activateKonsFilterAction(false);
	}
	public IFilter getFilter() {
		return this;
	}
	public String getId() {
		return "ch.elexis.icpc.konsfilter";
	}
	public boolean select(final Object toTest) {
		if(mine==null){
			return true;
		}
		if(toTest instanceof Konsultation){
			Konsultation k=(Konsultation)toTest;
			List<Encounter> list=new Query<Encounter>(Encounter.class,"EpisodeID",mine.getId()).execute();
			for(Encounter enc:list){
				if(enc.get("KonsID").equals(k.getId())){
					return true;
				}
			}
		}
		return false;
	}
}
