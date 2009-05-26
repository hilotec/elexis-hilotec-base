/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, SGAM.informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ServiceBlockElement.java 5319 2009-05-26 14:55:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;


public class ServiceBlockElement extends XChangeElement {
	public static final String XMLNAME = "serviceblock";
	public static final String ENCLOSING = "serviceblocks";
	public static final String ATTR_NAME = "name";
	
	public ServiceBlockElement(XChangeContainer p, Leistungsblock lb){
		super(p);
		setAttribute(ATTR_NAME, lb.getName());
		List<ICodeElement> ics = lb.getElements();
		for (ICodeElement ic : ics) {
			if (ic instanceof IVerrechenbar) {
				IVerrechenbar iv = (IVerrechenbar) ic;
				ServiceElement se = new ServiceElement(p, iv);
				add(se);
			}
		}
	}
	
	public ServiceBlockElement(XChangeContainer c, Element el){
		super(c, el);
	}
	
	public void doImport(){
		String name = getAttr(ATTR_NAME);
		if (!StringTool.isNothing(name)) {
			Leistungsblock ret = new Leistungsblock(name, Hub.actMandant);
			List<ServiceElement> lService =
				(List<ServiceElement>) getChildren(ServiceElement.XMLNAME, ServiceElement.class);
			for (ServiceElement se : lService) {
				XidElement xid = se.getXid();
				List<PersistentObject> ls = xid.findObject();
				boolean bFound = false;
				for (PersistentObject po : ls) {
					if (po instanceof IVerrechenbar) {
						ret.addElement((IVerrechenbar) po);
						bFound = true;
						break;
					}
				}
				if (!bFound) {
					ret.addElement(new Eigenleistung(se.getAttr(ServiceElement.ATTR_CONTRACT_CODE),
						se.getAttr(ServiceElement.ATTR_NAME), se.getAttr(ServiceElement.ATTR_COST),
						se.getAttr(ServiceElement.ATTR_PRICE)));
				}
			}
		}
		
	}
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
}
