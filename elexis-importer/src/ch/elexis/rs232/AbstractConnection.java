package ch.elexis.rs232;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public abstract class AbstractConnection implements PortEventListener {

	private static final String simulate = null; // "c:/abx.txt";

	protected final StringBuilder sbFrame = new StringBuilder();
	protected final StringBuilder sbLine = new StringBuilder();
	protected int frameStart, frameEnd, overhang, checksumBytes;
	protected final ComPortListener listener;
	protected long endTime;
	protected int timeToWait;
	private int timeout;
	
	private static byte lineSeparator;

	private CommPortIdentifier portId;
	private SerialPort sPort;
	private boolean bOpen;
	private OutputStream os;
	private InputStream is;
	private final String myPort;
	private final String[] mySettings;
	private final String name;

	private int state;

	private Thread watchdogThread;
	public static final String XON = "\013";
	public final static String XOFF = "\015";
	public final static String STX = "\002";
	public final static String ETX = "\003";
	public static final String NAK = "\025";
	public static final String CR = "\015";
	public static final String LF = "\012";

	public interface ComPortListener {
		public void gotData(AbstractConnection conn, byte[] bytes);

		public void gotBreak(AbstractConnection conn);

		public void timeout();
	}

	public AbstractConnection(final String portName, final String port,
			final String settings, final ComPortListener l) {
		listener = l;
		myPort = port;
		mySettings = settings.split(",");
		name = portName;
	}

	public String connect() {
		SerialParameters sp = new SerialParameters();
		sp.setPortName(myPort);
		sp.setBaudRate(mySettings[0]);
		sp.setDatabits(mySettings[1]);
		sp.setParity(mySettings[2]);
		sp.setStopbits(mySettings[3]);
		try {
			if (simulate != null) {
				final AbstractConnection mine = this;
				new Thread(new Runnable() {

					public void run() {
						try {
							Thread.sleep(1000);
							final String in = FileTool.readFile(
									new File(simulate)).replaceAll("\\r\\n",
									"\r");
							listener.gotData(mine, in.getBytes());
						} catch (Exception ex) {

						}

					}
				}).start();
			} else {
				sbLine.setLength(0);
				openConnection(sp);
			}
			return null;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ex.getMessage();
		}

	}

	/**
	 * Attempts to open a serial connection and streams using the parameters in
	 * the SerialParameters object. If it is unsuccesfull at any step it returns
	 * the port to a closed state, throws a
	 * <code>SerialConnectionException</code>, and returns.
	 * 
	 * Gives a timeout of 30 seconds on the portOpen to allow other applications
	 * to reliquish the port if have it open and no longer need it.
	 */
	private void openConnection(final SerialParameters parameters)
			throws SerialConnectionException {

		// Obtain a CommPortIdentifier object for the port you want to open.
		try {
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException e) {
			throw new SerialConnectionException(e.getMessage());
		}

		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a relatively long timeout of 30 seconds to allow
		// a different application to reliquish the port if the user
		// wants to.
		try {
			sPort = (SerialPort) portId.open(name, 30000);
		} catch (PortInUseException e) {
			throw new SerialConnectionException("Com-Port wird verwendet!");
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
	 * Sets the connection parameters to the setting in the parameters object.
	 * If set fails return the parameters object to origional settings and throw
	 * exception.
	 */
	public void setConnectionParameters(final SerialParameters parameters)
			throws SerialConnectionException {

		// Save state of parameters before trying a set.
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(), parameters
					.getDatabits(), parameters.getStopbits(), parameters
					.getParity());
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
	 * Wait for a frame of the device to be sent. Ignores all input until a
	 * start byte is found. collects all bytes from that point until an end byte
	 * was received or the timeout happened.
	 * 
	 * @param start
	 *            character defining the start of a frame
	 * @param end
	 *            character singalling end of frame
	 * @param following
	 *            number of bytes after end to wait for (e.g. checksum)
	 * @param timeout
	 *            number of seconds to wait for a frame to complete before givng
	 *            up
	 */
	public synchronized void awaitFrame(final int start, final int end, final int following,
			final int timeout) {
		frameStart = start;
		frameEnd = end;
		overhang = following;
		this.timeout = timeout;
		endTime = System.currentTimeMillis() + (timeout * 1000);
		watchdogThread = new Thread(new Watchdog());
		timeToWait = timeout;
		checksumBytes = overhang;
		watchdogThread.start();
	}

	/**
	 * Read a line of input from the serial port. A line is defined as a series
	 * of bytes delimited by the given delimiter (e.g. \n).
	 * 
	 * @param delimiter
	 *            The delimiter to recognize the end of line
	 * @param timeout
	 *            number of seconds to wait at most before giving up
	 */
	public void readLine(byte delimiter, int timeout) {
		lineSeparator = delimiter;
		// sbLine.setLength(0);
		this.timeout = timeout;
		endTime = System.currentTimeMillis() + (timeout * 1000);
		watchdogThread = new Thread(new Watchdog());
		timeToWait = timeout;
		watchdogThread.start();
	}

	/**
	 * Handles SerialPortEvents. The two types of SerialPortEvents that this
	 * program is registered to listen for are DATA_AVAILABLE and BI. During
	 * DATA_AVAILABLE the port buffer is read until it is drained, when no more
	 * data is available and 30ms has passed the method returns. When a BI event
	 * occurs the words BREAK RECEIVED are written to the messageAreaIn.
	 */

	public void serialEvent(final SerialPortEvent e) {
		endTime = System.currentTimeMillis() + (timeout * 1000);
		if (e.getEventType() == SerialPortEvent.BI) {
			breakInterrupt(state);
		} else {
			try {
				serialEvent(this.state, is, e);
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
	}

	public abstract void serialEvent(final int state,
			final InputStream inputStream, final SerialPortEvent e) throws IOException;
	
	public void breakInterrupt(final int state) {
		this.watchdogThread.interrupt();
		listener.gotBreak(this);
	}

	public void close() {
		endTime = System.currentTimeMillis();
		if ((watchdogThread != null) && watchdogThread.isAlive()) {
			watchdogThread.interrupt();
		}
		// avoid rxtx-deadlock when called from an EventListener
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(5000);
					sPort.close();
					bOpen = false;
				} catch (Exception ex) {

				}
			}
		}).start();
	}

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return bOpen;
	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak() {
		sPort.sendBreak(1000);
	}

	public boolean send(final String data) {
		return send(data.getBytes());
	}
	
	public boolean send(final byte[] bytes) {
		try {
			os.write(bytes);
			os.flush();
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static String[] getComPorts() {
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier
				.getPortIdentifiers();
		ArrayList<String> p = new ArrayList<String>();
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = ports.nextElement();
			p.add(port.getName());
		}
		return p.toArray(new String[0]);
	}

	class Watchdog implements Runnable {
		public void run() {
			while (System.currentTimeMillis() < endTime) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					return;
				}
			}
			listener.timeout();
		}
	}
	
	protected void interruptWatchdog() {
		this.watchdogThread.interrupt();
	}

	public byte getLineSeparator() {
		return lineSeparator;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
}
