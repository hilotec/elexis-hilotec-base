package ch.elexis.buchhaltung.util;

import ch.rgw.tools.TimeTool;

public class DateTool extends TimeTool{
	
	public DateTool(){
		super();
	}
	
	public DateTool(TimeTool other){
		super(other);
	}
	
	public DateTool(String other){
		super(other);
	}
	
	@Override
	public String toString(){
		return toString(TimeTool.DATE_SIMPLE);
	}
	
	
	public int compareTo(DateTool d1){
		long diff=(getTimeInMillis()-d1.getTimeInMillis())/86400000L;	// consider only day-differences
		return (int)diff;
	}
}
