/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 * $Id: ElexisEvent.java 6040 2010-02-01 12:54:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

public class ElexisEvent {
	/** The Object was newly created */
	public static final int EVENT_CREATE = 0x0001;
	/** The object is about to be deleted */
	public static final int EVENT_DELETE = 0x0002;
	/** The object has changed some of its properties */
	public static final int EVENT_UPDATE = 0x0004;
	/** All Objects of this class have been reloaded */
	public static final int EVENT_RELOAD = 0x0008;
	/** The Object has been selected */
	public static final int EVENT_SELECTED = 0x0010;
	/** All Objects of this type have been deselected */
	public static final int EVENT_DESELECTED = 0x0020;
	/** a user logged out or logged in */
	public static final int EVENT_USER_CHANGED = 0x0040;
	/** the mandator changed */
	public static final int EVENT_MANDATOR_CHANGED = 0x080;
	
	PersistentObject obj;
	Class<?> objClass;
	int type;
	
	public ElexisEvent(final PersistentObject o, final Class<?> c, final int type){
		obj = o;
		objClass = c;
		this.type = type;
	}
	
	/**
	 * Retrieve the object this event is about.
	 * 
	 * @return the object that might be null (if the event concern a class)
	 */
	public PersistentObject getObject(){
		return obj;
	}
	
	/**
	 * Retrieve the class this event is about
	 * 
	 * @return the class (that might be null)
	 */
	public Class<?> getObjectClass(){
		if (objClass == null) {
			if (obj != null) {
				return obj.getClass();
			}
		}
		return objClass;
	}
	
	/**
	 * Retrieve the event type
	 * 
	 * @return one ore more of the oabove EVENT_xxx flags
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * Check whether this event matches a template event. this method is only used internally by the
	 * framework and not intended to be called or overridden by clients
	 * 
	 * @param event
	 *            the template
	 * @return true on match
	 */
	
	boolean matches(final ElexisEvent event){
		if (event.getObject() != null) {
			if (!getObject().getId().equals(event.getObject().getId())) {
				return false;
			}
		}
		if (event.getObjectClass() != null) {
			if (!getObjectClass().equals(event.getObjectClass())) {
				return false;
			}
		}
		if (event.getType() != 0) {
			if ((type & event.getType()) == 0) {
				return false;
			}
		}
		return true;
	}
	
	boolean isSame(ElexisEvent other){
		if (other == null) {
			return false;
		}
		if (other.obj == null) {
			if (this.obj == null) {
				if (other.objClass != null) {
					if (other.objClass.equals(this.objClass)) {
						if (other.type == this.type) {
							return true;
						}
					}
				}
			}
		} else {
			if (other.obj.equals(this.obj)) {
				if (other.type == this.type) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static ElexisEvent createUserEvent(){
		return new ElexisEvent(Hub.actUser, Anwender.class, ElexisEvent.EVENT_USER_CHANGED);
	}
	
	public static ElexisEvent createPatientEvent(){
		return new ElexisEvent(ElexisEventDispatcher.getSelectedPatient(), Patient.class,
			EVENT_SELECTED);
	}
}
