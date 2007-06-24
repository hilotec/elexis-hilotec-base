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
 *  $Id: KonsExtension.java 2131 2007-03-19 16:33:53Z rgw_ch $
 *******************************************************************************/
package ch.elexis.icpc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.text.EnhancedTextField;
import ch.elexis.util.IKonsExtension;

public class KonsExtension implements IKonsExtension {
	EnhancedTextField mine;
	
	public String connect(EnhancedTextField tf) {
		mine=tf;
		mine.addDropReceiver(Episode.class, this);
		return Activator.PLUGIN_ID;
	}

	public boolean doLayout(StyleRange n, String provider, String id) {
		n.background=Desk.theColorRegistry.get(Desk.COL_GREEN);
		return true;
	}

	public boolean doXRef(String refProvider, String refID) {
		Encounter enc=Encounter.load(refID);
		if(enc.exists()){
			GlobalEvents.getInstance().fireSelectionEvent(enc);
		}
		return true;
	}

	public IAction[] getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	public void insert(Object o, int pos) {
		if(o instanceof Episode){
			Episode ep=(Episode)o;
			Encounter enc=new Encounter(GlobalEvents.getSelectedKons(),ep);
			mine.insertXRef(pos, "ICPC-Episode: "+ep.getLabel(), Activator.PLUGIN_ID, enc.getId());
		}

	}

	public void removeXRef(String refProvider, String refID) {
		// TODO Auto-generated method stub

	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub

	}

}
