/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ArzttarifFactory.java 3552 2008-01-17 12:51:41Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.lang.reflect.Method;

public class ArzttarifFactory extends PersistentObjectFactory {
	@SuppressWarnings("unchecked")
	public PersistentObject createFromString(String code){
	 try{
	        String[] ci=code.split("::"); //$NON-NLS-1$
	        Class clazz=Class.forName(ci[0]);
	        Method load=clazz.getMethod("load",new Class[]{String.class}); //$NON-NLS-1$
	        return  (PersistentObject)(load.invoke(null,new Object[]{ci[1]}));
	    }catch(Exception ex){
	    	//ExHandler.handle(ex);
	    	return null;
	    }
	}
	@Override
	public PersistentObject doCreateTemplate(Class<? extends PersistentObject> typ) {
		try {
			return (PersistentObject) typ.newInstance();
		} catch (Exception ex) {
			//ExHandler.handle(ex);
			return null;
		}
	}
	
}
