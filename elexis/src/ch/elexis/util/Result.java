/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Result.java 4307 2008-08-25 05:18:48Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.Hub;


/**
 * Universelles Rückgabe-Objekt. Ein Result beinhaltet die Information, ob
 * ein Fehler erfolgt ist, ggf. den Schweregrad des Fehlers, ein 
 * Rückgabeobjekt (bei Erfolgreicher Ausführung), eine Fehlerbeschreibung bei
 * Fehler. Ein Result kann mehrere Fehlermeldungen aufnehmen (und so durch mehrere Funktionen
 * propagiert werden)
 * Wenn ein Result mehr als ein Resultat enthält, so ist das Gesamtesultat das "schlimmste", also das
 * mit der höchsten severity. Wenn ein Result gar kein Resultat enthält, so ist es "OK".
 * Eine Methode kann entweder ein neues Result-Objekt erzeugen, oder ein übergebenes Resultobjekt um 
 * eine Meldung erweitern.
 * @author Gerry
 *
 */
public class Result<T>{
	List<msg> list=new ArrayList<msg>();
	private int severity=0;
	
	public int getSeverity(){
		return severity;
	}
	/**
	 * Kurze Abfrage, ob alles fehlerfrei war
	 * @return true wenn ja
	 */
	public boolean isOK(){
		if(list.size()>0){
			for(msg m:list){
				if(m.severity!=0){
					return false;
				}
			}
		}
		return true;
	}
	/** 
	 * Den "eigentlichen" Rückgabewert der Methode abholen
	 * @return
	 */
	public T get(){
		msg result=list.size()==0 ? null : list.get(0);
		if(list.size()>1){
			for(msg m:list){
				if(m.severity>result.severity){
					result=m;
				}
			}
		}
		return result==null ? null : result.result;
	}
	
	public int LevelToLogLevel(final Level level){
		switch (level.intValue()){
		case 400:
		case 500:
		case 600: return Log.DEBUGMSG;
		case 900: return Log.WARNINGS;
		case 1000: return Log.ERRORS;
		}
		return Log.TRACE;
	}
	/**
	 * Mapping zwischen logSeverity (Log.ERRORS, Log.INFOS, usw) und
	 * RCP Status Severity (Status.OK, Stauts.INFO, usw)
	 */
	public int getStatusSeverity(final int logSeverity) {
		switch (logSeverity) {
		case Log.NOTHING:
			return Status.OK;
		case Log.FATALS:
			return Status.ERROR;
		case Log.ERRORS:
			return Status.ERROR;
		case Log.INFOS:
			return Status.INFO;
		case Log.DEBUGMSG:
			return Status.INFO;
		case Log.TRACE:
			return Status.INFO;
		}
		return Status.ERROR;
	}
	
	/**
	 * Den Status als Eclipse IStatus bzw. MultiStatus abholen
	 * @return
	 */
	public IStatus asStatus(){
		if(list.size()==0){
			return org.eclipse.core.runtime.Status.OK_STATUS;
		}else if(list.size()==1){
			msg r=list.get(0);
			return new org.eclipse.core.runtime.Status(getStatusSeverity(r.severity),"ch.elexis",r.code,r.text==null ? "?" : r.text, null); //$NON-NLS-1$
		}else{
			ArrayList<IStatus> as=new ArrayList<IStatus>();
			msg r=list.get(0);
			for(msg m:list){
				as.add(new org.eclipse.core.runtime.Status(getStatusSeverity(m.severity),"ch.elexis",m.code,m.text,null)); //$NON-NLS-1$
				if(m.severity>r.severity){
					r=m;
				}
			}
			return new MultiStatus("ch.elexis",r.code,as.toArray(new IStatus[0]),r.text==null ? "?" : r.text,null); //$NON-NLS-1$
		}
	}
	/**
	 * Einen OK - Status abholen
	 * @param result
	 * @return
	 */
	public Result(T result){
		add(Log.NOTHING,0,"Ok",result,false); //$NON-NLS-1$
		//return new Result<Object>(Log.NOTHING,0,"Ok",result,false);
	}
	
	/**
	 * Ein neues Resultat hinzufügen
	 * @param severity
	 * @param code
	 * @param text
	 * @param result
	 * @param log
	 * @return
	 */
	public Result<T> add(int severity, int code, String text, T result, boolean log){
		list.add(new msg(code,text,severity, result));
		if(severity>this.severity){
			this.severity=severity;
		}
		if(log==true){
			Hub.log.log(text,severity);
		}
		return this;
	}
	/**
	 * Ein Result zu einem Result hinzufügen
	 * @param r
	 * @return
	 */
	public Result<T> add(Result<T> r){
		list.addAll(r.list);
		return this;
	}
	public Result(){
	}
	
	public Result(Level severity, int code, String text, T result, boolean log){
		add(LevelToLogLevel(severity),code,text,result,log);
	}
	public Result(int severity,int code, String text, T result, boolean log){
		add(severity,code,text,result,log);
	}
	
	class msg{
		int code;
		String text;
		int severity;
		T result;
		msg(int c,String t, int s, T r){
			code=c;
			text=t;
			severity=s;
			result=r;
		}
	}

	/**
	 * Return the result as String, cr-separated list of entries
	 */
	public String toString() {
		StringBuilder sb=new StringBuilder(200);
		for(msg m:list){
			sb.append(m.text).append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}
	/**
	 * Show an alert displaying the result
	 *
	 */
	public void display(String title){
		SWTHelper.showError(title, toString());
	}
}
