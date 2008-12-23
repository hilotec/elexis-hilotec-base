package util;

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
}
