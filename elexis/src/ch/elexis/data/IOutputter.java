/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: IOutputter.java 6051 2010-02-02 17:25:48Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

/**
 * A class capable to output something
 * @author gerry
 *
 */
public interface IOutputter {
	/** unique ID */
	public String getOutputterID();
	/** human readable description */
	public String getOutputterDescription();
	/** ID of a ch.elexis.data.DBImage to symbolize this outputter */
	public String getOutputterSymbolID();
}
