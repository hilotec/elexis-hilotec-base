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
 *  $Id: ACLContributor.java 4780 2008-12-09 18:10:22Z rgw_ch $
 *******************************************************************************/
package ch.elexis.agenda.acl;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControl;
import ch.elexis.admin.IACLContributor;

/**
 * Define access rights needed for the various actions with the agenda
 * @author gerry
 *
 */
public class ACLContributor implements IACLContributor {
	/** The right to use the agenda at all */
	public static String USE_AGENDA="ch.elexis.agenda/user";
	/** administrative rights to the agenda */
	public static String ADMIN_AGENDA="ch.elexis.agenda/admin";
	
	/** The right to see appointments */
	public static final String DISPLAY_APPOINTMENTS=USE_AGENDA+"/zeigeTermine";
	/** The right to modify appointments */
	public static final String CHANGE_APPOINTMENTS=USE_AGENDA+"/ändereTermine";
	/** The right to delete appointments */
	public static final String DELETE_APPOINTMENTS=USE_AGENDA+"/löscheTermine";
	
	/** The right to modify the day limits */
	public static final String CHANGE_DAYSETTINGS=ADMIN_AGENDA+"/Tagesgrenzen";
	
	/** The right to lock or unlock appointments */
	public static final String CHANGE_APPLOCK=ADMIN_AGENDA+"/TerminSperren";
	
	/**
	 * get the verbs describing the rights that should be managed.
	 */
	public String[] getACL() {
		return new String[]{
				USE_AGENDA,ADMIN_AGENDA,
				DISPLAY_APPOINTMENTS,CHANGE_APPOINTMENTS,DELETE_APPOINTMENTS,
				CHANGE_DAYSETTINGS,CHANGE_APPLOCK
		};
	}

	/**
	 * react on errors reserving rights
	 */
	public String[] reject(String[] acl) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * On first run of the agenda, set the rights to reasonable defaults
	 */
	public static void initialize(){
		Hub.acl.grant(AccessControl.USER_GROUP, USE_AGENDA);
		Hub.acl.grant(AccessControl.ADMIN_GROUP, ADMIN_AGENDA);
		Hub.acl.flush();
	}
}
