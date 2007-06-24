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

import ag.ion.noa.NOAException;

import ag.ion.noa.script.IScript;

import com.sun.star.script.provider.XScript;

/**
 * Script of the office scripting framework which can be invoked.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * @date 13.06.2006
 */ 
public class Script implements IScript {	
	
	private XScript xScript = null;
	
  //----------------------------------------------------------------------------
	/**
	 * Constructs a Script.
	 * 
	 * @param xScript OpenOffice.org XScript interface to be used
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public Script(XScript xScript) {
		Assert.isNotNull(xScript, XScript.class, this);
		this.xScript = xScript;
	}
  //----------------------------------------------------------------------------
	/**
	 * Returns OpenOffice.org XScript interface.
	 * 
	 * @return OpenOffice.org XScript interface
	 * 
	 * @author Andreas Bröker
	 * @date 14.06.2006
	 */
	public XScript getXScript() {
		return xScript;
	}
  //----------------------------------------------------------------------------
	/**
	 * Invokes the script.
	 * 
	 * @param parameters parameters to be used for script invocation
	 * @param outParameterIndices indices of output related parameters within the parameters
	 * @param outParameters storage for out parameters
	 * 
	 * @return output of the script
	 * 
	 * @throws NOAException if the script can not be invoked
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public Object invoke(Object[] parameters, short[][] outParameterIndices, Object[][] outParameters) throws NOAException {
		try {
			return xScript.invoke(parameters, outParameterIndices, outParameters);
		}
		catch(Throwable throwable) {
			throw new NOAException(throwable);
		}
	}
  //----------------------------------------------------------------------------
	/**
	 * Invokes the script without parameters.
	 * 
	 * @return output of the script
	 * 
	 * @throws NOAException if the script can not be invoked
	 * 
	 * @author Andreas Bröker
	 * @date 13.06.2006
	 */
	public Object invoke() throws NOAException {
		try {
			return xScript.invoke(new Object[0], new short[1][1], new Object[1][1]);
		}
		catch(Throwable throwable) {
			throw new NOAException(throwable);
		}
	}
  //----------------------------------------------------------------------------
	
}