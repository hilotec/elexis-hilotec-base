/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ConnectionElement.java 4673 2008-11-09 17:01:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeContainer;

/**
 * A connection e.g. phone or mail
 * 
 * @author gerry
 * 
 */
@SuppressWarnings("serial")
public class ConnectionElement extends XChangeElement {
	
	@Override
	public String getXMLName(){
		return "connection";
	}
	
	public ConnectionElement(XChangeContainer parent){
		super(parent);
	}
	
	public ConnectionElement(XChangeContainer parent, String type, String cx){
		super(parent);
	}
}
