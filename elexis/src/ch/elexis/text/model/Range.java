package ch.elexis.text.model;

import ch.elexis.exchange.text.IRange;

public class Range implements IRange {
	int length;
	int position;
	
	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setLength(int pos) {
		length=pos;
	}

	@Override
	public void setPosition(int pos) {
		position=pos;
	}

}
