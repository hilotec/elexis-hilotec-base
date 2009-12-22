/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich, SGAM.Informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 * $Id: XChangeImporter.java 5892 2009-12-22 11:41:50Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange;

import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import ch.elexis.exchange.elements.XChangeElement;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class XChangeImporter implements IDataReceiver {
	private XChangeContainer container=new XChangeContainer();
	private Log log=Log.get("xChange Importer");
	
	public Result finalizeImport(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public Result<Object> load(Element input, Object context){
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Find the registered Data handler that matches best the given element
	 * 
	 * @param el
	 *            Element o be imported
	 * @return the best matching handler or null if no handler exists at all for the given data type
	 */
	@SuppressWarnings("unchecked")
	public IExchangeDataHandler findImportHandler(XChangeElement el){
		IExchangeDataHandler ret = null;
		int matchedRestrictions = 0;
		for (IExchangeContributor iex : container.getXChangeContributors()) {
			IExchangeDataHandler[] handlers = iex.getImportHandlers();
			for (IExchangeDataHandler cand : handlers) {
				if (cand.getDatatype().equalsIgnoreCase(el.getXMLName())) {
					if (ret == null) {
						ret = cand;
					}
					String[] restrictions = cand.getRestrictions();
					if (restrictions != null) {
						int matches = 0;
						for (String r : restrictions) {
							try {
								XPath xpath = XPath.newInstance(r);
								List<Object> nodes = xpath.selectNodes(el);
								if (nodes.size() > 0) {
									if (++matches > matchedRestrictions) {
										ret = cand;
										matchedRestrictions = matches;
									} else if (matches == matchedRestrictions) {
										if (ret.getValue() < cand.getValue()) {
											ret = cand;
										}
									}
								}
							} catch (JDOMException e) {
								ExHandler.handle(e);
								log.log("Parse error JDOM " + e.getMessage(), Log.WARNINGS); //$NON-NLS-1$
							}
						}
						
					} else {
						if (ret.getValue() < cand.getValue()) {
							ret = cand;
						}
					}
					
				}
			}
		}
		return ret;
	}
	
	public void addBinary(String id, byte[] cnt){
		container.binFiles.put(id, cnt);
	}
	
	public XChangeContainer getContainer(){
		return container;
	}
}
