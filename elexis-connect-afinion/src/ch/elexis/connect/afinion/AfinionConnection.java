package ch.elexis.connect.afinion;

import gnu.io.SerialPortEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jonelo.jacksum.algorithm.Crc16;

import ch.elexis.rs232.Connection;
import ch.rgw.tools.ExHandler;

public class AfinionConnection extends Connection {
	private static final int NUL = 0x00;
	private static final int STX = 0x02;
	private static final int ETX = 0x03;
	private static final int ACK = 0x06;
	private static final int DLE = 0x10;
	private static final int NAK = 0x15;
	private static final int ETB = 0x17;

	public AfinionConnection(String portName, String port, String settings,
			ComPortListener l) {
		super(portName, port, settings, l);
	}
	
	/** 
	 * Crc calculation
	 * @param array
	 * @return
	 */
	private static long getCrc(byte[] array) {
		char crc = 0xFFFF;
		for (byte b: array) {
			char value = (char)b;
			value ^= crc & 0xFF;
			value ^= (value << 4) & 0xFF;
			crc = (char)((crc >>> 8) ^ (value << 8) ^ (value << 3) ^ (value >>> 4));
		}
		return crc;
	}
	
	private void sendACK(String packetNr) {
		System.out.print("Return: ");
		
		StringBuffer strBuf = new StringBuffer();
		System.out.print("<DLE>");
		strBuf.append(DLE);
		System.out.print("<STX>");
		strBuf.append(STX);
		System.out.print(packetNr);
		strBuf.append(packetNr);
		System.out.print("<ACK>");
		strBuf.append(ACK);
		System.out.print("<ETB>");
		strBuf.append(ETB);
	
		long crc = getCrc(strBuf.toString().getBytes());
		
		System.out.print(Long.toHexString(crc));
		strBuf.append(Long.toHexString(crc));
		System.out.print("<DLE>");
		strBuf.append(DLE);
		System.out.print("<ETX>");
		strBuf.append(ETX);

		System.out.println("Send: " + strBuf.toString());
		send(strBuf.toString());
	}
	
	private void sendNAK(String packetNr) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(DLE);
		strBuf.append(STX);
		strBuf.append(packetNr);
		strBuf.append(NAK);
		strBuf.append(ETB);
		
		long crc = getCrc(strBuf.toString().getBytes());
		
		strBuf.append(crc);
		strBuf.append(DLE);
		strBuf.append(ETX);

		send(strBuf.toString());
	}

	private String getText(int value) {
		if (value == NUL) {
			return "<NUL>";
		}
		if (value == STX) {
			return "<STX>";
		}
		if (value == ETX) {
			return "<ETX>";
		}
		if (value == ACK) {
			return "<ACK>";
		}
		if (value == DLE) {
			return "<DLE>";
		}
		if (value == NAK) {
			return "<NAK>";
		}
		if (value == ETB) {
			return "<ETB>";
		}
		
		return new Character((char)value).toString();
	}
	
	private boolean isControlChar(int value) {
		return (value == NUL) || (value == STX) || (value == ETX) || 
		(value == ACK) || (value == DLE) ||(value == NAK) || (value == ETB);
	}
	
	private void handleEvent(final int controldata, final InputStream inputStream) throws IOException {
		if (controldata == -1) {
			return;
		}
		int data;
		StringBuffer strBuf;
		
		System.out.println("Control=" + getText(controldata));
		switch (controldata) {
		case STX:
			data = inputStream.read();
			strBuf = new StringBuffer();
			while ((data != -1) && (!isControlChar(data))) {
				System.out.print(getText(data));
				strBuf.append(getText(data));
				data = inputStream.read();
			}
			System.out.println(strBuf);
			handleEvent(data, inputStream);
			if (strBuf.length() > 4) {
				String packetNr = strBuf.substring(0, 4);
				sendACK(packetNr);
			}
			break;
		case ETB:
			data = inputStream.read();
			strBuf = new StringBuffer();
			while ((data != -1) && (data != DLE)) {
				System.out.print(getText(data));
				strBuf.append(data);
				data = inputStream.read();
			}
			System.out.println();
			handleEvent(data, inputStream);
			break;
		default:
			data = inputStream.read();
			strBuf = new StringBuffer();
			while ((data != -1) && (!isControlChar(data))) {
				System.out.print(getText(data));
				strBuf.append(data);
				data = inputStream.read();
			}
			handleEvent(data, inputStream);
		}
	}
	
	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream,
			final SerialPortEvent e) throws IOException {

		System.out.println("AFINION INPUT START");
		final int code = inputStream.read();
		handleEvent(code, inputStream);
		System.out.println("AFINION INPUT END");

	}
}
