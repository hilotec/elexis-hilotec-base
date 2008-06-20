package ch.elexis.scripting;

import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;

public class Util {
	private static Log log=Log.get("Script utility");
	public static void display(String title, String contents){
		SWTHelper.showInfo(title, contents);
	}
	
	public static void log(String text){
		log.log(text, Log.WARNINGS);
	}
}
