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
 *  $Id: IExchangeContributor.java 2618 2007-06-24 10:08:05Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange;

import org.eclipse.core.runtime.IExecutableExtension;
import org.jdom.Element;

import ch.elexis.data.PersistentObject;

/**
 * A Class that wants to contribute data to eXChange or that can load data from eXChange must
 * implement this interface
 * @author gerry
 *
 */
public interface IExchangeContributor extends IExecutableExtension{

	/**
	 * An Element is to be exported. The method can contribute its own data
	 * @param container the target Container
	 * @param exporting the newly created Element containing the data
	 * @param object the data to be exported
	 */
	public void exportHook(XChangeContainer container, Element exporting, PersistentObject object);
	
	/**
	 * An Element ist to be imported. The method can fetch data it can handle
	 * @param container the source container
	 * @param importing the element to be imported
	 * @param target object
	 */
	public void importHook (XChangeContainer container, Element importing, PersistentObject dest);
}
