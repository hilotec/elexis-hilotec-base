/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Rechnung.java 2383 2007-05-18 11:51:18Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.*;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

public class Rechnung extends PersistentObject {
    //public static final DecimalFormat geldFormat=new DecimalFormat("0.00");
    // Texte für Trace-Meldungen
    public static final String STATUS_CHANGED="Statusänderung";
    public static final String PAYMENT="Zahlung";
    public static final String CORRECTION="Korrektur";
    public static final String REJECTED="Zurückgewiesen";
    public static final String OUTPUT="Ausgegeben";
       
	static{
		addMapping("RECHNUNGEN","RnNummer","FallID","MandantID",
				"RnDatum=S:D:RnDatum","RnStatus","StatusDatum=S:D:StatusDatum",
				"RnDatumVon=S:D:RnDatumVon","RnDatumBis=S:D:RnDatumBis",
				"Betragx100=Betrag","ExtInfo","Zahlungen=LIST:RechnungsID:ZAHLUNGEN:Datum");
	}
	
	public Rechnung(String nr, Mandant m,Fall f,String von, String bis, Money Betrag, int status){
		create(null);
		String Datum=new TimeTool().toString(TimeTool.DATE_GER);
		set(new String[]{"RnNummer","MandantID","FallID","RnDatumVon","RnDatumBis","Betragx100","RnStatus","RnDatum"},
				nr,m.getId(),f.getId(),von,bis,Betrag.getCentsAsString(),Integer.toString(status),Datum);
		
		new AccountTransaction(f.getPatient(),this,Betrag.multiply(-1.0),Datum,"Rechnung erstellt"); 
	}
	/** 
	 * Eine Rechnung aus einer Behandlungsserie erstellen. Es werde aus dieser Serie nur diejenigen Behandlungen
	 * verwendet, die zum selben Mandanten und zum selben Fall gehören
	 * @return Ein Result mit ggf. der erstellten Rechnung als Inhalt
	 */
    public static Result<Rechnung> build(List<Konsultation> behandlungen){
    	Result<Rechnung> result=new Result<Rechnung>();
        if((behandlungen==null)|| (behandlungen.size()==0)){
            return result.add(Log.INFOS,1,"Die Rechnung enthält keine Behandlungen",null,true);
        }
        Rechnung ret=new Rechnung();
        ret.create(null);
        TimeTool startDate=new TimeTool("31.12.2999");
        TimeTool endDate=new TimeTool("01.01.2000");
        TimeTool actDate=new TimeTool();
        Mandant m=null;
        Kontakt garant=null;
        Kontakt kostentraeger=null;
        List<IDiagnose> diagnosen=null;
        Fall f=null;
        //int summe=0;
        Money summe=new Money();
        for(Konsultation b:behandlungen){
            Mandant bm=b.getMandant();
            if( (bm==null) || (!bm.isValid())){
            	result=result.add(Log.ERRORS,1,"Ungültiger Mandant bei Konsultation "+b.getLabel(),ret,true);
            	continue;
            }
            if(m==null){
                m=bm;
                ret.set("MandantID",m.getId());
            }else{
            	/*
                if(!bm.equals(m)){
                    log.log("Rechnung kann nicht mit Behandlungen verschiedener Mandanten erstellt werden",Log.ERRORS);
                    return null;
                }
                */
            	if(!bm.getId().equals(Hub.actMandant.getId())){
            		result=result.add(Log.ERRORS,2,"Die Liste enthält unterschiedliche Mandanten "+b.getLabel(),ret,true);
            		continue;
            	}
            }
            Fall bf=b.getFall();
            if(bf==null){
            	result=result.add(Log.ERRORS,3,"Fehlender Fall bei Konsultation "+b.getLabel(),ret,true);
            	continue;
            }
            if(f==null){
            	f=bf;
            	ret.set("FallID",f.getId());
            }else{
            	if(!f.getId().equals(bf.getId())){
            		result=result.add(Log.ERRORS,4,"Die Liste enthält unterschiedliche Faelle "+b.getLabel(),ret,true);
            		continue;
            	}
            }

            if( (diagnosen==null) || (diagnosen.size()==0)){
            	diagnosen=b.getDiagnosen();
            }
            if(actDate.set(b.getDatum())==false){
        		result=result.add(Log.ERRORS,5,"Ungültiges Datum bei Konsultation "+b.getLabel(),ret,true);
            	continue;
            }
            if(actDate.isBefore(startDate)){
                startDate.set(actDate);
            }
            if(actDate.isAfter(endDate)){
                endDate.set(actDate);
            }
            List<Verrechnet> lstg=b.getLeistungen();
            
            for(Verrechnet l:lstg){
            	Money sz=l.getEffPreis().multiply(l.getZahl());
                summe.addMoney(sz);
            }
        }
        if(f==null){
        	result=result.add(Log.ERRORS, 8, "Die Rechnung hat keinen gültigen Fall ("+getRnDesc(ret)+")", ret, true);
        	garant=Hub.actMandant;
        }else{
        	garant=f.getGarant();
        	kostentraeger=f.getKostentraeger();
        }
        
        // check if there are any Konsultationen
        if (diagnosen == null || diagnosen.size() == 0) {
        	result=result.add(Log.ERRORS,6,"Die Rechnung enthält keine Diagnose ("+getRnDesc(ret)+")",ret,true);
        }

        if(garant==null || !garant.isValid()){
        	result=result.add(Log.ERRORS,7,"Die Rechnung hat keinen Garanten ("+getRnDesc(ret)+")",ret,true);
        }
        
        /* just take this out for now to allow me making bills
        if(kostentraeger==null || !kostentraeger.isValid()){
        	result=result.add(Log.ERRORS,8,"Die Rechnung hat keinen Kostenträger ("+getRnDesc(ret)+")",ret,true);
        }
        */
        String Datum=new TimeTool().toString(TimeTool.DATE_GER);
        ret.set("RnDatumVon",startDate.toString(TimeTool.DATE_GER));
        ret.set("RnDatumBis",endDate.toString(TimeTool.DATE_GER));
        ret.set("RnDatum",Datum);
        ret.setStatus(RnStatus.OFFEN);
        //summe.roundTo5();
        ret.set("Betragx100", summe.getCentsAsString());
        //ret.setExtInfo("Rundungsdifferenz", summe.getFracAsString());
        String nr=getNextRnNummer();
        ret.set("RnNummer",nr);
        if(!result.isOK()){
        	ret.delete();
        	return result;
        }
        
        for(Konsultation b:behandlungen){
        	b.setRechnung(ret);
        }
        if(ret.getOffenerBetrag().isZero()){
        	ret.setStatus(RnStatus.BEZAHLT);
        }else{
        	new AccountTransaction(f.getPatient(),ret,summe.negate(),Datum,"Rn "+nr+" erstellt.");
        }
        
        return result.add(0,0,"OK",ret,false);
    }

    private static String getRnDesc(Rechnung rn){
    	StringBuilder sb=new StringBuilder();
    	if(rn==null){
    		sb.append("Keine Rechnungsnummer");
    	}else{
        	Fall fall=rn.getFall();
        	sb.append("Rechnung: "+rn.getNr()).append(" / ");
	    	if(fall==null){
	    		sb.append("Kein Fall");
	    	}else{
	    		sb.append("Fall: "+fall.getLabel()).append(" / ");
	    		Patient pat=fall.getPatient();
	    		if(pat==null){
	    			sb.append("Kein Patient");
	    		}else{
	    			sb.append(pat.getLabel());
	    		}
	    	}
    	}
    	return sb.toString();
    }
    /** Die Rechnungsnummer holen */
	public String getNr(){
		return get("RnNummer");
	}
	
	/** Den Fall dieser Rechnung holen */
	public Fall getFall(){
		return Fall.load(get("FallID"));
	}
	/** Den Mandanten zu dieser Rechnung holen */
	public Mandant getMandant(){
		return Mandant.load(get("MandantID"));
	}
	/** Eine Liste aller Konsultationen dieser Rechnung holen */
	public List<Konsultation> getKonsultationen(){
		Query<Konsultation> qbe=new Query<Konsultation>(Konsultation.class);
		qbe.add("RechnungsID","=",getId());
		qbe.orderBy(false, new String[]{"Datum"});
		return qbe.execute();
	}
	/**
	 * Rechnung stornieren. Allenfalls bereits erfolgte Zahlungen für diese Rechnungen bleiben verbucht (das
	 * Konto weist dann einen Plus-Saldo auf).
	 * Der Rechnungsbetrag wird per Stornobuchung gutgeschrieben.
	 * @param reopen wenn True werden die in dieser Rechnung enthaltenen Behandlungen wieder freigegeben, andernfalls
	 * bleiben sie abgeschlossen.
	 */
	public void storno(boolean reopen){
		Money betrag=getBetrag();
		new Zahlung(this,betrag,"Storno");
		if(reopen==true){
			j.exec("UPDATE BEHANDLUNGEN SET RECHNUNGSID=NULL WHERE RECHNUNGSID="+getWrappedId());
		}
		setStatus(RnStatus.STORNIERT);
	}
	/** Datum der Rechnung holen */
	public String getDatumRn(){
		return get("RnDatum");
	}
	/** Datum der ersten Konsultation dieser Rechnung holen */
	public String getDatumVon(){
		return get("RnDatumVon");
	}
	
	/** Datum der letzten Konsultation dieser Rechnung holen */
	public String getDatumBis(){
		String raw=get("RnDatumBis");
		return raw==null ? "" : raw.trim();
	}
	
	/** Totalen Rechnungsbetrag holen */
	public Money getBetrag(){
		int raw=checkZero(get("Betragx100"));
		return new Money(raw);
	}
	
	/**
	 * Since different ouputters can use different rules for rounding, the sum of the
	 * bill that an outputter created might be different from the sum, the Rechnung#build
	 * method calculated. So an outputter should always use setBetrag to correct the final
	 * amount. If the difference between the internal calculated amount and the outputter's
	 * result is more than 1 currency unit or mor than 1% of the sum, this method will return 
	 * false an will not set the new value.
	 * Otherwise, the new value will be set, the account will be adjusted and the method returns true
	 * @param betrag new new sum
	 * @return true on success
	 */
	public boolean setBetrag(Money betrag){
		// use absolute value to fix earlier bug
		
		int oldVal=Math.abs(checkZero(get("Betragx100")));
		if(oldVal!=0){
			int newVal=betrag.getCents();
			int diff=Math.abs(oldVal-newVal);
			
			if( (diff>100) || ((diff*100)>oldVal)){
				Money old=new Money(oldVal);
				String message="Der errechnete Rechnungsbetrag ("+betrag.getAmountAsString()+") weicht vom Rechnungsbetrag ("+old.getAmountAsString()+") ab. Trotzdem weriterfahren?";
				if(MessageDialog.openConfirm(Desk.theDisplay.getActiveShell(), "Differenz bei der Rechnung", message)){
					return true;
				}
				return false;
			}
			Query<AccountTransaction> qa=new Query<AccountTransaction>(AccountTransaction.class);
			qa.add("RechnungsID", "=", getId());
			qa.add("ZahlungsID"	, "", null);
			List<AccountTransaction> as=qa.execute();
			if(as!=null && as.size()==1){
				AccountTransaction at=as.get(0); 
				if(at.exists()){
					Money negBetrag = new Money(betrag);
					negBetrag.negate();
					at.set("Betrag",negBetrag.getCentsAsString());
				}
			}
		}
		set("Betragx100",betrag.getCentsAsString());
		
		return true;
	}
	
	/** Offenen Betrag in Rappen/cents holen */
	public Money getOffenerBetrag(){
		List<Zahlung> lz=getZahlungen();
		//String betr=getBetrag();
		Money total=getBetrag();
		for(Zahlung z:lz){
			Money abzahlung=z.getBetrag();
			total.subtractMoney(abzahlung);
		}
		return new Money(total);
	}
	
	/** Bereits bezahlten Betrag holen. Es werden nur positive Wert (Zahlungen)
	 * addiert, negative Werte (Gebühren) werden übergangen. */
	public Money getAnzahlung(){
		List<Zahlung> lz=getZahlungen();
		Money total=new Money();
		for(Zahlung z:lz){
			Money abzahlung=z.getBetrag();
			if(!abzahlung.isNegative()){
				total.addMoney(abzahlung);
			}
		}
		return total;
	}
	
	/** Rechnungsstatus holen */
	public int getStatus(){
		try {
			int i= Integer.parseInt(checkNull(get("RnStatus")));
			if(i<0 || (i>=RnStatus.Text.length)){
				return RnStatus.UNBEKANNT;
			}
			return i;
		}catch(NumberFormatException e){
			return RnStatus.UNBEKANNT;
		}
	}
	
	/** Rechnungsstatus setzen */
	public void setStatus(int stat){
		set("RnStatus",Integer.toString(stat));
		set("StatusDatum",new TimeTool().toString(TimeTool.DATE_GER));
		addTrace(STATUS_CHANGED,Integer.toString(stat));
	}
	
	/** Eine Zahlung zufügen  */
	public void addZahlung(Money betrag, String text){
		if(betrag.isZero()){
			return;
		}
		Money offen=getOffenerBetrag();
		int cents=offen.getCents();
		offen.subtractMoney(betrag);
		if(offen.isNeglectable()){
			setStatus(RnStatus.BEZAHLT);
		}else if(offen.isNegative()){
			setStatus(RnStatus.ZUVIEL_BEZAHLT);
		}else if(offen.equals(getBetrag())){
			// if the remainder is equal to the total, it was probably a negative payment -> storno
			// So what might be the state of the bill after this payment?
			// It cannot simply be "OFFEN", because the bill was (almost sure) printed already
			// it cannot simply be OFFEN UND GEDRUCKT, beacuse it might have been 3. MAHNUNG GEDRUCKT already. 
			// So probably it's best to use the same state as it was before the last positive payment ?
			// thus let's check the last few states.
			List<String> zahlungen=getTrace(STATUS_CHANGED);
			if(zahlungen.size()<2){
				setStatus(RnStatus.OFFEN_UND_GEDRUCKT);
			}else{
				setStatus(Integer.parseInt(zahlungen.get(zahlungen.size()-2)));
			}
		}
		else if(offen.getCents()<cents){
				setStatus(RnStatus.TEILZAHLUNG);
		}
		new Zahlung(this,betrag,text);
	}
	
	/** EIne Liste aller Zahlungen holen */
	public List<Zahlung> getZahlungen(){
		List<String> ids=getList("Zahlungen",false);
		ArrayList<Zahlung> ret=new ArrayList<Zahlung> ();
		for(String id:ids){
			Zahlung z=Zahlung.load(id);
			ret.add(z);
		}
		return ret;
	}
	public String getBemerkung(){
		return getExtInfo("Bemerkung");
	}
	public void setBemerkung(String bem){
		setExtInfo("Bemerkung",bem);
	}
	
	public String getExtInfo(String key){
		Hashtable<String,String> ext=loadExtension();
		String ret=ext.get(key);
		return ret==null ? "" : ret;
	}
	public void setExtInfo(String key, String value){
		Hashtable<String,String> ext=loadExtension();
		ext.put(key,value);
		flushExtension(ext);
	}
	
	/**
	 * EIn Trace-Eintrag ist eine Notiz über den Verlauf. (Z.B. Statusänderungen, Zahlungen, Rückbuchungen etc.)
	 * @param name Name des Eintragstyps
	 * @param text Text zum Eintrag
	 */
	@SuppressWarnings("unchecked")
	public void addTrace(String name,String text){
		Hashtable hash=loadExtension();
		byte[] raw=(byte[])hash.get(name);
		List<String> trace=null;
		if(raw!=null){
			trace=StringTool.unpack(raw);
		}
		if(trace==null){
			trace=new ArrayList<String>();
		}
		trace.add(new TimeTool().toString(TimeTool.FULL_GER)+": "+text);
		hash.put(name,StringTool.flatten(trace));
		flushExtension(hash);
	}
	/**
	 * ALle Einträge zu einem bestimmten Eintragstyp holen
	 * @param name Name des Eintragstyps (z.B. "Zahlungen")
	 * @return eine List<String>, welche leer sein kann
	 */
	public List<String> getTrace(String name){
		Hashtable hash=loadExtension();
		byte[] raw=(byte[])hash.get(name);
		List<String> trace=null;
		if(raw!=null){
			trace=StringTool.unpack(raw);
		}
		if(trace==null){
			trace=new ArrayList<String>();
		}
		return trace;
	}
	
	public String getRnDatumFrist(){
		String stat=get("StatusDatum");
		int frist=0;
		switch(getStatus()){
		case RnStatus.OFFEN_UND_GEDRUCKT:
			frist=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL1ST, 30);
			break;
		case RnStatus.MAHNUNG_1_GEDRUCKT:
			frist=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL2ND, 10);
			break;
		case RnStatus.MAHNUNG_2_GEDRUCKT:
			frist=Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL3RD, 10);
			break;
		}
		TimeTool tm=new TimeTool(stat);
		tm.add(TimeTool.DAY_OF_MONTH, frist);
		return tm.toString(TimeTool.DATE_GER);
	}
	
	/**
	 * Mark bill as rejected
	 */
	public void reject(RnStatus.REJECTCODE reason, String text){
		setStatus(RnStatus.FEHLERHAFT);
		addTrace(REJECTED,reason.toString()+", "+text);
	}
	
	@SuppressWarnings("unchecked")
	public Hashtable<String,String> loadExtension(){
		return getHashtable("ExtInfo");
	}
	public void flushExtension(Hashtable ext){
		setHashtable("ExtInfo",ext);
	}
	public static Rechnung load(String id){
		Rechnung ret=new Rechnung(id);
		if(ret.exists()){
			return ret;
		}
		return null;
	}
	/** Die nächste Rechnungsnummer holen. */
	public static String getNextRnNummer(){
		Stm stm=j.getStatement();
		String nr=null;
		while(true){
			// Zugriff sperren
			String lockid=PersistentObject.lock("RechnungsNummer",true);
			// letzte Nummer holen
			String pid=j.queryString("SELECT WERT FROM CONFIG WHERE PARAM='RechnungsNr'");
			// ggf. Initialisieren
			if(StringTool.isNothing(pid)){
				pid="0";
				j.exec("INSERT INTO CONFIG (PARAM,WERT) VALUES ('RechnungsNr','0')");
			}
			// hochzählen
			int lastNum=Integer.parseInt(pid)+1;
			nr=Integer.toString(lastNum);
			// neue Höchstzahl speichern
			j.exec("UPDATE CONFIG SET WERT='"+nr+"' WHERE PARAM='RechnungsNr'");
			// Sperre lösen
			PersistentObject.unlock("RechnungsNummer",lockid);
			// Nochmal vergewissern, dass diese Nummer wirklich noch nicht existiert, sonst nächste Nummer holen
			String exists=j.queryString("SELECT ID FROM RECHNUNGEN WHERE RnNummer="+JdbcLink.wrap(nr));
			if(exists==null){
				break;
			}
		}
		j.releaseStatement(stm);
		return nr;
	}
	
	protected Rechnung() { /* leer */}
	protected Rechnung(String id) {
		super(id);
	}

	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		sb.append(getNr()).append(" ").append(getDatumRn());
		Fall fall=getFall();
		if((fall!=null) && fall.exists()){
			sb.append(": ").append(fall.getPatient().getLabel()).append(" ");
		}
		sb.append(getBetrag());
		return sb.toString();
	}

	/**
	 * Eine einfache eindeutige ID für die Rechnung liefern (Aus PatNr. und RnNr)
	 * @return
	 */
	public String getRnId(){
		Patient p=getFall().getPatient();
		String pid;
		if(Hub.globalCfg.get("PatIDMode","number").equals("number")){
			pid=StringTool.pad(SWT.LEFT,'0',p.getPatCode(),6);
		}else{
			pid=new TimeTool(p.getGeburtsdatum()).toString(TimeTool.DATE_COMPACT);
		}
		String nr=StringTool.pad(SWT.LEFT,'0',getNr(),6);
		return pid+nr;
	}
	
	
	@Override
	protected String getTableName() {
		return "RECHNUNGEN";
	}

}
