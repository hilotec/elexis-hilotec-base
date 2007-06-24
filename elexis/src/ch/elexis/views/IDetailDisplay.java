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
 *  $Id: IDetailDisplay.java 23 2006-03-24 15:36:01Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

/**
 * Detailansicht eines PersistentObject
 * @author Gerry
 *
 */
public interface IDetailDisplay {
	/**
	 * Das Display erzeugen. 
	 * @param parent Hat schon ein FillLayout.
	 * @param site Heimat-Viewsite dieses DetailDisplay
	 */
	public Composite createDisplay(Composite parent, IViewSite site);
	public Class getElementClass();
	public void display(Object obj);
	public String getTitle();
}
