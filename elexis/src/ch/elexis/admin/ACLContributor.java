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
 *  $Id: ACLContributor.java 4040 2008-06-13 12:45:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.admin;
import static ch.elexis.admin.AccessControlDefaults.*; 
/**
 * Contribution of the basic system's ACLs
 * @author gerry
 *
 */
public class ACLContributor implements IACLContributor {
	String[] acls=new String[]{
			 ACCOUNTING_GLOBAL, ACCOUNTING_BILLCREATE, ACCOUNTING_BILLMODIFY,
			 ACCOUNTING_READ,
			 ACL_USERS, DATA, KONTAKT_DELETE, DELETE,DELETE_FORCED,
			 KONTAKT_DISPLAY,KONTAKT_INSERT,KONTAKT_MODIFY,KONTAKT_EXPORT,
			 KONTAKT_ETIKETTE,PATIENT_DISPLAY,PATIENT_INSERT,PATIENT_MODIFY,
			 LAB_SEEN,
			 LSTG_VERRECHNEN,KONS_CREATE,KONS_DELETE,KONS_EDIT,KONS_REASSIGN,
			 AC_ABOUT,AC_CHANGEMANDANT,AC_CONNECT,AC_EXIT,
			 AC_HELP,AC_IMORT,AC_LOGIN,AC_PREFS,AC_PURGE,
			 AC_SHOWPERSPECTIVE,AC_SHOWVIEW,
			 DOCUMENT,DOCUMENT_CREATE,DOCUMENT_SYSTEMPLATE,DOCUMENT_TEMPLATE,
			 ADMIN_KONS_EDIT_IF_BILLED,ADMIN_VIEW_ALL_REMINDERS,
			 MEDICATION_MODIFY, DELETE_MEDICATION, DELETE_LABITEMS,
			 CASE_MODIFY,SCRIPT_EXECUTE, SCRIPT_EDIT
			 
	 };
	public String[] getACL() {
		return acls;
	}

	
	public String[] reject(final String[] acl) {
		// TODO Management of collisions
		return null;
	}

	
}
