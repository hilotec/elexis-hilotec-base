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
 *  $Id: ACE.java 4945 2009-01-13 17:50:00Z rgw_ch $
 *******************************************************************************/
package ch.elexis.admin;

/**
 * AcessControlElement: An item constituting a named right. AccessControlElements are
 * collected hiearchically in ACL's (AccessControlLists)
 * @since 2.0
 * @author gerry
 *
 */
public class ACE {
	public static ACE ROOT_ACE=new ACE(null,"root","Wurzel");
	
	private String name;
	private String localizedName;
	private ACE parent;
	
	public ACE(ACE parent, String name, String localizedName){
		this.parent=parent;
		this.name=name;
		this.localizedName=localizedName;
	}
	/**
	 * @return the non-translatable name of this ACE
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return the localised Name of this ACE
	 */
	public String getLocalizedName(){
		return localizedName;
	}
	
	/**
	 * @return the parent ACE
	 */
	public ACE getParent(){
		return parent;
	}
}
