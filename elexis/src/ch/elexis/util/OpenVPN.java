/*******************************************************************************
 * Copyright (c) 2010, Niklaus Giger and Medelexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Niklaus Giger - initial implementation
 *    
 *  $Id: NumberInput.java 5321 2009-05-28 12:06:28Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util;

public class OpenVPN {

	/*
	 * Open the connection and keeps it open for a specified time
	 * The connection will be tested using a ping.
	 * 
	 * @hostName	string, e.g. 172.25.144 or ftp.example.com
	 * @openVpnBaseDir	root where OpenVPN is installed
	 * @timeout	 	how long the connection will be opened
	 * 		0 		will be interpreted as default of 300" or 5 min
	 * 		-1		keep it until the server will close it
	 * @return	OpenVPN connection is okay
	 */
	public boolean openConnection(String hostName, String openVpnBaseDir,  int timeout){		
		return false;
	}
	/*
	 * Closes the OpenVPN connection
	 */
	public void closeConnection()
	{
		
	}
}
