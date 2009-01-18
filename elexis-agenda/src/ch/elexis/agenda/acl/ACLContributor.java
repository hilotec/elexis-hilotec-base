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
 *  $Id: ACLContributor.java 4969 2009-01-18 16:53:06Z rgw_ch $
 *******************************************************************************/
package ch.elexis.agenda.acl;

import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControl;
import ch.elexis.admin.IACLContributor;

/**
 * Define access rights needed for the various actions with the agenda
 * 
 * @author gerry
 * 
 */
public class ACLContributor implements IACLContributor {
	/** The right to use the agenda at all */
	public static final ACE ACE_AGENDA = new ACE(ACE.ACE_ROOT, Activator.PLUGIN_ID, "Agenda");
	public static final ACE USE_AGENDA = new ACE(ACE_AGENDA, "user", "benutzen");
	
	/** administrative rights to the agenda */
	public static final ACE ADMIN_AGENDA = new ACE(ACE_AGENDA, "admin", "Administration");
	
	/** The right to see appointments */
	public static final ACE DISPLAY_APPOINTMENTS =
		new ACE(USE_AGENDA, "zeigeTermine", "Termine zeigen");
	/** The right to modify appointments */
	public static final ACE CHANGE_APPOINTMENTS =
		new ACE(USE_AGENDA, "ändereTermine", "Termine ändern");
	/** The right to delete appointments */
	public static final ACE DELETE_APPOINTMENTS =
		new ACE(USE_AGENDA, "löscheTermine", "Termine löschen");
	
	/** The right to modify the day limits */
	public static final ACE CHANGE_DAYSETTINGS =
		new ACE(ADMIN_AGENDA, "Tagesgrenzen", "Tagesgrenzen");
	
	/** The right to lock or unlock appointments */
	public static final ACE CHANGE_APPLOCK =
		new ACE(ADMIN_AGENDA, "TerminSperren", "Termin sperren");
	
	/**
	 * get the ACE's that should be managed.
	 */
	public ACE[] getACL(){
		return new ACE[] {
			ACE_AGENDA,USE_AGENDA, ADMIN_AGENDA, DISPLAY_APPOINTMENTS, CHANGE_APPOINTMENTS,
			DELETE_APPOINTMENTS, CHANGE_DAYSETTINGS, CHANGE_APPLOCK
		};
	}
	
	/**
	 * react on errors reserving rights
	 */
	public ACE[] reject(ACE[] acl){
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
