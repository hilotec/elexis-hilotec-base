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
 *  $Id: ACLContributor.java 4972 2009-01-18 16:54:18Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.IACLContributor;

public class ACLContributor implements IACLContributor {
	public static final ACE ACE_BEFUNDE = new ACE(ACE.ACE_ROOT, "Messwert", "Messwert");
	public static final ACE DELETE_PARAM =
		new ACE(AccessControlDefaults.DELETE, "Messwertrubrik", "Messwertrubrik");
	public static final ACE ADD_PARAM = new ACE(ACE_BEFUNDE, "Befund zufügen", "Befund zufügen");
	
	public ACE[] getACL(){
		return new ACE[] {
			DELETE_PARAM, ADD_PARAM
		};
	}
	
	public ACE[] reject(ACE[] acl){
		return null;
	}
	
}
