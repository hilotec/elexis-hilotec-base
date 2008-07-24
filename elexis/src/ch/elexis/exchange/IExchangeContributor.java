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

import ch.elexis.data.PersistentObject;
import ch.elexis.util.Tree;

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
	 * @param object the data to be exported
	 */
	public void exportHook(XChangeContainer container, Tree<UserChoice> selectedObjects);
	
	/**
	 * An Element ist to be imported. The method can fetch data it can handle
	 * @param container the source container
	 */
	public void importHook (XChangeContainer container);
	
	/**
	 * Create Elements to lett the user chose a given Object to export or not.
	 * @param container the target container
	 * @return A Tree object containing the description of the whole plugin as root and the
	 * individual objects as leafs.
	 */
	public Tree<UserChoice> selectionHook(XChangeContainer container);
	
	static class UserChoice{
		boolean bSelected;
		String title;
		Object object;
		public boolean isSelected(){
			return bSelected;
		}
		public String getTitle(){
			return title;
		}
		public Object getObject(){
			return object;
		}
		public UserChoice(boolean bSelected, String title, Object object){
			this.bSelected=bSelected;
			this.title=title;
			this.object=object;
		}
	}
	
}
