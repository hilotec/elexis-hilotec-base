/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: EpisodeRefElement.java 5880 2009-12-19 19:25:22Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import ch.elexis.exchange.XChangeExporter;

public class EpisodeRefElement extends XChangeElement {
	public static final String XMLNAME = "episode";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public EpisodeRefElement asExporter(XChangeExporter parent, EpisodeElement episode){
		asExporter(parent);
		setAttribute("ref", episode.getAttr(ID));
		return this;
	}
}
