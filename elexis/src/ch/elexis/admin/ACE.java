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
 *  $Id: ACE.java 4967 2009-01-18 16:52:11Z rgw_ch $
 *******************************************************************************/
package ch.elexis.admin;

import java.io.Serializable;

/**
 * AcessControlElement: An item constituting a named right. AccessControlElements are collected
 * hiearchically in ACL's (AccessControlLists). An ACE has a parent, an internal name and a
 * (probably localized) external name that will be shown to the user
 * 
 * @since 2.0
 * @author gerry
 * 
 */
public class ACE implements Serializable {
	private static final long serialVersionUID = 34320020090119L;
	
	public static final ACE ACE_ROOT = new ACE(null, "root", "Wurzel");
	public static final ACE ACE_IMPLICIT = new ACE(ACE.ACE_ROOT, "implicit", "implizit");
	
	private String name;
	private String localizedName;
	private ACE parent;
	
	/**
	 * Create a new ACE
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-evel ACE, use ACE_ROOT as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID
	 * @param localizedName
	 *            the name that will be presented to the user.
	 */
	public ACE(ACE parent, String name, String localizedName){
		this.parent = parent;
		this.name = name;
		this.localizedName = localizedName;
	}
	
	/**
	 * create a new ACE with now localized name - The localized name will be the same as the
	 * internal name. So this constructor should not be used for ACE's that will be shown to the
	 * user.
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-evel ACE, use ACE_ROOT as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID.
	 */
	public ACE(ACE parent, String name){
		this(parent, name, name);
	}
	
	/**
	 * @return the non-translatable name of this ACE
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return the localized Name of this ACE
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
	
	/**
	 * Change the localized name of this ACE
	 * @param lName a new name to use as localized name
	 */
	public void setLocalizedName(String lName){
		localizedName = lName;
	}
	
	public String getCanonicalName(){
		StringBuilder sp=new StringBuilder();
		sp.append(getName());
		ACE parent=getParent();
		while((parent!=null) && (!parent.equals(ACE.ACE_ROOT))){
			sp.insert(0, parent.getName()+"/");
			parent=parent.getParent();
		}
		return sp.toString();
	}
}
