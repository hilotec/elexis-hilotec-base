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
 *  $Id: RecordElement.java 5319 2009-05-26 14:55:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas;
import ch.elexis.text.Samdas.Record;
import ch.elexis.text.Samdas.XRef;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.XMLTool;
import ch.rgw.tools.VersionedResource.ResourceItem;

public class RecordElement extends XChangeElement {
	public static final String XMLNAME = "record";

	public String getXMLName() {
		return XMLNAME;
	}

	public RecordElement(XChangeContainer c, Element el) {
		super(c, el);
	}

	public RecordElement(XChangeContainer c, Konsultation k) {
		super(c);

		setAttribute("date", new TimeTool(k.getDatum())
				.toString(TimeTool.DATE_ISO));
		Kontakt kMandant = k.getMandant();
		if (kMandant == null) {
			setAttribute("responsible", "unknown");
		} else {
			ContactElement cMandant = c.addContact(kMandant);
			setAttribute("responsible", cMandant.getID());
		}
		setAttribute(ID, XMLTool.idToXMLID(k.getId()));
		c.addChoice(this, k.getLabel(), k);
		VersionedResource vr = k.getEintrag();
		ResourceItem entry = vr.getVersion(vr.getHeadVersion());
		if (entry != null) {
			setAttribute("author", entry.remark);

			Samdas samdas = new Samdas(k.getEintrag().getHead());
			Record record = samdas.getRecord();
			if (record != null) {
				String st = record.getText();
				if (st != null) {
					Element eText = new Element("text", getContainer()
							.getNamespace());
					eText.addContent(st);
					getElement().addContent(eText);
					List<XRef> xrefs = record.getXrefs();
					for (XRef xref : xrefs) {
						MarkupElement me = new MarkupElement(getContainer(),
								xref);
						add(me);
					}
				}
			}
		}
		c.addMapping(this, k);
	}



	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nEintrag vom ").append(getAttr("date")).append(
				" erstellt von ").append(getAttr("author")).append("\n");
		List<Element> children = getElement().getChildren();
		if (children != null) {
			for (Element child : children) {
				if (child.getName().equals("text")) {
					continue;
				}
				sb.append(child.getName()).append(":\n");
				sb.append(child.getText()).append("\n");
			}
		}
		Element eText = getElement().getChild("text");
		if (eText != null) {
			String text = eText.getText();
			sb.append(text).append("\n------------------------------\n");
		}
		return sb.toString();
	}
}
