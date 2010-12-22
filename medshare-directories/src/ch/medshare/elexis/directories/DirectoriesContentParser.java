/*******************************************************************************
 * Copyright (c) 2007, medshare and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Imhof - initial implementation
 *    J. Sigle - 20101213-20101217 www.jsigle.com
 *    			 Hopefully, all of my changes are marked by a comment that has "js" in it.
 *    			 Adoption of processing of results in ADR_LIST format to changed html content,
 *                 as recently introduced by http://tel.local.ch.
 *               Filling of poBox (and some other fields) made conditional:
 *                 The target is now initialized as an empty string, and only filled from the
 *                 processed html when moveTo("SomeRespectiveTag") was successful.
 *                 Otherwise, some garbage could be read (or persist) in(to) the target
 *                 variable - and, in the case of poBox, if zusatz was empty, that garbage
 *                 would appear up there. Effectively, the user had to clean zusatz
 *                 more often than not e.g. when the address of a health insurance company
 *                 was obtained.
 *               Enhanced removeDirt() to also remove leading and trailing blanks/spaces.
 *                 I found addresses with trailing blanks in the street address field quite often.
 *               Some comments added with regard to program functionality.
 *               Some debug/monitoring output added in internal version only;
 *                 commented out again for published version: //System.out.print("jsdebug: ...           
 *                 
 *    			 Suggestion regarding getVornameNachname():
 *                 Maybe the first-name/last-name split should be changed,
 *    			   or an interactive selection of the split point be provided.
 *                 See new comment before the function header.
 *                 
 *               Suggestion regarding zusatz:
 *                 If a title like Dr. med., PD Dr. med., Prof. is encountered,
 *                 split it off and put it directly into the title field.
 *                 (We use zusatz for a Facharztbezeichnung like Innere Medizin FMH,
 *                  and title for a Titel like Dr. med. etc.)
 *    
 *               Suggestion regarding second level search request:
 *                 Problem: When I first search for anne müller in bern,
 *                 and thereafter double click on the first entry, I get data
 *                 in the add-contact-dialog that does NOT include her title (role).
 *                 (with java code revised by myself, which works for other
 *                  müllers returned in the first result list, including their titles).
 *                  
 *                 Similar problem: Looking for Henzi in Bern finds a whole list of results.
 *                 Double clicking the last (Stefan Henzi) returns another list.
 *                 Some of the entries have the title Dr. med. and some don't -
 *                 Elexis does NOT show the second list for further selection,
 *                 and it apparently evaluates one that doesn't have the title.
 *                 
 *                 Not similar - but related problem: Looking for Hamacher in Bern
 *                 returns two entries. Double click on Jürg Hamacher in the tel.local.ch
 *                 page in the WWW browser returns a single entry. This detail entry
 *                 (but not the previously shown list entry) includes the title and
 *                 the e-mail address. Neither is transferred to Elexis when double
 *                 clicking on the second list entry.
 *                 But when I search for Jürg Hamacher immediately, the single detailed
 *                 entry is found immediately, and all information from there transferred.  
 *                     
 *                     
 *                 I'm somewhat unsure whether my assumption regarding what happens
 *                 are correct here (would need to further review the program).
 *                 I currently guess that:
 *                 
 *                 If a user clicks on one entry from the list returned by an initial search,
 *                 then feed the second level search (which will feed the new kontakt entry dialog)
 *                 with both the name AND the address returned from the first level search.
 *                 
 *                 (a) The results from the second level search, may be processed, but are
 *                     NOT really used by Elexis. I.e. I see debug output from inside
 *                     extractKontakt(), but I don't see any effects (i.e. changed variable
 *                     content) in the Elexis Kontakt dialog.
 *                     (The Jürg Hamacher example)
 *                     
 *                 (b) Supplying ONLY the name, will not suffice to get a single-entry result
 *                     e.g. for Anne Müller in Bern, so her title which is available only in the
 *                     detailed result output will be missed.
 *                     (The Anna Müller Stefan Henzi examples)
 *                
 *                 Moreover, it is (maybe) shere luck that in this case,
 *                 we have only ONE Anne Müller in Bern in the result list - 
 *                 and all others have some additional names. Otherwise, I'm unsure
 *                 whether a result list containing e.g. TWO entries for Max Muster,
 *                 would return the correct one for either case in the second level search...
 *                 (The Stefan Henzi examples, I guess, *might* illustrate just that.)
 *
 *                 I have followed this through to WeiseSeitenSearchForm.java
 *                 openKontaktDialog() where the information from the list_format entry
 *                 (i.e. with empty zusatz, exactly the entry at which we double clicked) is supplied.
 *                 
 *                 I've also reviewed open.dialog() one step further - but then it becomes too
 *                 much for me for today. From my debugging output, I can clearly see that
 *                 (for the Jürg Hamacher example):
 *                 
 *                 *after* openKontaktDialog() is called,
 *                 there occurs another call of extractKontakte() (from where?!)
 *                 which calls extractKontakt(),
 *                 which returns all information from the detailed information (),
 *                 which is added as part of a new kontakte.entry at the end of the extractKontakt() loop,
 *                 and apparently ignored thereafter (why that?)
 *                 
 *                 I'm unsure whether this lasts search/search result processing should not
 *                 better occur *before* the dialog is opened, and its information used to
 *                 feed the dialog. But take care; that multiple dialogs may be opened if
 *                 multiple contacts are selected on the first list, so they all must be
 *                 fed with individual new searches, and the original contact list may not
 *                 be forgotten until the last dialog window so generated has been closed...
 *                 
 *                 Please look at my extensive Anne Müller related comments below;
 *                 and please note, that Anne Müller's "Zusatz" is not lost because
 *                 I changed some zusatz related lines, but because the second level
 *                 search request apparently returns a list_format result (again),
 *                 which does NOT have a title entry for her and/or because when a
 *                 detailed_format result is returned in a second level search,
 *                 it is processed, but its results are not honoured.
 *                 
 *                 Please review the output of tel.local.ch for all entries on the
 *                 first anne müller bern search result, and what happens with the
 *                 various titles. Some work, some don't.
 *                 Please also review the Stefan Henzi example case.
 *                 
 *                 You can easily switch on my debug output:
 *                 look for: //Anne Müller case debug output
 *                 in DirectoriesContentParser.java (this file)
 *                 and WeisseSeitenSearchView.java
 *                 Or just uncomment all occurences of "//System.out.print("jsdebug:"
 *                 in these files (except the one in the line above) with find/replace.
 *                 
 *                 You can also set the variable zusatz to a fixed value in either
 *                 extractKontakt() or extractListKontakt() functions and see what is used,
 *                 and what is ignored.
 *
 *                 Sorry - for myself, I just don't have the time to review that
 *                 problem in more detail by now today; I also think it's of secondary
 *                 importance compared to the restoration of the first level search
 *                 function in general.
 *    
 * $Id: DirectoriesContentParser.java 5277 2009-05-05 19:00:19Z tschaller $
 *******************************************************************************/

package ch.medshare.elexis.directories;

import java.io.IOException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * 
 * @author jsigle (comment and 20101213 update only)
 *
 * The service http://tel.local.ch provides a user-interface for WWW browsers
 * with a lot of additional content around the desired address/contact information.
 * The address/contact information is extracted from this material and returned in variable fields
 * suitable for further usage by Elexis.
 *  
 * If a search request returns multiple results, these appear in (what we call) "ADR_LIST" format.
 * If a search request returns exactly one result (or one entry from the list
 * is clicked at in the WWW browser), the result appears in (what we call) "ADR_DETAIL" format.
 * 
 * By 12/2010, a change in the output format of tel.local.ch required new marker strings for processing.
 * The processing of a result in ADR_DETAIL format continued to work,
 * But the processing of a result in ADR_LIST format would deliver an empty result.
 * 
 */
public class DirectoriesContentParser extends HtmlParser {
	
	//20101213js - 20101217js
	//
	//Original code before 20101213:
	//private static final String ADR_LIST_TAG = "class=\"vcard searchResult resrowclr"; //$NON-NLS-1$
	//private static final String ADR_DETAIL_TAG = "<div class=\"resrowclr";; //$NON-NLS-1$
	//
	//I tried several alternative markers with different results;
	//see additional notes in the non-published interim version dated 20101213. 
	//
	//New code, after 20101213:
	private static final String ADR_LIST_TAG = "class=\"searchResult phonebook"; //$NON-NLS-1$
	private static final String ADR_DETAIL_TAG = "<div class=\"resrowclr";; //$NON-NLS-1$
	
	public DirectoriesContentParser(String htmlText){
		super(htmlText);
	}
	
	/**
	 * Retourniert String in umgekehrter Reihenfolge
	 */
	private String reverseString(String text){
		if (text == null) {
			return "";
		}
		String reversed = "";
		for (char c : text.toCharArray()) {
			reversed = c + reversed;
		}
		return reversed;
	}

	/**
	 * Comment added: 201012130058js
	 * This splits the provided string at the first contained space.
	 * This is not optimal for all cases: Persons may have multiple given names / christian names,
	 * and they will very often be separated just by spaces. I actually observed in real life usage
	 * that a second given name went to the "Name"="Nachname"="Family name" field together with the
	 * true family name.
	 * It might be better to split the name at the *last* contained space,
	 * because multiple family names are usually linked by a dash (-),
	 * rather than separated by a space (this is my personal impression).
	 * However, I haven't changed the code so far.
	 */
	private String[] getVornameNachname(String text){
		String vorname = ""; //$NON-NLS-1$
		String nachname = text;
		int nameEndIndex = text.trim().indexOf(" "); //$NON-NLS-1$
		if (nameEndIndex > 0) {
			vorname = text.trim().substring(nameEndIndex).trim();
			nachname = text.trim().substring(0, nameEndIndex).trim();
		}
		return new String[] {
			vorname, nachname
		};
	}
	
	private String removeDirt(String text){
		//20101217js
		//remove leading and trailing whitespace characters
		text = text.replaceAll("^+\\s", "");
	    text = text.replaceAll("\\s+$", "");
	    
	    return text.replace("<span class=\"highlight\">", "").replace("</span>", "");
	}
	
	/**
	 * Informationen zur Suche werden extrahiert.
	 * 
	 * 20101213js added comments
	 * Bsp: (valid before 2010-12-xx)
	 * 
	 * <div class="summary"> <strong>23</strong>
	 * Treffer für <strong class="what">müller hans</strong> in <strong class="where">bern</strong>
	 * <div id="printlink" .... <span class="spacer">&nbsp;</span> <a
	 * href="http://tel.local.ch/de/">Neue Suche</a> </div>
	 * 
	 * Bsp: (valid after 2010-12-13, linebreaks added for clarity)
	 * 
	 * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	 * <html  lang="de" xml:lang="de">
	 * <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	 * <title>meier in bern - 965 Treffer auf local.ch</title>
	 * <link rel="stylesheet" ...
	 * 
	 */
	public String getSearchInfo(){
		reset();
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: getSearchInfo() running...\n");
		
		//20101213js 
		//The elexis Original line here was:
		//String searchInfoText = extract("<div class=\"summary\">", "<div id=\"printlink\"");
		
		//tel.local.ch however have changed the "<div id=" into "<a id=";
		//I'm actually unsure whether we can rely on this; but as the thing is called
		//print*link* we might assume that it's quite plausible to expect it in an <a ...> tag.
		
		//Trying it out...
		//OK this returns SOMETHING, but much much more than what we want...:
		//String searchInfoText = extract("<div class=\"summary\">", "<a id=\"printlink\"");
		
		//OK. I could probably find a better marker string, but actually, the thing that we want
		//is much more easily given in the <title> of the page, so why not just evaluate that... :-)
		String searchInfoText = extract("<title>", "</title>");
		//Works like a charm, result is beautiful :-) 201012130217js
		//N.B.: You might want to remove some postprocessing searchInfoText.replace()
		//which should have become obsolete by processing the <title> content;
		//see corresponding comment a few lines below. 
		
		if (searchInfoText == null) {
			return "";//$NON-NLS-1$
		}

		//System.out.print("jsdebug: DirectoriesContentParser.java: getSearchInfo(): searchInfoText != null\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: getSearchInfo(): \""+searchInfoText+"\"\n\n");

		//20101217js
		//In the updated version processing the <title> tag content,
		//we probably would not need the following replacements any more.
		//I leave them in for now, just in case someone reverts to processing html body code.
		return searchInfoText.replace("<strong class=\"what\">", "").replace(
			"<strong class=\"where\">", "").replace("<strong>", "").replace("</strong>", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Extrahiert Informationen aus dem retournierten Html. Anhand der <div class="xxx"> kann
	 * entschieden werden, ob es sich um eine Liste oder einen Detaileintrag (mit Telefon handelt).
	 * 
	 * Detaileinträge: "adrNameDetLev0", "adrNameDetLev1", "adrNameDetLev3" Nur Detaileintrag
	 * "adrNameDetLev2" darf nicht extrahiert werden
	 * 
	 * Listeinträge: "adrListLev0", "adrListLev1", "adrListLev3" Nur Listeintrag "adrListLev0Cat"
	 * darf nicht extrahiert werden
	 */
	public List<KontaktEntry> extractKontakte() throws IOException{
		reset();

		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte() running...\n");
		
		List<KontaktEntry> kontakte = new Vector<KontaktEntry>();
		
		int listIndex = getNextPos(ADR_LIST_TAG);
		int detailIndex = getNextPos(ADR_DETAIL_TAG);

		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte() initial values of...\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte().listIndex: "+listIndex+"\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte().detailIndex: "+detailIndex+"\n");
		
		while (listIndex > 0 || detailIndex > 0) {
			KontaktEntry entry = null;

			//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte() intraloop values of...\n");
			//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte().listIndex: "+listIndex+"\n");
			//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakte().detailIndex: "+detailIndex+"\n");
			
			if (detailIndex < 0 || (listIndex >= 0 && listIndex < detailIndex)) {
				// Parsing Liste
				entry = extractListKontakt();
			} else if (listIndex < 0 || (detailIndex >= 0 && detailIndex < listIndex)) {
				// Parsing Einzeladresse
				entry = extractKontakt();
			}
			
			if (entry != null) {
				kontakte.add(entry);
				
				//20101217js
				//Anne Müller case debug output:
				//System.out.print("jsdebug: extractKontakte() kontakte added entry.name:     "+entry.getName().toString()+"\n");
				//System.out.print("jsdebug: extractKontakte() kontakte added entry.vorname:  "+entry.getVorname().toString()+"\n");
				//System.out.print("jsdebug: extractKontakte() kontakte added entry.zusatz:   "+entry.getZusatz().toString()+"\n");
				//System.out.print("jsdebug: extractKontakte() kontakte added entry.isDetail: "+entry.isDetail()+"\n");
				//System.out.print("jsdebug: \n");				
			}
			listIndex = getNextPos(ADR_LIST_TAG);
			detailIndex = getNextPos(ADR_DETAIL_TAG);
		}
				
		return kontakte;
	}
	
	/**
	 * Extrahiert einen Kontakt aus einem Listeintrag
	 * 
	 * Bsp: (valid before 2010-12-xx)
	 * 
	 * <div id="te_ojUHu3vXsUWJbXidz2_sRQ"
	 * onmouseover="lcl.search.onEntryHover(this)" onclick="if (typeof(lcl.search) != 'undefined') { lcl.search.navigateTo(event, 'http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&start=3'); }"
	 * class="vcard searchResult resrowclr_yellow mappable"> <div class="imgbox"> <a
	 * href="http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&amp;start=3"> <img
	 * xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	 * src="http://s.staticlocal.ch/images/pois/na/blue1.png"
	 * alt="Dieser Eintrag kann auf der Karte angezeigt werden" height="26" width="27" /> </a>
	 * </div> <div class="entrybox"> <h4>
	 * <span class="category" title="Garage"> Garage </span> <br>
	 * <a class="fn" href="http://tel.local.ch/de/d/ILwo-yKRTlguXS4TFuVPuA?what=Meier&amp;start=3">
	 * Autocenter <span class="highlight">Meier</span> AG </a> </br></h4> <p
	 * class="bold phoneNumber"> <span class="label">Tel.</span> <span class="tel"> <a
	 * class="phonenr" href="callto://+41627234359"> 062 723 43 59 </a> </span> </p> <p class="adr">
	 * <span class="street-address"> Hauptstrasse 158 </span> , <span
	 * class="postal-code">5742</span> <span class="locality">Kölliken</span> </p> </div> <div
	 * style="clear: both;"></div> </div>
	 *
	 * 20101217js
	 * Bsp: (valid after 2010-12-13)
	 * *
	 * Now, every entry apparently seems to consist of:
	 * line1: <div data-slot="tel" class="searchResult..."
	 * line2: empty
	 * line3: <li class="detail">...
	 * line4: empty
	 *
	 * Please note (!) that the <li class="detail"> tag is a part of the ADR_LIST display type now,
	 * which could be confusing to other people reviewing this code...
	 * And please note that one entry does NOT begin with the <li class "detail"> tag,
	 * but (quite probably, I've not perfectly reviewed it) with the <div data-slot="... tag.
	 * 
	 * Please also note that the address/contact details seem to be included in either of the data carrying lines.
	 * 
	 * You might use the program tidy to re-format a downloaded result page to a more easily human readable style...
     *
     * <div data-slot="tel" class="searchResult phonebookSearchResult vcard mappable  ga ga\~entryBody\~entryClick  yellowSearchResult "><div class="poiContainer"></div><
div class="poiImageContainer hidden"></div><span class="category" title="Treuhandgesellschaft Treuhandb&#xFC;ro; Buchhaltungsb&#xFC;ro"><a href="/de/q/bern/Treuhan
dgesellschaft Treuhandb&#xFC;ro.html" title="Suche nach Treuhandgesellschaft Treuhandb&#xFC;ro in bern">Treuhandgesellschaft Treuhandb&#xFC;ro</a><span>; </span><a
 href="/de/q/bern/Buchhaltungsb&#xFC;ro.html" title="Suche nach Buchhaltungsb&#xFC;ro in bern">Buchhaltungsb&#xFC;ro</a></span><h3><a class="fn" href="http://tel.l
ocal.ch/de/d/Bern/3008/Treuhandgesellschaft-Treuhandbuero/AAA-services-meier-franzelli-D6PmwphJZ6_tq_InnYTDIw?what=meier&amp;where=bern">AAA services <span class="
highlight">meier</span> + franzelli</a><br /></h3><p class="contact phoneContact"><span class="label">Tel.: </span><span class="noads">* </span><span class="tel"><
a class="phonenr" href="callto://+41313825082">031 382 50 82</a></span></p><p class="address adr"><span class="street-address">G&#xFC;terstrasse 22</span>, <span c
lass="postal-code">3008</span> <span class="locality"><span class="highlight">Bern</span></span></p><ul class="links">
     *
     *           
     * <li class="detail"><img src="http://s.staticlocal.ch/2/74023/s/resultlist/images/localinfo/detail.png" alt="" /><a class="detail ga ga\~entryMoreDetails\~e
ntryClickResult" href="http://tel.local.ch/de/d/Bern/3008/Treuhandgesellschaft-Treuhandbuero/AAA-services-meier-franzelli-D6PmwphJZ6_tq_InnYTDIw?what=meier&amp;whe
re=bern">Mehr Details anzeigen</a></li></ul><div class="mapMarker"><span class="id">resultentry_0</span><span class="long">7.416991</span><span class="lat">46.9473
01</span><span class="head">AAA services <span class="highlight">meier</span> + franzelli</span><div class="body"><a class="fn" href="http://tel.local.ch/de/d/Bern
/3008/Treuhandgesellschaft-Treuhandbuero/AAA-services-meier-franzelli-D6PmwphJZ6_tq_InnYTDIw?what=meier&amp;where=bern&amp;flyout=true">AAA services <span class="h
ighlight">meier</span> + franzelli</a><p class="address adr"><span class="street-address">G&#xFC;terstrasse 22</span>, <span class="postal-code">3008</span> <span 
class="locality"><span class="highlight">Bern</span></span></p><p class="contact phoneContact"><span class="label">Tel.: </span><span class="noads">* </span><span 
class="tel">031 382 50 82</span></p></div><span class="iconKey">tel/search</span></div></div>
     *
	 */
	private KontaktEntry extractListKontakt() throws IOException, MalformedURLException{
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() running...\n");
		
		if (!moveTo(ADR_LIST_TAG)) { // Kein neuer Eintrag
			return null;
		}
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() extracting next entry...\n");
		
		// Name, Vorname, Zusatz
		
		//20101213js
		//please note: the same marker used here should be re-used further below.
		//
		//Original code was:
		//moveTo("<div class=\"entrybox\">");
		//
		//I tried these alternative markers, which didn't work as desired:
		//moveTo("<div class=\"detail\">");
		//moveTo("<li class=\"detail\">");
		//moveTo("<span class=\"id\">resultentry");
		//moveTo("<div class=\"body\">");
		//
		//this one finally worked:
		//New code:
		moveTo("<div class=\"poiContainer\">");
		

		//20101213js
		//please note: the same marker used here has been used further above.
		//
		//Original code was:
		//int nextEntryPoxIndex = getNextPos("<div class=\"entrybox\">");
		//
		//New code:
		int nextEntryPoxIndex = getNextPos("<div class=\"poiContainer\">");

		
		//20101213js-20101217js:
		//The attempted processing of Zusatz was somewhat disappointing.
		//Even before the change in tel.local.ch output format, I've seen
		//garbage including HTML fragments in the Zusatz field, which had to
		//be deleted by the user before the address could be stored.
		//
		//NOTA BENE: Beim Doppelklick auf einen Kontakt der Ergebnisliste wird
		//dieser offenbar nochmals neu abgefragt, und die *daraus erst* resultierende
		//Detail-Information wird in die Elexis-internen Kontaktdaten übernommen!!!
		//Deshalb ist es nicht ausreichend, die Zusatz-Abfrage hier
		//in extractListKontakt() zu entfernen, sondern insbesondere
		//bei extractKontakt() muss sie reviewt werden!
		//
		//Nota bene 2: I learned later that most probably, the problem comes
		//when some poBox field content is copied into the zusatz field,
		//if that had been empty before.
		//But if the 4 lines after String zusatz=""; are left active,
		//then e.g. Anne Müller receives the preceeding "Suche nach Craniotherapy in Bern"
		//(including HTML code) in zusatz; if they are commented out; then zusatz remains empty.
		//...
		//Hmm. Reviewing the HTML source again...
		//OK: The problem is that tel.local.ch now delivers one "role" before "fn",
		//which is a link to (I guess) searching all entries with the same role in the same locality;
		//and *another* "role" after "fn", which is the actual role field for the current entry.
		//
		//Possibly, the "role" before "fn" could be now what "category" has been
		//in a previous version of tel.local.ch output? Anyway, there's no category field in
		//the exemplary output obtained by searching for Anne Müller in Bern (who provides Craniosacral Therapy)
		//
		//Example:
		//
		//</script></div><div id="results"><div id="singleview" class="vcard"><div class="resrowclr_yellow"><br /><img class="imgbox" src="http://s.staticlocal.ch/images/pois/na/blue.png" alt="poi" /><p class="role"><a href="http://tel.local.ch/de/q/bern/Craniosacral Therapie.html" title="Suche nach Craniosacral Therapie in bern">Craniosacral Therapie</a>; <a href="http://tel.local.ch/de/q/bern/Massage Gesundheits- und Sport-.html" title="Suche nach Massage Gesundheits- und Sport- in bern">Massage Gesundheits- und Sport-</a>; <a href="http://tel.local.ch/de/q/bern/Gesundheitspraxis.html" title="Suche nach Gesundheitspraxis in bern">Gesundheitspraxis</a></p><h2 class="fn">Anne M&#xFC;ller</h2><p class="role">dipl. Physiotherapeutin Mitglied Cranio Suisse</p>
		//
		//N.B.: Anne Müller / Craniotherapy shows a role only in her detail_format output, but not if she appears within a list_format output.
		//If I double click on the first search result in the medshare-directories form,
		//then a detail entry is processed (I can see this in my debug output) -
		//where her title also becomes visible and known to the program -
		//BUT only the results from the list entry processing are transferred into the following dialog.
		//I can see that definitely if I hard-code some variable content in the extractKontakt() function,
		//or in the extractListKontakt() function.
		//
		//Hmmm. Only if I search for "anne müller craniotherapie" in "bern",
		//or for "anne müller" in "monbijoustrasse 12 bern",
		//the detail entry is processed and her title finally appears...
		//
		//But why's that the case?!
		//
		//I guess that anne müller is a very special case:
		//There are many anne müller search hits,
		//and probably, elexis only uses (all the) name information in order to get the detailed
		//search entry to be processed into the following dialog. So in Anne Müller's case,
		//ALL the name information is NOT sufficient to separate list entry 1 from all the other
		//entries, so even the refined search does NOT return a detail_format result, but
		//returns a list again - without her title, of course.
		//
		//So we should probably use the name and the street info if available
		//to feed the request for details, not just the name.
		//
		//
		//Besides, the relevant entry is in a paragraph rather than a span tag now:
		//<p class="role">THE ROLE</p>
		//instead of:
		//<span class="category">THE CATEGORY</span>
		//
     	//So I disable looking for a zusatz in "category" here,
		//but rather look for zusatz in "role" after "fn" below:
		//
		//Old code to fill the zusatz field from "category":
		//int catIndex = getNextPos("<span class=\"category\"");
		//String zusatz = "";
		//if (catIndex > 0 && catIndex < nextEntryPoxIndex) {
		//	moveTo("<span class=\"category\"");
		//	zusatz = extract("\">", "</span>");
		//	}

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
		
		//20101217js
		//Also see comment related to "category" above.
		//New code to fill the zusatz field from "role" after "fn":
		int catIndex = getNextPos("<p class=\"role\">");
		String zusatz = "";
		if (catIndex > 0 && catIndex < nextEntryPoxIndex) {
			moveTo("<p class=\"role\">");
			zusatz = extractTo("</p>");
			}
		
		//Anne Müller case debug output:
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() nameVornameText: "+nameVornameText+"\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() catIndex: "+catIndex+"\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() nextEntryPoxIndex: "+nextEntryPoxIndex+"\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt() zusatz: \""+zusatz+"\"\n\n");
		
		// Tel-Nr
		moveTo("<span class=\"tel\"");
		moveTo("<a class=\"phonenr\"");
		String telNr = extract(">", "</a>").replace("&nbsp;", "").replace("*", "").trim();
		
		// Adresse, Ort, Plz
		
		//201013js
		//Old code:
		//String adressTxt = extract("<p class=\"adr\">", "</p>");
		//
		//New code:
		String adressTxt = extract("<p class=\"address adr\">", "</p>");

		//System.out.print("jsdebug: DirectoriesContentParser.java: extractListKontakt().addressTxt:\n"+adressTxt+"\n\n");
		
		// 5.5.09 ts: verschachtelte spans -> alles bis zur nächsten span klasse holen
		// 20101213js: I'm unaware why this should be needed here.
		//             At the moment, it appears to be sufficient to get into strasse
		//             everything till the next </span> tag, and the same for plz and ort.
		//             I change it to be that way. If you are in doubt, you may want
		//             to review if this really works for many search examples.
		//
		//             What I observed multiple times is that a trailing extra space
		//             was returned in the strasse field. I guess it will be best
		//             to add its removal in the "removeDirt" function... 
		//
		//Old code:
		// String strasse =
		// removeDirt(new HtmlParser(adressTxt).extract("<span class=\"street-address\">",
		//		", <span class="));
		//
		//New code:
		String strasse =
		 	removeDirt(new HtmlParser(adressTxt).extract("<span class=\"street-address\">",
				"</span>"));
		String plz =
				removeDirt(new HtmlParser(adressTxt).extract("<span class=\"postal-code\">", "</span>"));
		String ort =
				removeDirt(new HtmlParser(adressTxt).extract("<span class=\"locality\">", "</span>"));
			
		return new KontaktEntry(vorname, nachname, zusatz, //$NON-NLS-1$
		strasse, plz, ort, telNr, "", "", false); //$NON-NLS-1$
	}
	

	/**
	 * Extrahiert einen Kontakt aus einem Detaileintrag
	 * 
	 * Comment added to support testing 201012130311js:
	 * A result of this type should be obtainable by searching for:
	 * Wer, Was: eggimann meier
	 * Wo: bern
     *
	 * Bsp:
	 * 
	 * <div class="resrowclr_yellow"> </br>
	 * <img class="imgbox" src="http://s.staticlocal.ch/images/pois/na/blue.png" alt="poi"/> <p
	 * class="role">Garage</p> <h2 class="fn">Auto Meier AG</h2> <p class="role">Opel-Vertretung</p>
	 * <div class="addressBlockMain"> <div class="streetAddress"> <span
	 * class="street-address">Hauptstrasse 253</span> </br> <span class="post-office-box">Postfach<br>
	 * </span> <span class="postal-code">5314</span> <span class="locality">Kleindöttingen</span>
	 * </div> </br>
	 * <table>
	 * <tbody>
	 * <tr class="phoneNumber">
	 * <td>
	 * <span class="contact">Telefon:</span></td>
	 * <td class="tel">
	 * <a class="phonenr" href="callto://+41562451818"> 056 245 18 18 </a></td>
	 * <td id="freecall"></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * </div> <br class="bighr"/> <div id="moreAddresses"> <h3>Zusatzeintrag</h3> <div
	 * class="additionalAddress" id="additionalAddress1"> <span class="role">Verkauf</span> </br>
	 * <table>
	 * <tbody>
	 * <tr class="phoneNumber">
	 * <td>
	 * <span class="contact">Telefon:</span></td>
	 * <td class="tel">
	 * <a class="phonenr" href="callto://+41448104211"> 044 810 42 11 </a></td>
	 * <td id="freecall"></td>
	 * </tr>
	 * <tr>
	 * <td>
	 * <span class="contact">Fax:</span></td>
	 * <td>
	 * &nbsp;044 810 54 40</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td></td>
	 * </tr>
	 * <tr class="">
	 * <td>
	 * <span class="contact">E-Mail:</span></td>
	 * <td>
	 * &nbsp;<a href="mailto:info@kvd.ch"> info@kvd.ch </a></td>
	 * <td></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * </div> </div> </div>
	 */
	private KontaktEntry extractKontakt(){
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakt() running...\n");
		
		if (!moveTo(ADR_DETAIL_TAG)) { // Kein neuer Eintrag
			return null;
		}
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakt() extracting next entry...\n");
				
		// Name, Vorname
		String nameVornameText = extract("<h2 class=\"fn\">", "</h2>");
		
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakt().nameVornameText: \""+nameVornameText+"\"\n");
		
		if (nameVornameText == null || nameVornameText.length() == 0) { // Keine leeren Inhalte
			return null;
		}
		String[] vornameNachname = getVornameNachname(nameVornameText);
		String vorname = vornameNachname[0];
		String nachname = vornameNachname[1];
		
		// Zusatz
		//
		//Comment added 20101213js:
		//Please note, that if zusatz remains empty, then further below it will receive
		//the content of the poBox field. This however, would return garbage (i.e. some
		//remainders of PLZ and ORT plus HTML tag leftovers) in versions before 2010-12-13.
		//That garbage, however, was introduced from the poBox related code below,
		//where plzCode was filled even when the corresponding tag was not available,
		//whereas the following 4 lines are (and have been) apparently ok:
		String zusatz = "";
		if (moveTo("<p class=\"role\">")) {
			zusatz = extractTo("</p>");
		}
		
		//Anne Müller case debug output:
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakt() nameVornameText: "+nameVornameText+"\n");
		//System.out.print("jsdebug: DirectoriesContentParser.java: extractKontakt() zusatz: \""+zusatz+"\"\n\n");
		
		// Adresse
		String adressTxt = extract("<div class=\"streetAddress\">", "</div>");
		
		HtmlParser parser = new HtmlParser(adressTxt);
		String streetAddress = removeDirt(parser.extract("<span class=\"street-address\">", "</span>"));
		
		//20101213js:
		//The simple (unconditional):
		//
		//Old code:
		//String poBox = removeDirt(parser.extract("<span class=\"post-office-box\">", "</span>"));
		//
		//would not really work if there was no post-office-box available at all.
		//In that case, it might return garbage.
		//And if zusatz was also empty (see further above), then any bad content
		//of poBox would be propagated up to zusatz.
		//Therefore, we want to be a bit more careful with doing anything into poBox:
		//
		//New code:
		String poBox = "";
		if (moveTo("<span class=\"post-office-box\">")) {
			poBox = removeDirt(extractTo("</span>"));
		}
		//Ja, so ist es gut :-)

		//plzCode
		//
		//20101217js:
		//It's probably better to also fill plzCode ONLY when moveTo() would not fail.
		//
		//Old code:
		//String plzCode = removeDirt(parser.extract("<span class=\"postal-code\">", "</span>"));
		//
		//New code:
		String plzCode = "";
		if (moveTo("<span class=\"postal-code\">")) {
			plzCode = removeDirt(extractTo("</span>"));
		}
		
		// Ort
		//
		//20101217js:
		//It's probably better to also fill Ort ONLY when moveTo() would not fail.
		//
		//Older code:
		// String ort = removeDirt(new HtmlParser(adressTxt).extract("<span class=\"locality\">",
		// "</span>"));
		//
		//Old code:
		//parser.moveTo("<tr class=\"locality\">");
		//parser.moveTo("<a href=");
		//String ort = removeDirt(parser.extract(">", "</a>").replace("&nbsp;", "").trim());
		//
		//New code:
		String ort = "";
		if (moveTo("<tr class=\"locality\">")) {
			moveTo("<a href=");
			ort = removeDirt(parser.extract(">", "</a>").replace("&nbsp;", "").trim());
		}

		
		// If zusatz is empty, then we copy the content of poBox into zusatz.
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
		
		return new KontaktEntry(vorname, nachname, zusatz, streetAddress, plzCode, ort, tel, fax,
			email, true);
	}
}
