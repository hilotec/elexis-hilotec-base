/*******************************************************************************
 * Copyright (c) 2007, medshare and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Imhof - initial implementation
 *    
 * $Id: DirectoriesContentParser.java 3438 2007-12-13 14:12:14Z michael_imhof $
 *******************************************************************************/

package ch.medshare.elexis.directories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

public class DirectoriesContentParser extends HtmlParser {

	private static final String B_SEARCH_WORDS_BEGIN = "<b class=\"searchWords\">"; //$NON-NLS-1$
	private static final String B_BEGIN = "<b>"; //$NON-NLS-1$
	private static final String B_END = "</b>"; //$NON-NLS-1$

	private static final String DIV_SEARCH_INFO_BEGIN = "<div id=\"searchInfo\">"; //$NON-NLS-1$
	private static final String DIV_END = "</div>"; //$NON-NLS-1$

	private static final String SPAN_BEGIN = "<span>"; //$NON-NLS-1$
	private static final String SPAN_END = "</span>"; //$NON-NLS-1$

	private static final String BR_BEGIN = "<br>"; //$NON-NLS-1$

	private static final String ADR_LIST_TAG = "adrListLev"; //$NON-NLS-1$
	private static final String ADR_LIST_AVOID_TAG = "adrListLev0Cat"; //$NON-NLS-1$

	private static final String ADR_DETAIL_TAG = "adrNameDetLev"; //$NON-NLS-1$
	private static final String ADR_DETAIL_AVOID_TAG = "adrNameDetLev2"; //$NON-NLS-1$

	public DirectoriesContentParser(String htmlText) {
		super(htmlText);
	}
	
	private String[] getSpacedParts(final String text) {
		List<String> parts = new Vector<String>();
		HtmlParser spaceParser = new HtmlParser(text);
		while (spaceParser.getNextPos(" ") > 0) {
			parts.add(spaceParser.extractTo(" "));
		}
		parts.add(spaceParser.getTail());
		return parts.toArray(new String[parts.size()]);
	}

	private String[] getPlzOrt(String text) {
		String plz = ""; //$NON-NLS-1$
		String ort = text;
		if (text != null && text.trim().length() > 0) {
			HtmlParser ortParser = new HtmlParser(text);
			if (ortParser.getNextPos(B_SEARCH_WORDS_BEGIN) >= 0) {
				plz = ortParser.extract(B_SEARCH_WORDS_BEGIN, B_END);
				ort = ortParser.extract(B_SEARCH_WORDS_BEGIN, B_END);
			} else {
				int plzEndIndex = text.trim().indexOf(" "); //$NON-NLS-1$
				if (plzEndIndex < 0) {
					plzEndIndex = 5;
				}
				plz = text.trim().substring(0, plzEndIndex).trim();
				ort = text.trim().substring(plzEndIndex).trim();
			}
		}
		return new String[] { plz, ort };
	}

	private String[] getVornameNachname(String text) {
		String vorname = ""; //$NON-NLS-1$
		String nachname = text;
		int nameEndIndex = text.trim().indexOf(" "); //$NON-NLS-1$
		if (nameEndIndex > 0) {
			vorname = text.trim().substring(nameEndIndex).trim();
			nachname = text.trim().substring(0, nameEndIndex).trim();
		}
		return new String[] { vorname, nachname };
	}

	/**
	 * Informationen zur Suche werden extrahiert. 
	 * Bsp:
	 *   <div id="searchInfo"> <b>4540</b> Treffer zu: <b>pfister</b></div>
	 */
	public String getSearchInfo() {
		reset();
		String searchInfoText = extract(DIV_SEARCH_INFO_BEGIN, DIV_END);
		if (searchInfoText == null) {
			return "";//$NON-NLS-1$
		}
		return searchInfoText.replace(B_BEGIN, "").replace(B_END, "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Extrahiert Informationen aus dem retournierten Html.
	 * Anhand der <div class="xxx"> kann entschieden werden, ob es sich
	 * um eine Liste oder einen Detaileintrag (mit Telefon handelt).
	 * 
	 * Detaileinträge: "adrNameDetLev0", "adrNameDetLev1", "adrNameDetLev3"
	 * Nur Detaileintrag "adrNameDetLev2" darf nicht extrahiert werden
	 * 
	 * Listeinträge: "adrListLev0", "adrListLev1", "adrListLev3"
	 * Nur Listeintrag "adrListLev0Cat" darf nicht extrahiert werden
	 */
	public List<KontaktEntry> extractKontakte() throws IOException {
		reset();
		List<KontaktEntry> kontakte = new Vector<KontaktEntry>();

		int listIndex = getNextPos(ADR_LIST_TAG);
		int detailIndex = getNextPos(ADR_DETAIL_TAG);
		while (listIndex > 0 || detailIndex > 0) {
			KontaktEntry entry = null;
			if (detailIndex < 0 || (listIndex >= 0 && listIndex < detailIndex)) {
				int listCatIndex = getNextPos(ADR_LIST_AVOID_TAG);
				if (listCatIndex != listIndex) {
					entry = extractListKontakt();
				} else { // adrListLev0Cat darf nicht
					moveTo(ADR_LIST_AVOID_TAG);
				}
			} else if (listIndex < 0
					|| (detailIndex >= 0 && detailIndex < listIndex)) {
				int detail2Index = getNextPos(ADR_DETAIL_AVOID_TAG);
				if (detail2Index != detailIndex) {
					entry = extractKontakt();
				} else { // adrNameDetLev2 darf nicht
					moveTo(ADR_DETAIL_AVOID_TAG);
				}
			}
			if (entry != null) {
				kontakte.add(entry);
			}
			listIndex = getNextPos(ADR_LIST_TAG);
			detailIndex = getNextPos(ADR_DETAIL_TAG);
		}

		return kontakte;
	}

	/**
	 * Extrahiert einen Kontakt aus einem Listeintrag
	 * Bsp: 
     *	 <div class="adrListLev0">
	 *	   <a
	 *	      href="/weisseseiten/base.aspx?do=goresultdetail&amp;entryid=78779&amp;searchtype=adr_simple">
	 *	      Albrecht Michael u. Imhof Nicole
     *	   </a>
     *	   , Wagenleise 1, 3904 Naters
     *	 </div>
	 */
	private KontaktEntry extractListKontakt() throws IOException,
			MalformedURLException {

		if (!moveTo(ADR_LIST_TAG)) { // Kein neuer Eintrag
			return null;
		}

		moveTo("href=\""); //$NON-NLS-1$

		// Name
		String name = extract("\">", "</a>"); //$NON-NLS-1$ //$NON-NLS-2$

		// Geo: Adresse, Ort, Plz
		String geo = extractTo(DIV_END);
		if (geo != null) {
			geo = geo.trim();
			if (geo.startsWith(",")) { //$NON-NLS-1$
				geo = geo.substring(1);
			}
			geo = geo.trim();
		}
		String adresse = ""; //$NON-NLS-1$
		int commaIndex = geo.indexOf(","); //$NON-NLS-1$
		if (commaIndex > 0) {
			adresse = geo.substring(0, commaIndex).trim();
			geo = geo.substring(commaIndex + 1).trim();
		}

		String[] plzOrt = getPlzOrt(geo);
		String[] vornameNachname = getVornameNachname(name);

		return new KontaktEntry(vornameNachname[0], vornameNachname[1], "", //$NON-NLS-1$
				adresse, plzOrt[0], plzOrt[1], "", "", "", false); //$NON-NLS-1$
	}
	
	private static String[] POSTFACH_TEXTE = { "Postfach", "Case postale", "Casella Postale" };
	/**
	 * Überprüft ob Postfach in Ort gespeichert ist.
	 */
	private String getPostfach(String text) {	
		String postfachText = "";
		for (String postfachStr: POSTFACH_TEXTE) {
			if (text.trim().toUpperCase().startsWith(postfachStr.toUpperCase())) {
				postfachText = postfachStr;
			}
		}
		if (postfachText.length() > 0) {
			// Hat es noch eine Nummer zu Postfach?
			String restText = text.substring(postfachText.length(), text.length()).trim();
			String[] parts = getSpacedParts(restText);
			String postfachNr = "";
			if (parts.length > 2) { // Könnte sein..
				try {
					postfachNr = " " + Integer.parseInt(parts[0]);
				} catch(NumberFormatException e) {
					// Wahrscheinlich ist das doch keine Postfach-Nr
				}
			}
			postfachText += postfachNr;
		}
		
		return postfachText;
	}

	/**
	 * Extrahiert einen Kontakt aus einem Detaileintrag
	 * Bsp: 
     *   <div class="adrNameDetLev0" style="">
	 *	    <span>Schaller Regula und Tony</span>
	 *		<br>
	 *	      Speckhubel 132
	 *		  <br>
	 *		    3631 Höfen b. Thun
	 *		    <br>
	 *	 </div>
	 */
	private KontaktEntry extractKontakt() {
		if (!moveTo(ADR_DETAIL_TAG)) { // Kein neuer Eintrag
			return null;
		}

		// Name
		String name = extract(SPAN_BEGIN, SPAN_END);
		if (name == null || name.length() == 0) { // Keine leeren Inhalte
			return null;
		}
		
		String[] vornameNachname = getVornameNachname(name);
		String vorname = vornameNachname[0];
		String nachname = vornameNachname[1];

		// Adresse. 1 oder 2 Einträge
		String[] adrArray = new String[3];
		int arrayPos = 0;
		int divPos = getNextPos(DIV_END);
		int nextPos = getNextPos(BR_BEGIN);
		moveTo(BR_BEGIN);
		while (nextPos < divPos && arrayPos < adrArray.length) {
			String entryText = extractTo(BR_BEGIN);
			adrArray[arrayPos] = entryText;
			nextPos = getNextPos(BR_BEGIN);
			arrayPos++;
		}

		String zusatz = ""; //$NON-NLS-1$
		String adresse = ""; //$NON-NLS-1$
		String plz = ""; //$NON-NLS-1$
		String ort = ""; //$NON-NLS-1$
		int max = 0;
		while (max < adrArray.length && adrArray[max] != null) {
			max++;
		}
		max--;
		
		// Füllen
		if (max >= 0) {
			zusatz = getPostfach(adrArray[max]);
			if (zusatz.length() > 0) {
				max--;
			}
			String[] plzOrt = getPlzOrt(adrArray[max]);
			plz = plzOrt[0];
			ort = plzOrt[1];
			max--;
		}
		if (max >= 0) {
			adresse = adrArray[max];
			max--;
		}
		if (max >= 0) {
			zusatz = adrArray[max];
			max--;
		}

		// Tel/Fax & Email
		int adrIndex = getNextPos(ADR_DETAIL_TAG);
		int listIndex = getNextPos(ADR_LIST_TAG);
		int endIndex = adrIndex;
		if ((listIndex >= 0 && listIndex < endIndex) || endIndex < 0) {
			endIndex = listIndex;
		}
		
		String[] adrStrings = extractAdressDetails(endIndex);
 		String tel = extractTelefonNr(adrStrings);
		String fax = extractFax(adrStrings);
		String email = extractEmailAdr(adrStrings);

		return new KontaktEntry(vorname, nachname, zusatz, adresse, plz, ort,
				tel, fax, email, true);
	}
	
	/**
	 * Extrahiert Adressdetails (Tel, Fax & Email) aus einem Detaileintrag. 
	 * Information ist nach den Kontaktinformationen gespeichert.
	 * Bsp: 
     *   <div class="adrNumDet" style="">
	 *	    <span class="noAdvert">*</span>
	 *		<a href="callto:+41333412345"
	 *	      onclick="return VoIp_Check('de');">
	 *		  033 341 23 45
	 *		</a>
	 *	 </div>
	 *   <div class="adrInfo" id="" style="">
	 *     Fax
	 *   </div>
	 *   <div class="adrNumDet" style="">
	 *     <span class="noAdvert">* </span>
	 *     033 341 23 46
	 *   </div>  
	 *   <div class="adrInfo" id="" style="">
	 *     E-Mail
	 *   </div>
	 *   <div class="adrNumDet" style="">
	 *     <span class="noAdvert">* </span>
	 *     <a href="mailto:info@medshare.net" target="_blank">
	 *       info@medshare.net
	 *     </a>
	 *   </div>
	 */
	private String[] extractAdressDetails(int endIndex) {
		String[] strings = new String[] { "", "", "" };
		
		// Tel Nr
		int nextPos = getNextPos("callto:");
		if (nextPos > 0 && (nextPos < endIndex || endIndex < 0)) { // Tel Nr:
			String tel = extract("callto:", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			if (tel != null) {
				tel = tel.replace("+41", "0"); //$NON-NLS-1$ //$NON-NLS-2$
				if (tel.length() > 8) {
					int len = tel.length();
					String tel3 = tel.substring(len - 2);
					String tel2 = tel.substring(len - 4, len - 2);
					String tel1 = tel.substring(len - 7, len - 4);
					String tel0 = tel.substring(0, len - 7);
					tel = tel0 + " " + tel1 + " " + tel2 + " " + tel3; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				strings[0] = tel;
			}
		}
		
		String email = null;
		String fax = null;
		
		// Fax
		nextPos = getNextPos("adrNumDet");
		if (nextPos > 0 && (nextPos < endIndex || endIndex < 0)) {
			moveTo("adrNumDet");
			fax = extract("</span>", "</div>"); //$NON-NLS-1$ //$NON-NLS-2$$
			if (fax.contains("mailto:")) {
				email = new HtmlParser(fax).extract("mailto:", "\"");
				fax = null;
			}
			if (fax.contains("http://")) { // Internet Adresse
				fax = null;
			}
			
		}
		
		// Email
		if (email == null) {
			nextPos = getNextPos("mailto:");
			if (nextPos > 0 && (nextPos < endIndex || endIndex < 0)) {
				email = extract("mailto:", "\"");
			}
		}
		
		if (fax != null) {
			strings[1] = fax;
		}
		if (email != null) {
			strings[2] = email;
		}
		
		return strings;
	}
	
	private String extractTelefonNr(String[] strings) {
		if (strings.length > 0) {
			return strings[0];
		}
		return "";
	}
	
	private String extractFax(String[] strings) {
		if (strings.length > 1) {
			return strings[1];
		}
		return "";
	}
	
	private String extractEmailAdr(String[] strings) {
		if (strings.length > 2) {
			return strings[2];
		}
		return "";
	}
}
