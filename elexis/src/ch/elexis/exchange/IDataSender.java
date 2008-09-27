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
 *  $Id: IDataSender.java 4450 2008-09-27 19:49:01Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange;

import org.jdom.Element;

import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

/**
 * A generic mediator between Elexis Objects and XML-Files. Any number of
 * Objects can be sent to the IDataSender, finishing with a call to
 * finalizeExport. The ultimate destination depends on the implementation
 * 
 * @author Gerry
 * 
 */
public interface IDataSender {
	/**
	 * Prepare an object for export
	 * 
	 * @param output
	 *            an object this IDataSender can handle
	 * @return the XML element created or an error code
	 */
	public Result<Element> store(Object output);

	/**
	 * Send the stored objects to this IDataSender's ultimate destinaion (e.g.
	 * file, URL). The IDataTransfer is invalid after finalizing.
	 * 
	 * @return true on success
	 */
	public boolean finalizeExport();

	/**
	 * Ask if this IDataSender can handle a certain type
	 * 
	 * @param clazz
	 *            the class in question
	 * @return true if it can handle objects of that class.
	 */
	public boolean canHandle(Class<? extends PersistentObject> clazz);
}
