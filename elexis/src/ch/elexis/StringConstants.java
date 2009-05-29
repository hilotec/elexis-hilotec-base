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
 *    $Id: StringConstants.java 5322 2009-05-29 10:59:45Z rgw_ch $
 *******************************************************************************
 */
package ch.elexis;

/**
 * Utility Class for different constants. To ensure that same things are named identically in different program parts
 * @author gerry
 *
 */
public class StringConstants {
	public static final String SLASH="/";
	public static final String BACKSLASH="\\";
	public static final String SPACE=" ";
	public static final String EMPTY="";
	public static final String COMMA=",";
	public static final String COLON=":";
	public static final String DOUBLECOLON="::";
	
	public static final String ROLE_NAMING="Rolle";
	public static final String ROLES_NAMING="Rollen";
	
	public static final String ROLE_ADMIN= "Admin";
	public static final String ROLE_USERS= "Anwender";
	public static final String ROLE_ALL= "Alle";
	public static final String ROLES_DEFAULT=ROLE_ADMIN+","+ROLE_USERS+","+ROLE_ALL;
	
	
}
