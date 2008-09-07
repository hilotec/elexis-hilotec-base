/*******************************************************************************
 * Copyright (c) 2007, Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich and D. Lutz - initial implementation
 *    
 * $Id: ObjectFilterRegistry.java 4382 2008-09-07 13:58:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.Hashtable;

import org.eclipse.jface.viewers.IFilter;

import ch.elexis.actions.GlobalEvents.IObjectFilterProvider;
import ch.elexis.data.PersistentObject;

public class ObjectFilterRegistry {
	
	private final Hashtable<Class<? extends PersistentObject>, GlobalEvents.IObjectFilterProvider> hash =
		new Hashtable<Class<? extends PersistentObject>, GlobalEvents.IObjectFilterProvider>();
	
	public synchronized void registerObjectFilter(final Class<? extends PersistentObject> clazz,
		final GlobalEvents.IObjectFilterProvider provider){
		IObjectFilterProvider old = hash.get(clazz);
		if (old != null) {
			old.deactivate();
		}
		hash.put(clazz, provider);
		provider.activate();
		GlobalEvents.getInstance().fireUpdateEvent(clazz);
	}
	
	public void unregisterObjectFilter(final Class<? extends PersistentObject> clazz,
		final GlobalEvents.IObjectFilterProvider provider){
		hash.remove(clazz);
		provider.deactivate();
		GlobalEvents.getInstance().fireUpdateEvent(clazz);
	}
	
	public IFilter getFilterFor(final Class<? extends PersistentObject> clazz){
		IObjectFilterProvider prov = hash.get(clazz);
		if (prov != null) {
			return prov.getFilter();
		}
		return null;
	}
}
