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
 *  $Id: Plafs.java 4967 2009-01-18 16:52:11Z rgw_ch $
 *******************************************************************************/
package ch.elexis.admin;

/**
 * Stub for lazter development of plafs: Provide Strings not omly depending of the locale
 * but also of the plaf.
 * A client request a String and Plafs returns the value of that String matching the
 * actual plaf
 * @author Gerry
 *
 */
public class Plafs {

	/**
	 * return a plaf'ed STring
	 * @param name Name of the String. The String may be prefixed by a namespace, separated with ::
	 * @return that String according to the current plaf
	 */
		public static String get(String name){
			String[] str=name.split("::");
			if(str.length>1){
				return str[1];
			}else{
				return str[0];
			}
		}
}
