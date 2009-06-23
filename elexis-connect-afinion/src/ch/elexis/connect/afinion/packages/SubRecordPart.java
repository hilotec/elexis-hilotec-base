package ch.elexis.connect.afinion.packages;

public class SubRecordPart extends AbstractPart {
	private double min;
	private double max;
	private double result;
	private int decimals;
	private boolean valid;
	private String unit;
	private String kuerzel;

	public SubRecordPart(final byte[] bytes, final int pos) {
		parse(bytes, pos);
	}
	
	public void parse(final byte[] bytes, final int pos) {
		min = getInteger(bytes, pos);
		max = getInteger(bytes, pos + 4);
		result = getInteger(bytes, pos + 8);
		decimals = getInteger(bytes, pos + 12);
		valid = (getInteger(bytes, pos + 16) == 0);
		unit = getString(bytes, pos + 20, 9);
		kuerzel = getString(bytes, pos + 29, 9);
	}
	
	@Override
	public int length() {
		return 40;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getResult() {
		return result;
	}

	public int getDecimals() {
		return decimals;
	}

	public boolean isValid() {
		return valid;
	}

	public String getUnit() {
		return unit;
	}

	public String getKuerzel() {
		return kuerzel;
	}
}
