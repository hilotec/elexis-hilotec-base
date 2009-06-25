package ch.elexis.rs232;

import gnu.io.SerialPortEventListener;

public interface PortEventListener extends SerialPortEventListener {

	public static final String XON = "\013";
	public final static String XOFF = "\015";
	public final static String STX = "\002";
	public final static String ETX = "\003";
	public static final String NAK = "\025";
	public static final String CR = "\015";
	public static final String LF = "\012";
}
