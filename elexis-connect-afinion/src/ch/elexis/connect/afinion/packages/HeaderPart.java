package ch.elexis.connect.afinion.packages;

import ch.rgw.tools.TimeTool;

public class HeaderPart extends AbstractPart {
	private int recordNum;
	private TimeTool date;

	public HeaderPart(final byte[] bytes) {
		parse(bytes);
	}
	
	public void parse(final byte[] bytes) {
		int recordNum = getInteger(bytes, 0);
		int dateSeconds = getInteger(bytes, 60); //Seconds since 1.1.1970
		date = new TimeTool(dateSeconds * 1000);
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
