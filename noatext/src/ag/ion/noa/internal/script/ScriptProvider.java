/****************************************************************************
 *                                                                          *
 * NOA (Nice Office Access)                                     						*
 * ------------------------------------------------------------------------ *
 *                                                                          *
 * The Contents of this file are made available subject to                  *
 * the terms of GNU Lesser General Public License Version 2.1.              *
 *                                                                          * 
 * GNU Lesser General Public License Version 2.1                            *
 * ======================================================================== *
 * Copyright 2003-2006 by IOn AG                                            *
 *                                                                          *
 * This library is free software; you can redistribute it and/or            *
 * modify it under the terms of the GNU Lesser General Public               *
 * License version 2.1, as published by the Free Software Foundation.       *
 *                                                                          *
 * This library is distributed in the hope that it will be useful,          *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 * Lesser General Public License for more details.                          *
 *                                                                          *
 * You should have received a copy of the GNU Lesser General Public         *
 * License along with this library; if not, write to the Free Software      *
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,                    *
 * MA  02111-1307  USA                                                      *
 *                                                                          *
 * Contact us:                                                              *
 *  http://www.ion.ag																												*
 *  http://ubion.ion.ag                                                     *
 *  info@ion.ag                                                             *
 *                                                                          *
 ****************************************************************************/
 
/*
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:21 $
 */
package ag.ion.noa.internal.script;

import ag.ion.bion.officelayer.util.Assert;

import ag.ion.noa.script.IScript;
import ag.ion.noa.script.IScriptProvider;

import com.sun.star.beans.XPropertySet;

import com.sun.star.script.browse.BrowseNodeTypes;
import com.sun.star.script.browse.XBrowseNode;

import com.sun.star.script.provider.XScript;
import com.sun.star.script.provider.XScriptProvider;

import com.sun.star.uno.UnoRuntime;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for scripts.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * @date 13.06.2006
 */ 
public class ScriptProvider implements IScriptProvider {

	private XScriptProvider scriptProvider = null;
	
  //----------------------------------------------------------------------------
	/**
	 * Constructs new ScriptProvider.
	 * 
	 * @param xScriptProvider OpenOffice.org XScriptProvider interface to be used
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public ScriptProvider(XScriptProvider xScriptProvider) {
		Assert.isNotNull(xScriptProvider, XScriptProvider.class, this);
		this.scriptProvider = xScriptProvider;
	}	
  //----------------------------------------------------------------------------
	/**
	 * Returns all scripts of the submitted type and library with the submitted name.
	 * 
	 * @see IScriptProvider#TYPE_BASIC
	 * @see IScriptProvider#TYPE_BEAN_SHELL
	 * @see IScriptProvider#TYPE_JAVA
	 * @see IScriptProvider#TYPE_JAVA_SCRIPT
	 * @see IScriptProvider#TYPE_PYTHON
	 * 
	 * @param type type of the scripts
	 * @param library name of the library
	 * 
	 * @return all scripts of the submitted type and library with the submitted name
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public IScript[] getScripts(String type, String library) {
		if(type == null || library == null)
			return IScript.EMPTY_ARRAY;
		return getScriptsInternal(type, library);
	}
  //----------------------------------------------------------------------------
	/**
	 * Returns all scripts of the library with the submitted name.
	 * 
	 * @param library name of the library
	 * 
	 * @return all scripts of the library with the submitted name
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public IScript[] getScripts(String library) {
		if(library == null)
			return IScript.EMPTY_ARRAY;
		return getScriptsInternal(null, library);
	}
  //----------------------------------------------------------------------------
	/**
	 * Collects scripts of the submitted type and library.
	 * 
	 * @param type type to be used (can be null - all types will be considered)
	 * @param library library to be used
	 * 
	 * @return scripts of the submitted type and library
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	private IScript[] getScriptsInternal(String type, String library) {
		XBrowseNode rootNode = (XBrowseNode)UnoRuntime.queryInterface(XBrowseNode.class, scriptProvider);
		XBrowseNode[] typeNodes = rootNode.getChildNodes();
		List list = new ArrayList();
		for(int i=0, n=typeNodes.length; i<n; i++) {
			XBrowseNode typeNode = typeNodes[i];	
			if(type == null || typeNode.equals(type)) {
				XBrowseNode libraryNode = getLibraryNode(typeNode, library);
				if(libraryNode != null) {
					buildScripts(list, libraryNode);
				}
			}
		}
		return (IScript[])list.toArray(new IScript[list.size()]);
	}
  //----------------------------------------------------------------------------
	/**
	 * Builds scripts from the submitted browse node.
	 * 
	 * @param list list to be used for the new builded scripts (can be null - 
	 * a new list will be builded)
	 * @param browseNode browse node to be used
	 * 
	 * @return builded scripts of the submitted browse node
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	private List buildScripts(List list, XBrowseNode browseNode) {
		XBrowseNode[] scriptNodes = browseNode.getChildNodes();
		if(list == null)
			list = new ArrayList();
		for(int i=0, n=scriptNodes.length; i<n; i++) {
			XBrowseNode scriptNode = scriptNodes[i];	
			if(scriptNode.getType() == BrowseNodeTypes.SCRIPT) {
				XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, scriptNode);
				if(propertySet != null) {
					try {
						Object object = propertySet.getPropertyValue("URI");
						XScript xScript = scriptProvider.getScript(object.toString());
						list.add(new Script(xScript));
					}
					catch(Throwable throwable) {
						//do not consume
					}
				}
			}
			else {
				//maybe a basic module
				buildScripts(list, scriptNode);
			}
		}
		return list;
	}
  //----------------------------------------------------------------------------
	/**
	 * Returns library node from the submitted type node. Returns null
	 * if a library with the submitted name is
	 * not available.
	 * 
	 * @param typeNode type node to be used
	 * @param library name of the library
	 * 
	 * @return library node from the submitted type node or null
	 * if a library with the submitted name is
	 * not available
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	private XBrowseNode getLibraryNode(XBrowseNode typeNode, String library) {
		XBrowseNode[] libraryNodes = typeNode.getChildNodes();
		for(int i=0, n=libraryNodes.length; i<n; i++) {
			XBrowseNode libraryNode = libraryNodes[i];
			if(libraryNode.getName().equals(library))
				return libraryNode;
		}
		return null;
	}
  //----------------------------------------------------------------------------
	
}