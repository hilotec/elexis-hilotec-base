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
 *  $Id: ESR.java 5242 2009-04-13 18:30:56Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import org.eclipse.swt.SWT;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

/**
 * Repräsentation eines ESR Einzahlungsscheins tn ist die Teilnehmernummer. id
 * kann null sein, dann ist es ein VESR, oder kann die subid des Bankkunden
 * sein, dann ist es ein BESR.
 * 
 * @author gerry
 * 
 */
public class ESR {
	public static final String ESR_NORMAL_FONT_NAME = "esr/normalfontname";
	public static final String ESR_NORMAL_FONT_SIZE = "esr/normalfontsize";
	public static final String ESR_OCR_FONT_NAME = "esr/ocrfontname";
	public static final String ESR_OCR_FONT_SIZE = "esr/ocrfontsize";
	public static final String ESR_OCR_FONT_WEIGHT = "esr/ocrfontweight";

	public static final String ESR_NORMAL_FONT_NAME_DEFAULT = "OCR-B";
	public static final int ESR_NORMAL_FONT_SIZE_DEFAULT = 9;
	public static final String ESR_OCR_FONT_NAME_DEFAULT = "OCR-B-10 BT";
	public static final int ESR_OCR_FONT_SIZE_DEFAULT = 12;
	public static final int ESR_OCR_FONT_WEIGHT_DEFAULT = SWT.MIN;

	public static final String ESR_PRINTER_CORRECTION_X = "esr/printer_correction_x";
	public static final String ESR_PRINTER_CORRECTION_Y = "esr/printer_correction_y";
	public static final String ESR_PRINTER_BASE_OFFSET_X = "esr/printer_base_x";
	public static final String ESR_PRINTER_BASE_OFFSET_Y = "esr/printer_base_y";
	public static final int ESR_PRINTER_CORRECTION_X_DEFAULT = 0;
	public static final int ESR_PRINTER_CORRECTION_Y_DEFAULT = 0;
	// base offset depends on the printable left/top margin of a specific
	// printer
	public static final int ESR_PRINTER_BASE_OFFSET_X_DEFAULT = 0;
	public static final int ESR_PRINTER_BASE_OFFSET_Y_DEFAULT = 0;

	public static final int ESR16 = 16;
	public static final int ESR27 = 27;
	private String tn;
	private String id;
	private String userdata;
	private int reflen;

	/**
	 * BESR mit besrdata erstellen.
	 * 
	 * @param ESR_tn
	 *            Teilnehmernummer im Format vv-xxx-P
	 * @param ESR_subid
	 *            Kundennummer oder null
	 * @param usr
	 *            individueller Identifikationscode des EZ-Scheins (z.B. aus
	 *            PatNr, und RnNummer aufgebaut)
	 * @param l
	 *            Länge der Referenznummer (nur 16 oder 27 zulässig)
	 */
	public ESR(String ESR_tn, String ESR_subid, String usr, int l) {
		tn = ESR_tn;
		id = ESR_subid == null ? "" : ESR_subid;
		reflen = l - 1;
		userdata = usr;
	}

	/**
	 * Codierzeile aufbauen
	 * 
	 * @param amount
	 *            Betrag in Rappen
	 * @param tcCode
	 *            Code des TrustCenters oder null: normale ESR-Zeile
	 * @return eine druckfertige Codierzeile
	 */
	public String createCodeline(String amount, String tcCode) {
		if (Integer.parseInt(amount) < 0) {
			amount = "0";
		}
		StringBuilder cl = new StringBuilder();
		if (tcCode == null) {
			tcCode = "01"; // ESR in CHF
		}
		// Betrag auf 10 Stellen erweitert
		String betrag = wrap(tcCode
				+ StringTool.pad(StringTool.LEFT, '0', amount, 10));
		cl.append(betrag);
		cl.append(">"); // Trennzeichen
		cl.append(makeRefNr(false)); // Referenznummer
		cl.append("+ "); // Trennzeichen
		cl.append(makeParticipantNumber(false)).append(">"); // Teilnehmernummer
		return cl.toString();
	}

	/**
	 * Zeile Referenznummer aufbauen
	 * 
	 * @param withSpaces
	 *            true: in Fünfergruppen aufteilen
	 * @return die gebrauchsfertige Referenznummer
	 */
	public String makeRefNr(boolean withSpaces) {
		StringBuilder ret = new StringBuilder();
		int il = id.length();
		int ul = userdata.length();
		int space = reflen - ul - il;
		if (space < 0) {
			userdata = userdata.substring(space * -1);
			SWTHelper.showError(Messages.ESR_esr_invalid,
					Messages.ESR_warning_esr_not_correct);
			ret.append(id).append(userdata);
		} else {
			ret.append(id).append(StringTool.filler("0", space)).append(
					userdata);
		}

		String refnr = wrap(ret.toString());
		if (withSpaces == false) {
			return refnr;
		}
		if (refnr.length() == 16) {
			return refnr.substring(0, 2) + " " + refnr.substring(3, 6) + " "
					+ refnr.substring(7);
		} else if (refnr.length() == 27) {
			String g1 = refnr.substring(0, 2);
			String g2 = refnr.substring(2, 7);
			String g3 = refnr.substring(7, 12);
			String g4 = refnr.substring(12, 17);
			String g5 = refnr.substring(17, 22);
			String g6 = refnr.substring(22);
			return g1 + " " + g2 + " " + g3 + " " + g4 + " " + g5 + " " + g6;
		} else {
			return "** ERROR **";
		}
	}

	/**
	 * Teilnehmernummer aufbauen
	 * 
	 * @param withSeparators
	 *            true: Bindestriche an geeigneten Stellen, wie im KOnstruktor
	 *            eingegeben
	 * @return die gebrauchsfertige Teilnehmernummer
	 */
	public String makeParticipantNumber(boolean withSeparators) {
		if (withSeparators == true) {
			return tn;
		}
		String[] ptn = tn.split("\\s*-\\s*");
		if (ptn.length != 3) {
			Hub.log.log(Messages.ESR_bad_user_defin + tn, Log.ERRORS);
			return "**FEHLER**";
		}
		return ptn[0] + StringTool.pad(StringTool.LEFT, '0', ptn[1], 6)
				+ ptn[2];
	}

	/**
	 * Eine beliebige Ziffernfolge mit der Modulo-10 Prüfsumme verpacken
	 * 
	 * @param number
	 *            darf nur aus Ziffern bestehen
	 * @return die Eingabefolge, ergänzt um ihre Prüfziffer
	 */
	public String wrap(String number) {
		int row = 0;
		String nr = number.replaceAll("[^0-9]", "");
		for (int i = 0; i < nr.length(); i++) {
			int col = Integer.parseInt(nr.substring(i, i + 1));
			row = checksum[row][col];
		}
		return number + Integer.toString(checksum[row][10]);

	}

	/** X-Offset der ESR-Codierzeile */
	public int getESRLineX() {
		int printerCorrectionX = Hub.localCfg.get(ESR_PRINTER_CORRECTION_X,
				ESR_PRINTER_CORRECTION_X_DEFAULT);

		return 59 + printerCorrectionX;
	}

	/** Y-Offset der ESR-Codierzeile */
	public int getESRLineY() {
		int printerCorrectionY = Hub.localCfg.get(ESR_PRINTER_CORRECTION_Y,
				ESR_PRINTER_CORRECTION_Y_DEFAULT);

		return 192 + 85 + printerCorrectionY;
	}

	/** Breite der ESR-Codierzeile */
	public int getESRLineWidth() {
		return 140;
	}

	/** Höhe der ESR-Codierzeile */
	public int getESRLineHeight() {
		return 4;
	}

	/**
	 * Druckt einen BESR auf einen Rechnungsvordruck, der im TextContainer
	 * bereits eingelesen ist. Der EInzahlungsschein wird als unterer Anhang des
	 * Vordrucks erwartet. Die Ränder des Vordrucks müssen rundherum auf 5mm
	 * definiert sein.
	 */
	public boolean printBESR(Kontakt bank, Kontakt schuldner,
			Kontakt empfaenger, String betragInRappen, TextContainer text) {
		// Eine Zeile des Post-Vorgabe ESR sind 4.23mm (1/6 Zoll)
		int yBase = 192; // Offset Einzahlungsschein 19.2cm (absolut vom
		// Papierrand)
		int xBase = 0; // Offset Einzahlunsschein 0mm (absolut vom Papierrand)
		int wFr = 30; // Breite des Franken-Felds
		int hFr = 5; // Höhe des FrankenFeld
		int wRp = 10; // Breite des Rappen-Felds
		int wRef = 81; // Breite des Ref-Nr-Felds
		int hRef = 10; // Höhe des Ref-Nr-Felds
		int xRef = 116; // x-Offset des Ref-Nr-Felds
		int yRef = 33; // y-Offset des Ref-Nr-Felds
		int xGiro = 60; // x-Offset des Giro-Abschnitts
		int hAdr = 10; // Höhe des Adressat-Felds
		int hBeg = 20; // Höhe des Begüpnstigten-Felds
		int xKonto = 22; // x-Offset der Kontonummer
		int wKonto = 30;
		int yKonto = 42; // y-Offset der Kontonummer
		int hKonto = 5; // Höhe der Kontonummer
		int yGarant1 = 60; // y-Offset des Absender-Adressblocks auf dem
		// Empfangsschein
		int yGarant2 = 50; // y-Offset des Absender-Adressblocks auf dem
		// Girozettel
		int manualYOffsetESR = Hub.localCfg.get(ESR_PRINTER_BASE_OFFSET_Y,
				ESR_PRINTER_BASE_OFFSET_Y_DEFAULT);
		int manualXOffsetESR = Hub.localCfg.get(ESR_PRINTER_BASE_OFFSET_X,
				ESR_PRINTER_BASE_OFFSET_X_DEFAULT);

		ITextPlugin p = text.getPlugin();
		String fontName = Hub.localCfg.get(ESR_NORMAL_FONT_NAME,
				ESR_NORMAL_FONT_NAME_DEFAULT);
		int fontSize = Hub.localCfg.get(ESR_NORMAL_FONT_SIZE,
				ESR_NORMAL_FONT_SIZE_DEFAULT);
		p.setFont(fontName, SWT.NORMAL, fontSize);

		// Korrekturen aus den Einstellungen anwenden.
		xBase += manualXOffsetESR;
		yBase += manualYOffsetESR;
		xGiro += manualXOffsetESR;
		xRef += manualXOffsetESR;
		
		if (bank != null && bank.isValid()) {
			// BESR

			// Bank
			StringBuilder badr = new StringBuilder();
			badr.append(bank.get("Bezeichnung1")).append(" ").append(
					bank.get("Bezeichnung2")).append("\n").append(
					bank.get("Plz")).append(" ").append(bank.get("Ort"));
			// auf Abschnitt
			p.insertTextAt(xBase, yBase + 8, xGiro - 5, hAdr - 2, badr
					.toString(), SWT.LEFT);
			// auf Giro-Zettel
			p.insertTextAt(xGiro, yBase + 8, xGiro - 5, hAdr - 2, badr
					.toString(), SWT.LEFT);

			// Empfaenger
			// auf Abschnitt
			p.insertTextAt(xBase, yBase + 20, xGiro - 5, hBeg - 1, empfaenger
					.getPostAnschrift(true), SWT.LEFT);
			// auf Giro-Zettel
			p.insertTextAt(xGiro, yBase + 20, xGiro - 5, hBeg - 1, empfaenger
					.getPostAnschrift(true), SWT.LEFT);
		} else {
			// VESR

			int height = hAdr + 2 + hBeg;
			p.insertTextAt(xBase, yBase + 8, xGiro - 5, height, empfaenger
					.getPostAnschrift(true), SWT.LEFT);
			p.insertTextAt(xGiro, yBase + 8, xGiro - 5, height, empfaenger
					.getPostAnschrift(true), SWT.LEFT);
		}

		// Geldbetrag in Boxen für Fr. und Rp. einsetzen
		int betrag = Integer.parseInt(betragInRappen);
		int fr = betrag / 100;
		int rp = betrag - (100 * fr);

		String Franken = Integer.toString(fr);
		String Rappen = StringTool.pad(StringTool.LEFT, '0', Integer
				.toString(rp), 2);
		p.insertTextAt(xBase + 5, yBase + 50, wFr, hFr - 3, Franken, SWT.RIGHT);
		

		p.insertTextAt(xBase + 40, yBase + 50, wRp, hFr - 3, Rappen, SWT.RIGHT);


		// Referenznummer
		p.insertTextAt(xRef, yBase + yRef, wRef, hRef, makeRefNr(true),
				SWT.CENTER);
		// Kontonummer
		String konto = makeParticipantNumber(true);
		p.insertTextAt(xBase + xKonto, yBase + yKonto, wKonto, hKonto, konto,
				SWT.LEFT);
		p.insertTextAt(xGiro + xKonto, yBase + yKonto, wKonto, hKonto, konto,
				SWT.LEFT);

		// remove leading zeros from reference number
		String refNr = makeRefNr(false).replaceFirst("^0+", "");
		String abs1 = refNr + "\n" + schuldner.getPostAnschrift(true);
		p.insertTextAt(xBase, yBase + yGarant1, xGiro, 25, abs1, SWT.LEFT);
		p.insertTextAt(xRef, yBase + yGarant2, xGiro, 25, schuldner
				.getPostAnschrift(true), SWT.LEFT);
		p.insertTextAt(xGiro + 5, yBase + 50, wFr, hFr - 3, Franken, SWT.RIGHT);
		p.insertTextAt(xGiro + 40, yBase + 50, wRp, hFr - 3, Rappen, SWT.RIGHT);
		printESRCodeLine(p, betragInRappen, null);

		return true;
	}

	/**
	 * ESR-Codierzeile auf das im TextContainer befindliche Blatt drucken
	 * 
	 * @param tcCode
	 *            Code des TrustCenters oder null. Bei null wird eine Post-ESR
	 *            erstellt, sonst eine TC-ESR
	 * */
	public void printESRCodeLine(ITextPlugin p, String betragInRappen,
			String tcCode) {
		String besr = createCodeline(betragInRappen, tcCode);

		String fontname = Hub.localCfg.get(ESR_OCR_FONT_NAME,
				ESR_OCR_FONT_NAME_DEFAULT);
		int fontscale = Hub.localCfg.get(ESR_OCR_FONT_SIZE,
				ESR_OCR_FONT_SIZE_DEFAULT);
		int fontweight = Hub.localCfg.get(ESR_OCR_FONT_WEIGHT,
				ESR_OCR_FONT_WEIGHT_DEFAULT);
		// String fontname=Hub.localCfg.get("esr/ocrfont", "OCR-B-10 BT");
		// int fontscale=Hub.localCfg.get("esr/fontscale", 12);
		p.setFont(fontname, fontweight, fontscale);
		// int y=(int)Math.round(getESRLineY());
		p.insertTextAt(getESRLineX(), getESRLineY(), getESRLineWidth(),
				getESRLineHeight(), besr, SWT.CENTER);
	}

	/** Array für den modulo-10-Prüfsummencode */
	private static final int[][] checksum = {
			{ 0, 9, 4, 6, 8, 2, 7, 1, 3, 5, 0 },
			{ 9, 4, 6, 8, 2, 7, 1, 3, 5, 0, 9 },
			{ 4, 6, 8, 2, 7, 1, 3, 5, 0, 9, 8 },
			{ 6, 8, 2, 7, 1, 3, 5, 0, 9, 4, 7 },
			{ 8, 2, 7, 1, 3, 5, 0, 9, 4, 6, 6 },
			{ 2, 7, 1, 3, 5, 0, 9, 4, 6, 8, 5 },
			{ 7, 1, 3, 5, 0, 9, 4, 6, 8, 2, 4 },
			{ 1, 3, 5, 0, 9, 4, 6, 8, 2, 7, 3 },
			{ 3, 5, 0, 9, 4, 6, 8, 2, 7, 1, 2 },
			{ 5, 0, 9, 4, 6, 8, 2, 7, 1, 3, 1 } };
}
