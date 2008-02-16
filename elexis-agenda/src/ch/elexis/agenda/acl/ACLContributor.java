/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ACLContributor.java 3682 2008-02-16 11:46:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.agenda.acl;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControl;
import ch.elexis.admin.IACLContributor;

public class ACLContributor implements IACLContributor {
	public static String USE_AGENDA="ch.elexis.agenda/user";
	public static String ADMIN_AGENDA="ch.elexis.agenda/admin";
	
	public static final String DISPLAY_APPOINTMENTS=USE_AGENDA+"/zeigeTermine";
	public static final String CHANGE_APPOINTMENTS=USE_AGENDA+"/ändereTermine";
	public static final String DELETE_APPOINTMENTS=USE_AGENDA+"/löscheTermine";
	
	public static final String CHANGE_DAYSETTINGS=ADMIN_AGENDA+"/Tagesgrenzen";
	public static final String CHANGE_APPLOCK=ADMIN_AGENDA+"/TerminSperren";
	
	public String[] getACL() {
		return new String[]{
				USE_AGENDA,ADMIN_AGENDA,
				DISPLAY_APPOINTMENTS,CHANGE_APPOINTMENTS,DELETE_APPOINTMENTS,
				CHANGE_DAYSETTINGS,CHANGE_APPLOCK
		};
	}

	public String[] reject(String[] acl) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void initialize(){
		Hub.acl.grant(AccessControl.USER_GROUP, USE_AGENDA);
		Hub.acl.grant(AccessControl.ADMIN_GROUP, ADMIN_AGENDA);
		Hub.acl.flush();
	}
}
