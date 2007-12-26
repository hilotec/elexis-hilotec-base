// $Id: TimeTool.java 3482 2007-12-26 08:19:21Z rgw_ch $

package ch.rgw.tools;

import java.util.GregorianCalendar;

import java.text.SimpleDateFormat;

/**
 * Klasse zur einfachen Datum- und Zeitberarbeitung
 * @author 		 G. Weirich
 */

public class TimeTool extends GregorianCalendar{
    /**
	 * 
	 */
	private static final long serialVersionUID = 0xc3efadd1L;
	public static String Version(){return "3.1.0";}
	public static final String BEGINNING_OF_UNIX_EPOCH="19700101";	// Erster Tag, der mit dieser Version verwendet werden kann
	public static final String END_OF_UNIX_EPOCH="20380118";		// Letzter Tag, der mit dieser Version verwendet werden kann
	
    public final static String[] Monate={Messages.getString("TimeTool.january"),Messages.getString("TimeTool.february"),Messages.getString("TimeTool.march"),Messages.getString("TimeTool.april"),Messages.getString("TimeTool.may"),Messages.getString("TimeTool.june"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                        Messages.getString("TimeTool.july"),Messages.getString("TimeTool.august"),Messages.getString("TimeTool.september"),Messages.getString("TimeTool.october"),Messages.getString("TimeTool.november"),Messages.getString("TimeTool.december")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

  public final static String[] Mon={Messages.getString("TimeTool.jan"),Messages.getString("TimeTool.feb"),Messages.getString("TimeTool.mar"),Messages.getString("TimeTool.apr"),Messages.getString("TimeTool.may"),Messages.getString("TimeTool.jun"),Messages.getString("TimeTool.jul"),Messages.getString("TimeTool.aug"),Messages.getString("TimeTool.sep"),Messages.getString("TimeTool.oct"),Messages.getString("TimeTool.nov"),Messages.getString("TimeTool.dec")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
  public final static String[] month_eng={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
  public final static String[] wdays={Messages.getString("TimeTool.su"),Messages.getString("TimeTool.mo"),Messages.getString("TimeTool.tu"),Messages.getString("TimeTool.we"),Messages.getString("TimeTool.th"),Messages.getString("TimeTool.fr"),Messages.getString("TimeTool.sa")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
  public final static String[] Wochentage={Messages.getString("TimeTool.sunday"),Messages.getString("TimeTool.monday"),Messages.getString("TimeTool.tuesday"),Messages.getString("TimeTool.wednesday"),Messages.getString("TimeTool.thursday"),Messages.getString("TimeTool.friday"),Messages.getString("TimeTool.saturday")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
  

  public static final int FULL_GER=0;
  public static final int LARGE_GER=1;
  public static final int TIME_FULL=2;
  public static final int TIME_SMALL=3;
  public static final int DATE_GER=4;
  public static final int FULL_MYSQL=5;
  public static final int DATE_MYSQL=6;
  // Aliases fuer ISO-Format
  public static final int FULL_ISO = FULL_MYSQL;
  public static final int DATE_ISO = DATE_MYSQL;
  // public static final int FULL_COMPACT=7;
    public static final int TIME_COMPACT=8;
  public static final int DATE_COMPACT=9;
  public static final int FULL=10;
  public static final int DATE_SIMPLE=11;
  public static final int WEEKDAY=12;
  public static final int TIMESTAMP=13;

  private static SimpleDateFormat full_ger=new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
  private static SimpleDateFormat large_ger=new SimpleDateFormat("dd.MM.yyyy, HH:mm");
  private static SimpleDateFormat time_full=new SimpleDateFormat("HH:mm:ss");
  private static SimpleDateFormat time_small=new SimpleDateFormat("HH:mm");
  private static SimpleDateFormat full_mysql=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static SimpleDateFormat date_mysql=new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat date_ger=new SimpleDateFormat("dd.MM.yyyy");
  // private static SimpleDateFormat full_compact=new SimpleDateFormat("ddMMyyyyHHmm");
  private static SimpleDateFormat time_compact=new SimpleDateFormat("HHmm");
  private static SimpleDateFormat date_compact=new SimpleDateFormat("yyyyMMdd");
  private static SimpleDateFormat timestamp=new SimpleDateFormat("yyyyMMddHHmmss");
  private static SimpleDateFormat pref_full=full_ger;
  private static SimpleDateFormat pref_small=date_ger;
  private static boolean wrap=true;
  private static int defaultResolution=1000;
  private long resolution;

  public static int getTimeInSeconds()
  {	return (int)(System.currentTimeMillis()/1000L);
  }
  public static int getMinutesFromTimeString(final String in)
  {	return getSecondsFromTimeString(in)/60;	
  }
  public static int getSecondsFromTimeString(final String in)
  {	int[] fld=parseTime(in);
	if(fld==null) {
  		return Integer.MAX_VALUE;
  	}
  	return fld[0]*3600+fld[1]*60+fld[2];
  }
  public TimeTool() {
  	resolution=defaultResolution;	// Sekunden-Auflösung
  }
  public TimeTool(final TimeTool other)
  { this.setTimeInMillis(other.getTimeInMillis());
  	resolution=other.resolution;
  }
  public TimeTool(final int t,final int res) {
	  if(res!=0){
		  resolution=res;
	  }
	  else{
		  resolution=defaultResolution;
	  }

	  if(t==0){
		  this.setTimeInMillis(System.currentTimeMillis());
	  }
	  else{
		  this.setTimeInMillis(t*resolution);
	  }
  }
  public TimeTool(final String pre)
  { set(pre);
  	resolution=defaultResolution;
  }
 
  public TimeTool(final String pre, final boolean bFailIfInvalid) throws TimeFormatException{
	  boolean formalOK=set(pre);
	  resolution=defaultResolution;
	  if(bFailIfInvalid && (!formalOK)){
		  throw new TimeFormatException("Invalid Time/Date String");  
	  }
	  
  }
  public TimeTool(final long millis)
  {	this.setTimeInMillis(millis);
  	resolution=defaultResolution;
  }
 /**
   * Parsed einen String im Format "datum zeit" oder "datum, zeit" oder "datum" oder "zeit".
   * Für datum sind folgende Formate gültig:
   * yyyy-MM-dd, yy-MM-dd, dd.MM.yyyy, dd.MM.YY, yyyyMMdd
   * für zeit sind folgende Formate gültig:
   * hh:mm:ss, hh:mm, hhmm, hmm
   * ausserdem das timestamp-format yyyyMMddhhmmss
   * wenn datum gegeben ist, nicht aber Zeit, wird die Zeit auf 00:00:00 gesetzt
   * wenn zeit gegeben ist, nicht aber Datum, wird das Datum nicht geändert
   */
  public boolean set(final String orig)
  { 
	 if(orig==null){
		 return false;
	 }
	 String s=orig.trim();
	 if(StringTool.isNothing(s)){
		 return false;
	 }
	 try{
	    if(s.startsWith("#") || s.startsWith("'") || s.startsWith("\""))
	    { s=s.substring(1,s.length()-1);
	    }
	    String trc=" ";
	    if(s.indexOf(',')!=-1)
	    { trc=",";
	    }else if(s.indexOf("T")!=-1){
	    	trc="T";
	    }
	    String[] s1=s.split(trc);
	    // int[] ret=new int[6];
	    if(s1.length!=2)          // kein Abstand
	    { int[] r=parseTime(s);   // Ist es ein Zeit-String?
	      if(r==null)             // Nein. Ist es ein Datum-String?
	      { r=parseDate(s);
	        if(r==null)           // Nein: Ist es ein timestamp?
	        { if(s.length()==14)
	          { set(Integer.parseInt(s.substring(0,4)),
	                Integer.parseInt(s.substring(4,6))-1,
	                Integer.parseInt(s.substring(6,8)),
	                Integer.parseInt(s.substring(8,10)),
	                Integer.parseInt(s.substring(10,12)),
	                Integer.parseInt(s.substring(12,14)));
	          	set(MILLISECOND, 0);
	          }
	          else
	          {  return false;      // Nein, Fehler
	          }
	        }
	        else                  // Ja, Datum-String
	        { set(r[2],r[1]-1,r[0],0,0,0);
	      	  set(MILLISECOND, 0);
	        }
	      }
	      else                    // Ja, Zeit-String
	      { set(HOUR_OF_DAY,r[0]);
	        set(MINUTE,r[1]);
	        set(SECOND,r[2]);
	      	set(MILLISECOND, 0);
	      }
	    }
	    else                        // Ja, Abstand
	    { int[] d=parseDate(s1[0]); // Datum
	      if(d==null) {
			return false;
		}
	      int[] t=parseTime(s1[1]); // Und Zeit einsetzen
	      if(t==null) {
			return false;
		}
	      set(d[2],d[1]-1,d[0],t[0],t[1],t[2]);
	      set(MILLISECOND, 0);
	    }
	    return true;
	 }catch(Exception ex){
		 ExHandler.handle(ex);
		 return false;
	 }
  }
  // nur Datum setzen, Zeit unverändert lassen
  public boolean setDate(final String dat)
  { 
	  try{
		  int [] d=parseDate(dat);
	    if(d==null) {
			return false;
		}
	    set(YEAR,d[2]);
	    set(MONTH,d[1]-1);
	    set(DAY_OF_MONTH,d[0]);
	    return true;
	  }catch(Exception ex){
		  ExHandler.handle(ex);
		  return false;
	  }
  }
  public void setDate(final TimeTool o)
  { set(YEAR,o.get(YEAR));
    set(MONTH,o.get(MONTH));
    set(DAY_OF_MONTH,o.get(DAY_OF_MONTH));
  }
  // Nur Zeit setzen. Datum unver�ndert lassen
  public void setTime(final TimeTool o)
  {	set(HOUR_OF_DAY,o.get(HOUR_OF_DAY));
  	set(MINUTE,o.get(MINUTE));
  	set(SECOND,o.get(SECOND));
  	set(MILLISECOND, 0);
  }
  /* ParseDate hat ein Problem: Es kann nicht a priori wissen,
  * ob das Datum als yyyy-mm-dd, yy-mm-dd, dd-mm-yy oder dd-mm-yyyy
  * zu verstehen ist. dazu kommen noch dieselben Varianten mit .
  * statt - als Trenner.
  * Die Heuristik geht so vor:
  * Wenn eines vierstellig ist, ist das das Jahr.
  * Wenn keines vierstellig ist, dann gilt:
  * Wenn eines über 31 ist, ist es das Jahr.
  * Wenn keines über 31 ist, dann wird bei Trennzeichen -
  * angenommen, dass zuerst das Jahr steht (MySQL-Stil), wenn .
  * das Trennzeichen ist, wird angenommen, dass das Jahr hinten
  * steht. Wenn eine so ermittelte zweistellige Jahreszahl grösser ist, als das aktuelle
  * Jahr, werden 100 Jahre abgezogen.
  * In der internen Repräsentation steht immer der Tag an erster Stelle
  */
  public static int[] parseDate(final String tx)
  { boolean trenner=false;
    String t=tx.trim();
    String[] dat=t.split("-");
    if(dat.length!=3)
    { dat=t.split("\\.");
      if(dat.length!=3)
      { dat=t.split("/");
      }
      trenner=true;
      if(dat.length!=3)
      { if(t.length()==8)   // YYYYMMDD
        { dat=new String[3];
          dat[2]=t.substring(0,4);
          dat[1]=t.substring(4,6);
          dat[0]=t.substring(6,8);
        }
        else
        { return null;
        }
      }
    }
    int[] ret=new int[3];
    ret[1]=0;
    int s1=0;
    int s2=0;
    try{
    	s1=Integer.parseInt(dat[0]);
    	ret[1]=Integer.parseInt(dat[1]);
    	s2=Integer.parseInt(dat[2]);
    }catch(NumberFormatException nex){
    	ExHandler.handle(nex);
    	return null;
    }
    if(s1>31)
    { ret[0]=s2;
      ret[2]=s1;
    }
    else if(s2>31)
    { ret[0]=s1;
      ret[2]=s2;
    }
    else if(trenner==true)  // . als Trenner
    { ret[0]=s1;
      ret[2]=s2;
    }
    else
    { ret[0]=s2;
      ret[2]=s1;
    }
    if(ret[2]<1900){
    	ret[2]+=2000;
    	int act=new TimeTool().get(TimeTool.YEAR);
    	if(act<(ret[2]-5)){
    		ret[2]-=100;
    	}
    }
    return ret;
  }
  /**
   * Zerlegt einen Zeitstring in Stunden- Minuten- und Sekundenanteile
   * @param tx
   * @return int[hr,min,s]
   */
  public static int[] parseTime(final String tx)
  { String t=tx.trim();
    String[] tim=t.split(":"); // hh:mm
    if(tim.length<2)
    { if(t.matches("[0-2][0-9][0-5][0-9]"))//t.length()==4)           //hhmm
      { tim=new String[2];
        tim[0]=t.substring(0,2);
        tim[1]=t.substring(2);
      }
      else if(t.matches("[0-9][0-5][0-9]")) //t.length()==3)      //hmm
      { tim=new String[2];
        tim[0]=t.substring(0,1);
        tim[1]=t.substring(1);
      }
      else
      {   return null;
      }
    }
    int[] ret=new int[3];
    ret[0]=Integer.parseInt(tim[0]);
    ret[1]=Integer.parseInt(tim[1]);
    if(tim.length>2)
    { ret[2]=Integer.parseInt(tim[2]);
    }
    else
    {	ret[2]=0;
    }
    return ret;
  }
  /*
  public void set(Date d)
  {	super.setTime(d);
  }*/
  public void set(final GregorianCalendar gcal)
  { this.setTimeInMillis(gcal.getTime().getTime());
  }
  public void set(final TimeTool o)
  { this.setTimeInMillis(o.getTimeInMillis());
  	setResolution(o.resolution);
  }
  public void setAsUnits(final int d)
  {	this.setTimeInMillis(d*resolution);
  }
  // Differenz in Sekunden von diesem zu o
  // (positiv, wenn o > dieses,
 //  negativ, wenn o < dieses)
  public int secondsTo(final TimeTool o)
  { long ot=o.getTimeInMillis()/1000L;
    long mt=getTimeInMillis()/1000L;
    long res=ot-mt;
    return (int)res;
  }
  public void setResolution(final long res)
  {	resolution=res;
  }
  
  /**
   * Differenz zu einem anderen TimeTool, ausgedrückt in "resolution"-Einheiten
   * @param o das andere TimeTool
   * @param res die gewünschte Auflösung (in Millisekunden), oder 0, dann wird die
   * Standard-resolution dieses TimeTools genommen
   * @return die differenz, abgerundet auf "res"
   */
  public long diff(final TimeTool o, final long res)
  {	
	  long mine=getTimeInMillis();
	  long other=o.getTimeInMillis();
	  long diff=mine-other;
	  return diff / (res==0 ? this.resolution : res );
  }
  public boolean isBeforeOrEqual(final TimeTool o)
  { return (diff(o,resolution)<=0) ? true : false; 
  }
  public boolean isBefore(final TimeTool o)
  { return (diff(o,resolution)<0) ? true : false;
  }
  public boolean isAfterOrEqual(final TimeTool o)
  {	return (diff(o,resolution)>=0) ? true : false;
  }
  public boolean isAfter(final TimeTool o)
  {	return (diff(o,resolution)>0) ? true : false;
  }
  public boolean isEqual(final TimeTool o)
  { return (diff(o,resolution)==0);
  }

  /**
   * Checks whether two TimeTool values represent the same day (ignoring the time)
   * @param o the TimeTool to compare with
   * @return true, if both times represent the same day
   */
  public boolean isSameDay(final TimeTool o) {
	  if (o == null) {
		  return false;
	  }
	  
	  int year1 = get(TimeTool.YEAR);
	  int month1 = get(TimeTool.MONTH);
	  int day1 = get(TimeTool.DAY_OF_MONTH);
	  
	  int year2 = o.get(TimeTool.YEAR);
	  int month2 = o.get(TimeTool.MONTH);
	  int day2 = o.get(TimeTool.DAY_OF_MONTH);
	  
	  if ((year1 == year2) && (month1 == month2) && (day1 == day2)) {
		  return true;
	  } else {
		  return false;
	  }
  }
  
  public int getTimeInUnits()
  {	return (int)(getTimeInMillis()/resolution);
  }
  public long getTimeAsLong()
  { return getTimeInMillis();
  }
  /**
   * Inhalt kürzen. 
   * @param w 0: Millisekunden weg, 1: Sekunden, 2: Minuten, 3: Stunden
   */
  public void chop(final int w)
  {   set(MILLISECOND,0);
    if(w>0) {
		set(SECOND,0);
	}
    if(w>1) {
		set(MINUTE,0);
	}
    if(w>2) {
		set(HOUR_OF_DAY,0);
	}
  }
  public void addUnits(final int m)
  {	add(MILLISECOND,m*(int)resolution);
  }
  public void addMinutes(final int m)
  { add(MINUTE,m);
  }
  public void addHours(final int h)
  { add(HOUR_OF_DAY,h);
  }
  public void addSeconds(final int s)
  { add(SECOND,s);
  }
  public static void setDefaultResolution(final int r)
  { defaultResolution=r;
  }
  public static void setPreferredFormat(final String full, final String small, final String wr)
  { pref_full=new SimpleDateFormat(full);
    pref_small=new SimpleDateFormat(small);
    if(wr.equals("1")) {
		wrap=true;
	} else {
		wrap=false;
	}
  }
  public String toDBString(final boolean full)
  { String res;
  	if(full==true)
  	{	res=pref_full.format(getTime());
  	}
  	else
  	{	res=pref_small.format(getTime());
  	}
	if(wrap==true)
	{ return JdbcLink.wrap(res);
	} else {
		return res;
	}
  }
  @Override
public String toString()
  {	return Long.toHexString(getTimeInMillis());
  }
  public String dump(){
	  return toString(FULL);
  }
  public String toString(final int f)
  { String res;
    switch(f)
    { case FULL: res= pref_full.format(getTime()); break;
      case DATE_SIMPLE: res= pref_small.format(getTime()); break;
      case FULL_GER: res=full_ger.format(getTime()); break;
      case LARGE_GER: res= large_ger.format(getTime()); break;
      case TIME_FULL: res= time_full.format(getTime()); break;
      case TIME_SMALL: res= time_small.format(getTime()); break;
      case FULL_MYSQL: res= full_mysql.format(getTime()); break;
      case DATE_MYSQL: res= date_mysql.format(getTime()); break;
      case DATE_GER: res= date_ger.format(getTime()); break;
      case TIME_COMPACT: res= time_compact.format(getTime()); break;
      case DATE_COMPACT: res= date_compact.format(getTime()); break;
      case WEEKDAY:	res=wdays[get(DAY_OF_WEEK)-1]; break;
      // case FULL_COMPACT: res= full_compact.format(getTime()); break;
      case TIMESTAMP: res=timestamp.format(getTime()); break;
      default: res= "00:00";
    }
   	return res;
  }
  @Override
public int hashCode(){
	  return toString().hashCode();
  }
  
  public static int minutesStringToInt(final String in){
		String[] hm=in.split("[:\\.]");
		String h="0";
		String m="0";
		if(hm.length==1){
			if(hm[0].length()<3){
				return 0;
			}
			if(hm[0].length()<4){
				hm[0]="0"+hm[0];
			}
			h=hm[0].substring(0,2);
			m=hm[0].substring(2,4);
		}else{
			h=hm[0];
			m=hm[1];
		}
		return Integer.parseInt(h)*60+Integer.parseInt(m);
	}
  
    public static class TimeFormatException extends Exception{
		private static final long serialVersionUID = -7509724431749474725L;
		public TimeFormatException(final String msg){
    		super(msg);
    	}
    }
}