package ch.elexis.hl7.model;

import java.text.ParseException;
import java.util.Date;

import ch.elexis.hl7.util.HL7Helper;

public class StringData implements IValueType {
	String name;
	String unit;
	String value;
	String range;
	Date date;
	
	public StringData(String name, String unit, String value, String range, String dateStr) throws ParseException {
		super();
		this.name = name;
		this.unit = unit;
		this.value = value;
		this.range = range;
		if (dateStr != null && dateStr.length() > 0) {
			this.date = HL7Helper.stringToDate(dateStr);
		}
	}
	
	public String getName(){
		return name;
	}

	public String getUnit(){
		return unit;
	}

	public String getValue(){
		return value;
	}

	public String getRange(){
		return range;
	}

	public Date getDate(){
		return date;
	}
	
	public void setDate(Date date){
		this.date = date;
	}
}
