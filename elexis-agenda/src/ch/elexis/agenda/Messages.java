/*******************************************************************************
* Copyright (c) 2005 IBM Corporation and others.
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM - initial API and implementation
*******************************************************************************/

package ch.elexis.agenda;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "ch.elexis.agenda.messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String TagesView_showToday;
	public static String TagesView_previousDay;
	public static String TagesView_selectDay;
	public static String TagesView_nextDay;
	public static String TagesView_printDay;
	public static String TagesView_14;
	public static String TagesView_lockPeriod;
	public static String TagesView_changeTermin;
	public static String TagesView_changeThisTermin;
	public static String TagesView_shortenTermin;
	public static String TagesView_enlargeTermin;
	public static String TagesView_newTermin;
	public static String TagesView_createNewTermin;
	public static String TagesView_bereich;
	public static String TagesView_selectBereich;
	public static String TagesView_praxis;

	public static String TerminDialog_startTime;
	public static String TerminDialog_duration;
	public static String TerminDialog_endTime;
	public static String TerminDialog_earlier;
	public static String TerminDialog_later;
	public static String TerminDialog_locked;
	public static String TerminDialog_serie;
	public static String TerminDialog_set;
	public static String TerminDialog_createTermin;
	public static String TerminDialog_change;
	public static String TerminDialog_changeTermin;
	public static String TerminDialog_delete;
	public static String TerminDialog_deleteTermin;
	public static String TerminDialog_find;
	public static String TerminDialog_findTermin;
	public static String TerminDialog_enterPersonalia;
	public static String TerminStatusDialog_terminState;
	public static String TerminStatusDialog_enterState;
	public static String TerminSuchenDialog_findTermin;
	public static String TerminSuchenDialog_enterfind;
	public static String TerminDialog_enterText;
	public static String TerminDialog_enterFreeText;
	public static String TerminDialog_remarks;
	public static String TerminDialog_typeandstate;
	public static String TerminDialog_reason;
	public static String TerminDialog_32;
	public static String TerminDialog_editTermins;
	public static String TerminDialog_termin;
	public static String TerminDialog_noPatSelected;
	public static String TerminDialog_40;
	
	public static String AgendaFarben_colorSettings;
	public static String AgendaDefinitionen_ranges;
	public static String AgendaDefinitionen_states;
	public static String AgendaImages_imagesForAgenda;
	public static String AgendaDefinitionen_defForAgenda;
	public static String AgendaDefinitionen_enterNames;
	public static String AgendaDefinitionen_enterTypes;
	public static String AgendaDefinitionen_enterTypeList;
	public static String AgendaDefinitionen_enterStates;
	public static String AgendaImages_change;
	public static String AgendaDefinitionen_5;
	public static String AgendaImages_cannotCopy;
	public static String AgendaDefinitionen_types;
	public static String AgendaDefinitionen_12;
	public static String AgendaDefinitionen_state;
	public static String AgendaImages_6;
	public static String AgendaImages_7;
	public static String AgendaImages_8;
	public static String Tageseinteilung_dayPlanning;
	public static String Tageseinteilung_praxis;
	public static String Tageseinteilung_enterPeriods;
	public static String Tageseinteilung_mo;
	public static String Tageseinteilung_tu;
	public static String Tageseinteilung_we;
	public static String Tageseinteilung_th;
	public static String Tageseinteilung_fr;
	public static String Tageseinteilung_sa;
	public static String Tageseinteilung_so;
	public static String Tageseinteilung_su;
	public static String Zeitvorgaben_timePrefs;
	public static String Zeitvorgaben_praxis;
	public static String Zeitvorgaben_terminTypes;
	public static String AgendaDefinitionen_shortCutsForBer;
	
	public static String Synchronizer_connctNotSuccessful;
	public static String AgendaActions_unblock;
	public static String AgendaActions_state;
	public static String AgendaActions_deleteDate;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}