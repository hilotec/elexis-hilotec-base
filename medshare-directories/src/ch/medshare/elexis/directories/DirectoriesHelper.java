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
 * $Id: DirectoriesHelper.java 3286 2007-10-26 04:37:22Z rgw_ch $
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
	
	private static String cleanupText(String text) {
		text = text.replace("</nobr>", "").replace("<nobr>", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		text = text.replace("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("<b class=\"searchWords\">", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("</b>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace((char) 160, ' '); // Spezielles Blank Zeichen wird ersetzt
		return text;
	}

	private static URL getURL(String name, String geo) throws MalformedURLException {
		name = name.replace(' ', '+');
		geo = geo.replace(' ', '+');
		String urlPattern = "http://www.directories.ch/weisseseiten/base.aspx?language={0}&searchtype=adr_simple&do=search&name={1}&geo={2}"; //$NON-NLS-1$
		return new URL(MessageFormat.format(urlPattern, new Object[] { Locale.getDefault().getLanguage(), name, geo }));
	}
	
	/**
     * Schreibt binäre Datei
     */
    public static void writeFile(String filenamePath, final String text)
            throws IOException {
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
			throws IOException, MalformedURLException {
		URL content = getURL(name, geo);
		InputStream input = content.openStream();

		StringBuffer sb = new StringBuffer();
		int count = 0;		
		char[] c = new char[10000];
		InputStreamReader isr = new InputStreamReader(input);	
		try {
			while ((count = isr.read(c)) > 0){
				sb.append(c, 0, count);
			}
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return cleanupText(sb.toString());
	}
}
