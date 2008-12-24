package ch.elexis.buchhaltung.util;

import java.util.Calendar;

import ch.rgw.tools.TimeTool;

public class DateTool extends TimeTool {
	
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
	
	@Override
	public int compareTo(Calendar arg0){
		long diff=(getTimeInMillis()-arg0.getTimeInMillis());
		if(diff<0){
			return -1;
		}else if(diff>0){
			return 1;
		}
		return 0;
	}
}
