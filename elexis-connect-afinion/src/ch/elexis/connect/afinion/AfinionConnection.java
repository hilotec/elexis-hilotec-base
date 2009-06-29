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
	public static final int WAIT_TIME_FINISHED = 2;
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
		setState(WAIT_TIME_FINISHED);
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
				strBuf.append(", ");
				if (counter > 16) {
					strBuf.append("\n");
					counter = 1;
				}
			}
			String byteStr = Long.toHexString((long) b);
			while (byteStr.length() < 2) {
				byteStr = "0" + byteStr;
			}
			strBuf.append("0x" + byteStr);
			
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
			packetNrStr = "0" + packetNrStr;
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
		
		String dayStr = (day < 10 ? "0" : "") + Integer.valueOf(day).toString();
		String monthStr = (month < 10 ? "0" : "") + Integer.valueOf(month).toString();
		String yearStr = Integer.valueOf(year).toString();
		String hourStr = (hour < 10 ? "0" : "") + Integer.valueOf(hour).toString();
		String minuteStr = (minutes < 10 ? "0" : "") + Integer.valueOf(minutes).toString();
		String secondStr = (seconds < 10 ? "0" : "") + Integer.valueOf(seconds).toString();
		
		String dateStr = yearStr + monthStr + dayStr + " " + hourStr + ":" + minuteStr + ":"
		+ secondStr;
		strBuf.append(dateStr);
	}
	
	private void addContentStart(ByteArrayOutputStream os){
		debug("<DLE>");
		os.write(DLE);
		debug("<STX>");
		os.write(STX);
	}
	
	private void addContentEnd(ByteArrayOutputStream os){
		debug("<DLE>");
		os.write(DLE);
		debug("<ETB>");
		os.write(ETB);
	}
	
	private void addEnding(ByteArrayOutputStream os) throws IOException{
		long crc = getCrc(os.toByteArray());
		
		String crcStr = Long.toHexString(crc).toUpperCase();
		while (crcStr.length() < 4) {
			crcStr = "0" + crcStr;
		}
		debug(crcStr);
		os.write(crcStr.getBytes());
		debug("<DLE>");
		os.write(DLE);
		debug("<ETX>");
		os.write(ETX);
		debugln("");
	}
	
	private void sendPacketACK(String packetNr){
		debug("-->");
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(packetNr);
			os.write(packetNr.getBytes());
			debug("<ACK>");
			os.write(ACK);
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				debugln("OK");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessageACK(){
		debug("-->");
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			String packetNr = nextPacketNr();
			debug(packetNr);
			os.write(packetNr.getBytes());
			String cmdack = "0025:cmdack@";
			debug(cmdack);
			os.write(cmdack.getBytes());
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				debugln("OK");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacketNAK(String packetNr){
		debug("-->");
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(packetNr);
			os.write(packetNr.getBytes());
			debug("<NAK>");
			os.write(NAK);
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				debugln("OK");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sendPatRecordRequest(){
		debug("-->");
		
		StringBuffer contentBuf = new StringBuffer();
		String packetNrStr = nextPacketNr();
		contentBuf.append(packetNrStr);
		contentBuf.append("0025:record,patient@");
		addDate(contentBuf);
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			debug(contentBuf.toString());
			os.write(contentBuf.toString().getBytes());
			addContentEnd(os);
			addEnding(os);
			
			debugln("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				debugln("OK");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return packetNrStr;
	}
	
	private String getText(int value){
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
		debugln("");
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
				setState(WAIT_TIME_FINISHED);
				last_time_ms = new GregorianCalendar().getTimeInMillis();
			}
		} else if (getState() == PAT_REQUEST_SENDED || getState() == PAT_REQUEST_ACK) {
			// Resend nach 10 sekunden
			long time_ms = new GregorianCalendar().getTimeInMillis();
			if (time_ms - last_time_ms > RESEND_IN_MS) {
				setState(WAIT_TIME_FINISHED);
				last_time_ms = new GregorianCalendar().getTimeInMillis();
			}
		}
	}
	
	private void acknowledge(final InputStream inputStream) throws IOException{
		readToEnd(inputStream);
		debugln("");
		sendPacketACK(ackPacketNr);
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
					logBuffer.append(getText(data));
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
		
		if (getState() == WAIT_TIME_FINISHED) {
			setState(PAT_REQUEST_SENDED);
			awaitPacketNr = sendPatRecordRequest();
		}
		
		int data = inputStream.read();
		if (data == DLE) {
			debug("<DLE>");
			data = inputStream.read();
			if (data == STX) {
				debug("<STX>");
				String packetNr = "";
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
					debug("@");
					String headerStr = header.toString();
					if (headerStr.indexOf("0025:record,patient") != -1) {
						if (getState() == PAT_REQUEST_ACK) {
							setState(RECORDS_READING);
							handlePatientRecord(inputStream);
						} else {
							setState(WAIT_TIME_FINISHED);
						}
					} else if (headerStr.indexOf("0024:record.control") != -1) {
						acknowledge(inputStream);
					} else if (headerStr.indexOf("cmdack") != -1) {
						acknowledge(inputStream);
					} else if (headerStr.indexOf("cmderr") != -1) {
						acknowledge(inputStream);
					} else if (headerStr.indexOf("cmdcmpl") != -1) {
						setState(REQUEST_FINISHED);
					} else if (headerStr.indexOf("debugmsg") != -1) {
						acknowledge(inputStream);
					} else if (headerStr.indexOf("FFFF:IC") != -1) {
						acknowledge(inputStream);
					}
				}
			} else if (data == ETX) {
				debugln("<ETX>");
				if (ackPacketNr != null) {
					sendPacketACK(ackPacketNr);
					ackPacketNr = null;
				}
				if (shouldMessageAcknowledge) {
					sendMessageACK();
					shouldMessageAcknowledge = false;
				}
			} else if (data == ETB) {
				debugln("<ETB>");
			} else {
				if (debug) {
					debug(getText(data));
				}
			}
		} else if (data == NUL) {
			// NUL ignorieren
			while ((data = inputStream.read()) == NUL) {}
		} else {
			if (debug) {
				debug(getText(data));
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
			setState(WAIT_TIME_FINISHED);
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
