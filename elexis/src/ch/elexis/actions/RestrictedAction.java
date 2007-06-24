/*******************************************************************************
 * Copyright (c) 2007, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - final implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;

import ch.elexis.Hub;

/**
 * Special class for actiosn requiring special access rights.
 * 
 * The run() method of this class checks the required access rights
 * and runs doRun() if access is granted.
 * 
 * Classes extending this class must implement the method doRun() instead
 * of run().
 * 
 * Users of this class may register a listener to get informed about missing
 * rights during execution of the action. (Usually, this is not required,
 * because the action is disabled if the user has not the required rights.
 * See setEnabled())
 * 
 */
abstract public class RestrictedAction extends Action {
	protected String necessaryRight;
	private List<RestrictionListener> listeners = new ArrayList<RestrictionListener>();
	
	public RestrictedAction(String necessaryRight) {
		this.necessaryRight = necessaryRight;
	}

	public RestrictedAction(String necessaryRight, String text, int style) {
		super(text, style);
		this.necessaryRight = necessaryRight;
	}

	public RestrictedAction(String necessaryRight, String text) {
		super(text);
		this.necessaryRight = necessaryRight;
	}

	/**
	 * Sets the enabled status of this action according to the required right.
	 * Unchecks the action if the required right is not available.
	 */
	public void reflectRight() {
		if (Hub.acl.request(necessaryRight)) {
			setEnabled(true);
		} else {
			setEnabled(false);
			setChecked(false);
		}
	}

	/**
	 * Checks the required access rights and then calls doRun().
	 */
	public void run() {
		if (Hub.acl.request(necessaryRight)) {
			doRun();
		} else {
			RestrictionEvent event = new RestrictionEvent(necessaryRight);
			fireRestrictionEvent(event);
		}
	}

	private void fireRestrictionEvent(RestrictionEvent event) {
		for (RestrictionListener listener : listeners) {
			listener.restricted(event);
		}
	}
	
	/**
	 * Called by RestrictedAction.run() after access rights check.
	 *
	 * Classes extending RestrictedAction must implement this method instead
	 * of run().
	 */
	abstract public void doRun();
	
	/**
	 * Register a listener to get informed about missing rights.
	 * @param listener the listener to register.
	 */
	public void addRestrictionListener(RestrictionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a previously registered listener.
	 * @param listener the listener to remove.
	 */
	public void removeRestrictionListener(RestrictionListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Users of this class can register a RestrictionListener to get informed
	 * about missing rights during execution of the action.
	 *  
	 * @author danlutz
	 */
	public interface RestrictionListener {
		/**
		 * The action has been executed with missing rights.
		 * @param event a RestrictionEvent containing the required right.
		 */
		public void restricted(RestrictionEvent event);
	}
	
	/**
	 * Event containing the required right. 
	 * @author danlutz
	 */
	public class RestrictionEvent {
		public String necessaryRight;
		
		public RestrictionEvent(String necessaryRight) {
			this.necessaryRight = necessaryRight;
		}
	}
}
