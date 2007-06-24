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
 *  $Id: ACLContributor.java 2328 2007-05-04 16:35:21Z rgw_ch $
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControl;
import ch.elexis.admin.IACLContributor;

/**
 * The ACLContributor defines, what rights should be configured to use this plugin
 * @author gerry
 *
 */
public class ACLContributor implements IACLContributor {
	public static final String KB="Kassenbuch";
	public static final String BOOKING=KB+"/Buchung";
	public static final String STORNO=KB+"/Storno";
	public static final String VIEW=KB+"/Display";
	
	public String[] getACL() {
		return new String[]{KB,BOOKING,STORNO,VIEW};
	}

	public String[] reject(String[] acl) {
		// TODO Auto-generated method stub
		return null;
	}

	void initialize(){
		Hub.acl.grant(AccessControl.USER_GROUP, KB);
	}
}
