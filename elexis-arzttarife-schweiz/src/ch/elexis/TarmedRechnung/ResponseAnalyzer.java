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
 * $Id: ResponseAnalyzer.java 3279 2007-10-21 15:12:53Z rgw_ch $
 *******************************************************************************/

package ch.elexis.TarmedRechnung;

import java.io.InputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.ExHandler;

/**
 * Class to deal with mdinvoiceresponses 
 *(http://www.forum-datenaustausch.ch/mdinvoiceresponse_xml4.00_v1.1_d.pdf)
 * @author Gerry
 *
 */
public class ResponseAnalyzer {
	final static Namespace ns=Namespace.getNamespace("invoice", "http://www.xmlData.ch/xmlInvoice/XSD");
	final static Namespace xsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
	final static Namespace nsSchema=Namespace.getNamespace("schemaLocation", "http://www.xmlData.ch/xmlInvoice/XSD");

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
		Element eHeader=eRoot.getChild("header",ns);
		Element eSender=eHeader.getChild("sender",ns);
		Element eIntermediate=eHeader.getChild("intermediate",ns);
		Element eRecipient=eHeader.getChild("recipient",ns);
		ret.append("Sender: ").append(eSender.getAttributeValue("ean_party")).append("\n");
		ret.append("Intermediär: ").append(eIntermediate.getAttributeValue("ean_party")).append("\n");
		ret.append("Empfänger: ").append(eRecipient.getAttributeValue("ean_party")).append("\n");
		ret.append("Status:\n______\n");
		Element eInvoice=eRoot.getChild("invoice",ns);
		String rnId=eInvoice.getAttributeValue("invoice_id");
		int tr=rnId.lastIndexOf('0');
		String rnNr;
		if(tr==-1){
			rnNr=rnId;
		}else{
			String patNr=Integer.toString(Integer.parseInt(rnId.substring(0, tr))); // eliminate leading zeroes
			rnNr=rnId.substring(tr+1);
		}
		Rechnung rn=Rechnung.getFromNr(rnNr);
		if(rn==null){
			ret.append("Die in der Antwort genannte Rechnung ist nicht bekannt!");
		}else{
			ret.append("Rechnungsnummer: ").append(rnNr).append("\n");
			ret.append("Patient: ").append(rn.getFall().getPatient().getLabel()).append("\n");
			ret.append("Datum: ").append(rn.getDatumRn()).append("\n----------------------\n");
		}
		Element eStatus=eRoot.getChild("status",ns);
		List<Element> lStatus=eStatus.getChildren();
		if(lStatus.size()!=1){
			ret.append("Nicht standardgemäss deklariert.\n");
		}else{
			Element eStatusType=lStatus.get(0);
			Element eError=eStatusType.getChild("error",ns);
			Element eExpl=eStatusType.getChild("explanation",ns);
			String explanation="Keine Erläuterung angegeben";
			if(eExpl!=null){
				explanation=eExpl.getText();
			}
			String status=eStatusType.getName().toLowerCase();
			if(status.equals("rejected")){
				ret.append("Zurückgewiesen.\n").append(explanation).append("\n");
				if(eError!=null){
					ret.append("Fehlercode: ");
					ret.append(eError.getAttributeValue("major")).append(".");
					ret.append(eError.getAttributeValue("minor")).append("->");
					ret.append(eError.getAttributeValue("error")).append("\n");
				}
					
				
			}else if(status.equals("calledin")){
				ret.append("Weitere Informationen angefordert.\n").append(explanation).append("\n");
				if(eError!=null){
					ret.append("Code: ").append(eError.getAttributeValue("major"));
				}
			}else if(status.equals("pending")){
				ret.append("In Bearbeitung.\n").append(explanation).append("\n");
			}else if(status.equals("resend")){
				ret.append("Bitte nochmal senden.\n").append(explanation).append("\n");
			}else if(status.equals("modified")){
				ret.append("Korrigiert.\n").append(explanation).append("\n");
				if(eError!=null){
					ret.append("Korrekturcode: ");
					ret.append(eError.getAttributeValue("major")).append(".")
						.append(eError.getAttributeValue("minor")).append(" -> ")
						.append(eError.getAttributeValue("error")).append("\n");
				}
			}else if(status.equals("anulment")){
				ret.append("Storno.\n").append(explanation).append("\n");
				List<Element> reasons=eStatusType.getChildren();
				Element eReason=reasons.get(0);
				ret.append(eReason.getName()).append("\n");
			}else if(status.equals("creditadvice")){
				ret.append("Gutschrift.\n").append(explanation).append("\n");
				Element eAnswer=(Element)eStatusType.getChildren().get(0);
				ret.append(eAnswer.getName()).append("\n");
			}else{
				ret.append("Unbekannter Statustyp\n");
			}
		}
		return ret.toString();
	}
}
