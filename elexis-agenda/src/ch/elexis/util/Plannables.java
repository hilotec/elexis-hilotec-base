/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Plannables.java 1808 2007-02-15 12:47:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.text.DecimalFormat;
import java.util.*;

import org.eclipse.swt.graphics.*;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.data.IPlannable;
import ch.elexis.data.Query;
import ch.elexis.data.Termin;
import ch.elexis.preferences.PreferenceConstants;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

/**
 * Utility-Klasse für Operationen mit und an Plannables
 * @author Gerry
 *
 */
public class Plannables {
	private Plannables(){}
	private static DecimalFormat df=new DecimalFormat("00");
	
	/** Feststellen, ob zwei Plannables sich überlappen */
	public static boolean isOverlapped(IPlannable p1, IPlannable p2){
		if(p1.getDay().equals(p2.getDay()))
		  	{	int Beginn=p1.getStartMinute();
		  	    int oBeginn=p2.getStartMinute();
		  	    int Dauer=p1.getDurationInMinutes();
		        int oDauer=p2.getDurationInMinutes();
		        if(Beginn < oBeginn)
		  		{	if( (Beginn+Dauer) <= oBeginn )
		  		    	return false;
		  			return true;
		  		}
		  		else if(Beginn > oBeginn)
		  		{	if( (oBeginn+oDauer) <= Beginn)
		  		    	return false;
		  			return true;
		  		}
		  		return true;
		  	}
		  	return false;
	}
	
	/** Feststellen, ob zwei Plannables identisch sind */
	public static boolean isEqual(IPlannable p1, IPlannable p2){
		if(p1.getDay().equals(p2.getDay())){
			if(p1.getStartMinute()==p2.getStartMinute()){
				if(p1.getDurationInMinutes()==p2.getDurationInMinutes()){
					return true;
				}
			}
		}
		return false;
	}
	
	/** Feststellen, ob ein Plannable mit einer Liste von Planables kollidiert */
	public static boolean collides(IPlannable p1, Collection<IPlannable> list){
		for(IPlannable p2:list){
			if(isEqual(p1,p2)){
				continue;
			}
			if(isOverlapped(p1, p2)){
				return true;
			}
		}
	    return false;  
	}

	/** Feststellen, ob eine Zeitspane mit einem Plannable der Liste kollidiert */
	public static boolean collides(TimeSpan ts, Collection<IPlannable> list,Termin exclude) {
		if(list==null){
			return false;
		}
		for(IPlannable p:list){
			if((exclude!=null) && isEqual(p, exclude)){
				continue;
			}
			TimeTool tt=new TimeTool(p.getDay());
			tt.add(TimeTool.MINUTE, p.getStartMinute());
			TimeSpan o=new TimeSpan(tt,p.getDurationInMinutes());
	          if(ts.overlap(o)!=null)
	              return true;
		}
		 return false;
	  }
	/** Feststellen, ob eine Zeitspane mit einem Plannable der Liste kollidiert */
	public static boolean collides(TimeSpan ts, IPlannable[] list,Termin exclude) {
		if(list==null){
			return false;
		}
		for(IPlannable p:list){
			if((exclude!=null) && isEqual(p, exclude)){
				continue;
			}
			TimeTool tt=new TimeTool(p.getDay());
			tt.add(TimeTool.MINUTE, p.getStartMinute());
			
			TimeSpan o=new TimeSpan(tt,p.getDurationInMinutes());
			System.out.println(ts.dump()+" / "+o.dump());
	        if(ts.overlap(o)!=null) {
	             return true;
	        }
		}
		 return false;
	  }
	
	
	 /** Die einem Plannable-Typ zugeordnete Farbe holen */
	  public static Color getTypColor(IPlannable p){
		  String coldesc=Hub.userCfg.get(PreferenceConstants.AG_TYPCOLOR_PREFIX+p.getType(), "FFFFFF");
		  return Desk.getColorFromRGB(coldesc);
		  /*
		  if(p.getType().equals(Termin.typReserviert())){
			  return Desk.theColorRegistry.get("weiss");
		  }else{
			  return Desk.theColorRegistry.get("schwarz");
		  }
		  */
	  }
	  /** Das einem Plannable-Typ zugeordnete Bild holen */
	  public static Image getTypImage(IPlannable p){
		  return getTypImage(p.getType());
	  }
	  /** Das einem Plannable-Titel zugeordnete Bild holen */
	  public static Image getTypImage(String t){
		  String ipath=Hub.userCfg.get(PreferenceConstants.AG_TYPIMAGE_PREFIX+t, null);
		  if(!StringTool.isNothing(ipath)){
			  Image ret=Desk.theImageRegistry.get(ipath);
			  if(ret==null){
				  Desk.theImageRegistry.put(ipath, Activator.getImageDescriptor(ipath));
				  ret=Desk.theImageRegistry.get(ipath);
			  }
			  return ret;
		  }
		  return null;
	  }
	  
	  /** Die einem Plannable-Status zugeordnete Farnbe holen */
	  public static Color getStatusColor(IPlannable p){
		  if(p.getType().equals(Termin.typReserviert())){
			  return Desk.theColorRegistry.get("schwarz");
		  }
		  String coldesc=Hub.userCfg.get(PreferenceConstants.AG_STATCOLOR_PREFIX+p.getStatus(), "000000");
		  return Desk.getColorFromRGB(coldesc);
	  }
	  
	  /** Die Startzeit eines Plannable in hh:mm - Form holen */
	  public static String getStartTimeAsString(IPlannable p){
		  int s=p.getStartMinute();
		  int h=s/60;
		  int m=s%60;
		  StringBuilder sb=new StringBuilder();
		  sb.append(df.format(h)).append(":").append(df.format(m));
		  return sb.toString();
	  }
	  /** Die End-Zeit eines Plannable in hh:mm - Form holen */
	  public static String getEndTimeAsString(IPlannable p){
		  int s=p.getStartMinute()+p.getDurationInMinutes();
		  int h=s/60;
		  int m=s%60;
		  StringBuilder sb=new StringBuilder();
		  sb.append(df.format(h)).append(":").append(df.format(m));
		  return sb.toString();
	  }
	  
	  public static Termin getFollowingTermin(String bereich, TimeTool date, Termin termin){
		  List<IPlannable> list=loadTermine(bereich,date);
		  boolean mark=false;
		  for(IPlannable p:list){
			  if(mark){
				  return (Termin)p;
			  }
			  if(p.getStartMinute()==termin.getStartMinute()){
				  mark=true;
			  }
		  }
		  return null;
	  }
	  /**
	   * Alle Termine eines Tages sortiert einlesen. Freiräume belassen.
	   * @param mandant 
	   * @param date
	   * @return
	   */
	  @SuppressWarnings("unchecked")
	public static List<IPlannable> loadTermine(String bereich, TimeTool date){
		  if(StringTool.isNothing(bereich)){
			  return new ArrayList<IPlannable>();
		  }


		    Query qbe=new Query(Termin.class);
		    String day=date.toString(TimeTool.DATE_COMPACT);
		    qbe.add("Tag","=",day);
		    qbe.and();
		
		    qbe.add("BeiWem", "=", bereich);
		    if(Hub.userCfg.get(PreferenceConstants.AG_SHOWDELETED,"0").equals("0")){
		    	qbe.and();
		    	qbe.add("deleted","=","0");
		    }
		    List list=qbe.execute();
		    if(list==null){
		    	Activator.log.log("Dastenbankstrukturfehler, kann nicht laden (211)",Log.ERRORS);
		    	return new ArrayList<IPlannable>();
		    }
		    if(list.isEmpty()){
		    	Hashtable<String, String>map=getDayPrefFor(bereich);
		    	int d=date.get(Calendar.DAY_OF_WEEK);
		    	String ds =map.get(TimeTool.wdays[d-1]);
		    	if(ds==null){
		    		ds="0000-0800\n1800-2359";
		    	}
		    	String[] flds=ds.split("\r*\n\r*");
		    	for(String fld:flds){
		    		String from=fld.substring(0,4);
		    		String until=fld.replaceAll("-", "").substring(4);
		    		list.add(new Termin(bereich,date.toString(TimeTool.DATE_COMPACT),
		    				TimeTool.getMinutesFromTimeString(from),
		    				TimeTool.getMinutesFromTimeString(until),
		    				Termin.typReserviert(),Termin.statusLeer()));
		    	}
		    	
		    }
		    Collections.sort(list);
		    return list;
	  }
	  /** 
	   * Alle Termine eines Tages sortiert einlesen und in Freiräume zwischen
	   * zwei Terminen jeweils ein Plannable vom Typ Termin.Free einsetzen, so dass
	   * eine lückenlose Liste von Plannables entsteht. 
	   * */
	  public static IPlannable[] loadDay(String bereich, TimeTool date){
		  	ArrayList<IPlannable> e=new ArrayList<IPlannable>(50);
		  	List<IPlannable> list=loadTermine(bereich, date);
		     IPlannable n=null;
			IPlannable last=null;
			String day=date.toString(TimeTool.DATE_COMPACT);
		    for(IPlannable o:(List<IPlannable>)list){
		        n=(IPlannable)o;
		        if(n.getStartMinute()!=0)         // Termin fängt nicht bei 0 Uhr an
		        {   if(last==null){              	// Und es war auch noch keiner vorher
		             								// Dann neuen Anfangstermin einsetzen
		              IPlannable res=new Termin.Free(day,0,n.getStartMinute());
		              e.add(res);
		            }else {                   // Es gibt schon einen vorherigen Termin
		               // Freien Eintrag einsetzen, falls Beginn von diesem nicht unmittelbar nach previous
		              if((last.getStartMinute()+last.getDurationInMinutes())<n.getStartMinute()) // Freiraum 
		              { IPlannable fr=new Termin.Free(day,last.getStartMinute()+last.getDurationInMinutes(),n.getStartMinute());
		                // Prüfen, ob ein früherer Termin mit diesem Freiraum kollidiert
		              	for(IPlannable p:e){
		              		if(Plannables.isOverlapped(p, fr)){
		              	       fr.setStartMinute(p.getStartMinute()+p.getDurationInMinutes());		
		              		}
		              	}
		                // Freiraum nur einhängen, wenn er noch existiert
		                if(fr.getStartMinute()<n.getStartMinute()){
		                    fr.setDurationInMinutes(n.getStartMinute()-fr.getStartMinute());
		                    e.add(fr);  
		                }
		              }
		          }
		        }
		        e.add(n);   // Eingelesenen Termin einsetzen
		        last=n;
		    }
		    if(e.isEmpty())	// Keine Termine gefunden
		    {	Termin.Free ae=new Termin.Free(	// Dann alles frei
		    		day,0,1439);
		        	e.add(ae);
		        	return e.toArray(new IPlannable[0]);
		    }
		        // Letzter Termin reicht nicht ans Tagesende?
		    if(n.getStartMinute()+n.getDurationInMinutes()<1439) // (23*60)+59
		    {	
		        int b=n.getStartMinute()+n.getDurationInMinutes();
		        Termin.Free en=new Termin.Free(day,b,1439-b);
		       	e.add(en);
		    }
		    return e.toArray(new IPlannable[0]);

	  }
	  
	  /** 
	   * EIn Plannable zeichnen
	   * @param gc Der GC, in den das Plannable gezeichnet werden soll
	   * @param p das Plannable
	   * param r Rechteck, in das gezeichnet werden soll
	   * @param times Anfang- und Endzeit des Bereichs, den gc abdeckt
	   */
	  public static void paint(GC gc, IPlannable p, Rectangle r, int start, int end) {
		  double minutes=end-start;
		  double pixelPerMinute=(double)r.width/minutes;
		  int x=(int)Math.round((p.getStartMinute()-start)*pixelPerMinute);
		  int w=(int)Math.round(p.getDurationInMinutes()*pixelPerMinute);
		  gc.setBackground(getTypColor(p));
		  gc.fillRectangle(x, r.y, w, r.height);
	  }
	  
	  public static Hashtable<String,String> getTimePrefFor(String mandantLabel){
		  Hashtable<String,String> map=new Hashtable<String,String>();
		  String mTimes= Hub.globalCfg.get(PreferenceConstants.AG_TIMEPREFERENCES+"/"+mandantLabel, "");
			if(!StringTool.isNothing(mTimes)){
				String[] types=mTimes.split("::");
				for(String t:types){
					String[] line=t.split("=");
					if(line.length!=2){
						Hub.log.log("Fehler in der Terminangabe "+mTimes, Log.WARNINGS);
						continue;
					}
					map.put(line[0],line[1]);
				}
			}
			if(map.get("std")==null){
				map.put("std", "30");
			}
			return map;
	  }
	  public static void setTimePrefFor(String mandantLabel,Hashtable<String,String> map){
		  StringBuilder e=new StringBuilder(200);
		  Enumeration<String> keys=map.keys();
		  while(keys.hasMoreElements()){
			  String k=keys.nextElement();
			  e.append(k).append("=").append(map.get(k));
			  if(keys.hasMoreElements()){
				  e.append("::");
			  }
		  }
		  Hub.globalCfg.set(PreferenceConstants.AG_TIMEPREFERENCES+"/"+mandantLabel, e.toString());
	  }
	  @SuppressWarnings("unchecked")
	public static Hashtable<String,String> getDayPrefFor(String mandantLabel){
		  Hashtable<String,String> map=StringTool.foldStrings(Hub.globalCfg.get(PreferenceConstants.AG_DAYPREFERENCES+"/"+mandantLabel, null));
		  return map==null ? new Hashtable<String,String>() : map;
	  }
	  public static void setDayPrefFor(String mandantLabel, Hashtable<String,String> map){
		  String flat=StringTool.flattenStrings(map);
		  Hub.globalCfg.set(PreferenceConstants.AG_DAYPREFERENCES+"/"+mandantLabel, flat);
	  }
}
