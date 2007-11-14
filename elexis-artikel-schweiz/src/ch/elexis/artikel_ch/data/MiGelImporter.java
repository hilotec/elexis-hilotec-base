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
 * $Id: MiGelImporter.java 3341 2007-11-14 13:35:48Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_ch.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import au.com.bytecode.opencsv.CSVReader;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class MiGelImporter extends ImporterPage
{
	boolean bDelete=false;
	Button bClear;
	String mode;
	public MiGelImporter(){}
	@Override
	public String getTitle() {
		return "MiGel";
	}
	
	@Override
	public String getDescription(){
		return "Bitte wählen Sie die Datei (CSV- oder Text-Format) aus, aus der die Artikel importiert werden sollen";
	}

	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception {
		mode=" (Modus: Daten ergänzen/update)";
		if(bDelete==true){
			PersistentObject.getConnection().exec("DELETE FROM ARTIKEL WHERE TYP='MiGeL'");
			mode=" (Modus: Alles neu erstellen)";
		}			
		final String line=
			"([0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9]\\.[0-9]) +L? +(.+)  +(.+)  +(.+)  +.+";
		try{
			File file=new File(results[0]);
			long l=file.length();
			monitor.beginTask("MiGeL Import "+mode, (int)l/100);
			if(file.getName().toLowerCase().endsWith("csv")){
				return importCSV(file,monitor);
			}else{
			//long l=file.length();
				InputStreamReader is=new InputStreamReader(new FileInputStream(file),"iso-8859-1");
				BufferedReader br=new BufferedReader(is);
				
				String in;
				monitor.subTask("MiGel - Import");
				Pattern pat=Pattern.compile(line);
				//Query qbe=new Query(MiGelArtikel.class);
				LineFeeder lf=new LineFeeder(br);
				while((in=lf.nextLine())!=null){
					Matcher match=pat.matcher(in);
					if(match.matches()){
						String code=match.group(1);
						String text=match.group(2);
						String unit=match.group(3);
						Money price=new Money(match.group(4));
						/*MiGelArtikel migel=*/ new MiGelArtikel(code,text,unit,price);
					}
				}
				return Status.OK_STATUS;
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return Status.CANCEL_STATUS;
	}

	
	@Override
	public void collect() {
		bDelete=bClear.getSelection();
	}
	@Override
	public Composite createPage(final Composite parent) {
		Composite ret=new ImporterPage.FileBasedImporter(parent,this);
		ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		bClear=new Button(parent,SWT.CHECK|SWT.WRAP);
		bClear.setText("Alle Daten vorher löschen (empfehlenswert)");
		bClear.setSelection(true);
		bClear.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		return ret;

	}
	class LineFeeder{
		static final String codeline="[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9]\\.[0-9].+";
		String prev;
		BufferedReader br;
		LineFeeder(final BufferedReader b) throws Exception{
			br=b;
			prev=br.readLine();
		}
		char peek(){
			return prev.charAt(0);
		}

		String nextl() throws Exception{
			String r;
			while((r=br.readLine())!=null){
				if(r.matches(codeline)){
					break;
				}
			}
			return r;
		}
		String nextLine()throws Exception{
			if(prev==null){
				return null;
			}
			if(!prev.matches(codeline)){
				prev=nextl();
			}
			String ret=prev;
			prev=br.readLine();
			if(prev==null){
				br.close();
				return ret;
			}
			while(!prev.matches(codeline) && !prev.startsWith(" ")){
				if(ret.matches(".*- +[CHIM]?$")){
					ret=ret.replaceFirst("- +[CHIM]?$",prev.trim());					
				}else if(ret.matches(".* +[CHIM]$")){
					ret=ret.replaceFirst("[CHIM]$",prev.trim());
				}else{
					ret+=" "+prev.trim();
				}
				prev=br.readLine();
				if(prev==null){
					br.close();
					return ret;
				}
			}
			return ret;
		}
		boolean atEOF(){
			return prev==null;
		}
		public void close() throws Exception{
			br.close();
		}
	}

	private IStatus importCSV(final File file, final IProgressMonitor monitor) throws FileNotFoundException,IOException{
		CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()));
	    String [] line;
		monitor.subTask("MiGel einlesen");
	    while ((line = reader.readNext()) != null) {
	    	if(line.length<3){
	    		continue;
	    	}
	    	Money betrag;
			try{
				betrag=new Money(Double.parseDouble(line[3]));
			}catch(Exception ex){
				betrag=new Money();
			}
			new MiGelArtikel(line[0],line[1],line[2],betrag);
			monitor.worked(1);
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}
