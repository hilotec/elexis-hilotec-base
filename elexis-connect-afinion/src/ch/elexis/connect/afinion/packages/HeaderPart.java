package ch.elexis.connect.afinion.packages;

import java.util.Calendar;

import ch.rgw.tools.TimeTool;

public class HeaderPart extends AbstractPart {
	private int recordNum;
	private int runNr;
	private String lotNr;
	private String id;
	private TimeTool date;

	public HeaderPart(final byte[] bytes) {
		parse(bytes);
	}
	
	public void parse(final byte[] bytes) {
		recordNum = getInteger(bytes, 0);
		runNr = getInteger(bytes, 4);
		lotNr = getString(bytes, 25, 17);
		id = getString(bytes, 42, 17);
		
		long dateSeconds = getInteger(bytes, 60); //Seconds since 1.1.1970 00:00 local time
		
		date = new TimeTool(dateSeconds * 1000);
	}

	@Override
	public int length() {
		return 68;
	}

	public int getRecordNum() {
		return recordNum;
	}
	
	public String getLotNr() {
		return lotNr;
	}
	
	public int getRunNr() {
		return runNr;
	}
	
	public String getId() {
		return id;
	}

	public TimeTool getDate() {
		return date;
	}
	
	private String toDate(TimeTool tt) {
		int day = tt.get(Calendar.DAY_OF_MONTH);
		String dayStr = day > 9? "" + day: "0" + day;
		int month = tt.get(Calendar.MONTH) + 1;
		String monthStr = month > 9? "" + month: "0" + month;
		String yearStr = new Integer(tt.get(Calendar.YEAR)).toString();
		
		return dayStr + "." + monthStr + "." + yearStr;
	}
	
	private String toTime(TimeTool tt) {
		int hour = tt.get(Calendar.HOUR_OF_DAY);
		String hourStr = hour > 9? "" + hour: "0" + hour;
		int minute = tt.get(Calendar.MINUTE);
		String minStr = minute > 9? "" + minute: "0" + minute;
		
		return hourStr + ":" + minStr;
	}
	
	public String toString() {
		String str = "";
		str += "Record: " + recordNum + "\n";
		str += "Date: " + toDate(date) + "\n";
		str += "Time: " + toTime(date) + "\n";
		str += "Run#: " + runNr + "\n";
		str += "ID: " + id + "\n";
		str += "Lot#: " + lotNr + "\n";
		
		return str;
	}
}
