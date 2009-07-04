package ch.elexis.connect.afinion.packages;

import java.util.Calendar;

import ch.elexis.data.Patient;

/**
 * Diese Klasse ist Platzhalter für eine Patient Record
 * 
 * @author immi
 * 
 */
public class Record {
	private HeaderPart header;
	private SubRecordPart[] parts = new SubRecordPart[4];
	private boolean isValid = false;
	
	public Record(final byte[] bytes){
		parse(bytes);
	}
	
	/**
	 * Header, Subparts werden geparst Footer interessiert nicht
	 * 
	 * @param bytes
	 */
	private void parse(byte[] bytes){
		header = new HeaderPart(bytes);
		int pos = header.length();
		for (int i = 0; i < 4; i++) {
			parts[i] = new SubRecordPart(bytes, pos);
			if (parts[i].isValid()) {
				isValid = true;
			}
			pos += parts[i].length();
		}
	}
	
	public String getId(){
		return this.header.getId();
	}
	
	public Calendar getCalendar(){
		return this.header.getCalendar();
	}
	
	public int getRunNr(){
		return this.header.getRunNr();
	}
	
	public boolean isValid(){
		return this.isValid;
	}
	
	public String getText(){
		String text = "";
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isValid()) {
				if (text.length() > 0) {
					text += ", ";
				}
				text +=
					parts[i].getKuerzel() + " = " + parts[i].getResultStr() + " "
						+ parts[i].getUnit();
			}
		}
		return text;
	}
	
	/**
	 * Schreibt die Werte in die Datenbank
	 * 
	 * @param patient
	 * @throws PackageException
	 */
	public void write(Patient patient) throws PackageException{
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isValid()) {
				Value val = Value.getValue(parts[i].getKuerzel(), parts[i].getUnit());
				val.fetchValue(patient, parts[i].getResultStr(), "", this.header.getDate());
			}
		}
	}
	
	public String toString(){
		String str = header.toString() + "\n";
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isValid()) {
				str += "S-Record " + i + ";";
				str += parts[i].toString() + "\n";
			}
		}
		return str;
	}
	
}
