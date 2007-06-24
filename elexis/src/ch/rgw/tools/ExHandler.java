// $Id: ExHandler.java 1182 2006-10-29 14:48:00Z rgw_ch $

package ch.rgw.tools;
import java.io.*;

/**
 * Einfacher catch-all Exeption handler. Kann Exceptions anzeigen oder loggen
 * @author G. Weirich
 */

public class ExHandler {
  static final String Version(){return "1.6.3";}
  private static PrintStream out;
  private static String[] mine=null;;

  private ExHandler() { }
  
  static
  { out=System.err;
  }
  
  /**
   * Ausgabestream für Exception-Meldungen setzen
   * @param name der Ausgabestream
   */
  public static void setOutput(String name)
  {  if( (name==null) || (name.equals("")) || (name.equals("none")))
    { out=System.err;
    }else if(name.equals("sysout")){
    	out=System.out;
    }
    else
    { try{
        File f=new File(name);
        f.createNewFile();
        out=new PrintStream(new FileOutputStream(f));
      }
      catch(Exception ex)
      { System.err.println(Messages.getString("ExHandler.cantRedirectOutput")); //$NON-NLS-1$
      }
    }
  }
  /** Aktuellen Output stream lesen  */
  public static PrintStream output()
  {	return out;
  }
  
  /** Interessierende Klassen setzen (Präfixe).
   * (Nur die Klassen mit dieser Präfix werden im Stack-Trace ausgegeben.
   *  Wenn keine angegeben werden, werden alle angezeigt.
   * @param interest Alle interessierenden Klassen.
   */
  public static void setClasses(String[] interest)
  {  mine=interest;
  }

  /**
   * Exception behandelt. Gibt standardmässig die Exeptions-Klasse, die
   * message der Exception und einen Stack-Trace der interessierenden Klassen aus.
   * @param ex die Exception
   */
  public static void handle(Throwable ex)
  { //synchronized(out)
  	{
  		out.flush();
    	out.println("--------------Exception--------------");
    	out.println(ex.getClass().getName());
    	out.println(ex.getMessage());
    	if(ex.getClass().getName().equals("java.sql.SQLException"))
    	{ out.println(((java.sql.SQLException)ex).getSQLState());
      	return;
    	}
    	StackTraceElement[] ste=ex.getStackTrace();
    	for(int i=0;i<ste.length;i++)
    	{ String sts=ste[i].getClassName();
      	if(mine==null)
      	{ if(sts.startsWith("java"))
        	{ continue;
        	}
        	if(sts.startsWith("sun."))
        	{ continue;
        	}
        	out.println(ste[i].toString());
      	}
      	else
      	{ for(int j=0;j<mine.length;j++)
	      	{ if(sts.startsWith(mine[j]))
          		{ out.println(ste[i].toString());
          		}
        	}
      	}
    }
    // ex.printStackTrace(out);
    out.println("-----------End Exception handler-----");
    out.flush();
  	}
  }
}
