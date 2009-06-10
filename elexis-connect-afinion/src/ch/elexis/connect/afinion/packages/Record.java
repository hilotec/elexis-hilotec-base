package ch.elexis.connect.afinion.packages;

import ch.elexis.data.Patient;

public class Record {
	private HeaderPart header;
	private SubRecordPart part1;
	private SubRecordPart part2;
	private SubRecordPart part3;
	private SubRecordPart part4;
	private Patient patient;

	public Record(final String content, final Patient pat) {
		this.patient = pat;
		parse(content);
	}
	
	private void parse(String content) {
		header = new HeaderPart(content);
		content = content.substring(header.length());
		part1 = new SubRecordPart(content);
		content = content.substring(part1.length());
		part2 = new SubRecordPart(content);
		content = content.substring(part2.length());
		part3 = new SubRecordPart(content);
		content = content.substring(part3.length());
		part4 = new SubRecordPart(content);
		content = content.substring(part4.length());
	}
	
	public void write() throws PackageException {
		Value val = Value.getValue(part1.getKuerzel(), part1.getUnit());
		String value = new Double(part1.getResult()).toString();

		val.fetchValue(patient, value, "", this.header.getDate());
	}
	
	
}
