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
 * $Id: Connection.java 3206 2007-09-25 19:38:54Z rgw_ch $
 *******************************************************************************/

package ch.elexis.rs232;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.CommPortOwnershipListener;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class Connection implements SerialPortEventListener{
	 private CommPortIdentifier portId;
	 private SerialPort sPort;
	 private boolean bOpen;
	 private OutputStream os;
	 private InputStream is;
	 
	 /**
	   Attempts to open a serial connection and streams using the parameters
	   in the SerialParameters object. If it is unsuccesfull at any step it
	   returns the port to a closed state, throws a 
	   <code>SerialConnectionException</code>, and returns.

	   Gives a timeout of 30 seconds on the portOpen to allow other applications
	   to reliquish the port if have it open and no longer need it.
	   */
	   public void openConnection(final SerialParameters parameters) throws SerialConnectionException {

		// Obtain a CommPortIdentifier object for the port you want to open.
			try {
			    portId = 
				 CommPortIdentifier.getPortIdentifier(parameters.getPortName());
			} catch (NoSuchPortException e) {
			    throw new SerialConnectionException(e.getMessage());
			}
	
			// Open the port represented by the CommPortIdentifier object. Give
			// the open call a relatively long timeout of 30 seconds to allow
			// a different application to reliquish the port if the user 
			// wants to.
			try {
			    sPort = (SerialPort)portId.open("SerialDemo", 30000);
			} catch (PortInUseException e) {
			    throw new SerialConnectionException(e.getMessage());
			}
	
			// Set the parameters of the connection. If they won't set, close the
			// port before throwing an exception.
			try {
			    setConnectionParameters(parameters);
			} catch (SerialConnectionException e) {	
			    sPort.close();
			    throw e;
			}
	
			// Open the input and output streams for the connection. If they won't
			// open, close the port before throwing an exception.
			try {
			    os = sPort.getOutputStream();
			    is = sPort.getInputStream();
			} catch (IOException e) {
			    sPort.close();
			    throw new SerialConnectionException("Error opening i/o streams");
			}
	
		
			// Add this object as an event listener for the serial port.
			try {
			    sPort.addEventListener(this);
			} catch (TooManyListenersException e) {
			    sPort.close();
			    throw new SerialConnectionException("too many listeners added");
			}
	
			// Set notifyOnDataAvailable to true to allow event driven input.
			sPort.notifyOnDataAvailable(true);
	
			// Set notifyOnBreakInterrup to allow event driven break handling.
			sPort.notifyOnBreakInterrupt(true);
	
			// Set receive timeout to allow breaking out of polling loop during
			// input handling.
			try {
			    sPort.enableReceiveTimeout(30);
			} catch (UnsupportedCommOperationException e) {
			}
	
			
			bOpen = true;
	    }
	   /**
	    Sets the connection parameters to the setting in the parameters object.
	    If set fails return the parameters object to origional settings and
	    throw exception.
	    */
	    public void setConnectionParameters(final SerialParameters parameters) throws SerialConnectionException {

		// Save state of parameters before trying a set.
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity   = sPort.getParity();
		int oldFlowControl = sPort.getFlowControlMode();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
		    sPort.setSerialPortParams(parameters.getBaudRate(),
					      parameters.getDatabits(),
					      parameters.getStopbits(),
					      parameters.getParity());
		} catch (UnsupportedCommOperationException e) {
		    parameters.setBaudRate(oldBaudRate);
		    parameters.setDatabits(oldDatabits);
		    parameters.setStopbits(oldStopbits);
		    parameters.setParity(oldParity);
		    throw new SerialConnectionException("Unsupported parameter");
		}

		// Set flow control.
		try {
		    sPort.setFlowControlMode(parameters.getFlowControlIn() 
				           | parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
		    throw new SerialConnectionException("Unsupported flow control");
		}
	    }
	
	  /**
    Handles SerialPortEvents. The two types of SerialPortEvents that this
    program is registered to listen for are DATA_AVAILABLE and BI. During 
    DATA_AVAILABLE the port buffer is read until it is drained, when no more
    data is availble and 30ms has passed the method returns. When a BI
    event occurs the words BREAK RECEIVED are written to the messageAreaIn.
    */

    public void serialEvent(final SerialPortEvent e) {
 	// Create a StringBuffer and int to receive input data.
	StringBuffer inputBuffer = new StringBuffer();
	int newData = 0;

	// Determine type of event.
	switch (e.getEventType()) {

	    // Read data until -1 is returned. If \r is received substitute
	    // \n for correct newline handling.
	    case SerialPortEvent.DATA_AVAILABLE:
		    while (newData != -1) {
		    	try {
		    	    newData = is.read();
			    if (newData == -1) {
				break;
			    }
			    if ('\r' == (char)newData) {
			   	inputBuffer.append('\n');
			    } else {
			    	inputBuffer.append((char)newData);
			    }
		    	} catch (IOException ex) {
		    	    System.err.println(ex);
		    	    return;
		      	}
   		    }

		// Append received data to messageAreaIn.
		//messageAreaIn.append(new String(inputBuffer));
		break;

	    // If break event append BREAK RECEIVED message.
	    case SerialPortEvent.BI:
		//messageAreaIn.append("\n--- BREAK RECEIVED ---\n");
	}

    }   

    /**
    Reports the open status of the port.
    @return true if port is open, false if port is closed.
    */
    public boolean isOpen() {
	return bOpen;
    }
    /**
    Send a one second break signal.
    */
    public void sendBreak() {
	sPort.sendBreak(1000);
    }
}
