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
 *  $Id: PrintFindingsDialog.java 2516 2007-06-12 15:56:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde.xchange;

import ch.elexis.befunde.Messwert;
import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.MetaElement;
import ch.elexis.exchange.elements.ResultElement;
import ch.elexis.exchange.elements.XidElement;

@SuppressWarnings("serial")
public class BefundeItem extends FindingElement {
	
	BefundeItem(XChangeContainer home, Messwert mw, String field){
		super(home);
		setAttribute(ATTR_NAME,mw.getLabel()+":"+field);
		setAttribute(ATTR_GROUP,"Messwert");
		XidElement eXid=new XidElement(home);
		eXid.addIdentity(Xid.DOMAIN_ELEXIS, mw.getId()+field, Xid.ASSIGNMENT_LOCAL, true);
		eXid.setMainID(null);
		addContent(eXid);
		add(new MetaElement(home,ResultElement.ATTRIB_CREATOR,Messwert.PLUGIN_ID));
	}
	
}
