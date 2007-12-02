/**
 * (c) 2007 by G. Weirich
 * All rights reserved
 * $Id: HL7.java 3407 2007-12-02 10:35:11Z rgw_ch $
 */
 

package ch.elexis.importers;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.matchers.KontaktMatcher;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * This class parses a HL7 file containing lab results. It tries to comply with several possible
 * Substandards of the HL7 and to return always reasonable values for each field. 
 * @author Gerry
 *
 */
public class HL7 {
	private  String separator; 
	String labName;
	String labID;
	
	String[] lines;
	Kontakt labor;
	Patient pat;
	
	/**
	 * We can force this hl7 to be attributed to a specific lab (if we know, who the sender should be)
	 * by providing a name and a short name. If we pass null, the lab will be taken out of the file 
	 * (if a sender is provided here) 
	 * @param labor String
	 * @param kuerzel String
	 */
	public HL7(final String labor, final String kuerzel){
		labName=labor;
		labID=kuerzel;
	}
	/**
	 * Load file into memory and break it up to separate lines. All other methods should only
	 * be called after load was successful. To comply with some of the many standards around, we 
	 * accept \n and \r and any combination thereof as field separators
	 * @param filename String
	 * @return
	 */
	public Result<String> load(final String filename){
		File file=new File(filename);
		if(!file.canRead()){
			return new Result<String>(Log.WARNINGS,1,"Kann Datei nicht lesen",filename,true);
		}
		try{
			FileReader fr=new FileReader(file);
			char[] in=new char[(int)file.length()];
			if(fr.read(in)!=in.length){
				return new Result<String>(Log.WARNINGS,3,"EOF",filename,true);
			}
			String hl7raw=new String(in);
			lines=hl7raw.split("[\\r\\n]+");
			separator="\\"+lines[0].substring(3,4);
			fr.close();
			return new Result<String>("OK");
		}catch(Exception ex){
			ExHandler.handle(ex);
			return new Result<String>(Log.ERRORS,2,"Exception beim Lesen",ex.getMessage(),true);
		}
		
	}
	
	/**
	 * find a single HL7-Record
	 * @param header header identifying the desired record
	 * @param start what line to start scanning
	 * @return the first occurence of an element of type 'header' after 'start' lines or an empty
	 *  Element if no such record was found
	 */
	private String[] getElement(final String header, final int start){
		for(int i=start;i<lines.length;i++){
			if(lines[i].startsWith(header)){
				return lines[i].split(separator);
			}
		}
		return new String[0];
	}
	
	/**
	 * This method tries to find the patient denoted by this HL7-record. We try the PID-field PatientID, that is documented
	 * as "PlacerID". But unfortunately not all labs use this field. Thus we try secondly the ORC-field
	 * "Placer Order Number". Id the fields are different, we use the ORC field. The Order number then is
	 * interpreted as a checksummed order number (Patient-Number+modulo10+ -HHmm).
	 * If we cannot find the Patient using this method, we try to find him/her with the name and birthdate.
	 * If we still cannot make an unambiguous identification, we ask the user to tell us, who this lab result
	 * belongs to. If the user can't decide we refuse the import.
	 * 
	 * This mess happens, because the labs interpret the hl7 'standard' differently and inconsistently.
	 * @param createIfNotFound create the patient record in the database if neccessary
	 * @return the Patient or null id it was not found and createIfNotFound was false, or
	 * an error indicating the problem if it could not be created
	 */
	public Result<Patient> getPatient(final boolean createIfNotFound){
		if(pat==null){
			String[] elPid=getElement("PID",0);
			String patid=elPid[2];
			if(StringTool.isNothing(patid)){
				patid=elPid[3];
				if(StringTool.isNothing(patid)){
					patid=elPid[4];
					if(patid==null){
						patid="";
					}
				}
			}
			String[] pidflds=patid.split("[\\^ ]+");
			String pid=pidflds[pidflds.length-1];
			
			String[] orc=getElement("ORC",0);
			if(orc.length>2){
				String orderNumber=orc[2];		// Placer order number
				if(orderNumber.length()>0){
					if(orderNumber.indexOf('-')!=-1){
						pid=StringTool.checkModulo10(orderNumber.split("-")[0]);
					}else{
						pid=orderNumber;
					}
				}
				
			}
			if(pid.indexOf('-')!=-1){
				pid=StringTool.checkModulo10(pid.split("-")[0]);
			}
			// Find a patient with the given ID
			Query<Patient> qbe=new Query<Patient>(Patient.class);
			qbe.add("PatientNr", "=", pid);
			List<Patient> list=qbe.execute();
			String[] name=elPid[5].split("\\^");
			String gebdat=elPid[7];
			String sex=null;
			if(elPid.length>8){
				sex=elPid[8].equalsIgnoreCase("M") ? "m" : "w";
			}
			
			
			if(list.size()==0){
				// We did not find the patient using the PatID, so we try the name and birthdate
				qbe.clear();
				qbe.add("Name", "=", StringTool.normalizeCase(name[0]));
				qbe.add("Vorname", "=", StringTool.normalizeCase(name[1]));
				qbe.add("Geburtsdatum", "=", new TimeTool(gebdat).toString(TimeTool.DATE_COMPACT));
				list=qbe.execute();
				if(list.size()==1){
					pat=list.get(0);	
				}else{
					if(createIfNotFound){
						String address="";
						String phone="";
						if(elPid.length>11){
							address=elPid[11];
							if(elPid.length>13){
								phone=elPid[13];
							}
						}
						pat=new Patient(name[0],name[1],gebdat,sex==null ? StringTool.isFemale(name[1]) ? "w" : "m" : sex);
						pat.set("PatientNr",pid);
						String[] adr=address.split("\\^+");
						Anschrift an=pat.getAnschrift();
						if(adr.length>0){
							an.setStrasse(adr[0]);
							if(adr.length>1){
								an.setOrt(adr[1]);
								if(adr.length>2){
									an.setPlz(adr[2].length()>5 ? adr[2].substring(0, 4) : adr[2]);
									if(adr.length>3){
										an.setLand(adr[3]);
									}
								}
							}
						}
						
						pat.setAnschrift(an);
						pat.set("Telefon1", phone);
					}else{
						pat=(Patient) KontaktSelektor.showInSync(Patient.class, "Patient auswählen", "Wer ist "+name[0]+" "+name[1]+"?");
						if(pat==null){
						//KontaktSelektor ksl=new KontaktSelektor(Hub.getActiveShell(),Patient.class,"Patient auswählen","Wer ist "+name[0]+" "+name[1]+"?");
						//if(ksl.open()==Dialog.OK){
						//	pat=(Patient)ksl.getSelection();
						//}else{
							return new Result<Patient>(Log.WARNINGS,1,"Patient nicht in Datenbank",null,true);
						}
					}
				}
			}else{
				// if the patient with the given ID was found, we verify, if it is the correct name and sex
				pat= list.get(0);
				if(!KontaktMatcher.isSame(pat, name[0], name[1], gebdat)){
					pat=null;
					return new Result<Patient>(Log.WARNINGS,4,"Patient mit dieser ID schon mit anderem Namen vorhanden",null,true);
				}
			}
		}
		return new Result<Patient>(pat);
	}
	
	public Result<String> getUID(){
		String[] msh=getElement("MSH", 0);
		if(msh.length>9){
			return new Result<String>(msh[9]);
		}
		return new Result<String>(Log.ERRORS,1,"Invalid MSH","Error",true);
	}
	/**
	 * Find the lab issuing this file. If we provided a lab name in ze constructor, ths will return that lab.
	 * @return the lab or null if it could not be found
	 */
	public Result<Kontakt> getLabor(){
		if(labor==null){
			if(labName==null){
				if(lines.length>1){
					String[] orc=getElement("ORC", 0);
					if(orc.length>10){
						labName=orc[10];
						if(labID==null){
							labID=orc[10].length()>10 ? orc[10].substring(0,10): orc[10];
						}
					} 
				}
			}
			Query<Labor> qbe=new Query<Labor>(Labor.class);
			qbe.startGroup();
			qbe.add("Kuerzel","LIKE","%"+labName+"%");
			qbe.or();
			qbe.add("Name","LIKE","%"+labName+"%");
			qbe.or();
			qbe.add("Kuerzel","=",labID);
			qbe.endGroup();
			List<Labor> list=qbe.execute();
			if(list.size()!=1){
				labor=new Labor(labName,"Labor "+labName);
			}else{
				labor=list.get(0);
			}
		}
		return new Result<Kontakt>(labor);
	}
	/**
	 * Find the first OBR record in the file
	 * @return an OBR or null if none was found
	 */
	public OBR firstOBR(){
		return new OBR(0).nextOBR(0);
	}

	/**
	 * Find the index of the next Element of a given type 
	 * @param type String
	 * @param prev position to start searching
	 * @return
	 */
	int findNext(final String type,final int prev){
		for(int i=prev;i<lines.length;i++){
			if(lines[i].startsWith(type)){
				return i;
			}
		}
		return -1;
	}
	public class OBR{
		int of;
		String[] field;
		OBR(final int off){
			of=off;
			field=lines[of].split(separator);
		}
		public OBR nextOBR(final OBR obr){
			return nextOBR(obr.of);
		}
		OBR nextOBR(final int of){
			int n=findNext("OBR",of+1);
			if(n==-1){
				return  null;
			}
			return new OBR(n);
		}
		/**
		 * Find the next OBX after a given OBX
		 * @param old the OBX from which to start searching
		 * @return the next OBX or null if none was found
		 */
		public OBX nextOBX(final OBX old){
			return nextOBX(old.of);
		}
		/**
		 * Find the first OBX of this OBR
		 * @return an OBX or null if none found
		 */
		public OBX firstOBX(){
			while(++of<lines.length){
				if(lines[of].startsWith("OBX")){
					return new OBX(this,of);
				}
				if(lines[of].startsWith("OBR")){
					return null;
				}
			}
			return null;
		}
		/**
		 * Find the next OBX after a given position
		 * @param old the position to start looking from
		 * @return the first OBX after 'old' or null if none was found
		 */
		OBX nextOBX(final int old){
			int nf=old+1;
			while(true){
				if(nf>=lines.length){
					return null;
				}
				if(lines[nf].startsWith("OBX")){
					return new OBX(this,nf);
				}
				if(lines[nf].startsWith("OBR")){
					return null;
				}
				nf+=1;
			}
		}
		/**
		 * Unfortunately, not all labs use all date fields. So we try several possible
		 * positions. 
		 * @return the OBR's date (obr[7]). If none was found, it will be the date of today.
		 */
		public TimeTool getDate(){
			String date=field[7];
			if(StringTool.isNothing(date)){
				if(field.length>22){
					date=field[22];
				}else{
					date=field[6];
					if(date.length()==0){
						return new TimeTool();
					}
				}
			}
			TimeTool tt=makeTime(date);
			return tt;
		}
		
	}
	
	public class OBX{
		int of;
		String[] obxFields;
		OBR myOBR;
		
		OBX(final OBR obr, final int off){
			of=off;
			obxFields=lines[of].split(separator);
			myOBR=obr;
		}
		public String getObxNr(){
			return obxFields[1];
		}
		public String getItemCode(){
			return obxFields[3].split("\\^")[0];
		}
		public String getItemName(){
			String raw=getField(3);
			String[] split=raw.split("\\^");
			if(split.length>1){
				return split[1];
			}
			return split[0];
		}
		public String getResultValue(){
			return getField(5);
		}
		public String getUnits(){
			return getField(6);
		}
		public String getRefRange(){
			return getField(7);
		}
		/**
		 * Unfortunately, the date field is not provided by all applications. If
		 * we don't find an OBX date, we use the OBR date.
		 * @return
		 */
		public TimeTool getDate(){
			String tim=getField(14);
			if(tim.length()==0){
				return myOBR.getDate();
			}
			return makeTime(tim);
		}
		/**
		 * This is greatly simplified from the possible values <<, <, >,>>, +, ++, -, -- and so on
		 * we just say "it's pathologic".
		 * @return true if it's any of the pathologic values.
		 */
		public boolean isPathologic(){
			String abnormalFlag=getField(8);
			if(StringTool.isNothing(abnormalFlag)){
				return false;
			}
			return true;
		}
		public boolean isFormattedText(){
			return (obxFields[2].equals("FT"));
		}
		
		/**
		 * Find the comment field of this OBX. Funny enough this is stored outside of the OBX usually.
		 * To make things simpler, we put all comments one after another in the same string (why not?).
		 * @return The comment (that can be an empty String or might contain several NTE records)
		 */
		public String getComment(){
			StringBuilder ret=new StringBuilder();
			for(int i=0;i<lines.length;i++){
				if(lines[i].startsWith("NTE")){
					String[] nte=lines[i].split(separator);
					if(nte.length>1){
						if(nte[1].equals(getObxNr())){
							if(nte.length>3){
								ret.append(nte[3]).append("\n");
							}		
						}
					}
				}
			}
			return ret.toString();
		
		}
		private String getField(final int f){
			if(obxFields.length>f){
				return obxFields[f];
			}
			return "";
		}
	}

	public static TimeTool makeTime(final String datestring){
		String date=datestring.substring(0,8);
		TimeTool ret=new TimeTool();
		if(ret.set(date)){
			return ret;
		}
		return null;
	}
}
