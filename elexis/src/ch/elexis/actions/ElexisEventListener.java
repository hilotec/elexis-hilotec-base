/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: LazyTreeLoader.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

public interface ElexisEventListener {
	/**
	 * An Event was fired
	 * @param ev the Event
	 */
	public void catchElexisEvent(ElexisEvent ev);
	
	/**
	 * Filter the events this listener wants to be informed
	 * @return An ElexisEvent with matching<ul>
	 * <li>object: Only events ov this object will be sent</li>
	 * <li>class: Only events ov this class wuill be sent</li>
	 * <li>type: Only eevnts matching to one ore more flags in type will be sent</li>
	 * </ul>
	 */
	public ElexisEvent getElexisEventFilter();
}
