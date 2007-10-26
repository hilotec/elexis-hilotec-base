package ch.medshare.elexis.directories;

public class HtmlParser {
	private final StringBuffer htmlText;
	private int currentPos = 0;

	public HtmlParser(String htmlText) {
		super();
		this.htmlText = new StringBuffer(htmlText);
	}
	
	public void reset() {
		this.currentPos = 0;
	}

	public boolean startsWith(String prefix) {
		if (prefix == null) {
			return false;
		}
		return htmlText.substring(currentPos, currentPos + prefix.length())
				.startsWith(prefix);
	}

	public boolean moveTo(String keyString) {
		int newPos = getNextPos(keyString);
		if (newPos >= 0) {
			currentPos = newPos + keyString.length();
			display(currentPos);
			return true;
		}
		return false;
	}

	public String extractTo(String endKeyString) {
		int newPos = getNextPos(endKeyString);
		String text = null;
		if (newPos >= 0) {
			text = htmlText.substring(currentPos, newPos);
			currentPos = newPos + endKeyString.length();
			display(currentPos);
		}
		return text;
	}

	private void display(int pos) {
		int theEnd = pos + 1000;
		if (theEnd >= htmlText.length()) {
			theEnd = htmlText.length() - 1;
		}
		//System.out.println("Current: " + htmlText.substring(pos, theEnd));
	}

	public String extract(String startKeyString, String endKeyString) {
		if (moveTo(startKeyString)) {
			return extractTo(endKeyString);
		}
		return null;
	}

	public int getNextPos(String keyString, int pos) {
		return htmlText.indexOf(keyString, pos);
	}

	public int getNextPos(String keyString) {
		return getNextPos(keyString, currentPos);
	}
}
