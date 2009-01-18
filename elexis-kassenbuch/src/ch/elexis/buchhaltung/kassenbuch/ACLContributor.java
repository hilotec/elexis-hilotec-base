/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ACLContributor.java 4968 2009-01-18 16:52:47Z rgw_ch $
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import ch.elexis.Hub;
import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControl;
import ch.elexis.admin.IACLContributor;

/**
 * The ACLContributor defines, what rights should be configured to use this plugin
 * 
 * @author gerry
 * 
 */
public class ACLContributor implements IACLContributor {
	
	public static ACE KB = new ACE(ACE.ACE_ROOT, "Kassenbuch", "Kassenbuch");
	public static final ACE BOOKING = new ACE(KB, "Buchung", "Buchung");
	public static final ACE STORNO = new ACE(KB, "Storno", "Storno");
	public static final ACE VIEW = new ACE(KB, "Display", "Anzeigen");
	
	public ACE[] getACL(){
		return new ACE[] {
			KB, BOOKING, STORNO, VIEW
		};
	}
	
	public ACE[] reject(ACE[] acl){
		// TODO Auto-generated method stub
		return null;
	}
	
	void initialize(){
		Hub.acl.grant(AccessControl.USER_GROUP, KB);
	}
}
