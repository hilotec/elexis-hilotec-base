// $Id: VersionInfo.java 1068 2006-10-06 17:08:18Z rgw_ch $

package ch.rgw.tools;

import org.eclipse.swt.SWT;


/**
 * Einfaches API zum Vergleich von Versionsnummern des Typs maior.minor.rev
 * @author G. Weirich
 * @version 1.6.0
 */

public class VersionInfo implements Comparable{
	
  public static String Version(){return "1.6.0";}
  String orig;
  String[] spl;
  public VersionInfo()
  { this(Version());
  }
   public VersionInfo(String v)
   { if(StringTool.isNothing(v))
       {    orig=null;
           spl=null;
       }
       else {
     orig=v;
     spl=orig.split("\\.");
       }
   }
   public String maior()
   {   if((spl==null) || (spl.length<1)) return "0";
       if(StringTool.isNothing(spl[0])) return "0";
       return spl[0];
   }
   public String minor()
   { if((spl==null) ||( spl.length<2)) return "0";
     return spl[1];
   }
   public String rev()
   { if((spl==null) ||(spl.length<3)) return "0";
     return spl[2];
   }
   public String version()
   { return orig;
   }
   public boolean isNewer(String other)
   {   VersionInfo vo=new VersionInfo(other);
       return isNewer(vo);
   }
   public boolean isOlder(String other)
   {    VersionInfo vn=new VersionInfo(other);
        return isOlder(vn);
   }
   /**
    * Ist diese Version neuer, als die andere?
    * @param vo die andere
    * @return true:ja, false: nein
    */
   public boolean isNewer(VersionInfo vo)
   {    return (compareTo(vo)>0);
   }
   public boolean isOlder(VersionInfo vo)
   {    return (compareTo(vo)<0);  
   }
   public boolean isEqual(VersionInfo vo)
   {    return (compareTo(vo)==0);
   }

    public int compareTo(Object arg0) {
    VersionInfo vo=(VersionInfo)arg0;
    int c=compareElem(this.maior(),vo.maior());
    if(c!=0) return c;
    c=compareElem(this.minor(),vo.minor());
    if(c!=0) return c;
    return compareElem(this.rev(),vo.rev());
  
    }
    private int compareElem(String a,String b)
    {	int al=a.length();
    	int bl=b.length();
    	if(al==bl)
    	{	return a.compareToIgnoreCase(b);
    	}
    	int diff=Math.abs(al-bl);
    	String x=StringTool.pad(SWT.LEFT,'0',a,al+diff+1);
    	String y=StringTool.pad(SWT.LEFT,'0',b,al+diff+1);
    	return x.compareToIgnoreCase(y);
    }
 }