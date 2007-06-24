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
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:26 $
 */
package ag.ion.noa.script;

/**
 * Provider for scripts.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * @date 13.06.2006
 */ 
public interface IScriptProvider {
	
	/** Type for Bean Shell scripts. */ 
	public static final String TYPE_BEAN_SHELL 	= "BeanShell";
	/** Type for Basic scripts. */ 
	public static final String TYPE_BASIC 			= "Basic";
	/** Type for Java scripts. */ 
	public static final String TYPE_JAVA 				= "Java";
	/** Type for Java Script scripts. */ 
	public static final String TYPE_JAVA_SCRIPT = "JavaScript";
	/** Type for Python scripts. */ 
	public static final String TYPE_PYTHON 			= "Python";
		
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
	public IScript[] getScripts(String type, String library);
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
	public IScript[] getScripts(String library);
  //----------------------------------------------------------------------------
	
}