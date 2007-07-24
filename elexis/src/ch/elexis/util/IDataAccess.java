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
 * $Id$
 *******************************************************************************/

package ch.elexis.util;

import java.util.List;

import ch.elexis.data.PersistentObject;

/**
 * Interface that defines a number of data types to expose and grant access 
 * to them
 * @author gerry
 *
 */
public interface IDataAccess {
	public enum TYPE{STRING,INTEGER,DOUBLE}
	public static final int INVALID_PARAMETERS=1;
	public static final int OBJECT_NOT_FOUND=2;
	
	public static class Element{
		public Element(final TYPE typ, final String name, final Class<? extends PersistentObject> ref, final int numOfParams){
			this.typ=typ;
			this.name=name;
			this.reference=ref;
			this.numOfParams=numOfParams;
		}
		public TYPE getTyp(){
			return typ;
		}
		public String getName(){
			return name;
		}
		TYPE typ;
		String name;
		Class<? extends PersistentObject> reference;
		int numOfParams;
	}
	
	/**
	 * return a list of all data provided by this interface
	 * @return a (possibly empty) List of Elements 
	 */
	public List<Element> getList();
	
	/**
	 * return specified data
	 * @param name	Name of the element to retrieve
	 * @param params parameters that might be required for this element
	 * @return
	 */
	public Result<Object> getObject(String name, PersistentObject ref, String...params);
	
}
