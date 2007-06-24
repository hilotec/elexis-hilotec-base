/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: KontaktImporterBlatt.java 2158 2007-03-22 15:10:35Z rgw_ch $
 *******************************************************************************/

package ch.elexis.importers;

import java.io.FileInputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VCard;

/**
 * A class to import data from different sources  into the contacts
 * To simplify things we request specific formats
 * <ul>
 * <li>A vCard</li>
 * <li><An XML-File with the root element containing fields "IstPerson", "Bezeichnung1",
 * "Bezeichnung2","Geburtsdatum","Geschlecht","E-Mail","Website","Telefon 1","Telefon 2","Strasse","Plz","Ort","Postadresse".
 * All fields are strings. The first one is interpreted boolean where empty or "0" maps to false, all other
 * values map to true. 
 * </li>
 * <li>A Microsoft(tm) Excel(tm) Spreadsheet containing a page 0 with the above fields. Each field must be present but may
 * be empty.</li>
 * <li>A File in CSV format containing the above fields</li>
 * </ul>
 * @author Gerry
 *
 */
public class KontaktImporterBlatt extends Composite{
	String filename;
	Label lbFileName;
	Combo cbMethods;
	int method;
	private Log log=Log.get("KontaktImporter");
	static final String[] methods=new String[]{"XLS","XML","CSV","vCard","KK-Liste"};
	
	public KontaktImporterBlatt(Composite parent){
		super(parent,SWT.NONE);
		setLayout(new GridLayout(2,false));
		new Label(this,SWT.NONE).setText("Dateityp");
		new Label(this,SWT.NONE).setText("Datei");
		cbMethods=new Combo(this,SWT.SINGLE);
		cbMethods.setItems(methods);
		cbMethods.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				method=cbMethods.getSelectionIndex();
			}
		});
		cbMethods.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bLoad=new Button(this,SWT.PUSH);
		
		bLoad.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
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
	}

	public boolean doImport(){
		if(filename.length()>0){
			switch(method){
			case 0: return importExcel(filename);
			case 1: return importXML(filename);
			case 2: return importCSV(filename);
			case 3: return importVCard(filename);
			case 4: return importKK(filename);
			}
		}
		return false;
	}
	public boolean importKK(String file){
		ExcelWrapper exw=new ExcelWrapper();
		exw.load(file, 0);
		List<String> row;
		for(int i=exw.getFirstRow()+1;i<=exw.getLastRow();i++){
			row=exw.getRow(i);
			if(row==null){
				continue;
			}
			if(row.size()!=7){
				continue;
			}
			log.log("Importiere "+StringTool.join(row, " "), Log.INFOS);
			String bagnr=row.get(0);
			String name=row.get(1); 
			String zweig=row.get(2);
			String adresse=row.get(3);
			String typ=row.get(4);
			String EANInsurance=row.get(5);
			String EANReceiver=row.get(6);
			if(typ.equalsIgnoreCase("KVG")){
				Organisation kk=getKK("KK",name,zweig,"TG",adresse);
				if(kk==null){
					return false;
				}
				kk.setInfoElement("EAN", EANInsurance);
				kk.setInfoElement("BAGNr", bagnr);
				kk=getKK("KK",name,zweig,"TP",adresse);
				if(kk==null){
					return false;
				}
				kk.setInfoElement("EAN", EANReceiver);
				kk.setInfoElement("BAGNr", bagnr);
			}else{
				Organisation kk=getKK("UVG",name,zweig,"TP",adresse);
				if(kk==null){
					return false;
				}
				kk.setInfoElement("EAN", EANReceiver);
				kk.setInfoElement("BAGNr", bagnr);
			}
			
		}
		return true;
	}
	private Organisation getKK(String typ,String name, String zweig, String mode, String adresse){
		Query<Organisation> qbe=new Query<Organisation>(Organisation.class);
		if(StringTool.isNothing(name)){
			name=StringTool.getFirstWord(zweig);
			if(StringTool.isNothing(name)){
				return null;
			}
		}
		qbe.add("Name", "=", name);
		qbe.add("Zusatz1", "=", zweig);
		String krz=typ+name.substring(0,3)+mode;
		qbe.add("Kuerzel", "=", krz);
		List<Organisation> list=qbe.execute();
		Organisation ret=null;
		if(list.size()==1){
			ret= list.get(0);
		}else{
			ret= new Organisation(name,zweig);
			ret.set("Kuerzel", krz);
		}
		ret.set("Anschrift", adresse);
		return ret;
	}
	public boolean importExcel(String file){
		ExcelWrapper exw=new ExcelWrapper();
		exw.load(file, 0);
		List<String> row;
		for(int i=exw.getFirstRow();i<=exw.getLastRow();i++){
			row=exw.getRow(i);
			if(row==null){
				continue;
			}
			String typ=row.get(0);
			String bez1=row.get(1);
			String bez2=row.get(2);
			Kontakt k=null;
			if(StringTool.isNothing(typ)|| typ.equals("0")){
				k=new Organisation(bez1,bez2);
			}else{
				k=new Person(bez1,bez2,row.get(3),row.get(4));
			}
			k.set("E-Mail", row.get(5));
			k.set("Website", row.get(6));
			k.set("Telefon1", row.get(7));
			k.set("Telefon2", row.get(8));
			k.set("Strasse", row.get(9));
			k.set("Plz", row.get(10));
			k.set("Ort", row.get(11));
			k.set("Anschrift", row.get(12));
		}
		return true;
		
	}
	public boolean importXML(String file){
		return false;
	}
	public boolean importCSV(String file){
		return false;
	}
	public boolean importVCard(String file){
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
