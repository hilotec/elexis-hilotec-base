package ch.elexis.connect.afinion;

import gnu.io.SerialPortEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	 * Berechnet CRC Pruefsumme
	 * @param content
	 * @return
	 */
	private static long getCRC(String content) {
		int crc = 0xFFFF;
		for (int i=0; i<content.length(); i++) {
			char ch = content.charAt(i);
			ch ^= crc | 0xFF;
			ch ^= ch << 4;
			crc = (crc >> 8) ^ (ch << 8) ^ (ch << 3) ^ (ch >> 4);
		}
		return crc;
	}
	
	private void sendACK(String packetNr) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(DLE);
		strBuf.append(STX);
		strBuf.append(packetNr);
		strBuf.append(ACK);
		strBuf.append(ETB);
		
		long crc = getCRC(strBuf.toString());
		
		strBuf.append(crc);
		strBuf.append(DLE);
		strBuf.append(ETX);

		send(strBuf.toString());
	}
	
	private void sendNAK(String packetNr) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(DLE);
		strBuf.append(STX);
		strBuf.append(packetNr);
		strBuf.append(NAK);
		strBuf.append(ETB);
		
		long crc = getCRC(strBuf.toString());
		
		strBuf.append(crc);
		strBuf.append(DLE);
		strBuf.append(ETX);

		send(strBuf.toString());
	}

	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream,
			final SerialPortEvent e) throws IOException {

		final int code = inputStream.read();
		switch (code) {
		case DLE:
			int data = inputStream.read();
			StringBuffer strBuf = new StringBuffer();
			if (data == STX) {
				while ((data != -1) && (data != ACK) && (data != NAK) && (data != DLE)) {
					data = inputStream.read();
					strBuf.append(data);
				}
				String packetNr = strBuf.substring(0, 4);
				sendACK(packetNr);
			}
			break;
		default:
			// Ignore
		}

	}
}
