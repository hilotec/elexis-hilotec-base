package ch.elexis.text.model;


public class Range implements IRange {
	int length;
	int position;
	
	public Range(final int start, final int len){
		length=len;
		position=start;
	}
	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setLength(final int pos) {
		length=pos;
	}

	@Override
	public void setPosition(final int pos) {
		position=pos;
	}

}
