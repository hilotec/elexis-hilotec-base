/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Extensions.java 4625 2008-10-22 17:04:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import ch.rgw.tools.ExHandler;

/**
 * Vereinfachung der Handhabung von Extensions. Verschidene statische Methoden zum Auflisten von
 * Extensionpoint.-clients
 * 
 * 2008: Implementation eines Service-Providers
 * 
 * @author gerry
 * 
 */
public class Extensions {
	
	/**
	 * EIne Liste von IConfigurationElements (=komplette Definition) liefern, die an einem
	 * bestimmten Extensionpoint hängen
	 * 
	 * @param ext
	 *            Name des Extensionpoints
	 */
	public static List<IConfigurationElement> getExtensions(String ext){
		LinkedList<IConfigurationElement> ret = new LinkedList<IConfigurationElement>();
		IExtensionRegistry exr = Platform.getExtensionRegistry();
		IExtensionPoint exp = exr.getExtensionPoint(ext);
		if (exp != null) {
			IExtension[] extensions = exp.getExtensions();
			for (IExtension ex : extensions) {
				IConfigurationElement[] elems = ex.getConfigurationElements();
				for (IConfigurationElement el : elems) {
					ret.add(el);
				}
			}
			
		}
		return ret;
	}
	
	/**
	 * Eine Liste von bereits initialisierten Klassen liefern, die an einem bestimmten parameter
	 * eines bestimmten Extensionpoints hängen
	 * 
	 * @param list
	 *            eine Liste, wie von getExtension geliefert
	 * @param points
	 *            Name der Klasse
	 * @return eine Liste der konstruierten Klassen
	 * @deprecated Use {@link #getClasses(List<IConfigurationElement>,String,boolean)} instead
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public static List getClasses(List<IConfigurationElement> list, String points){
		return getClasses(list, points, true);
	}
	
	/**
	 * Eine Liste von bereits initialisierten Klassen liefern, die an einem bestimmten parameter
	 * eines bestimmten Extensionpoints hängen
	 * 
	 * @param list
	 *            eine Liste, wie von getExtension geliefert
	 * @param points
	 *            Name der Klasse
	 * @param bMandatory
	 *            false: do not handle exceptions
	 * @return eine Liste der konstruierten Klassen
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public static List getClasses(List<IConfigurationElement> list, String points,
		boolean bMandatory){
		List ret = new LinkedList();
		for (IConfigurationElement el : list) {
			try {
				Object o = el.createExecutableExtension(points);
				if (o != null) {
					ret.add(o);
				}
			} catch (CoreException e) {
				if (bMandatory) {
					ExHandler.handle(e);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Shortcut für getClasses(getExtensions(extension),points);
	 */
	public static List getClasses(String extension, String points){
		return getClasses(getExtensions(extension), points, true);
	}
	
	/**
	 * Eine Liste von Werten liefern, die ein bestimmtest Attribut hat
	 * 
	 * @param list
	 * @param attr
	 * @return
	 */
	public static List<String> getStrings(List<IConfigurationElement> list, String attr){
		List<String> ret = new LinkedList<String>();
		for (IConfigurationElement el : list) {
			ret.add(el.getAttribute(attr));
		}
		return ret;
	}
	
	public static List<String> getStrings(String ext, String attr){
		return getStrings(getExtensions(ext), attr);
	}
	
	/**
	 * We sort of replicate the OSGi Service Registry, but place it on top of the Extension point
	 * system.
	 * 
	 * A Plugin can publish a service by accessing the ExtensionPoint ch.elexis.ServiceRegistry and
	 * defining a service with an aritrary name.
	 * A different plugin can implement the same Service "better" by using the same name but declaring
	 * a higher "value"
	 * A Client can retrieve and use Services through isServiceAvailable() and findBestService()
	 * @param name name of the service to load
	 * @return the interface Object that the "best" Service with this name defines or null if no
	 * service with the given name could be loaded.
	 */
	public static Object findBestService(String name){
		int value=Integer.MIN_VALUE;
		IConfigurationElement best=null;
		List<IConfigurationElement> services=getExtensions("ch.elexis.ServiceRegistry");
		for(IConfigurationElement ic:services){
			String nam=ic.getAttribute("name");
			if(nam.equalsIgnoreCase(name)){
				String val=ic.getAttribute("value");
				if(val!=null){
					int ival=Integer.parseInt(val);
					if(ival>value){
						value=ival;
						best=ic;
					}
				}
			}
		}
		if(best==null){
			return null;
		}else {
			try {
				return best.createExecutableExtension("actor");
				
			} catch (CoreException e) {
				ExHandler.handle(e);
				return null;
			}
		}
		
		
	}
	
	public static Object executeService(Object service, String method, Class[] types, Object[] params){
		try{
			Method m=service.getClass().getMethod(method, types);
			return m.invoke(service, params);
			
		}catch(Exception ex){
			ExHandler.handle(ex);
			return null;
		}
		
	}
	public static boolean isServiceAvailable(String name){
		List<IConfigurationElement> services=getExtensions("ch.elexis.ServiceRegistry");
		for(IConfigurationElement ic:services){
			String nam=ic.getAttribute("name");
			if(nam.equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}
}
