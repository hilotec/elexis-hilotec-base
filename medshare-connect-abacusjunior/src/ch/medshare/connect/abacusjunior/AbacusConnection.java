package ch.medshare.connect.abacusjunior;

import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;

import ch.elexis.rs232.Connection;

public class AbacusConnection extends Connection {
	public AbacusConnection(String portName, String port, String settings,
			ComPortListener l) {
		super(portName, port, settings, l);
	}

	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream,
			final SerialPortEvent e) throws IOException {
		int newData;
		switch (state) {
		case PASS_THRU:
			sbFrame.setLength(0);
			while ((newData = inputStream.read()) != -1) {
				sbFrame.append((char) newData);
			}
			listener.gotChunk(this, sbFrame.toString());
			break;
		case AWAIT_START:
			while ((newData = inputStream.read()) != -1) {
				if (newData == frameStart) {
					setState(AWAIT_END);
					sbFrame.append((char) frameStart);
					serialEvent(e);
				}
			}
			break;
		case AWAIT_END:
			while ((newData = inputStream.read()) != -1) {
				sbFrame.append((char) newData);
				if (newData == frameEnd) {
					setState(AWAIT_CHECKSUM);
					serialEvent(e);
				}
			}
			endTime = System.currentTimeMillis() + timeToWait;
			break;
		case AWAIT_CHECKSUM:
			while ((overhang > 0) && ((newData = inputStream.read()) != -1)) {
				sbFrame.append((char) newData);
				overhang -= 1;
			}
			if (overhang == 0) {
				listener.gotChunk(this, sbFrame.toString());
				sbFrame.setLength(0);
				interruptWatchdog();
				setState(AWAIT_START);
				overhang = checksumBytes;
				serialEvent(e);
			}
			break;
		case AWAIT_LINE:
			while ((newData = inputStream.read()) != -1) {
				if (newData == getLineSeparator()) {
					String res = sbLine.toString();
					sbLine.setLength(0);
					listener.gotChunk(this, res);
				} else {
					sbLine.append((char) newData);
				}
			}
		}

	}
}
