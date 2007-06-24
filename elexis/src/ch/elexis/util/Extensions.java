/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Extensions.java 1183 2006-10-29 15:11:21Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import ch.rgw.tools.ExHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Vereinfachung der Handhabung von Extensions. Verschidene statische Methoden zum Auflisten von Extensionpoint.-clients
 * @author gerry
 *
 */
public class Extensions {
	
	/**
	 * EIne Liste von IConfigurationElements (=komplette Definition) liefern, die an einem bestimmten Extensionpoint hängen
	 * @param ext Name des Extensionpoints
	 */
	public static List<IConfigurationElement> getExtensions(String ext){
		LinkedList<IConfigurationElement> ret=new LinkedList<IConfigurationElement>();
		IExtensionRegistry exr=Platform.getExtensionRegistry();
		IExtensionPoint exp=exr.getExtensionPoint(ext);
		if(exp!=null){
			IExtension[] extensions=exp.getExtensions();
			for(IExtension ex:extensions){
				IConfigurationElement[] elems=ex.getConfigurationElements();
				for(IConfigurationElement el:elems){
					ret.add(el);
				}
			}
		
		}
		return ret;
	}

	/** 
	 * Eine Liste von bereits initialisierten Klassen liefern, die an einem bestimmten parameter eines bestimmten 
	 * Extensionpoints hängen
	 * @param list eine Liste, wie von getExtension geliefert
	 * @param points Name der Klasse
	 * @return eine Liste der konstruierten Klassen
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static List getClasses(List<IConfigurationElement> list,String points){
		List ret=new LinkedList();
		for(IConfigurationElement el:list){
			try {
				  Object o=el.createExecutableExtension(points);
				  if(o!=null){
					  ret.add(o);
				  }
			} catch (CoreException e) {
				ExHandler.handle(e);
			}
		}
		return ret;
	}
	
	/**
	 * Shortcut für getClasses(getExtensions(extension),points);
	 */
	public static List getClasses(String extension, String points){
			return getClasses(getExtensions(extension),points);
	}

	/**
	 * Eine Liste von Werten liefern, die ein bestimmtest Attribut hat
	 * @param list
	 * @param attr
	 * @return
	 */
	public static List<String> getStrings(List<IConfigurationElement> list, String attr){
		List<String> ret=new LinkedList<String>();
		for(IConfigurationElement el:list){
			ret.add(el.getAttribute(attr));
		}
		return ret;
	}
	
	public static List<String> getStrings(String ext,String attr){
		return getStrings(getExtensions(ext),attr);
	}
}
