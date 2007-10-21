/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ResponseAnalyzer.java 3276 2007-10-21 07:23:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import java.io.InputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.rgw.tools.ExHandler;

/**
 * Class to deal with mdinvoiceresponses 
 *(http://www.forum-datenaustausch.ch/mdinvoiceresponse_xml4.00_v1.1_d.pdf)
 * @author Gerry
 *
 */
public class ResponseAnalyzer {
	Document responseDoc;
	Element eRoot;
	public Document load(InputStream xmlResponse){
		try{
			SAXBuilder builder=new SAXBuilder();
			responseDoc=builder.build(xmlResponse);
			eRoot=responseDoc.getRootElement();
			return responseDoc;
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return null;
	}
	
	public String getResume(){
		if(eRoot==null){
			return "";
		}
		StringBuilder ret=new StringBuilder();
		Element eHeader=eRoot.getChild("header");
		Element eSender=eHeader.getChild("sender");
		Element eIntermediate=eHeader.getChild("intemediate");
		Element eRecipient=eHeader.getChild("recipient");
		ret.append("Sender: ").append(eSender.getAttributeValue("ean_party")).append("\n");
		ret.append("Intermediär: ").append(eIntermediate.getAttributeValue("ean_party")).append("\n");
		ret.append("Empfänger: ").append(eRecipient.getAttributeValue("ean_party")).append("\n");
		ret.append("Status:\n______\n");
		Element eInvoice=eRoot.getChild("invoice");
		
		Element eStatus=eRoot.getChild("status");
		List<Element> lStatus=eStatus.getChildren();
		if(lStatus.size()!=1){
			ret.append("Nicht standardgemäss deklariert.\n");
		}else{
			Element eStatusType=lStatus.get(0);
			String status=eStatusType.getName().toLowerCase();
			if(status.equals("rejected")){
				ret.append("Zurückgewiesen.\n");
				
			}else if(status.equals("calledin")){
				ret.append("Weitere Informationen angefordert.\n");
			}else if(status.equals("pending")){
				ret.append("In Bearbeitung.\n");
			}else if(status.equals("resend")){
				ret.append("Bitte nochmal senden.\n");
			}else if(status.equals("modified")){
				ret.append("Korrigiert.\n");
			}else if(status.equals("anulment")){
				ret.append("storniert.\n");
			}else if(status.equals("creditadvice")){
				ret.append("Gutschrift.\n");
			}else{
				ret.append("Unbekannter Statustyp\n");
			}
		}
		return ret.toString();
	}
}
