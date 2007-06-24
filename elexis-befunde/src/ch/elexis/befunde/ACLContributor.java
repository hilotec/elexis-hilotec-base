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
 *  $Id: ACLContributor.java 2516 2007-06-12 15:56:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.IACLContributor;

public class ACLContributor implements IACLContributor {
	public static final String DELETE_PARAM=AccessControlDefaults.DELETE+"/Messwertrubrik";
	public static final String ADD_PARAM= "Messwert/Befund zufügen";
			
	public String[] getACL() {
		return new String[]{
				DELETE_PARAM,
				ADD_PARAM
		};
	}

	public String[] reject(String[] acl) {
		return null;
	}

}
