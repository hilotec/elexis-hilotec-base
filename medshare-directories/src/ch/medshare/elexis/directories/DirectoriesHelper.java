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
 * $Id: DirectoriesHelper.java 4628 2008-10-23 07:57:50Z michael_imhof $
 *******************************************************************************/

package ch.medshare.elexis.directories;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

public class DirectoriesHelper {
	
	private static String cleanupText(String text){
		text = text.replace("</nobr>", "").replace("<nobr>", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		text = text.replace("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("<b class=\"searchWords\">", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("</b>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace((char) 160, ' '); // Spezielles Blank Zeichen wird
												// ersetzt
		return text;
	}
	
	private static String cleanupUmlaute(String text) {
		text = text.replace("&#xE4;", "ä");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xC4;", "Ä");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xF6;", "ö");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xD6;", "Ö");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xFC;", "ü");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xDC;", "Ü");//$NON-NLS-1$ //$NON-NLS-2$
		
		text = text.replace("&#xE8;", "è");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xE9;", "é");//$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("&#xEA;", "ê");//$NON-NLS-1$ //$NON-NLS-2$
		
		text = text.replace("&#xE0;", "à");//$NON-NLS-1$ //$NON-NLS-2$
		
		text = text.replace("&#xA0;", " ");//$NON-NLS-1$ //$NON-NLS-2$
		
		return text;
    }
	
	private static URL getURL(String name, String geo)
		throws MalformedURLException{
		name = name.replace(' ', '+');
		geo = geo.replace(' ', '+');
		
		String urlPattern = "http://tel.local.ch/{0}/q/?what={1}&where={2}"; //$NON-NLS-1$
		
		return new URL(MessageFormat.format(urlPattern, new Object[] {
			Locale.getDefault().getLanguage(), name, geo
		}));
	}
	
	/**
	 * Schreibt binäre Datei
	 */
	public static void writeFile(String filenamePath, final String text)
		throws IOException{
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(filenamePath);
			output.write(text.getBytes());
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}
	
	/**
	 * Liest Inhalt einer Web-Abfrage auf www.directories.ch/weisseseiten
	 */
	public static String readContent(final String name, final String geo)
		throws IOException, MalformedURLException{
		URL content = getURL(name, geo);
		InputStream input = content.openStream();
		
		StringBuffer sb = new StringBuffer();
		int count = 0;
		char[] c = new char[10000];
		InputStreamReader isr = new InputStreamReader(input);
		try {
			while ((count = isr.read(c)) > 0) {
				sb.append(c, 0, count);
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return cleanupUmlaute(cleanupText(sb.toString()));
	}
}
