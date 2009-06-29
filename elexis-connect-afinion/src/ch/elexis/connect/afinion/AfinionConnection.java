package ch.elexis.connect.afinion;

import gnu.io.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ch.elexis.rs232.AbstractConnection;

public class AfinionConnection extends AbstractConnection {
	private static final int NUL = 0x00;
	private static final int STX = 0x02;
	private static final int ETX = 0x03;
	private static final int ACK = 0x06;
	private static final int DLE = 0x10;
	private static final int NAK = 0x15;
	private static final int ETB = 0x17;
	
	private static final long WAIT_IN_MS = 1000; // 1 Sekunden
	private static final long RESEND_IN_MS = 30000; // 30 Sekunden
	
	private boolean shouldMessageAcknowledge = false;
	
	// Abfrage noch nicht gestartet
	public static final int INIT = 0;
	// 1 Minute warten
	public static final int WAITING = 1;
	// 1 Minute Wartezeit vorbei
	public static final int SEND_REQUEST = 2;
	// Patient Record Request gesendet. Wartet auf Record Meldung
	public static final int PAT_REQUEST_SENDED = 4;
	// Patient Record Request acknowledge
	public static final int PAT_REQUEST_ACK = 5;
	// Records werden gelesen
	public static final int RECORDS_READING = 6;
	// Request beendet
	public static final int REQUEST_FINISHED = 7;
	
	private String awaitPacketNr;
	private String ackPacketNr = null;
	
	private static int pc_packet_nr = 21;
	
	private long last_time_ms = 0;
	
	private Calendar currentCal = new GregorianCalendar();
	
	// Wird für Fehlerhandling verwendet. Alles wird in console geloggt.
	private static final boolean debug = true;
	
	public AfinionConnection(String portName, String port, String settings, ComPortListener l){
		super(portName, port, settings, l);
		setState(INIT);
	}
	
	public void setCurrentDate(Calendar cal){
		this.currentCal = cal;
	}
	
	/**
	 * Wenn variable debug = true, dann werden alle bytes in die console geloggt.
	 * 
	 * @param text
	 */
	private void debug(String text){
		if (debug) {
			System.out.print(text);
		}
	}
	
	/**
	 * Wenn variable debug = true, dann werden alle bytes in die console geloggt.
	 * 
	 * @param text
	 */
	private void debugln(String text){
		if (debug) {
			System.out.println(text);
		}
	}
	
	/**
	 * Crc calculation
	 * 
	 * @param array
	 * @return
	 */
	private static long getCrc(byte[] array){
		char crc = 0xFFFF;
		for (byte b : array) {
			char value = (char) b;
			value ^= crc & 0xFF;
			value ^= (value << 4) & 0xFF;
			crc = (char) ((crc >>> 8) ^ (value << 8) ^ (value << 3) ^ (value >>> 4));
		}
		return crc;
	}
	
	private String getByteStr(byte[] bytes){
		StringBuffer strBuf = new StringBuffer();
		int counter = 1;
		for (byte b : bytes) {
			if (strBuf.length() > 0) {
				strBuf.append(", "); //$NON-NLS-1$
				if (counter > 16) {
					strBuf.append("\n"); //$NON-NLS-1$
					counter = 1;
				}
			}
			String byteStr = Long.toHexString((long) b);
			while (byteStr.length() < 2) {
				byteStr = "0" + byteStr; //$NON-NLS-1$
			}
			strBuf.append("0x" + byteStr); //$NON-NLS-1$
			
			counter++;
		}
		return strBuf.toString();
	}
	
	/**
	 * Liest nächste Elexis-interne PacketNr
	 * 
	 * @param strBuf
	 * @param date
	 */
	private String nextPacketNr(){
		String packetNrStr = new Integer(pc_packet_nr).toString();
		pc_packet_nr++;
		while (packetNrStr.length() < 4) {
			packetNrStr = "0" + packetNrStr; //$NON-NLS-1$
		}
		return packetNrStr;
	}
	
	/**
	 * Fuegt Datum als String yyyyMMdd HH:mm:ss dazu (ohne Timezone-Umwandlung)
	 */
	private void addDate(StringBuffer strBuf){
		int day = this.currentCal.get(Calendar.DATE);
		int month = this.currentCal.get(Calendar.MONTH) + 1;
		int year = this.currentCal.get(Calendar.YEAR);
		int hour = this.currentCal.get(Calendar.HOUR_OF_DAY);
		int minutes = this.currentCal.get(Calendar.MINUTE);
		int seconds = this.currentCal.get(Calendar.SECOND);
		
		String dayStr = (day < 10 ? "0" : "") + Integer.valueOf(day).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		String monthStr = (month < 10 ? "0" : "") + Integer.valueOf(month).toString();
		String yearStr = Integer.valueOf(year).toString();
		String hourStr = (hour < 10 ? "0" : "") + Integer.valueOf(hour).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		String minuteStr = (minutes < 10 ? "0" : "") + Integer.valueOf(minutes).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		String secondStr = (seconds < 10 ? "0" : "") + Integer.valueOf(seconds).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		
		String dateStr = yearStr + monthStr + dayStr + " " + hourStr + ":" + minuteStr + ":" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		+ secondStr;
		strBuf.append(dateStr);
	}
	
	private void addContentStart(ByteArrayOutputStream os){
		debug("<DLE>"); //$NON-NLS-1$
		os.write(DLE);
		debug("<STX>"); //$NON-NLS-1$
		os.write(STX);
	}
	
	private void addContentEnd(ByteArrayOutputStream os){
		debug("<DLE>"); //$NON-NLS-1$
		os.write(DLE);
		debug("<ETB>"); //$NON-NLS-1$
		os.write(ETB);
	}
	
	private void addEnding(ByteArrayOutputStream os) throws IOException{
		long crc = getCrc(os.toByteArray());
		
		String crcStr = Long.toHexString(crc).toUpperCase();
		while (crcStr.length() < 4) {
			crcStr = "0" + crcStr; //$NON-NLS-1$
		}
		debug(crcStr);
		os.write(crcStr.getBytes());
		debug("<DLE>"); //$NON-NLS-1$
		os.write(DLE);
		debug("<ETX>"); //$NON-NLS-1$
		os.write(ETX);
		debugln(""); //$NON-NLS-1$
	}
	
	private void sendPacketACK(String packetNr){
		debug("-->"); //$NON-NLS-1$
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(packetNr);
			os.write(packetNr.getBytes());
			debug("<ACK>"); //$NON-NLS-1$
			os.write(ACK);
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray())); //$NON-NLS-1$
			if (send(os.toByteArray())) {
				debugln("OK"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessageACK(){
		debug("-->"); //$NON-NLS-1$
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			String packetNr = nextPacketNr();
			debug(packetNr);
			os.write(packetNr.getBytes());
			String cmdack = "0025:cmdack@"; //$NON-NLS-1$
			debug(cmdack);
			os.write(cmdack.getBytes());
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray())); //$NON-NLS-1$
			if (send(os.toByteArray())) {
				debugln("OK"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacketNAK(String packetNr){
		debug("-->"); //$NON-NLS-1$
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(packetNr);
			os.write(packetNr.getBytes());
			debug("<NAK>"); //$NON-NLS-1$
			os.write(NAK);
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray())); //$NON-NLS-1$
			if (send(os.toByteArray())) {
				debugln("OK"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sendPatRecordRequest(){
		debug("-->"); //$NON-NLS-1$
		
		StringBuffer contentBuf = new StringBuffer();
		String packetNrStr = nextPacketNr();
		contentBuf.append(packetNrStr);
		contentBuf.append("0025:record,patient@"); //$NON-NLS-1$
		addDate(contentBuf);
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(contentBuf.toString());
			os.write(contentBuf.toString().getBytes());
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray())); //$NON-NLS-1$
			if (send(os.toByteArray())) {
				debugln("OK"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return packetNrStr;
	}
	
	private String getText(int value){
		if (value == NUL) {
			return "<NUL>"; //$NON-NLS-1$
		}
		if (value == STX) {
			return "<STX>"; //$NON-NLS-1$
		}
		if (value == ETX) {
			return "<ETX>"; //$NON-NLS-1$
		}
		if (value == ACK) {
			return "<ACK>"; //$NON-NLS-1$
		}
		if (value == DLE) {
			return "<DLE>"; //$NON-NLS-1$
		}
		if (value == NAK) {
			return "<NAK>"; //$NON-NLS-1$
		}
		if (value == ETB) {
			return "<ETB>"; //$NON-NLS-1$
		}
		
		return new Character((char) value).toString();
	}
	
	private void readToEnd(final InputStream inputStream) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		int data = inputStream.read();
		while (data != -1 && data != ETX) {
			while (data != -1 && data != DLE) {
				os.write(data);
				data = inputStream.read();
			}
			os.write(data);
			data = inputStream.read();
		}
		os.write(data);
		debugln(""); //$NON-NLS-1$
		debugln(getByteStr(os.toByteArray()));
	}
	
	private void checkState(){
		if (getState() == INIT) {
			setState(WAITING);
			last_time_ms = new GregorianCalendar().getTimeInMillis();
		} else if (getState() == WAITING) {
			// 1 Minute warten bis Request gesendet wird.
			long time_ms = new GregorianCalendar().getTimeInMillis();
			if (time_ms - last_time_ms > WAIT_IN_MS) {
				setState(SEND_REQUEST);
				last_time_ms = new GregorianCalendar().getTimeInMillis();
			}
		} else if (getState() == PAT_REQUEST_SENDED || getState() == PAT_REQUEST_ACK) {
			// Resend nach 10 sekunden
			long time_ms = new GregorianCalendar().getTimeInMillis();
			if (time_ms - last_time_ms > RESEND_IN_MS) {
				setState(SEND_REQUEST);
				last_time_ms = new GregorianCalendar().getTimeInMillis();
			}
		}
	}
	
	private void acknowledge(final InputStream inputStream) throws IOException{
		readToEnd(inputStream);
		debugln(""); //$NON-NLS-1$
		if (ackPacketNr != null) {
			sendPacketACK(ackPacketNr);
		}
	}
	
	private void handlePatientRecord(final InputStream inputStream) throws IOException{
		StringBuffer logBuffer = new StringBuffer();
		// nächste 2560 Bytes lesen
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// <DLE><ETB> suchen
		int data = inputStream.read();
		int pos = 0;
		while (data != -1 && data != ETB) {
			while (data != -1 && data != DLE) {
				if (debug) {
					//logBuffer.append(getText(data));
				}
				baos.write(data);
				data = inputStream.read();
				pos++;
			}
			if (data == DLE) { // <DLE><DLE> wird zweites DLE nicht beachtet
				data = inputStream.read();
			}
			if (debug) {
				logBuffer.append(getText(data));
			}
			baos.write(data);
			data = inputStream.read();
			pos++;
		}
		if (debug) {
			logBuffer.append(getText(data));
			debugln(logBuffer.toString());
		}
		
		readToEnd(inputStream);
		if (baos.size() < 2560) {
			sendPacketNAK(ackPacketNr);
		} else {
			sendPacketACK(ackPacketNr);
			sendMessageACK();
			listener.gotData(this, baos.toByteArray());
		}
	}
	
	private void handleEvent(final InputStream inputStream) throws IOException{
		
		checkState();
		
		if (getState() == SEND_REQUEST) {
			awaitPacketNr = sendPatRecordRequest();
			setState(PAT_REQUEST_SENDED);
		}
		
		int data = inputStream.read();
		if (data == DLE) {
			debug("<DLE>"); //$NON-NLS-1$
			data = inputStream.read();
			if (data == STX) {
				debug("<STX>"); //$NON-NLS-1$
				String packetNr = ""; //$NON-NLS-1$
				for (int i = 0; i < 4; i++) {
					data = inputStream.read();
					packetNr += (char) data;
				}
				debug(packetNr);
				if (!packetNr.equals(awaitPacketNr)) {
					ackPacketNr = packetNr;
				} else {
					setState(PAT_REQUEST_ACK);
				}
				// ACK/ NAK
				data = inputStream.read();
				if (data == NAK || data == ACK) {
					// Ok, nichts zu machen
				} else {
					StringBuffer logBuffer = new StringBuffer();
					// Content lesen
					StringBuffer header = new StringBuffer();
					while ((data != -1) && (data != '@')) {
						if (debug) {
							logBuffer.append(getText(data));
						}
						header.append((char) data);
						data = inputStream.read();
					}
					debug(logBuffer.toString());
					debug("@"); //$NON-NLS-1$
					String headerStr = header.toString();
					if (headerStr.indexOf("0025:record,patient") != -1) { //$NON-NLS-1$
						if (getState() == PAT_REQUEST_ACK) {
							setState(RECORDS_READING);
							handlePatientRecord(inputStream);
						}
					} else if (headerStr.indexOf("0024:record.control") != -1) { //$NON-NLS-1$
						acknowledge(inputStream);
					} else if (headerStr.indexOf("cmdack") != -1) {//$NON-NLS-1$
						acknowledge(inputStream);
					} else if (headerStr.indexOf("cmderr") != -1) {//$NON-NLS-1$
						acknowledge(inputStream);
						setState(SEND_REQUEST);
					} else if (headerStr.indexOf("cmdcmpl") != -1) {//$NON-NLS-1$
						acknowledge(inputStream);
						setState(SEND_REQUEST);
					} else if (headerStr.indexOf("debugmsg") != -1) {//$NON-NLS-1$
						acknowledge(inputStream);
					} else if (headerStr.indexOf("FFFF:IC") != -1) {//$NON-NLS-1$
						acknowledge(inputStream);
					}
				}
			} else if (data == ETX) {
				debugln("<ETX>"); //$NON-NLS-1$
				if (ackPacketNr != null) {
					sendPacketACK(ackPacketNr);
					ackPacketNr = null;
				}
				if (shouldMessageAcknowledge) {
					sendMessageACK();
					shouldMessageAcknowledge = false;
				}
			} else if (data == ETB) {
				debugln("<ETB>"); //$NON-NLS-1$
			} else {
				if (debug) {
					//debug(getText(data));
				}
			}
		} else if (data == NUL) {
			// NUL ignorieren
			while ((data = inputStream.read()) == NUL) {}
		} else {
			if (debug) {
				//debug(getText(data));
			}
		}
	}
	
	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream, final SerialPortEvent e)
		throws IOException{
		
		try {
			handleEvent(inputStream);
		} catch (IOException ex) {
			if (ackPacketNr != null) {
				sendPacketNAK(ackPacketNr);
				ackPacketNr = null;
			}
			;
			throw ex;
		}
	}
	
	@Override
	public void breakInterrupt(final int state){
		setState(INIT);
		super.breakInterrupt(state);
	}
	
	@Override
	public String connect(){
		setState(INIT);
		return super.connect();
	}
}
