package ch.elexis.rs232;

import gnu.io.SerialPortEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.rgw.tools.ExHandler;

public class LogConnection extends AbstractConnection {
	final FileOutputStream fos;

	public LogConnection(String portName, String port, String settings,
			ComPortListener l, String logFilenamePath) throws IOException {
		super(portName, port, settings, l);
		File logFile = new File(logFilenamePath);
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		fos = new FileOutputStream(logFile, true);
	}

	public void serialEvent(final int state, final InputStream inputStream,
			final SerialPortEvent e) throws IOException {
		int data;
		try {
			String dateStr = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
			fos.write(("-- START " + dateStr + "\r\n").getBytes());
			while (((data = inputStream.read()) != -1)) {
				System.out.println(data);
				fos.write(data);
			}
			fos.write("\r\n-- END\r\n".getBytes());
		} catch (Exception ex) {
			try {
				fos.write(("\r\n-- ERROR: " + ex.getMessage() + "\r\n").getBytes());
			} catch (IOException ioe) {
				// Do nothing
			}
			ExHandler.handle(ex);
		}
		fos.flush();
	}

	@Override
	public void close() {
		try {
			if (this.fos != null) {
				this.fos.close();
			}
		} catch (IOException ex) {
			ExHandler.handle(ex);
		}
		super.close();
	}
}
