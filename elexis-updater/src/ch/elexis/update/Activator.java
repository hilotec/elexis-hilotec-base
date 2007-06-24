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
 *  $Id: Activator.java 2573 2007-06-23 11:09:48Z rgw_ch $
 *******************************************************************************/
package ch.elexis.update;

import ch.elexis.ApplicationActionBarAdvisor;
import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.IACLContributor;
import ch.elexis.util.SWTHelper;

public class Activator implements org.eclipse.ui.IStartup, IACLContributor{
	public static String AC_UPDATE=AccessControlDefaults.ACTIONS+"/update";
	boolean rejected=false;
	
	/**
	 * We need to plug our menu item into the file-menu. This should happen right
	 * at the program initialization. So we use the org.eclipse.ui.startup
	 * extension point.
	 * This method will be called just after creation of the workbench window
	 * finishes.
	 */
	public void earlyStartup() {
		UpdateAction ac=new UpdateAction();
		ApplicationActionBarAdvisor.fileMenu.appendToGroup(
				ApplicationActionBarAdvisor.IMPORTER_GROUP, ac);
		
		// Do we have to auto-update?
		int interval=Hub.localCfg.get(Preferences.AUTO_UPDATE_INTERVAL, 0);
		if(interval!=0){
			int days=Hub.localCfg.get(Preferences.DAYS_UNTIL_NEXT_UPDATE, 0);
			if(days==0){
				days=interval;
				AutoUpdate update=new AutoUpdate();
				update.doUpdate();
			}else{
				days-=1;
			}
			Hub.localCfg.set(Preferences.DAYS_UNTIL_NEXT_UPDATE, days);
		}
	}

	public String[] getACL() {
		return new String[]{AC_UPDATE};
	}

	public String[] reject(String[] acl) {
		if(rejected){
			SWTHelper.showError("Fehler bei ACL-Import", "ch.elexis.update konnte seine ACL's nicht integrieren");
			return null;
		}else{
			AC_UPDATE=AccessControlDefaults.ACTIONS+"/ch.elexis.update/updade";
			rejected=true;
			return new String[]{AC_UPDATE};			
		}
		
	}



}
