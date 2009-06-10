package ch.elexis.connect.afinion.packages;

import ch.rgw.tools.TimeTool;

public class HeaderPart extends AbstractPart {
	private int recordNum;
	private TimeTool date;

	public HeaderPart(final String content) {
		parse(content);
	}
	
	public void parse(final String content) {
		String recordNumStr = content.substring(0, 4);
		String dateSecondStr = content.substring(60, 64); //Seconds since 1.1.1970
		
		try {
			recordNum = Integer.parseInt(recordNumStr);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
		
		try {
			long dateSeconds = Integer.parseInt(dateSecondStr);
			date = new TimeTool(dateSeconds * 1000);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
	}

	@Override
	public int length() {
		return 68;
	}

	public int getRecordNum() {
		return recordNum;
	}

	public TimeTool getDate() {
		return date;
	}
}
