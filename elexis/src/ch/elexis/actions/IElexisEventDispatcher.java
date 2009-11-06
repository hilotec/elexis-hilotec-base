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
 * $Id: IElexisEventDispatcher.java 5796 2009-11-06 05:47:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

/**
 * An IElexisEventDispatcher can be authoritative for a specific kind of originating Object
 * and/or for a specific EventType. It does so by registering with 
 * ElexisEventDistpatcher#registerDispatcher
 * in that case, Events of the goven Object/type will always be sent to the dispatcher instead
 * of sent directly to the listeners. Also, ElexisEvent#addListener and removeListener is
 * forwarded to the custom dispatcher.
 * @author gerry
 *
 */
public interface IElexisEventDispatcher {
	public void addListener(ElexisEventListener el);
	public void removeListener(ElexisEventListener el);
	public void fire(ElexisEvent ev);
}
