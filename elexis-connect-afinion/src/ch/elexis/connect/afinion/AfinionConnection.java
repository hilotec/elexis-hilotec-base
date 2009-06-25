package ch.elexis.connect.afinion;

import gnu.io.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
	
	// Abfrage noch nicht gestartet
	public static final int INIT = 0;
	// Patient Record wird erwartet
	public static final int AWAIT_RECORDS = 1;
	// Message completed wird erwartet
	public static final int AWAIT_CMD_COMPLETED = 2;
	
	private String awaitPacketNr;
	private String ackPacketNr = null;
	
	private static int pc_packet_nr = 21;
	
	

	public AfinionConnection(String portName, String port, String settings,
			ComPortListener l) {
		super(portName, port, settings, l);
		setState(INIT);
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
	
	private String getByteStr(byte[] bytes) {
		StringBuffer strBuf = new StringBuffer();
		int counter = 1;
		for (byte b: bytes) {
			if (strBuf.length() > 0) {
				strBuf.append(", ");
				if (counter > 16) {
					strBuf.append("\n");
					counter = 1;
				}
			}
			String byteStr = Long.toHexString((long)b);
			while (byteStr.length() < 2) {
				byteStr = "0" + byteStr;
			}
			strBuf.append("0x" + byteStr);
			
			counter++;
		}
		return strBuf.toString();
	}
	
	private void addDate(StringBuffer strBuf, Date date) {
		SimpleDateFormat formatter= new SimpleDateFormat ("yyyyMMdd hh:mm:ss");
		String dateString = formatter.format(date);
		strBuf.append(dateString);
	}
	
	private void addContentStart(ByteArrayOutputStream os) {
		System.out.print("<DLE>");
		os.write(DLE);
		System.out.print("<STX>");
		os.write(STX);
	}
	
	private void addContentEnd(ByteArrayOutputStream os) {
		System.out.print("<DLE>");
		os.write(DLE);
		System.out.print("<ETB>");
		os.write(ETB);
	}
	
	private void addEnding(ByteArrayOutputStream os) throws IOException {
		long crc = getCrc(os.toByteArray());
		
		String crcStr = Long.toHexString(crc).toUpperCase();
		while (crcStr.length() < 4) {
			crcStr = "0" + crcStr;
		}
		System.out.print(crcStr);
		os.write(crcStr.getBytes());
		System.out.print("<DLE>");
		os.write(DLE);
		System.out.print("<ETX>");
		os.write(ETX);
		System.out.println();
	}
	
	private void sendACK(String packetNr) {
		System.out.print("-->");
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			System.out.print(packetNr);
			os.write(packetNr.getBytes());
			System.out.print("<ACK>");
			os.write(ACK);
			addContentEnd(os); 
			addEnding(os);
			
			System.out.println("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				System.out.println("OK");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendNAK(String packetNr) {
		System.out.print("-->");
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			System.out.print(packetNr);
			os.write(packetNr.getBytes());
			System.out.print("<NAK>");
			os.write(NAK);
			addContentEnd(os); 
			addEnding(os);
			
			System.out.println("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				System.out.println("OK");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sendPatRecordRequest(Date date) {
		System.out.print("-->");
		
		StringBuffer contentBuf = new StringBuffer();
		String packetNrStr = new Integer(pc_packet_nr).toString();
		pc_packet_nr++;
		while (packetNrStr.length() < 4) {
			packetNrStr = "0" + packetNrStr;
		}
		contentBuf.append(packetNrStr);
		contentBuf.append("0025:record,patient@");
		addDate(contentBuf, date);
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			addContentStart(os);
			System.out.print(contentBuf.toString());
			os.write(contentBuf.toString().getBytes());
			addContentEnd(os);
			addEnding(os);
	
			System.out.println("Send: " + getByteStr(os.toByteArray()));
			if (send(os.toByteArray())) {
				System.out.println("OK");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return packetNrStr;
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
		
		System.out.println("State=" + getState());
		if (getState() == INIT) {
			sendPatRecordRequest(new Date());
			setState(AWAIT_RECORDS);
		}
		
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
	
	private void handleEvent(final InputStream inputStream) throws IOException {

		if (getState() == INIT) {
			Calendar cal = new GregorianCalendar();
			//cal.add(Calendar.DATE, -5);
			//awaitPacketNr =sendPatRecordRequest(cal.getTime());
			//setState(AWAIT_RECORDS);
		}
		
		int data = inputStream.read();
		if (data == DLE) {
			System.out.print("<DLE>");
			data = inputStream.read();
			if (data == STX) {
				System.out.print("<STX>");
				String packetNr = "";
				for (int i=0; i<4; i++) {
					data = inputStream.read();
					packetNr += (char)data;
				}
				System.out.print(packetNr);
				if (packetNr.equals(awaitPacketNr)) {
					System.out.println("Judihui, mein Request wurde bestaetigt!");
				} else {
					ackPacketNr = packetNr;
				}
				// ACK/ NAK 
				data=inputStream.read();
				if (data==NAK || data==ACK) {
					// Ok, nichts zu machen
				} else {
					// Content lesen
					StringBuffer header = new StringBuffer();
					while ((data!= -1) && (data != '@')) {
						System.out.print(getText(data));
						header.append((char)data);
						data=inputStream.read();
					}
					System.out.print("@");
					String headerStr = header.toString();
					if (headerStr.indexOf("0025:record,patient") != -1) {
						 // nächste 2568 Bytes lesen
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						while ((data=inputStream.read()) != -1) {
							System.out.print(data);
							baos.write(data);
						}
						listener.gotData(this, baos.toByteArray());
					} else if (headerStr.indexOf("0024:record.control") != -1) {
						
					} else if (headerStr.indexOf("cmdack") != -1) {
						
					} else if (headerStr.indexOf("cmderr") != -10) {
						
					} else if (headerStr.indexOf("cmdcmpl") != -1) {
						
					} else if (headerStr.indexOf("debugmsg") != -1) {
						
					} else if (headerStr.indexOf("FFFF:IC") != -1) {
						
					}
				}
				handleEvent(inputStream);
			} else if (data == ETX) {
				System.out.println("<ETX>");
				if (ackPacketNr != null) {
					sendACK(ackPacketNr);
					ackPacketNr = null;
				}
			} else {
				System.out.print(getText(data));
			}
		} else {
			System.out.print(getText(data));
		}
		
		/*while ((data = inputStream.read()) != -1) {
			System.out.print(getText(data));
		}*/
	}
	
	/**
	 * Handles serial event.
	 */
	public void serialEvent(final int state, final InputStream inputStream,
			final SerialPortEvent e) throws IOException {

		//final int code = inputStream.read();
		handleEvent(inputStream);
		
		/*
		System.out.println("Test");
		int data;
		StringBuffer buffer = new StringBuffer();
		FileInputStream is = new FileInputStream(new File("C:/ASPA1__RECORDS_RAW.bin"));
		try {
			
			while ((data=is.read()) != -1) {
				buffer.append((char)data);
				System.out.print(data + ", ");
			}
			System.out.println();
			System.out.println(buffer.toString());
			
			
			handleEvent(is);
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			is.close();
		}*/
	}

	@Override
	public void breakInterrupt(final int state) {
		setState(INIT);
		super.breakInterrupt(state);
	}

	@Override
	public String connect() {
		setState(INIT);
		return super.connect();
	}
}
