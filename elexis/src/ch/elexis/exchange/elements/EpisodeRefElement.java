/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: EpisodeRefElement.java 5080 2009-02-03 18:28:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.exchange.XChangeContainer;

public class EpisodeRefElement extends XChangeElement {
	public static final String XMLNAME = "episode";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public EpisodeRefElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	
	public EpisodeRefElement(XChangeContainer parent, EpisodeElement episode){
		super(parent);
		setAttribute("ref", episode.getAttr("id"));
	}
}
