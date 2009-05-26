/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FindingElement.java 5319 2009-05-26 14:55:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.LabItem;
import ch.elexis.exchange.XChangeContainer;

public class FindingElement extends XChangeElement {
	public static final String ENCLOSING = "findings";
	public static final String XMLNAME = "finding";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_NORMRANGE = "normRange";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_UNITS = "unit";
	public static final String ATTR_GROUP = "group";

	public static final String ELEMENT_XID = "xid";
	public static final String XIDBASE = "www.xid.ch/labitems/";

	public static final String TYPE_NUMERIC = "numeric";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_ABSOLUTE = "absolute";

	public String getXMLName() {
		return XMLNAME;
	}

	protected FindingElement(XChangeContainer p, Element el) {
		super(p, el);
	}

	FindingElement(XChangeContainer home, LabItem li) {
		super(home);

		setAttribute(ATTR_NAME, li.getKuerzel());
		if (li.getTyp().equals(LabItem.typ.NUMERIC)) {
			setAttribute(ATTR_TYPE, TYPE_NUMERIC);
			setAttribute(ATTR_NORMRANGE, li.getRefM()); // TODO anpassen
			setAttribute(ATTR_UNITS, li.getEinheit());

		} else if (li.getTyp().equals(LabItem.typ.ABSOLUTE)) {
			setAttribute(ATTR_TYPE, TYPE_ABSOLUTE);
		} else if (li.getTyp().equals(LabItem.typ.TEXT)) {
			setAttribute(ATTR_TYPE, TYPE_TEXT);
		}
		setAttribute(ATTR_GROUP, li.getGroup());
		XidElement eXid = new XidElement(home, li);
		add(eXid);
	}

}
