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
package ch.elexis.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.elexis.util.OpenVPN;
import ch.elexis.tests.Preferences;

public class OpenVPNTest {
	static OpenVPN ovpn;
	static String srvName = ch.elexis.tests.Preferences.getOvpnServer();
	
	private boolean ping(String hostname){
		boolean result = false;
		try {
			System.out.println("InetAddress "+InetAddress.getByName(hostname));
			System.out.println("ping  "+InetAddress.getByName(hostname).isReachable(3000));
			result = InetAddress.getByName(hostname).isReachable(3000);
		} catch (UnknownHostException e) {
			fail("NoSuchHost: " + hostname);
			return false;
		} catch (IOException e) {
			fail("Host " + hostname + " could not be pinged");
			return false;
		}
		return result;
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		ovpn = new OpenVPN();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception{}
	
	@Test
	public void testOpenConnection(){
		int step = 0;
		boolean res = false;
		System.out.println("testOpenConnection");

		try {		
		String testUser = ch.elexis.tests.Preferences.getElexisUsername(1);
		assertEquals("elexis-1", testUser);
		res = ovpn.openConnection(srvName, ch.elexis.tests.Preferences.getOpenVpnInstallDir(), 0);
		assert (res);
		step =10 ;
		assert (ping(srvName));
		System.out.println("testOpenConnection ping successfull to "+srvName);
		step =20 ;
		}
		finally {
			assert(res);
			assertEquals(step, 20);
		}
	}
	
	@Test
	public void testCloseConnection(){
		ovpn.closeConnection();
		assert (ping(srvName));
	}
	
}
