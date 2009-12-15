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
 * $Id: LazyTreeLoader.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import ch.elexis.data.PersistentObject;

public class ElexisEvent {
	/** The Object was newly created */
	public static final int EVENT_CREATE=0x0001;
	/** The object is about to be deleted */
	public static final int EVENT_DELETE=0x0002;
	/** The object has changed some of its properties */
	public static final int EVENT_UPDATE=0x0004;
	/** All Objects of this class have been reloaded */
	public static final int EVENT_RELOAD=0x0008;
	/** The Object has been selected */
	public static final int EVENT_SELECTED=0x0010;
	/** All Objects of this type have been deselected */
	public static final int EVENT_DESELECTED=0x0020;
	
	PersistentObject obj;
	Class<? extends PersistentObject> objClass;
	int type;
	
	public ElexisEvent(PersistentObject o, Class<? extends PersistentObject> c, int type){
		obj=o;
		objClass=c;
		this.type=type;
	}
	
	/**
	 * Retrieve the object this event is about.
	 * @return the object that might be null (if the event concern a class)
	 */
	public PersistentObject getObject(){
		return obj;
	}
	
	/**
	 * Retrieve the class this event is about
	 * @return the class (that might me null)
	 */
	public Class<? extends PersistentObject> getObjectClass(){
		return objClass;
	}
	
	/**
	 * Retrieve the event type
	 * @return one ore more of the oabove EVENT_xxx flags
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * Check whether this event matches a template event. this method is
	 * only used internally by the framework and not intended to be called
	 * or overridden by clients
	 * @param event the template
	 * @return true on match
	 */
	public boolean matches(ElexisEvent event){
		if(event.getObject()!=null){
			if(!getObject().getId().equals(event.getObject().getId())){
				return false;
			}
		}
		if(event.getObjectClass()!=null){
			if(!getObjectClass().equals(event.getObjectClass())){
				return false;
			}
		}
		if(event.getType()!=0){
			if((type&event.getType())==0){
				return false;
			}
		}
		return true;
	}
}
