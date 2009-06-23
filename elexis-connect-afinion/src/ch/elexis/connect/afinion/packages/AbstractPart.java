package ch.elexis.connect.afinion.packages;

public abstract class AbstractPart {

	public abstract int length();
	
	protected int getInteger(final byte[] bytes, int pos) {
		if (pos > bytes.length) {
			throw new ArrayIndexOutOfBoundsException("Pos > byte.length");
		}
		if (pos + 4 > bytes.length) {
			throw new ArrayIndexOutOfBoundsException("Pos + 4 > byte.length");
		}
		int index = pos + 3;
		int value = bytes[index--];
		value <<= 8;
		value += bytes[index--];
		value <<= 8;
		value += bytes[index--];
		value <<= 8;
		value += bytes[index];
		return value;
	}
	
	protected String getString(final byte[] bytes, int pos, int length) {
		if (pos > bytes.length) {
			throw new ArrayIndexOutOfBoundsException("Pos > byte.length");
		}
		if (pos + length > bytes.length) {
			throw new ArrayIndexOutOfBoundsException("Pos + length > byte.length");
		}
		StringBuffer buffer =new StringBuffer();
		for (int i=pos; i<pos+length; i++) {
			if (bytes[i] != 0) {
				buffer.append((char)bytes[i]);
			}
		}
		return buffer.toString();
	}
}
