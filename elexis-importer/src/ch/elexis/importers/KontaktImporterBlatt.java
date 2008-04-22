/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: KontaktImporterBlatt.java 3834 2008-04-22 15:43:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.importers;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.elexis.matchers.KontaktMatcher;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.BinConverter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VCard;

/**
 * A class to import data from different sources  into the contacts
 * To simplify things we request specific formats
 * <ul>
 * <li>A Microsoft(tm) Excel(tm) 97(tm) Spreadsheet(tm) containing a page 0 with the following fields:<br/> 
 *  "ID","IstPerson", "Titel", "Bezeichnung1",
 * "Bezeichnung2","Zusatz", "Geburtsdatum","Geschlecht","E-Mail","Website","Telefon 1","Telefon 2",
 * "Mobil","Strasse","Plz","Ort","Postadresse","EAN".
 * All fields are strings. The field istPerson one is interpreted boolean where empty or "0" maps to false, all other
 * values map to true. 
 * Each field must be present but may be empty.</li>
 * <li>A File in CSV format containing the above fields</li>
 * <li>some preset files</li>
 * </ul>
 * @author Gerry
 *
 */
public class KontaktImporterBlatt extends Composite{
	String filename;
	Label lbFileName;
	Combo cbMethods;
	boolean bKeepID;
	int method;
	private final Log log=Log.get("KontaktImporter");
	static final String[] methods=new String[]{"XLS","CSV","KK-Liste"};
	private static final String PRESET_RUSSI="e3ad14dc49e27dbcc4771b41b34cdd902f9cfcc6";
	private static final String PRESET_UNIVERSAL="be99f1d4a3feae5e5eb84fae8ccddeee9582df8d";
	private static final String PRESET_HERTEL="a4a9f3bd410443399ee05d5e033d94513a64239b";
	
	
	public KontaktImporterBlatt(final Composite parent){
		super(parent,SWT.NONE);
		setLayout(new GridLayout(2,false));
		new Label(this,SWT.NONE).setText("Dateityp");
		new Label(this,SWT.NONE).setText("Datei");
		cbMethods=new Combo(this,SWT.SINGLE);
		cbMethods.setItems(methods);
		cbMethods.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				method=cbMethods.getSelectionIndex();
			}
		});
		cbMethods.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bLoad=new Button(this,SWT.PUSH);
		
		bLoad.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e){
				FileDialog fd=new FileDialog(getShell(),SWT.OPEN);
				String file=fd.open();
				lbFileName.setText(file==null ? "" : file);
				filename=lbFileName.getText();
			}
		});
		bLoad.setText("Datei wählen");
		lbFileName=new Label(this,SWT.NONE);
		bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbFileName.setText("Bitte Dateityp und Datei wählen");
		lbFileName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		final Button bKeep=new Button(this,SWT.CHECK);
		bKeep.setText("ID beibehalten (Achtung: Bitte Handbuch beachten)");
		bKeep.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		bKeep.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				bKeepID=bKeep.getSelection();
			}
			
		});
	}

	public boolean doImport(final IProgressMonitor moni){
		if(filename!=null && filename.length()>0){
			switch(method){
			case 0: return importExcel(filename,moni);
			case 1: return importCSV(filename);
			case 2: return importKK(filename);
			}
		}
		return false;
	}
	public boolean importKK(final String file){
		ExcelWrapper exw=new ExcelWrapper();
		exw.setFieldTypes(new Class[]{Integer.class,String.class,String.class,String.class,
				String.class,Integer.class,Integer.class});
		exw.load(file, 0);
		
		String[] row;
		for(int i=exw.getFirstRow()+1;i<=exw.getLastRow();i++){
			row=exw.getRow(i).toArray(new String[0]);
			if(row==null){
				continue;
			}
			if(row.length!=7){
				continue;
			}
			log.log("Importiere "+StringTool.join(row, " "), Log.INFOS);
			String bagnr=StringTool.getSafe(row,0);
			String name=StringTool.getSafe(row,1); 
			String zweig=StringTool.getSafe(row,2);
			String adresse=StringTool.getSafe(row,3);
			String typ=StringTool.getSafe(row,4);
			String EANInsurance=StringTool.getSafe(row,5);
			String EANReceiver=StringTool.getSafe(row,6);
			String[] adr=splitAdress(adresse);
			Organisation kk=KontaktMatcher.findOrganisation(name, adr[0], adr[1], adr[2], true);
			if(kk==null){
				return false;
			}
			kk.setInfoElement("EAN", EANInsurance);
			kk.setInfoElement("BAGNr", bagnr);
			kk.set("Bezeichnung2", zweig);
			kk.set("Kuerzel",StringTool.limitLength("KK"+StringTool.getFirstWord(name),39));
		}
		return true;
	}
	String[] splitAdress(final String adr){
		String[] ret=new String[3];
		String[] m1=adr.split("\\s*,\\s*");
		String[] plzOrt=m1[m1.length-1].split(" ",2);
		if(m1.length==1){
			ret[0]="";
		
		}else{
			ret[0]=m1[0];
		}
		ret[1]=plzOrt[0];
		ret[2]=plzOrt.length>1 ? plzOrt[1] : "";
		return ret;
	}
	
	public boolean importExcel(final String file, final IProgressMonitor moni){
		ExcelWrapper exw=new ExcelWrapper();
		exw.load(file, 0);
		List<String> row=exw.getRow(exw.getFirstRow());		// we load the first row to figure out whether we know the format
		try{
			MessageDigest digest=MessageDigest.getInstance("SHA1");
			for(String field:row){
				digest.update(field.getBytes("iso-8859-1"));
			}
			byte[] dg=digest.digest();
			String vgl=BinConverter.bytesToHexStr(dg);
			
			if(vgl.equals(PRESET_RUSSI)){
				return Presets.importRussi(exw,bKeepID,moni);
			}else if(vgl.equals(PRESET_UNIVERSAL)){
				return Presets.importUniversal(exw, bKeepID,moni);
			}else if(vgl.equals(PRESET_HERTEL)){
				return Presets.importHertel(exw, bKeepID,moni);
			}else{
				SWTHelper.showError("Datatype error", "Unbekannter Datentyp", "Die Feldnamen dieses Files sind nicht bekannt");
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		
		return false;
		
	}
	public boolean importXML(final String file){
		return false;
	}
	public boolean importCSV(final String file){
		return false;
	}
	public boolean importVCard(final String file){
		try{
			VCard vcard=new VCard(new FileInputStream(file));
			String name,vorname,tel,email,title;
			String gebdat="";
			String strasse="";
			String plz="";
			String ort="";
			String fqname=vcard.getElement("N");
			if(fqname==null){
				return false;
			}
			String[] names=vcard.getValue(fqname).split(";");
			email=vcard.getElementValue("EMAIL");
			String address=vcard.getElementValue("ADR");
			title=vcard.getElementValue("TITLE");
			tel=vcard.getElementValue("TEL");
			if(address!=null){
				String[] adr=address.split(";");
				strasse=adr[2];
				plz=adr[5];
				ort=adr[3];
			}
			name=names[0];
			vorname=names[1];
			Kontakt k=KontaktImporter.queryKontakt(name, vorname, strasse, plz, ort, false);
			if(k==null){
				k=new Person(name,vorname,gebdat,"m");
				k.set("Title", title);
			}
			return true;
		}catch(Exception ex){
			
		}
		return false;
	}
}
