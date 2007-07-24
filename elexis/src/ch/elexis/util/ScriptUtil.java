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
 * $Id$
 *******************************************************************************/

package ch.elexis.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;

public class ScriptUtil {
	public static String[][] loadDataFromPlugin(final String connector){
		String[] adr=connector.split(":");
		if(adr.length!=2){
			return null;
		}
		String[] parms=adr[1].split("\\.");
		String name=parms[0];
		String rf=parms[1];
		String[] extra=null;
		PersistentObject ref=null;
		if(parms.length>2){
			extra=new String[parms.length-2];
			for(int i=0;i<extra.length;i++){
				extra[i]=parms[2+i];
			}
		}
		if(rf.equals("Patient")){
			ref=GlobalEvents.getSelectedPatient();
		}
		for(IConfigurationElement ic:Extensions.getExtensions("ch.elexis.DataAccess")){
			String icName=ic.getAttribute("name");
			if(icName.equals(adr[0])){
				IDataAccess ida;
				try {
					ida = (IDataAccess) ic.createExecutableExtension("class");
					Result<Object> ret=ida.getObject(name, ref, extra);
					if(ret.isOK()){
						return (String[][])ret.get();
					}else{
						ret.display("Fehler beim  Einsetzen von Feldern");
					}
				} catch (CoreException e) {
					ExHandler.handle(e);
				}
				
			}
		}
		return null;
	}
}
