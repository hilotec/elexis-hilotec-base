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
 * $Id: DirectoriesContentParser.java 5255 2009-04-17 11:07:53Z tschaller $
 *******************************************************************************/

package ch.medshare.elexis.directories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

public class DirectoriesContentParser extends HtmlParser {

	private static final String ADR_LIST_TAG = "class=\"vcard searchResult resrowclr"; //$NON-NLS-1$
	private static final String ADR_DETAIL_TAG = "<div class=\"resrowclr";; //$NON-NLS-1$

	public DirectoriesContentParser(String htmlText) {
		super(htmlText);
	}
	
	/**
	 * Retourniert String in umgekehrter Reihenfolge
	 */
	private String reverseString(String text) {
		if (text == null) {
			return "";
		}
		String reversed = "";
		for (char c: text.toCharArray()) {
			reversed = c + reversed;
		}
		return reversed;
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
	
	private String removeDirt(String text) {
		return text.replace("<span class=\"highlight\">","").replace("</span>", "");
	}

	/**
	 * Informationen zur Suche werden extrahiert. 
	 * Bsp:
	 * 	<div class="summary"> 
	 * 		<strong>23</strong> Treffer für 
	 * 		<strong	class="what">müller hans</strong> in 
	 * 		<strong class="where">bern</strong>
	 * 		<div id="printlink" ....
	 * 		<span class="spacer">&nbsp;</span> 
	 * 		<a href="http://tel.local.ch/de/">Neue Suche</a> 
	 * 	</div>
	 */	 
	public String getSearchInfo() {
		reset();
		String searchInfoText =
			extract("<div class=\"summary\">", "<div id=\"printlink\"");
		if (searchInfoText == null) {
			return "";//$NON-NLS-1$
		}
		return searchInfoText.replace("<strong class=\"what\">", "").replace(
			"<strong class=\"where\">", "").replace("<strong>", "").replace(
			"</strong>", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
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
				// Parsing Liste
				entry = extractListKontakt();
			} else if (listIndex < 0
					|| (detailIndex >= 0 && detailIndex < listIndex)) {
				// Parsing Einzeladresse
				entry = extractKontakt();
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
		<div id="te_ojUHu3vXsUWJbXidz2_sRQ"
				onmouseover="lcl.search.onEntryHover(this)"
				onclick="if (typeof(lcl.search) != 'undefined') { lcl.search.navigateTo(event, 'http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&start=3'); }"
				class="vcard searchResult resrowclr_yellow mappable">
				<div class="imgbox">
					<a
						href="http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&amp;start=3">
						<img
							xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
							src="http://s.staticlocal.ch/images/pois/na/blue1.png"
							alt="Dieser Eintrag kann auf der Karte angezeigt werden"
							height="26" width="27" />
					</a>
				</div>
				<div class="entrybox">
					<h4>
						<span class="category" title="Garage">
							Garage
						</span>
						<br>
							<a class="fn"
								href="http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&amp;start=3">
								Autocenter
								<span class="highlight">Meier</span>
								AG
							</a>
						</br>
					</h4>
					<p class="bold phoneNumber">
						<span class="label">Tel.</span>
						<span class="tel">
							<a class="phonenr"
								href="callto://+41627234359">
								062 723 43 59
							</a>
						</span>
					</p>
					<p class="adr">
						<span class="street-address">
							Hauptstrasse 158
						</span>
						,
						<span class="postal-code">5742</span>
						<span class="locality">Kölliken</span>
					</p>
				</div>
				<div style="clear: both;"></div>
			</div>
	 */
	private KontaktEntry extractListKontakt() throws IOException,
			MalformedURLException {

		if (!moveTo(ADR_LIST_TAG)) { // Kein neuer Eintrag
			return null;
		}
		
		// Name, Vorname, Zusatz
		moveTo("<div class=\"entrybox\">");

		int catIndex = getNextPos("<span class=\"category\"");
		int nextEntryPoxIndex = getNextPos("<div class=\"entrybox\">");
		String zusatz = "";
		if (catIndex > 0 && catIndex < nextEntryPoxIndex) {
			moveTo("<span class=\"category\"");
			zusatz = extract("\">", "</span>");
		}

		// Name, Vorname
		moveTo("<a class=\"fn\"");
	
		String nameVornameText = extract("\">", "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		nameVornameText = removeDirt(nameVornameText);
		
		if (nameVornameText == null || nameVornameText.length() == 0) { // Keine leeren Inhalte
			return null;
		}
		String[] vornameNachname = getVornameNachname(nameVornameText);
		String vorname = vornameNachname[0];
		String nachname = vornameNachname[1];
		
		// Tel-Nr
		moveTo("<span class=\"tel\"");
		moveTo("<a class=\"phonenr\"");
		String telNr = extract(">", "</a>").replace("&nbsp;", "").replace("*", "").trim();
		
		// Adresse, Ort, Plz
		String adressTxt = extract("<p class=\"adr\">", "</p>");
		String strasse = removeDirt(new HtmlParser(adressTxt).extract("<span class=\"street-address\">", "</span>"));
		String plz = removeDirt(new HtmlParser(adressTxt).extract("<span class=\"postal-code\">", "</span>"));
		String ort = removeDirt(new HtmlParser(adressTxt).extract("<span class=\"locality\">", "</span>"));

		return new KontaktEntry(vorname, nachname, zusatz, //$NON-NLS-1$
			strasse, plz, ort, telNr, "", "", false); //$NON-NLS-1$
	}

	/**
	 * Extrahiert einen Kontakt aus einem Detaileintrag
	 * Bsp: 
		<div class="resrowclr_yellow">
		</br>
		<img class="imgbox"
			src="http://s.staticlocal.ch/images/pois/na/blue.png" alt="poi"/>
		<p class="role">Garage</p>
		<h2 class="fn">Auto Meier AG</h2>
		<p class="role">Opel-Vertretung</p>
		<div class="addressBlockMain">
			<div class="streetAddress">
				<span class="street-address">Hauptstrasse 253</span>
				</br>
				<span class="post-office-box">Postfach<br></span>
				<span class="postal-code">5314</span>
				<span class="locality">Kleindöttingen</span>
			</div>
			</br>
			<table>
				<tbody>
					<tr class="phoneNumber">
						<td>
							<span class="contact">Telefon:</span>
						</td>
						<td class="tel">
							<a class="phonenr" href="callto://+41562451818">
								056 245 18 18
							</a>
						</td>
						<td id="freecall"></td>
					</tr>
				</tbody>
			</table>
		</div>
		<br class="bighr"/>
		<div id="moreAddresses">
			<h3>Zusatzeintrag</h3>
			<div class="additionalAddress" id="additionalAddress1">
				<span class="role">Verkauf</span>
				</br>
				<table>
					<tbody>
						<tr class="phoneNumber">
							<td>
								<span class="contact">Telefon:</span>
							</td>
							<td class="tel">
								<a class="phonenr"
									href="callto://+41448104211">
									044 810 42 11
								</a>
							</td>
							<td id="freecall"></td>
						</tr>
						<tr>
							<td>
								<span class="contact">Fax:</span>
							</td>
							<td>
								*&nbsp;044 810 54 40
							</td>
						</tr>
						<tr>
							<td>&nbsp;
							</td>
							<td>&nbsp;
							</td>
							<td></td>
						</tr>
						<tr class="">
							<td>
								<span class="contact">E-Mail:</span>
							</td>
							<td>
								*&nbsp;<a href="mailto:info@kvd.ch">
									info@kvd.ch
									</a>
							</td>
							<td></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	 */
	private KontaktEntry extractKontakt() {
		if (!moveTo(ADR_DETAIL_TAG)) { // Kein neuer Eintrag
			return null;
		}

		// Name, Vorname
		String nameVornameText = extract("<h2 class=\"fn\">", "</h2>");
		if (nameVornameText == null || nameVornameText.length() == 0) { // Keine leeren Inhalte
			return null;
		}
		String[] vornameNachname = getVornameNachname(nameVornameText);
		String vorname = vornameNachname[0];
		String nachname = vornameNachname[1];
		
		//Zusatz
		String zusatz = "";
		if (moveTo("<p class=\"role\">")) {
			zusatz = extractTo("</p>");
		}

		// Adresse
		String adressTxt = extract("<div class=\"streetAddress\">", "</div>");
		HtmlParser parser = new HtmlParser(adressTxt);
		String streetAddress = removeDirt(parser.extract("<span class=\"street-address\">", "</span>"));
		String poBox = removeDirt(parser.extract("<span class=\"post-office-box\">", "</span>"));
		String plzCode = removeDirt(parser.extract("<span class=\"postal-code\">", "</span>"));
		
		//Ort
		//String ort = removeDirt(new HtmlParser(adressTxt).extract("<span class=\"locality\">", "</span>"));
		parser.moveTo("<tr class=\"locality\">");
		parser.moveTo("<a href=");
		String ort = removeDirt(parser.extract(">", "</a>").replace("&nbsp;", "").trim());
		
		if (zusatz == null || zusatz.length() == 0) {
			zusatz = poBox;
		}

		// Tel/Fax & Email	
		moveTo("<tr class=\"phoneNumber\">");
		String tel = "";
		if (moveTo("<span class=\"contact\">Telefon")) {
			moveTo("<td class=\"tel\"");
			moveTo("<a class=\"phonenr\"");
			tel = extract(">", "</a>").replace("&nbsp;", "").replace("*", "").trim();
		}
		String fax = "";
		if (moveTo("<span class=\"contact\">Fax")) {
			fax = extract("<td>", "</td>").replace("&nbsp;", "").replace("*", "").trim();
		}
		String email = "";
		if (moveTo("<span class=\"contact\">E-Mail")) {
			moveTo("<span class=\"obfuscml\"");
			email = extract("\">", "</span>");
			// Email Adresse wird verkehrt gesendet
			email = reverseString(email);
		}
		
		return new KontaktEntry(vorname, nachname, zusatz, streetAddress, plzCode, ort,
				tel, fax, email, true);
	}
}
