package ch.elexis.connect.reflotron;

import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;

import ch.elexis.rs232.AbstractConnection;
import ch.elexis.util.Log;

public class ReflotronConnection extends AbstractConnection {
	private static final int STX = 0x02;
	private static final int ETX = 0x03;
	Log _elexislog = Log.get("ReflotronConnection");
	
	public ReflotronConnection(String portName, String port, String settings, ComPortListener l){
		super(portName, port, settings, l);
	}
	
	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream, final SerialPortEvent e)
		throws IOException{
		StringBuffer strBuf = new StringBuffer();
		
		int data = inputStream.read();
		// Start of TeXt
		while ((data != -1) && (data != STX)) {
			data = inputStream.read();
		}
		
		if (data != -1) {
			data = inputStream.read();
			while ((data != -1) && (data != ETX)) {
				strBuf.append((char) data);
				data = inputStream.read();
			}
		}
		_elexislog.log("buffer: " + strBuf.toString(), Log.DEBUGMSG);
		this.listener.gotData(this, strBuf.toString().getBytes());
	}
}
