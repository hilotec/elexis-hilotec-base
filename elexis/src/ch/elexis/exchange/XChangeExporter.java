/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XChangeExporter.java 2618 2007-06-24 10:08:05Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * this is, at present, merely a stub for the export-personality of the Container
 * @author gerry
 *
 */
public class XChangeExporter extends XChangeContainer{
	
	/**
	 * Create a default xChange Document with the actually logged-in mandator as responsible. 
	 *
	 */
	@SuppressWarnings("unchecked")
	public XChangeExporter(){
		
		
	}
	
	

}
