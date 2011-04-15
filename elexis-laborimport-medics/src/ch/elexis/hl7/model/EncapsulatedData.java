package ch.elexis.hl7.model;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import ch.elexis.hl7.util.HL7Helper;

public class EncapsulatedData implements IValueType {
	
	String name;
	byte[] data;
	Date date;
	
	public EncapsulatedData(String name, String encoding, String text, String dateStr)
		throws ParseException{
		super();
		this.name = name;
		if (dateStr != null && dateStr.length() > 0) {
			this.date = HL7Helper.stringToDate(dateStr);
		}
		if (encoding != null && "base64".equals(encoding.trim().toLowerCase())) { //$NON-NLS-1$
			data = Base64.decodeBase64(text.getBytes());
		} else {
			data = text.getBytes();
		}
	}
	
	public String getName(){
		return name;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public Date getDate(){
		return date;
	}
	
	public void setDate(Date date){
		this.date = date;
	}
}
