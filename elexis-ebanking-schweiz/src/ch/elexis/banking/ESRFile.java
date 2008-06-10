/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ESRFile.java 4018 2008-06-10 16:05:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.data.Query;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.ExHandler;

/**
 * Ein ESRFile ist eine Datei, wie sie von der Bank heruntergeladen werden kann, um
 * VESR-Records zu verbuchen
 * @author gerry
 *
 */
public class ESRFile {
	List<ESRRecord> list=new ArrayList<ESRRecord>();
	String name;
	/**
	 * ein ESR-File einlesen
	 * @param filename vollständiger Pfadname der Datei
	 * @return true wenn die Datei erfolgreich gelesen werden konnte
	 */
	public Result<List<ESRRecord>> read(File file, final IProgressMonitor monitor){
		
		if(!file.exists()){
			return new Result<List<ESRRecord>>(Log.ERRORS,1,"Die Angegebene ESR-Datei wurde nicht gefunden",null,true);
		}
		if(!file.canRead()){
			return new Result<List<ESRRecord>>(Log.ERRORS,2,"Kann ESR-Datei nicht lesen",null,true);
		}
		name=file.getName();
		Query<ESRRecord> qesr=new Query<ESRRecord>(ESRRecord.class);
		qesr.add("File", "=", name);
		List<ESRRecord> list=qesr.execute();
		if(list.size()>0){
			return new Result<List<ESRRecord>>(Log.ERRORS,4,"Diese ESR-Datei wurde bereits eingelesen",null,true);
		}
		try{
			InputStreamReader ir=new InputStreamReader(new FileInputStream(file));
			BufferedReader br=new BufferedReader(ir);
			String in;
			//String date=new TimeTool().toString(TimeTool.DATE_COMPACT);
			LinkedList<String> records=new LinkedList<String>();
			while((in=br.readLine())!=null){
				for(int i=0;i<in.length();i+=128){
					int eidx=i+125;
					if(eidx>=in.length()){
						eidx=in.length()-1;
					}
					records.add(in.substring(i, eidx));
				}
			}
			for(String s:records){
				ESRRecord esr=new ESRRecord(name,s);
				list.add(esr);
				monitor.worked(1);
			}
			return new Result<List<ESRRecord>>(0,0,"OK",list,false);
			
		}catch(Exception ex){
			ExHandler.handle(ex);
			return new Result<List<ESRRecord>>(Log.ERRORS,3,"Exception while parsing",list,true);
		}
		
	}

	public List<ESRRecord> getLastResult(){
		return list;
	}
	
	public String getFilename(){
		return name;
	}
}
