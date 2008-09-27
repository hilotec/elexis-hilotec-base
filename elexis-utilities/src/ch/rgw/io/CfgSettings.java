// $Id: CfgSettings.java 2698 2007-07-03 12:51:47Z rgw_ch $
package ch.rgw.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;

/**
 * Settings-Implementation, die auf einem flat file basiert. Alle Schlüssel sind
 * in der Form 
 * <code>key=value</code>
 * in je einer eigenen Zeile abgelegt. Hierarchische Schlüssel (Branches/Zweige)
 * sind einfach als
 * <code>branch/key=value</code>
 * abgespeichert-
 * 
  * @author Gerry Weirich
 */
public class CfgSettings extends Settings
{   /**
	 * 
	 */
	private static final long serialVersionUID = 0xaaa34e1L;
	
public static final String Version(){return "1.0.2";}
    private transient String filename;
    private transient String message="CfgSettings v"+Version();
    
    public CfgSettings(String n, String msg){
        filename=n;
        message=msg;
        undo();
      }
      /**
       * �ffnet Settings oder legt sie neu an, w�hlt automatisch den user.home-Pfad und den
       * vorgegebenen Dateinamen.
       * @param name Filename f�r das settings-file
       * @param msg Einzeilige Nachricht, die als Kommentar in die erste Zeile kommt
       */
      static public Settings open(String name, String msg)
      { 
        try {
            if(exists(name)) {
                return new CfgSettings(name,msg);
            }
            String h=System.getProperty("user.home","");
            if(!h.equals(""))
            { h+=java.io.File.separator;
            }
            if(exists(h+name)) {
                return new CfgSettings(h+name,msg);
            }
            if(exists(name+".cfg")) {
                return new CfgSettings(name+".cfg",msg);
            }
            if(exists(h+name+".cfg")) {
                return new CfgSettings(h+name+".cfg",msg);
            }
            if(name.matches(".*"+File.pathSeparator+".*")) {
                return new CfgSettings(name,msg);
            }
            return new CfgSettings(h+name,msg);
        }
        catch(Exception ex) {
            ExHandler.handle(ex);
            return null;
        }
      
      }

       /**
       * Fragt, ob ein Settings-file existiert (Pr�ft nicht auf Syntax)
       * @param name Dateiname
       * @return true wenn die Datei existiert
       */

      static public boolean exists(String name)
      { File fi=new File(name);
        return fi.exists();
      }
      
      /**
       * @see ch.rgw.IO.Settings#flush()
       */
      protected void flush_absolute()
      {
    	  if(filename==null){
    		  return;
    	  }
    	  try{
    		  BufferedWriter fo=new BufferedWriter(new FileWriter(filename));
    		  fo.write("#"+message);
    		  ArrayList<String> all=getAll();
    		  Iterator<String> it=all.iterator();
    		  while(it.hasNext()){
    			  String el=(String)it.next();
    			  String v=get(el,null);
    			  fo.write(el+"="+v);
    			  fo.newLine();
    		  }
    		  fo.close();
    	  }
    	  catch(Exception e1)
    	  { ExHandler.handle(e1);
    	  }
      }
      /**
       * @see ch.rgw.IO.Settings#undo()
       */
      public void undo()      {
    	  if(filename==null){
    		  return;
    	  }
    	  clear();
    	  try{
    		  BufferedReader fi=new BufferedReader(new FileReader(filename));
    		  String line;
    		  while((line=fi.readLine())!=null){
    			  line=line.replaceAll("#.*","").trim();
    			  if(StringTool.isNothing(line)){
    				  continue;
    			  }
    			  String[] pair=line.split("=");
    			  if(pair.length!=2){
    				  log.log("Format Error in config file "+filename,Log.ERRORS);
    				  continue;
    			  }
    			  set(pair[0].trim(),pair[1].trim());
    		  }
    		  fi.close();
    		  cleaned();
    	  }
    	  catch(Exception ex)
    	  { 	ExHandler.handle(ex);
    	  }
      }

    public void kill()
    {
        if(filename==null){
            return;
        }
        File f=new File(filename);
        f.delete();
    }
}