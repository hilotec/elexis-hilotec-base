/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    R. Zweifel - SMTP-Authentifizierung
 * 
 *  $Id: PreferenceConstants.java 5952 2010-01-22 17:17:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.mail;

public class PreferenceConstants {
	public static final String MAIL_MODE="ch_elexis_mail/pop_or_imap";
	public static final String MAIL_SERVER="ch_elexis_mail/pop-imap-server";
	public static final String MAIL_SMTP="ch_elexis_mail/smtp-server";
	public static final String MAIL_SMTPPORT="ch_elexis_mail/smtp-serverport";
	public static final String MAIL_SENDER="ch_elexis_mail/sender";
	public static final String MAIL_USER="ch_elexis_mail/user";
	public static final String MAIL_PASS="ch_elexis_mail/pass";
	public static final String MAIL_AUTH="ch_elexis_mail/auth";
	public static final String MAIL_QFA_ADDRESS="ch_elexis_mail/qfa-recipient";
	public final static String MAIL_SEND_QFA="ch_elexis_mail/send_qfa";
	public static final String MAIL_ELEXIS_LOG="ch_elexis_mail/elexislogfile";
	public static final String MAIL_ECLIPSE_LOG="ch_elexis_mail/eclipselogfile";
}
