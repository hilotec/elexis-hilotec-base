// $Id: Log.java 2666 2007-06-29 13:39:32Z danlutz $ 
package ch.elexis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

	/**
	 * Standardisiertes Log. Ein Programm kann das Log mit Log.get(pr�fix) anfordern
	 * und fortan Ausgaben ins Log mittels Log(Text,level) machen. Die Ausgabe erfolgt
	 * einstellbar nach stdout oder in eine Datei. Ob eine bestimmte Ausgabe ins Log gelangt,
	 * h�ngt vom LogLevel und dem Text-Level ab. Wenn der Level einer Meldung gleich oder
	 * niedriger ist, als der aktuell eingestellte LogLevel, wird die Ausgabe gemacht, 
	 * andernfalls wird sie verworfen.
	 * Ausserdem kann festgelegt werden, ab welchem level eine Nachricht zu einer direkten Benachrichtigung
	 * des Anwenders mittels MessageBox f�hrt (setAlert und setAlertLevel
	 * 
	 * @author G. Weirich
	 */

public class Log {
	 /** Experimentell  */
	public static final int NOTHING=0;
	  /** Fatale Fehler, Programmabbruch */
	  public static final int FATALS=1;
	  /** Nichtfatale Fehler, Programm kann weiterlaufen */
	  public static final int ERRORS=2;
	  /** Warnung, Programm l�uft normal weiter, es k�nnten aber Probleme auftreten */
	  public static final int WARNINGS=3;
	  /** Reine Informationen, kein Einfluss aufs Programm */
	  public static final int INFOS=4;
	  /** F�r Debugzwecke gedachte Meldungen */
	  public static final int DEBUGMSG=5;
	  /** Immer auszugebende Meldungen, die aber keinem Fehler entsprechen */
	  public static final int TRACE=6;
	  /** Immer auszugebende Meldungen, automatisch mit einem Timestamp versehen */
	  public static final int SYNCMARK=-1;
	  
	  public static final int DEFAULT_LOGFILE_MAX_SIZE = 200000;
	  
	  private static PrintStream out;
	  String prefix;
	  private static int LogLevel;
	  private static int alertLevel;
	  private static String lastError;
	  private static Shell doAlert;
	  
	  static
	  { out=System.out;
	  	LogLevel=2;
	  	doAlert=null;
	  	alertLevel=Log.ERRORS;
	  }
	  /**
	   * Ausgabeziel einstellen. Ist immer global f�r alle Klassen des aktuellen Pogramms.
	   * @param name null oder "" oder none: Ausgabe nach stdout, andernfalls ein
	   * Dateiname, der die Ausgabedatei definiert.
	   * @param maxSize maximale Grösse (unbeschränkt, falls <= 0)
	   */
	  static public void setOutput(String name, int maxSize)
	  { if( (name==null) || (name.equals("")) || (name.equals("none")) ) //$NON-NLS-1$ //$NON-NLS-2$
	    { out=System.out;
	    }
	    else
	    { try{
	        File f=new File(name);
	        if(f.exists()){
	        	if (maxSize > 0) {
	        		if(f.length()>maxSize){
	        			f.createNewFile();
	        		}
	        	}
	        }else{
	        	f.createNewFile();
	        }
	        out=new PrintStream(new FileOutputStream(f, true));
	      }
	      catch (Exception ex)
	      { ExHandler.handle(ex);
	      }
	    }
	  }
	  /**
	   * LogLevel einstellen
	   * @param l der gew�nschte Level. Ist immer global f�r alle Klassen des aktuellen Programms.
	   */
	  static public void setLevel(int l)
	  { LogLevel=l;
	  }
	  /**
	   * AlertLevel einstellen. wenn der Level einer Nachricht unter diesem Level liegt,
	   * wird eine Alertbox zur Nazeige der Nachricht ge�ffnet (Anstatt nur zu loggen).
	   * Dies geht nur, wenn mit setAlert auch eine parent-Shell gesetzt worden ist.
	   */
	  static public void setAlertLevel(int l){
		  alertLevel=l;
	  }
	  /**
	   * Alert inetellen oder l�schen. 
	   * Wenn cmp nicht null ist, wird bei jeder Fehlermeldung>Log.Errors eine
	   * Alertbox mit der Fehlermeldung ausgegeben.
	   * @param cmp die Paent-Komponente f�r die Alertbox
	   */
	  static public void setAlert(Shell cmp)
	  {
	      doAlert=cmp;
	  }
	  /**
	   * Das Log anfordern. Es gibt pro Programm nur ein Log. 
	   * @param prefix Ein String, der allen Log-Ausgaben dieser Instanz vorangestellt wird
	   * @return eine Log-Instanz
	   */
	  static public Log get(String prefix)
	  {   return new Log(prefix);
	  }
	  /**
	   * Eine Log-Nachricht ausgeben.
	   * @param message die Nachricht
	   * @param level der level
	   */
	  public void log(String message, int level)
	  { synchronized(out)
	  	{
		    if(level<=LogLevel)
		    { 
		    	TimeTool t=new TimeTool();
			    out.print(t.toString(TimeTool.FULL_GER));
	          lastError=prefix+": "+message; //$NON-NLS-1$
		      out.println("** "+lastError); //$NON-NLS-1$
		      out.flush();
	          if(level<=alertLevel) {
                  if(level!=SYNCMARK){
    	        	  if(doAlert==null){
    	        		  IWorkbench wb=Hub.plugin.getWorkbench();
    	        		  if(wb!=null){
    	        			  IWorkbenchWindow wi=wb.getActiveWorkbenchWindow();
    	        			  if(wi!=null){
    	        				  doAlert=wi.getShell();
    	        			  }
    	        		  }
    	        	  }
    	        	  if(doAlert==null){
    	        		  if(Desk.theDisplay==null){
    	        			  Desk.theDisplay=PlatformUI.createDisplay();
    	        			  doAlert=new Shell(Desk.theDisplay);
    	        		  }
    	        	  }else{
    	            	  Desk.theDisplay.asyncExec(new Runnable(){
							public void run() {
								 MessageBox msg=new MessageBox(doAlert,SWT.ICON_ERROR|SWT.OK);
								 msg.setMessage(lastError);
		    	                 msg.open();		
							}
    	            	  });
    	            	 
    	              }
                  }
	          }
		    }
	  	}
	  }
	  public static void trace(String msg)
	  {	StringBuffer mark=new StringBuffer(100);
		mark.append("--TRACE: "); //$NON-NLS-1$
		mark.append(new TimeTool().toString(TimeTool.FULL_GER));
		mark.append(": ").append(msg); //$NON-NLS-1$
		out.println(mark.toString());
	  }
	  private Log() { /* leer */ }
	  private Log(String p)
	  { prefix=p;
	  }
	
}