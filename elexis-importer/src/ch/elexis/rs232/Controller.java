/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.rs232;

import ch.elexis.rs232.Connection.ComPortListener;
import ch.rgw.tools.ExHandler;

public class Controller implements ComPortListener {
	public static final String XON="\013";
	public final static String XOFF="\015";
	public final static String STX="\002";
	public final static String ETX="\003";
	
	Connection conn=new Connection(this);
	Receiver myReceiver;
	String myPort;
	String[] mySettings;
	public Controller(final String port, final String settings, final Receiver receiver){
		myPort=port;
		mySettings=settings.split(",");
		myReceiver=receiver;
	}
	
	public boolean connect(){
		SerialParameters sp=new SerialParameters();
		sp.setPortName(myPort);
		sp.setBaudRate(mySettings[0]);
		sp.setDatabits(mySettings[1]);
		sp.setParity(mySettings[2]);
		sp.setStopbits(mySettings[3]);
		try{
			conn.openConnection(sp);
			return true;
		}catch(Exception ex){
			ExHandler.handle(ex);
			return false;
		}
		
	}

	public boolean send(final String data){
		return conn.send(data);
	}
	public void disconnect(){
		conn.close();
	}

	public void gotBreak() {
		myReceiver.breakReceived(this);
	}

	public void gotChunk(final String chunk) {
		myReceiver.dataReceived(this, chunk);
	}

}
