/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: KonsExtension.java 4728 2008-12-04 12:04:58Z rgw_ch $
 *******************************************************************************/
package ch.elexis.icpc;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.text.EnhancedTextField;
import ch.elexis.util.IKonsExtension;

public class KonsExtension implements IKonsExtension {
	EnhancedTextField mine;
	static final String EPISODE_TITLE = "Problem: ";
	
	public String connect(final EnhancedTextField tf){
		mine = tf;
		mine.addDropReceiver(Episode.class, this);
		return Activator.PLUGIN_ID;
	}
	
	public boolean doLayout(final StyleRange n, final String provider, final String id){
		n.background = Desk.getColor(Desk.COL_GREEN);
		return true;
	}
	
	public boolean doXRef(final String refProvider, final String refID){
		Encounter enc = Encounter.load(refID);
		if (enc.exists()) {
			GlobalEvents.getInstance().fireSelectionEvent(enc);
		}
		return true;
	}
	
	public IAction[] getActions(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void insert(final Object o, final int pos){
		if (o instanceof Episode) {
			Episode ep = (Episode) o;
			final Konsultation k = GlobalEvents.getSelectedKons();
			Encounter enc = new Encounter(k, ep);
			List<IDiagnose> diags = ep.getDiagnoses();
			for (IDiagnose dg : diags) {
				k.addDiagnose(dg);
			}
			mine.insertXRef(pos, EPISODE_TITLE + ep.getLabel(), Activator.PLUGIN_ID, enc.getId());
			k.updateEintrag(mine.getDocumentAsText(), false);
			GlobalEvents.getInstance().fireObjectEvent(k, GlobalEvents.CHANGETYPE.update);
		}
		
	}
	
	public void removeXRef(final String refProvider, final String refID){
		Encounter encounter = Encounter.load(refID);
		encounter.delete();
		
	}
	
	public void setInitializationData(final IConfigurationElement config,
		final String propertyName, final Object data) throws CoreException{
	// TODO Auto-generated method stub
	
	}
	
}
