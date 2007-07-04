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
 *  $Id: Konsultation.java 2701 2007-07-04 17:12:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.text.Samdas;
import ch.elexis.util.*;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Eine Konsultation ist ein einzelner Mandant/Patient-Kontakt. Eine Konsultation
 * gehört immer zu einem Fall und zu einem Mandanten, und hat ein bestimmtes
 * Datum. Eine Konsultation kann eine oder mehrere der Fall-Diagnosen betreffen.
 * Eine Konsultation enthält ausserdem auch einen Behandlungstext, und nicht zuletzt
 * auch einen Verrechnungs-Set. Eine Konsultation kann nicht mehr geändert werden, wenn sie
 * geschlossen ist
 * 
 * @author gerry
 */
public class Konsultation extends PersistentObject implements Comparable{
	volatile int actEntry;
    protected String getTableName() {
		return "BEHANDLUNGEN";
	}
	static{
		addMapping("BEHANDLUNGEN",
				"MandantID","Datum=S:D:Datum","FallID","RechnungsID","Eintrag=S:V:Eintrag",
				"Diagnosen=JOINT:BehandlungsID:DiagnoseID:BEHDL_DG_JOINT"
				);
	}
	
	protected Konsultation(String id){
		super(id);
        
	}
    /**
     * Prüfen, ob diese Konsultation gültig ist. Dies ist dann der Fall, wenn
     * sie in der Datenbank existiert und wenn sie einen zugeordneten Mandanten
     * und einen zugeordeten Fall hat.
     */
	public boolean isValid(){
		if(!super.isValid()){
			return false;
		}
		Mandant m=getMandant();
		if((m==null) || (!m.isValid())){
			return false;
		}
		Fall fall=getFall();
		if((fall==null) || (!fall.isValid())){
			return false;
		}
		
		return true;
	}
    /** Den zugehörigen Fall holen */
	public Fall getFall() {
		return Fall.load(get("FallID"));
	}
	/** Die Konsultation einem Fall zuordnen */
	public void setFall(Fall f){
		if(isEditable(true)){
			Fall alt=getFall();
			set("FallID",f.getId());
			if(alt!=null){
				List<Verrechnet> vv=getLeistungen();
				for(Verrechnet v:vv){
					v.setStandardPreis();
				}
			}
		}
	}
    /** Eine neue Konsultation zu einem Fall erstellen */
	Konsultation(Fall fall){
		if(fall==null){
			MessageDialog.openError(null,"Kein Fall ausgewählt","Bitte zunächst einen Fall auswählen, dem die neue Konsultation zugeordnet werden soll");
		}
		if(fall.isOpen()==false){
			MessageDialog.openError(null,"Fall geschlossen","Zu einem abgeschlossenen Fall kann keine neue Konsultation erstellt werden");
		}else{
			create(null);
			set(new String[]{"Datum","FallID","MandantID"},
					new TimeTool().toString(TimeTool.DATE_GER),
					fall.getId(),
					Hub.actMandant.getId());
			fall.getPatient().setInfoElement("LetzteBehandlung",getId());
		}
	}
    /** Eine Konsultation anhand ihrer ID von der Datenbank einlesen */
	public static Konsultation load(String id){
		Konsultation ret=new Konsultation(id);
		if(ret.exists()){
			return ret;
		}
		return null;
	}
	public int getHeadVersion(){
	    VersionedResource vr=getVersionedResource("Eintrag",false);
        return vr.getHeadVersion();
    }
    public VersionedResource getEintrag(){
        VersionedResource vr=getVersionedResource("Eintrag",true);
        return vr;
    }
    
    /**
     * Insert an XREF to the EMR text
     * @param provider unique String identifying the provider
     * @param id String identifying the item
     * @param pos position of the item as offset relative to the contents
     * @param text text to insert
     */
    public void addXRef(String provider, String id, int pos, String text){
    	VersionedResource vr=getEintrag();
    	String ntext=vr.getHead();
    	Samdas samdas=new Samdas(ntext);
    	Samdas.Record record=samdas.getRecord();
    	String recText=record.getText();
    	if((pos==-1)|| pos>recText.length()){
    		pos=recText.length();
    		recText+=text;
    	}else{
    		recText=recText.substring(0,pos)+text+recText.substring(pos);
    	}
    	record.setText(recText);
    	Samdas.XRef xref=new Samdas.XRef(provider,id,pos,text.length());
    	record.add(xref);
    	updateEintrag(samdas.toString(),true); // XRefs my always be added
    }
    
    /**
     * Remove an XREF from the EMR text. Will remove all XREFS of the given provider with the given ID from
     * this EMR. Warning: The IKonsExtension's removeXRef method will not be called.
     * @param provider unique provider id
     * @param id item ID
     */
    public void removeXRef(String provider, String id){
    	VersionedResource vr=getEintrag();
    	String ntext=vr.getHead();
    	Samdas samdas=new Samdas(ntext);
    	Samdas.Record record=samdas.getRecord();
    	String recText=record.getText();
    	List<Samdas.XRef> xrefs=record.getXrefs();
    	boolean changed=false;
		for(Samdas.XRef xref:xrefs){
			if((xref.getProvider().equals(provider)) &&
			  (xref.getID().equals(id))){
				recText=recText.substring(0, xref.getPos())+recText.substring(xref.getPos()+xref.getLength());
				record.setText(recText);
				record.remove(xref);
				changed=true;
			}
			
		}
		if(changed){
			updateEintrag(samdas.toString(),true);
		}
		
    }
    
    private boolean isEintragEditable() {
    	boolean editable = false;
    	boolean hasRight = Hub.acl.request(AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED);
    	if (hasRight) {
    		// user has right to change Konsultation. in this case, the user
    		// may change the text even if the Konsultation has already been
    		// billed, so don't check if it is billed
    		editable = isEditable(true, false, true);
    	} else {
    		// normal case, check all
    		editable = isEditable(true, true, true);
    	}
    	
    	return editable;
    }
    
    /** 
     * Den Eintrag eintragen. Da es sich um eine VersionedResource
     * handelt, wird nicht der alte Eintrag gelöscht, sondern der
     * neue wird angehängt.
     * @param force bei true wird der Eintrag auch dann geändert, wenn die Konsultation eigentlich
	 * nicht änderbar ist.
     */
	public void setEintrag(VersionedResource eintrag, boolean force){
		if(force || isEintragEditable()){
			setVersionedResource("Eintrag",eintrag.getHead());
		}
	}
	/**
	 * Eine Änderung des Eintrags hinzufügen (der alte Eintrag wird nicht überschrieben)
	 * @param force bei true wird der Eintrag auch dann geändert, wenn die Konsultation eigentlich
	 * nicht änderbar ist.
	 */
    public void updateEintrag(String eintrag, boolean force){
    	if(force || isEintragEditable()){
    		setVersionedResource("Eintrag",eintrag);
    		GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.update);
    	}
    }
    public void purgeEintrag(){
    	VersionedResource vr=getEintrag();
    	vr.purge();
    	setBinary("Eintrag",vr.serialize());
    }
    /** Den zugeordneten Mandanten holen */
	public Mandant getMandant(){
		return Mandant.load(get("MandantID"));
	}
    /** Die Konsultation einem Mandanten zuordnen */
	public void setMandant(Mandant m){
			set("MandantID",m.getId());
	}
	
	/** Das Behandlungsdatum setzen 
	 * @param force TODO*/
	public void setDatum(String dat, boolean force){
		if(force || isEditable(true)){
			set("Datum",dat);
		}
	}
	
	/** das Behandlungsdatum auslesen */
	public String getDatum(){
		return  get("Datum");
	}
	public Rechnung getRechnung(){
		return Rechnung.load(get("RechnungsID"));
	}
	public void setRechnung(Rechnung r){
		set("RechnungsID",r.getId());
	}

	/**
	 * Checks if the Konsultation can be altered.
	 * This method is internally used.
	 * @param checkMandant checks whether the current mandant is the owner of this Konsultation
	 * @param checkBill checks whether the Konsultation has already been billed
	 * @param showError if true, show error messages
	 * @return true if the Konsultation can be altered in repsect to the given checks, else otherwise.
	 */
	private boolean isEditable(boolean checkMandant, boolean checkBill, boolean showError) {
		Mandant m = getMandant();

		boolean mandantOK = true;
		boolean billOK = true;

		// if m is null, ignore checks (return true)
		if (m != null) {
			if (checkMandant && !(m.getId().equals(Hub.actMandant.getId()))) {
				mandantOK = false;
			}

			// TODO use getRechnung().exists()
			if (checkBill && getRechnung() != null) {
				billOK = false;
			}
		}
		
		boolean ok = billOK && mandantOK;
		if (ok) {
			return true;
		}
	
		// something is not ok
		if (showError) {
			String msg = "";
			if (!billOK) {
				msg = "Für diese Behandlung wurde bereits eine Rechnung erstellt.";
			} else {
				msg = "Diese Behandlung ist nicht von Ihnen";
			}
			Status status = new Status(Status.WARNING, "ch.elexis", 1, msg, null);
			ErrorDialog.openError(Desk.theDisplay.getActiveShell(), "Konsultation kann nicht geändert werden", msg, status);
		}
		
		return false;
	}
	
	/**
	 * Checks if the Konsultation can be altered.
	 * @param showError if true, show error messages
	 * @return true if the Konsultation can be altered, else otherwise.
	 */
	public boolean isEditable(boolean showError) {
		// check mandant and bill
		return isEditable(true, true, showError);
	}
	
    public int getStatus(){
    	Rechnung r=getRechnung();
    	if(r!=null){
    		return r.getStatus();
    	}
    	Mandant rm=getMandant();
        if((rm!=null) && (rm.equals(Hub.actMandant))){
            if(getDatum().equals(new TimeTool().toString(TimeTool.DATE_GER))){
                return RnStatus.VON_HEUTE;
            }else{
                return RnStatus.NICHT_VON_HEUTE;
            }
        }else{
            return RnStatus.NICHT_VON_IHNEN;
        }
    }
 
    public String getStatusText(){
    	return RnStatus.Text[getStatus()];
    }
   
	/** Eine einzeilige Beschreibung dieser Konsultation holen */
    public String getLabel(){
        StringBuffer ret=new StringBuffer();
        Mandant m=getMandant();
        ret.append(getDatum()).append(" (")
            .append(getStatusText()).append(") - ")
            .append((m==null) ? "?" : m.getLabel());
        return ret.toString();
    }
   
    public String getVerboseLabel(){
        StringBuilder ret =new StringBuilder();
        ret.append(getFall().getPatient().getName()).append(" ")
            .append(getFall().getPatient().getVorname()).append(", ")
            .append(getFall().getPatient().getGeburtsdatum()).append(" - ")
            .append(getDatum());
        return ret.toString();
    }
    /** Eine Liste der Diagnosen zu dieser Konsultation holen */ 
    public ArrayList<IDiagnose> getDiagnosen(){
        ArrayList<IDiagnose> ret=new ArrayList<IDiagnose>();
    	Stm stm=j.getStatement();
    	ResultSet rs1=stm.query("SELECT DIAGNOSEID FROM BEHDL_DG_JOINT WHERE BEHANDLUNGSID="+JdbcLink.wrap(getId()));
        StringBuilder sb=new StringBuilder(); 
    	try{
    		while(rs1.next()==true){
    			String dgID=rs1.getString(1);
    			
    			Stm stm2=j.getStatement();
    			ResultSet rs2=stm2.query("SELECT DG_CODE,KLASSE FROM DIAGNOSEN WHERE ID="+JdbcLink.wrap(dgID));
    			if(rs2.next()){
                    sb.setLength(0);
                    sb.append(rs2.getString(2)).append("::");
    				sb.append(rs2.getString(1));
                    try{
                        PersistentObject dg=Hub.poFactory.createFromString(sb.toString());
                        if(dg!=null){
                        	ret.add((IDiagnose)dg);
                        }
                    }catch(Exception ex){
                        log.log("Fehlerhafter Diagnosecode "+sb.toString(),Log.ERRORS);
                    }
    			}
    			rs2.close();
    			j.releaseStatement(stm2);
    		}
    		rs1.close();
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		log.log(ex.getMessage(),Log.ERRORS);
    	}
    	finally{
    		j.releaseStatement(stm);
    	}
    	return ret;
    }
    
    /** Eine weitere Diagnose dieser Konsultation zufügen */
    public void addDiagnose(IDiagnose dg){
    	if(!isEditable(true)){
    		return;
    	}
        String exists=j.queryString("SELECT ID FROM DIAGNOSEN WHERE KLASSE="+JdbcLink.wrap(dg.getClass().getName())+" AND DG_CODE="+JdbcLink.wrap(dg.getCode()));
        StringBuilder sql=new StringBuilder(200);
        if(StringTool.isNothing(exists)){
            exists=StringTool.unique("bhdl");
            sql.append("INSERT INTO DIAGNOSEN (ID, DG_CODE, DG_TXT, KLASSE) VALUES (")
               .append(JdbcLink.wrap(exists)).append(",")
               .append(JdbcLink.wrap(dg.getCode())).append(",")
               .append(JdbcLink.wrap(dg.getText())).append(",")
               .append(JdbcLink.wrap(dg.getClass().getName()))
               .append(")");
            j.exec(sql.toString());
            sql.setLength(0);
        }
        sql.append("INSERT INTO BEHDL_DG_JOINT (ID,BEHANDLUNGSID,DIAGNOSEID) VALUES (")
           .append(JdbcLink.wrap(StringTool.unique("bhdx"))).append(",")
           .append(getWrappedId()).append(",")
           .append(JdbcLink.wrap(exists)).append(")");
        j.exec(sql.toString());
        
        // Statistik nachführen
    	getFall().getPatient().countItem(dg);
    	Hub.actUser.countItem(dg);

    }
        
    /** Eine Diagnose aus der Diagnoseliste entfernen */
    public void removeDiagnose(IDiagnose dg) {
    	if(isEditable(true)){
	    	StringBuilder sql=new StringBuilder();
	    	sql.append("SELECT ID FROM DIAGNOSEN WHERE DG_CODE=")
	    	.append(JdbcLink.wrap(dg.getCode())).append(" AND ")
	    	.append("KLASSE=").append(JdbcLink.wrap(dg.getClass().getName()));
	    	String dgid=j.queryString(sql.toString());
	    	
	    	sql.setLength(0);
	    	sql.append("DELETE FROM BEHDL_DG_JOINT WHERE BEHANDLUNGSID=")
	    		.append(getWrappedId()).append(" AND ")
	    		.append("DIAGNOSEID=").append(JdbcLink.wrap(dgid));
	    	j.exec(sql.toString());
    	}	
    }
        
    /** Die zu dieser Konsultation gehörenden Leistungen holen */
    @SuppressWarnings("unchecked")
	public List<Verrechnet> getLeistungen(){
    	Query qbe=new Query(Verrechnet.class);
    	qbe.add("Konsultation","=",getId());
    	List ret=qbe.execute();
    	return (List<Verrechnet>)ret;
    }
    
    /**
     * Eine Verrechenbar aus der Konsultation entfernen
     * @param ls die Verrechenbar
     * @return Ein Optifier- Resultat
     */

	public Result removeLeistung(Verrechnet ls)
    {
    	if(isEditable(true)){
    		IVerrechenbar v=ls.getVerrechenbar();
    		int z=ls.getZahl();
	        Result result=v.getOptifier().remove(ls,this);
	        if(result.isOK()){
	            if(v instanceof Artikel){
	        		Artikel art=(Artikel)v;
	        		art.einzelRuecknahme(z);
	        	}
	        }
	        return result;
    	}
        return new Result<Verrechnet>(Log.WARNINGS,3,"Behandlung geschlossen oder nicht von Ihnen",null,false);
    }
    
    /** Eine Verrechenbar zu dieser Konsultation zufügen 
     *	@return ein Verifier-Resultat. 
     * */
	public Result addLeistung(IVerrechenbar l){
    	if(isEditable(false)){
	    	Result result=l.getOptifier().add(l,this);
	    	if(result.isOK()){
	        	// Statistik nachführen
	        	getFall().getPatient().countItem(l);
	        	Hub.actUser.countItem(l);
	        	if(l instanceof Artikel){
	        		Artikel art=(Artikel)l;
	        		art.einzelAbgabe(1);
	        		Prescription p=new Prescription(art,getFall().getPatient(),"","");
	        		p.set("RezeptID","Direktabgabe");
	        	}
	    	}
	    	return result;
    	}
    	return new Result<IVerrechenbar>(Log.WARNINGS,2,"Behandlung geschlossen oder nicht von Ihnen",null,false);
    }
    /** Wieviel hat uns diese Konsultation gekostet? */
    public int getKosten(){
    	int sum=0;
    	/*
    	TimeTool mine=new TimeTool(getDatum());
    	List<Verrechenbar> l=getLeistungen();
    	for(Verrechenbar v:l){
    		sum+=(v.getZahl()*v.getKosten(mine));
    	}
    	*/
    	Stm stm=j.getStatement();
    	try{
	    	ResultSet res=stm.query("SELECT EK_KOSTEN FROM LEISTUNGEN WHERE BEHANDLUNG="+getWrappedId());
	    	while((res!=null) && res.next()){
	    		sum+=res.getInt(1);
	    	}
	    }
    	catch(Exception ex){
    		ExHandler.handle(ex);
    		return 0;
    	}finally{
    		j.releaseStatement(stm);
    	}
    	return sum;
    	
    }
    /** Wieviel Zeit können wir für diese Konsultation anrechnen? */
    public int getMinutes() {
    	int sum=0;
    	List<Verrechnet> l=getLeistungen();
    	for(Verrechnet v:l){
    		sum+=(v.getZahl()*v.getVerrechenbar().getMinutes());
    	}
    	return sum;
    
	}
    /** Wieviel Umsatz (in Rappen) bringt uns diese Konsultation ein? */
    public double getUmsatz(){
    	double sum=0.0;
    	Stm stm=j.getStatement();
    	try{
	    	ResultSet res=stm.query("SELECT VK_PREIS,ZAHL,SCALE FROM LEISTUNGEN WHERE BEHANDLUNG="+getWrappedId());
	    	while((res!=null) && res.next()){
	    		double scale=res.getDouble(3)/100.0;
	    		sum+=(res.getDouble(1)*res.getDouble(2))*scale;
	    	}
	    }
    	catch(Exception ex){
    		ExHandler.handle(ex);
    		return 0;
    	}finally{
    		j.releaseStatement(stm);
    	}
    	return sum;

    }
    
    /** Wieviel vom Umsatz bleibt uns von dieser Konsultation? */
    public double getGewinn(){
    	return getUmsatz()-getKosten();
    }
    
   
    public void changeScale(IVerrechenbar v, int scale){
    	if(isEditable(true)){
	    	StringBuilder sb=new StringBuilder();
	    	sb.append("UPDATE LEISTUNGEN SET SCALE='")
	    		.append(scale).append("' WHERE BEHANDLUNG=")
	    		.append(getWrappedId()) /*.append(" AND ")
	    		.append("KLASSE=").append(JdbcLink.wrap(v.getClass().getName())) */
	    			.append(" AND LEISTG_CODE=").append(JdbcLink.wrap(v.getId()));
	    	
	    	j.exec(sb.toString());
    	}
    }
    /** Zahl einer Leistung ändern */
    public void changeZahl(IVerrechenbar v, int nz){
    	if(isEditable(true)){
	    	StringBuilder sql=new StringBuilder();
			sql.append("UPDATE LEISTUNGEN SET ZAHL=").append(nz)
				/*.append(" WHERE KLASSE=").append(JdbcLink.wrap(v.getClass().getName())) */
				.append(" WHERE LEISTG_CODE=").append(JdbcLink.wrap(v.getId()))
				.append(" AND BEHANDLUNG=").append(getWrappedId());
			j.exec(sql.toString());
    	}
    }
    
    /** Den tatsächlich verrechneten Preis (in Rappen) eines Artikels holen */
    @Deprecated
    public Money getEffPreis(IVerrechenbar v){
    	StringBuilder sb=new StringBuilder();
    	sb.append("SELECT VK_PREIS,SCALE FROM LEISTUNGEN WHERE BEHANDLUNG=")
    		.append(getWrappedId()).append(" AND KLASSE=")
    		.append(JdbcLink.wrap(v.getClass().getName()))
    		.append("AND LEISTG_CODE=").append(JdbcLink.wrap(v.getId()));
    	Stm stm=j.getStatement();
    	try{
    		ResultSet rs=stm.query(sb.toString());
    		if(rs!=null && rs.next()){
    			double base=rs.getDouble(1);
    			double scale=rs.getDouble(2)/100.0;
    			return new Money((int)Math.round(base*scale));
    		}
    		return new Money();
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		return new Money();
    	}finally{
    		j.releaseStatement(stm);
    	}
    }
    
    public Hashtable getDetailsFor(IVerrechenbar v){
    	Stm stm=j.getStatement();
    	try {
        	StringBuilder sb=new StringBuilder();
        	sb.append("SELECT DETAIL FROM LEISTUNGEN WHERE BEHANDLUNG=")
        		.append(getWrappedId()) /*.append(" AND KLASSE=")
        		.append(JdbcLink.wrap(v.getClass().getName()))*/
        		.append("AND LEISTG_CODE=").append(JdbcLink.wrap(v.getId()));

			ResultSet rs=stm.query(sb.toString());
			Hashtable ret=new Hashtable();
			if(rs!=null && rs.next()){
				byte[] blob=rs.getBytes(1);
				if(blob!=null && blob.length>1){
					ret=StringTool.fold(blob,StringTool.GUESS,null);
				}
				rs.close();
			}
			return ret;
		} catch (Exception e) {
			ExHandler.handle(e);
			return null;
		}finally{
			j.releaseStatement(stm);
		}
    }
    public void flushDetailsFor(IVerrechenbar v, Hashtable hash){
    	if(!isEditable(true)){
    		return;
    	}
    	byte[] bin=StringTool.flatten(hash,StringTool.ZIP,null);
    	Hashtable res=StringTool.fold(bin,StringTool.GUESS,null);
    	if(res==null){
    		MessageDialog.openError(null,"Interner Fehler","Hashtable nicht wiederherstellbar.\nBitte melden Sie diesen Fehler, und wie\ner genau entstand");
    		return;
    	}
    	StringBuilder sql=new StringBuilder(1000);
        sql.append("UPDATE LEISTUNGEN SET DETAIL=? WHERE BEHANDLUNG=")
           		.append(getWrappedId()) /*.append(" AND KLASSE=")
        		.append(JdbcLink.wrap(v.getClass().getName())) */
        		.append("AND LEISTG_CODE=").append(JdbcLink.wrap(v.getId()));

        String cmd=sql.toString();
        PreparedStatement stm=j.prepareStatement(cmd);
        try{
            stm.setBytes(1,bin);
            stm.executeUpdate();
        }catch(Exception ex){
            ExHandler.handle(ex);
            log.log("Fehler beim Ausführen der Abfrage "+cmd,Log.ERRORS);
        }

    }
    public boolean remove(boolean forced){
    	if(forced || isEditable(true)){
	    	List<Verrechnet> vv=getLeistungen();
			//VersionedResource vr=getEintrag();
	    	if((vv.size()==0) || 
	    	(forced==true) && (Hub.acl.request(AccessControlDefaults.DELETE_FORCED)==true)){
	    		j.exec("DELETE FROM LEISTUNGEN WHERE BEHANDLUNG="+getWrappedId());
	    		j.exec("DELETE FROM BEHDL_DG_JOINT WHERE BEHANDLUNGSID="+getWrappedId());
	    		return super.delete();
	    	}
    	}
    	return false;
    }
    /** Interface Comparable, um die Behandlungen nach Datum sortieren zu können */
	public int compareTo(Object o) {
		if(o instanceof Konsultation){
			Konsultation b=(Konsultation)o;
			TimeTool me=new TimeTool(getDatum());
			TimeTool other=new TimeTool(b.getDatum());
			return me.compareTo(other);
		}
		return -1;
	}
	/**
	 * Helper:
	 * Get the "active" cons. Normally, it is the actually selected cons.
	 * if the actually selected cons does not match the actually selected patient, then it is
	 * rather the latest cons of the actually selected patient.
	 * @return the consultation that letter will belong to
	 * @author gerry new concept due to some obscure selection problems
	 */
	public static Konsultation getAktuelleKons(){
		Konsultation ret=GlobalEvents.getSelectedKons();
		Patient pat=GlobalEvents.getSelectedPatient();
		if((ret!= null) &&  ((pat==null) || (ret.getFall().getPatient().getId().equals(pat.getId())))){
			return ret;
		}
		if(pat!=null){
			ret= pat.getLetzteKons(true);
			return ret;
		}
		SWTHelper.showError("Kein Patient ausgewählt", "Bitte wählen Sie zuerst aus, wem dieses Dokument zugeordnet werden soll");
		return null;
	}
	protected Konsultation(){}
    static class BehandlungsComparator implements Comparator{
        boolean rev;
        BehandlungsComparator(boolean reverse){
            rev=reverse;
        }
        public int compare(Object arg0, Object arg1)
        {
            Konsultation b1 = (Konsultation) arg0;
            Konsultation b2 = (Konsultation) arg1;
            TimeTool t1=new TimeTool(b1.getDatum());
            TimeTool t2=new TimeTool(b2.getDatum());
            if(rev==true){
                return t2.compareTo(t1);
            }else{
                return t1.compareTo(t2);
            }
        }
        
    }
	@Override
	public boolean isDragOK() {
		return true;
	}

}
