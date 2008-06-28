package ch.elexis.scripting;

import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class Util {
	private static Log log=Log.get("Script utility");
	public static void display(String title, String contents){
		SWTHelper.showInfo(title, contents);
	}
	
	public static void log(String text){
		log.log(text, Log.WARNINGS);
	}
	
	public static int compareDates(String d1, String d2){
		TimeTool tt1=new TimeTool();
		TimeTool tt2=new TimeTool();
		if(tt1.set(d1) && tt2.set(d2)){
			return tt1.compareTo(tt2);
		}
		return 0;
	}
}
