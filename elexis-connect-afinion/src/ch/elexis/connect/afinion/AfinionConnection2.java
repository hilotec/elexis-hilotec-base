package ch.elexis.connect.afinion;

import gnu.io.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ch.elexis.rs232.AbstractConnection;

public class AfinionConnection2 extends AbstractConnection {
	private static final int NUL = 0x00;
	private static final int STX = 0x02;
	private static final int ETX = 0x03;
	private static final int ACK = 0x06;
	private static final int DLE = 0x10;
	private static final int NAK = 0x15;
	private static final int ETB = 0x17;
	
	private static final long RESEND_IN_MS = 30000; // 30 Sekunden
	
	private boolean shouldMessageAcknowledge = false;
	
	
	// Patientenrequest senden
	public static final int SEND_PAT_REQUEST = 2;
	// Patient Record Request gesendet. Wartet auf Record Ack Meldung
	public static final int PAT_REQUEST_SENDED = 3;
	// Patient Record Request Acknowledge erhalten. Nun können Daten gelesen werden.
	public static final int PAT_REQUEST_ACK = 4;
	
	private String awaitPacketNr;
	
	private static int pc_packet_nr = 21;
	
	private long last_time_ms = 0;
	
	private Calendar currentCal = new GregorianCalendar();
	
	// Wird für Fehlerhandling verwendet. Alles wird in console geloggt.
	private static final boolean debug = false;
	
	public AfinionConnection2(String portName, String port, String settings, ComPortListener l){
		super(portName, port, settings, l);
		setState(SEND_PAT_REQUEST);
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
	
	/**
	 * Retourniert Byte-Array als Textausgabe.
	 */
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
	 * Textausgabe für debugging
	 * @param value
	 * @return
	 */
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
	
	/**
	 * Patienteanfrage wird gesendet
	 * @return
	 */
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
	
	/**
	 * Liest Stream bis zum nächsten <DEL><ETX>
	 */
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
	
	/**
	 * Liest Datenstream bis <DLE><ETX> und sendet anschliessend das Ack
	 * @param inputStream
	 * @throws IOException
	 */
	private void readToEndAndACK(final String packetNr, final InputStream inputStream) throws IOException{
		readToEnd(inputStream);
		debugln(""); //$NON-NLS-1$
		if (packetNr != null) {
			sendPacketACK(packetNr);
		}
	}
	
	/**
	 * Verarbeitet Patientendaten
	 */
	private void handlePatientRecord(final String packetNr, final InputStream inputStream) throws IOException{
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
			sendPacketNAK(packetNr);
		} else {
			sendPacketACK(packetNr);
			sendMessageACK();
			listener.gotData(this, baos.toByteArray());
		}
	}
	
	private void dataAvailable(final InputStream inputStream) throws IOException{
		
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
				
				// ACK/ NAK
				data = inputStream.read();
				if (data == NAK) {
					// Do nothing
				} else if (data == ACK) {
					if (packetNr.equals(awaitPacketNr)) {
						// Request wurde bestaetigt
						setState(PAT_REQUEST_ACK);
					}
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
							handlePatientRecord(packetNr, inputStream);
						}
					} else if (headerStr.indexOf("0024:record.control") != -1) { //$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
					} else if (headerStr.indexOf("cmdack") != -1) {//$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
					} else if (headerStr.indexOf("cmderr") != -1) {//$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
						setState(SEND_PAT_REQUEST);
					} else if (headerStr.indexOf("cmdcmpl") != -1) {//$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
						setState(SEND_PAT_REQUEST);
					} else if (headerStr.indexOf("debugmsg") != -1) {//$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
					} else if (headerStr.indexOf("FFFF:IC") != -1) {//$NON-NLS-1$
						readToEndAndACK(packetNr, inputStream);
					}
				}
			} else {
			   // Sollte nicht vorkommen
		       readToEnd(inputStream);
			}
		} else {
			// {Text} <LF>
			readToEnd(inputStream);
		}
		
		if (getState() == SEND_PAT_REQUEST) {
			awaitPacketNr = sendPatRecordRequest();
			setState(PAT_REQUEST_SENDED);
		}
		
		// Überprüft Status. Nach x Sekunden wird Request nochmals gesendet
		if (getState() == PAT_REQUEST_SENDED) {
			// Resend nach 30 sekunden
			long time_ms = new GregorianCalendar().getTimeInMillis();
			if (time_ms - last_time_ms > RESEND_IN_MS) {
				setState(SEND_PAT_REQUEST);
				last_time_ms = new GregorianCalendar().getTimeInMillis();
			}
		}
	}
	
	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream, final SerialPortEvent e)
		throws IOException{
		
		switch(e.getEventType()) {
        case SerialPortEvent.BI:
        case SerialPortEvent.OE:
        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.CD:
        case SerialPortEvent.CTS:
        case SerialPortEvent.DSR:
        case SerialPortEvent.RI:
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
        case SerialPortEvent.DATA_AVAILABLE:
        	dataAvailable(inputStream);
            break;
        }

	}
	
	@Override
	public void breakInterrupt(final int state){
		setState(SEND_PAT_REQUEST);
		super.breakInterrupt(state);
	}
	
	@Override
	public String connect(){
		setState(SEND_PAT_REQUEST);
		return super.connect();
	}
}
