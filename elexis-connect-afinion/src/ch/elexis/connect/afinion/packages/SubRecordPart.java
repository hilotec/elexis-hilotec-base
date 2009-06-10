package ch.elexis.connect.afinion.packages;

public class SubRecordPart extends AbstractPart {
	private double min;
	private double max;
	private double result;
	private int decimals;
	private boolean valid;
	private String unit;
	private String kuerzel;

	public SubRecordPart(final String content) {
		parse(content);
	}
	
	public void parse(final String content) {
		String minStr = content.substring(0, 4);
		String maxStr = content.substring(4, 8);
		String resultStr = content.substring(8, 12);
		String decimalStr = content.substring(12, 16);
		String validStr = content.substring(16, 20);
		unit = content.substring(20, 29);
		kuerzel = content.substring(29, 38);
		
		try {
			min = Double.parseDouble(minStr);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
		
		try {
			max = Double.parseDouble(maxStr);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
		
		try {
			result = Double.parseDouble(resultStr);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
		
		try {
			decimals = Integer.parseInt(decimalStr);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
		
		try {
			int validValue = Integer.parseInt(validStr);
			valid = (validValue > 0);
		} catch(NumberFormatException e) {
			// TODO: Exception handling
		}
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
