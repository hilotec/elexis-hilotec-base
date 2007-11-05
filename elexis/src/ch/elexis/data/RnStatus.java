/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: RnStatus.java 3311 2007-11-05 17:58:56Z rgw_ch $
 *******************************************************************************/


package ch.elexis.data;

public class RnStatus {
	public static final int UNBEKANNT=			 0;
	public static final int VERRECHNET=			 1;
	public static final int NICHT_VERRECHNET=	 2;
	public static final int LAUFEND=			 3;
	public static final int OFFEN=				 4;
	public static final int OFFEN_UND_GEDRUCKT=	 5;
	public static final int MAHNUNG_1=			 6;
	public static final int MAHNUNG_1_GEDRUCKT=	 7;
	public static final int MAHNUNG_2=		 	 8;
	public static final int MAHNUNG_2_GEDRUCKT=	 9;
	public static final int MAHNUNG_3=			10;
	public static final int MAHNUNG_3_GEDRUCKT=	11;
	public static final int IN_BETREIBUNG=		12;
	public static final int TEILVERLUST=		13;
	public static final int TOTALVERLUST=		14;
	public static final int TEILZAHLUNG=		15;
	public static final int BEZAHLT=			16;
	public static final int ZUVIEL_BEZAHLT=		17;
	public static final int STORNIERT=			18;
	public static final int VON_HEUTE=			19;
	public static final int NICHT_VON_HEUTE=	20;
	public static final int NICHT_VON_IHNEN=	21;
	public static final int FEHLERHAFT=			22;
	public static final int ZU_DRUCKEN=			23;
	public static final int AUSSTEHEND=			24;
	
    public static final String[] Text={"Unbekannt","Verrechnet","Nicht verrechnen",
        "laufend","Rn. offen","Offen und gedruckt","Zahlungserinnerung",
        "Zahlungserinnerung gedruckt","2. Mahnung","2. Mahnung gedruckt",
        "3. Mahnung","3. Mahnung gedruckt","In Betreibung","Teilverlust",
        "Totalverlust","Teilw. bezahlt","Bezahlt","Zuviel bezahlt","Storniert",
        "von heute","nicht von heute","nicht von Ihnen","fehlerhaft","zu Drucken",
        "ausstehend"};
    
    public static enum REJECTCODE{
    	NO_DIAG,NO_MANDATOR,NO_CASE,NO_DEBITOR,NO_GUARANTOR,VALIDATION_ERROR,REJECTED_BY_PEER,SUM_MISMATCH,INTERNAL_ERROR;
    };
   
    public static final String[] RejectStrings={"Keine Diagnose",
    	"Kein Rechnungsempfänger", "Kein Garant", "Validierungsfehler",
    	"Vom Empfänger zurückgewiesen"};

}
