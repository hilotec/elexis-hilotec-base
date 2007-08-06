/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *    $Id$
 *******************************************************************************/

package org.iatrix.data;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.icpc.Episode;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.JdbcLink.Stm;

/** 
 * Datentyp für Problemliste.
 * 
 * Ein Problem wird den Konsulationen zugeordnet, in denen das Problem
 * behandelt wird.
 * Fuer jedes Problem muessen KK-Diagnosen definiert werden, die dann auf der
 * Rechnung erscheinen. Jedes Problem verwaltet diese Diagnosen selber und
 * gleicht dann die Konsulationen ab, denen dieses Problem zugeordnet ist.
 * Pro Problem koennen Dauermedikamente festgelegt werden. Diese Zuordnung
 * verwendet die bereits vorhandene Relation PATIENT_ARTIKEL_JOINT.
 *
 * Update:
 * Das hier beschriebene "Problem" enspricht ziemlich genau dem Konzept
 * einer "Episode" bei ICPC-2. Deshalb werden diese beiden Konzepte
 * zusammengefuehrt. Somit kann ein Problem als eine Erweiterung einer
 * Episode betrachtet werden.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 *
 */

public class Problem extends Episode {
    /**
     * Separator String for Lists returned as String
     */
    public static final String TEXT_SEPARATOR = "::";
    
    // version key in globalCfg
    private static final String IATRIX_VERSION_KEY = "org.iatrix/dbversion";
    
    private static final String FIELD_PROCEDERE = "Procedere";
    
    private static final String STANDARD_PROBLEM = "Standardproblem";

    private static final String PROBLEM_BEHDL_TABLENAME = "IATRIX_PROBLEM_BEHDL_JOINT";
    private static final String PROBLEM_DG_TABLENAME = "IATRIX_PROBLEM_DG_JOINT";
    private static final String PROBLEM_DAUERMEDIKATION_TABLENAME = "IATRIX_PROBLEM_DAUERMEDIKATION_JOINT";
    
    private static final String IATRIX_DB_VERSION = "0.2.0";

    private static final String CREATE =
    /*
    "CREATE TABLE " + PROBLEM_TABLENAME + " ("+
    "ID          VARCHAR(25) primary key,"+
	"deleted     CHAR(1) default '0',"+
    "PatientID   VARCHAR(25),"+
    "EpisodeID   VARCHAR(25),"+
    "Bezeichnung VARCHAR(50),"+  // deprecated, see Episode.Title
    "Nummer      VARCHAR(10),"+  // deprecated, see Episode.Number
    "Datum       VARCHAR(20),"+  // deprecated, see Episode.StartDate
    "Procedere   VARCHAR(80),"+
    "Status      CHAR(1) DEFAULT '1',"+  // deprecated, see Episode.Status
    "ExtInfo     BLOB"+
    ");"+
    ""+
    */
    "CREATE TABLE " + PROBLEM_BEHDL_TABLENAME + " ("+
    "ID            VARCHAR(25) primary key," +
    "ProblemID     VARCHAR(25),"+
    "BehandlungsID VARCHAR(25)"+
    ");"+
    "CREATE INDEX problembehdl1 on " + PROBLEM_BEHDL_TABLENAME + " (ProblemID);"+
    ""+
    "CREATE TABLE " + PROBLEM_DG_TABLENAME + " ("+
    "ID				VARCHAR(25) primary key,"+
    "ProblemID      VARCHAR(25),"+
    "DiagnoseID		VARCHAR(25)"+
    ");"+
    ""+
    "CREATE INDEX problemdg1 on " + PROBLEM_DG_TABLENAME + " (ProblemID);"+
    ""+
    "CREATE TABLE " + PROBLEM_DAUERMEDIKATION_TABLENAME + " ("+
    "ID                VARCHAR(25) primary key,"+
    "ProblemID         VARCHAR(25),"+
    "DauermedikationID VARCHAR(25)"+
    ");"+
    ""+
    "CREATE INDEX problemdauermedikation1 on " + PROBLEM_DAUERMEDIKATION_TABLENAME + " (ProblemID);";
    /*
    ""+
    "INSERT INTO " + PROBLEM_TABLENAME + " (ID) VALUES ('__SETUP__');";
    */

    /*
    static{
        addMapping(PROBLEM_TABLENAME,
                "PatientID",
                "EpisodeID",
                "Bezeichnung",
                "Nummer",
                "Datum",
                "Procedere",
                "Status",
                "Konsultationen=JOINT:ProblemID:BehandlungsID:" + PROBLEM_BEHDL_TABLENAME,
                "Diagnosen=JOINT:ProblemID:DiagnoseID:" + PROBLEM_DG_TABLENAME,
                "Dauermedikation=JOINT:ProblemID:DauermedikationID:" + PROBLEM_DAUERMEDIKATION_TABLENAME
        );
    }
    
    /*
     * DB Update
     */
    /*
	static final String[] dbUpdateVersions = {
	 "0.2.0"
	};
	static final String[] dbUpdateCmds = {
	 "",
	};
	*/
    
    static {
    	init();
    }
    
    /**
     * Oeffentlicher Konstruktor zur Erstellung eines neuen Problems
     * @param pat Der Patient, dem dieses Problem zugeordnet werden soll
     * @param bezeichnung Bezeichnung des Problems
     */
    public Problem(Patient pat, String bezeichnung) {
    	super(pat, bezeichnung);
    }

    /**
     * Hier werden Konfigurationseinzelheiten eingelesen. Wenn das Lesen fehlschlägt,
     * nimmt die Methode an, dass die Tabelle noch nicht existiert und legt sie neu an.
     * Bei Bedarf wird ein DB-Update vorgenommen.
     * @return
     */
    private static void init() {
    	String version = Hub.globalCfg.get(IATRIX_VERSION_KEY, null);
    	if (version == null) {
    		// tables don't yet exist or pre 0.2.0

    		// get old-style version object
    		OldProblem oldSetup = OldProblem.load("__SETUP__");
    		if (!oldSetup.exists()) {
    			// create tables
    			try{
    				ByteArrayInputStream bais = new ByteArrayInputStream(CREATE.getBytes("UTF-8"));
    				if (j.execScript(bais, true, false) == false) {
    					MessageDialog.openError(null, "Fehler bei Problem", "Konnte die Tabellen für Probleme nicht erstellen");
    					return;
    				}
    				Hub.globalCfg.set(IATRIX_VERSION_KEY, IATRIX_DB_VERSION);
    			}catch(Exception ex){
    				ExHandler.handle(ex);
    			}
    		} else {
    			// convert old-style problems

    			convertOldProblemsToEpisodes();
				Hub.globalCfg.set(IATRIX_VERSION_KEY, IATRIX_DB_VERSION);
    		}
    	} else {
    		// here, we can do table updates
    		// no update required yet
    	}
    }

    /**
     * Convert old-style problems to problems based on episodes.
     */
    private static void convertOldProblemsToEpisodes() {
    	// get all available problems (OldProblem)
    	List<OldProblem> oldProblems = OldProblem.getOldProblems();
    	for (OldProblem oldProblem : oldProblems) {
    		Problem problem = new Problem(oldProblem.getId());
    		if (problem.isAvailable()) {
    			// new-style problem already exists
    			continue;
    		}
    		
    		Patient patient = Patient.load(oldProblem.get("PatientID"));
    		if (patient.isAvailable()) {
    			problem.create(null);
    			problem.set("PatientID", patient.getId());

    			String title = oldProblem.get("Bezeichnung");
				String number = oldProblem.get("Nummer");
				String startDate = oldProblem.get("Datum");
				String procedere = oldProblem.get("Procedere");
				String sStatus = oldProblem.get("Status");
    			
				int status;
				if (sStatus != null && sStatus.equals(ACTIVE_VALUE)) {
					status = ACTIVE;
				} else {
					status = INACTIVE;
				}
    			
    			problem.setTitle(title);
    			problem.setNumber(number);
    			problem.setStartDate(startDate);
    			problem.setProcedere(procedere);
    			problem.setStatus(status);
    		}
    	}
    }

    public static Problem load(String id){
    	Problem problem = new Problem(id);
    	return problem;
    }
    
    /** Der parameterlose Konstruktor wird nur von der Factory gebraucht und sollte nie
     * public sein.
     */
    protected Problem(){
        // empty
    }

    /**
     * Creates a new problem with the given id.
     * @param id the id of this object
     */
    protected Problem(String id){
        super(id);
    }
    
	/**
	 * Ein Problem aus der Datenbank entfernen. Dabei werden auch alle verknüpften Daten
	 * gelöscht (?)
	 * @param force bei true wird das Problem auf jeden Fall gelöscht, bei false nur, wenn keine
	 * vernknüpften Daten (?) von ihm existieren.
	 * @return false wenn das Problem nicht gelöscht werden konnte.
	 */
    public boolean remove(boolean force){
        if (true || (force == true) && (Hub.acl.request(AccessControlDefaults.DELETE_FORCED) == true)) {
        	// TODO verknuepfte Daten loeschen falls vorhanden
            	// TODO Sicherstellen, dass Problem von allen Konsulationen entfernt wird.
	    	ExHandler.handle(new Exception("Alle Probleme von Konsulationen entfernen"));

        	return super.delete();
        }
        return false;
    }

    public String getProcedere() {
    	return getExtField(FIELD_PROCEDERE);
    }
    
    public void setProcedere(String procedere) {
    	setExtField(FIELD_PROCEDERE, procedere);
    }
    
	public String toString() {
		return getLabel();
	}
	
	/**
	 * Fuegt ein Problem einer Konsultation hinzu, d. h. das Problem
	 * wurde in dieser Konsultation behandelt (oder soll behandelt werden).
	 * 
	 * @param konsultation die Konsultation, zu der das Problem hinzugefuegt werden soll.
	 */
	// TODO do this with encounters
	public void addToKonsultation(Konsultation konsultation) {
		// check if Problem has already been added to the Konsultatioj
		String problemKonsultationId = j.queryString(
				"SELECT ID FROM " + PROBLEM_BEHDL_TABLENAME + " WHERE ProblemID = " + getWrappedId() + " AND BehandlungsID = " + konsultation.getWrappedId());
        if (StringTool.isNothing(problemKonsultationId)) {
        	// add the Problem to the Konsultation
			StringBuilder sql = new StringBuilder(200);
			sql.append(
					"INSERT INTO " + PROBLEM_BEHDL_TABLENAME
							+ " (ID, ProblemID, BehandlungsID) VALUES (")
					.append(JdbcLink.wrap(StringTool.unique("problembhdl")))
					.append(",").append(getWrappedId()).append(",").append(
							JdbcLink.wrap(konsultation.getId())).append(")");
			j.exec(sql.toString());

			// add this Problem's Diagnosen to the Konsultation

    		// Konsultation doesn't check if Diagnose has already been added
			// existing Diagnosen
    		ArrayList<IDiagnose> existingDiagnosen = konsultation.getDiagnosen();

    		// Problem's Diagnosen
			List<IDiagnose> diagnosen = getDiagnosen();
			for (IDiagnose diagnose : diagnosen) {
	    		boolean exists = false;
	    		for (IDiagnose dg : existingDiagnosen) {
	    			// note: IDiagnose doesn't guarantee that equals() is impelmented.
	    			// but IDiagnose objects usually extend PersistentObject
	    			if (dg.getId().equals(diagnose.getId())) {
	    				exists = true;
	    			}
	    		}
	    		if (!exists) {
	    			konsultation.addDiagnose(diagnose);
	    		}
			}
		}
	}

	/**
	 * Entfernt ein Problem von einer Konsultation hinzu, d. h. das Problem soll
	 * in dieser Konsultation nicht behandelt werden.
	 * 
	 * @param konsultation
	 *            die Konsultation, von der das Problem entfernt werden soll.
	 */
	// TODO do this with encounters
	public void removeFromKonsultation(Konsultation konsultation) {
		// remove assignment in database
		
		StringBuilder sql = new StringBuilder(200);
		sql.append("DELETE FROM " + PROBLEM_BEHDL_TABLENAME)
		    .append(" WHERE ProblemID = " + getWrappedId())
		    .append(" AND")
		    .append(" BehandlungsID = " + konsultation.getWrappedId());
		j.exec(sql.toString());
		
		// remove Diagnosen from Konsultation
		
		List<IDiagnose> diagnosen = getDiagnosen();
		removeDiagnosenFromKonsultation(konsultation, diagnosen);
	}
	
	/*
	 * Remove a List of Diagnosen from a Konsultation this Problem is assigned to.
	 * But don't remove Diagnosen from other Problems that are assigned to this Konsultation.
	 * This method is used by removeDiagnose(IDiagnose)
	 */
	private void removeDiagnosenFromKonsultation(Konsultation konsultation, List<IDiagnose> diagnosen) {
		// all other assigned Problem's Diagnosen
		List<IDiagnose> otherProblemsDiagnosen = new ArrayList<IDiagnose>();
		List<Problem> problems = getProblemsOfKonsultation(konsultation);
		for (Problem problem : problems) {
			if (!problem.equals(this)) {
				otherProblemsDiagnosen.addAll(problem.getDiagnosen());
			}
		}
		
		// remove all Diagnosen except if it is in otherProblemsDiagnosen
		for (IDiagnose diagnose : diagnosen) {
			if (!otherProblemsDiagnosen.contains(diagnose)) {
				if (konsultation.isEditable(false)) {
					konsultation.removeDiagnose(diagnose);
				}
			}
		}
	}
	
	/*
	 * Remove a single Diagnose from a Konsultation.
	 * Also see removeDiagnosenFromKonsultation(Konsultation, List<IDiagnose>)
	 * This method is used by removeProblemFromKonsultation(Konsultation)
	 */
	private void removeDiagnoseFromKonsultation(Konsultation konsultation, IDiagnose diagnose) {
		// Create List of Diagnosen with a single element
		ArrayList<IDiagnose> diagnosen = new ArrayList<IDiagnose>();
		diagnosen.add(diagnose);
		
		removeDiagnosenFromKonsultation(konsultation, diagnosen);
	}

	/**
	 * Liefert die Probleme, die dem Patienten zugeordnet sind.
	 * 
	 * @param der Patient, von dem die Probleme zugureckgegeben werden sollen
	 * @return eine Liste mit den Problemen des Patienten
	 */
    public static List<Problem> getProblemsOfPatient(Patient patient) {
        Query<Problem> query = new Query<Problem>(Problem.class);
        query.add("PatientID", "=", patient.getId());
        List<Problem> problems = query.execute();
        if (problems != null) {
        	return problems;
        } else {
        	// error, return empty list
        	return new ArrayList<Problem>();
        }
    }


	/**
	 * Hole alle Probleme, die einer Konsulation zugeordnet sind.
	 * @param konsultation die Konsulation, von der die Probleme geholt werden sollen
	 * @return eine Liste aller Probleme zu dieser Konsulation
	 */
	public static List<Problem> getProblemsOfKonsultation(Konsultation konsultation) {
        ArrayList<Problem> problems = new ArrayList<Problem>();
 
        StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT ProblemId FROM " + PROBLEM_BEHDL_TABLENAME)
			.append(" WHERE BehandlungsID = " + konsultation.getWrappedId());
		
    	Stm stm = j.getStatement();
    	ResultSet rs = stm.query(sql.toString());
    	try {
    		while(rs.next()) {
    			String id = rs.getString(1);
    			Problem problem = Problem.load(id);
    			if (problem != null) {
    				problems.add(problem);
    			}
    		}
    		rs.close();
    	} catch (Exception ex) {
    		ExHandler.handle(ex);
    		log.log(ex.getMessage(), Log.ERRORS);
    	} finally {
    		j.releaseStatement(stm);
    	}
    	return problems;
	}
	
	/**
	 * Liefert eine Liste aller Konsulationen zurueck, denen dieses Problem
	 * zugeordnet ist. 
	 * @return Liste aller Konsulationen
	 */
	public List<Konsultation> getKonsultationen() {
        ArrayList<Konsultation> konsultationen = new ArrayList<Konsultation>();
        
        StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT BehandlungsID FROM " + PROBLEM_BEHDL_TABLENAME)
			.append(" WHERE ProblemID = " + getWrappedId());
		
    	Stm stm = j.getStatement();
    	ResultSet rs = stm.query(sql.toString());
    	try {
    		while(rs.next()) {
    			String id = rs.getString(1);
    			Konsultation konsultation = Konsultation.load(id);
    			if (konsultation != null) {
    				konsultationen.add(konsultation);
    			}
    		}
    		rs.close();
    	} catch (Exception ex) {
    		ExHandler.handle(ex);
    		log.log(ex.getMessage(), Log.ERRORS);
    	} finally {
    		j.releaseStatement(stm);
    	}
    	return konsultationen;
	}
	
    /** Eine Liste der Diagnosen zu diesem Problem holen */ 
    public ArrayList<IDiagnose> getDiagnosen(){
        ArrayList<IDiagnose> ret=new ArrayList<IDiagnose>();
    	Stm stm=j.getStatement();
    	ResultSet rs1=stm.query("SELECT DiagnoseID FROM IATRIX_PROBLEM_DG_JOINT WHERE ProblemID="+JdbcLink.wrap(getId()));
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

    /**
     * Liefert eine Text-Repraesentation der Diagnosenliste zurueck
     * @return
     */
	public String getDiagnosenAsText() {
		StringBuilder sb = new StringBuilder();
		
		List<IDiagnose> diagnosen = getDiagnosen();
		
		boolean isFirst = true;
		for (IDiagnose diagnose : diagnosen) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(TEXT_SEPARATOR);
			}
			sb.append(diagnose.getLabel());
		}
		
		return sb.toString();
	}

    /** Eine weitere Diagnose diesem Problem zufügen */
    public void addDiagnose(IDiagnose dg) {
        String diagnoseId = j.queryString("SELECT ID FROM DIAGNOSEN WHERE KLASSE = " + JdbcLink.wrap(dg.getClass().getName()) + " AND DG_CODE = " + JdbcLink.wrap(dg.getCode()));
        StringBuilder sql=new StringBuilder(200);
        if (StringTool.isNothing(diagnoseId)) {
            diagnoseId = StringTool.unique("problemdg");
            sql.append("INSERT INTO DIAGNOSEN (ID, DG_CODE, DG_TXT, KLASSE) VALUES (")
               .append(JdbcLink.wrap(diagnoseId)).append(",")
               .append(JdbcLink.wrap(dg.getId())).append(",")
               .append(JdbcLink.wrap(dg.getText())).append(",")
               .append(JdbcLink.wrap(dg.getClass().getName()))
               .append(")");
            j.exec(sql.toString());
            sql.setLength(0);
        }
        
        // add Diagnose if it doesn't yet exists
        String exists = j.queryString("SELECT ID FROM " + PROBLEM_DG_TABLENAME + " WHERE ProblemID = " + getWrappedId() + " AND DiagnoseID = " + JdbcLink.wrap(diagnoseId));
        if (StringTool.isNothing(exists)) {
        	String problemDiagnoseId = StringTool.unique("problemdg");
        	sql.append("INSERT INTO " + PROBLEM_DG_TABLENAME + " (ID, ProblemID, DiagnoseID) VALUES (")
        	.append(JdbcLink.wrap(problemDiagnoseId)).append(",")
        	.append(getWrappedId()).append(",")
            .append(JdbcLink.wrap(diagnoseId)).append(")");
        	j.exec(sql.toString());

        	// add Diagnose to 
        	addDiagnoseToKonsultationen(dg);
        }
    }
    
    private void addDiagnoseToKonsultationen(IDiagnose diagnose) {
    	// pre: Diagnose has already been added to the Problem

    	List<Konsultation> konsultationen = getKonsultationen();
    	for (Konsultation konsultation : konsultationen) {
    		// Konsultation doesn't check if Diagnose has already been added
    		ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
    		boolean exists = false;
    		for (IDiagnose dg : diagnosen) {
    			// note: IDiagnose doesn't guarantee that equals() is impelmented.
    			// but IDiagnose objects usually extend PersistentObject
    			if (dg.getId().equals(diagnose.getId())) {
    				exists = true;
    			}
    		}
    		if (!exists) {
				if (konsultation.isEditable(false)) {
					konsultation.addDiagnose(diagnose);
				}
    		}
    	}
    }
    
    /** Eine Diagnose aus der Diagnoseliste entfernen */
    public void removeDiagnose(IDiagnose diagnose) {
    	// remove diagnose in db
    	
    	StringBuilder sql=new StringBuilder();
    	sql.append("SELECT ID FROM DIAGNOSEN WHERE DG_CODE=")
    	.append(JdbcLink.wrap(diagnose.getId())).append(" AND ")
    	.append("KLASSE=").append(JdbcLink.wrap(diagnose.getClass().getName()));
    	String dgid=j.queryString(sql.toString());
    	
    	sql.setLength(0);
    	sql.append("DELETE FROM " + PROBLEM_DG_TABLENAME + " WHERE ProblemID=")
    		.append(getWrappedId()).append(" AND ")
    		.append("DiagnoseID=").append(JdbcLink.wrap(dgid));
    	j.exec(sql.toString());

		// remove Diagnose from all Konsultationen
    	
    	List<Konsultation> konsultationen = getKonsultationen();
		for (Konsultation konsultation : konsultationen) {
			removeDiagnoseFromKonsultation(konsultation, diagnose);
		}
    }

	/**
	 * Add a Prescription specific to this Problem
	 * 
	 * @param prescription the Prescription to be added
	 * @return true if the Prescription has been added, else otherwise
	 */
	public boolean addPrescription(Prescription prescription) {
        String exists = j.queryString("SELECT ID FROM " + PROBLEM_DAUERMEDIKATION_TABLENAME + " WHERE ProblemID = " + getWrappedId() + " AND DauermedikationID = " + prescription.getWrappedId());
        if (StringTool.isNothing(exists)) {
        	String problemDaueredikationId = StringTool.unique("problemdauermedikation");
            StringBuilder sql=new StringBuilder(200);
        	sql.append("INSERT INTO " + PROBLEM_DAUERMEDIKATION_TABLENAME + " (ID, ProblemID, DauermedikationID) VALUES (")
        	.append(JdbcLink.wrap(problemDaueredikationId)).append(",")
        	.append(getWrappedId()).append(",")
            .append(prescription.getWrappedId()).append(")");
        	j.exec(sql.toString());

        	return true;
        }
        
        return false;
	}
	
	/**
	 * Remove Prescription from this Problem
	 * 
	 * @param prescription the Prescription to be removed
	 */
	public void removePrescription(Prescription prescription) {
		StringBuilder sql = new StringBuilder(200);
		sql.append("DELETE FROM " + PROBLEM_DAUERMEDIKATION_TABLENAME)
		    .append(" WHERE ProblemID = " + getWrappedId())
		    .append(" AND")
		    .append(" DauermedikationID = " + prescription.getWrappedId());
		j.exec(sql.toString());
	}
	
	/**
	 * Return all Prescriptions specific to this problem
	 * 
	 * @return a List of all Prescriptions
	 */
	public List<Prescription> getPrescriptions() {
        ArrayList<Prescription> prescriptions = new ArrayList<Prescription>();
        
        StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT DauermedikationID FROM " + PROBLEM_DAUERMEDIKATION_TABLENAME)
			.append(" WHERE ProblemID = " + getWrappedId());
		
    	Stm stm = j.getStatement();
    	ResultSet rs = stm.query(sql.toString());
    	try {
    		while(rs.next()) {
    			String id = rs.getString(1);
    			Prescription prescription = Prescription.load(id);
    			if (prescription != null) {
    				prescriptions.add(prescription);
    			}
    		}
    		rs.close();
    	} catch (Exception ex) {
    		ExHandler.handle(ex);
    		log.log(ex.getMessage(), Log.ERRORS);
    	} finally {
    		j.releaseStatement(stm);
    	}
    	return prescriptions;
	}
	
    /**
     * Returns a textual representation of all Prescriptions. The Prescriptions
     * are separated with TEXT_SEPARATOR ("::").
     * 
     * @return a list of all Prescriptions
     */
	public String getPrescriptionsAsText() {
		StringBuilder sb = new StringBuilder();
		
		List<Prescription> prescriptions = getPrescriptions();
		
		boolean isFirst = true;
		for (Prescription prescription : prescriptions) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(TEXT_SEPARATOR);
			}
			String label = prescription.getArtikel().getLabel() + " (" + prescription.getDosis() + ")";
			sb.append(label);
		}
		
		return sb.toString();
	}


	/**
	 * Standardproblem erstellen
	 * @param patient der Patient, fuer den ein Standardproblem erstellt werden soll
	 * @return das gerade erstellte Problem
	 */
	public static Problem createStandardProblem(Patient patient) {
        Problem problem = new Problem(patient, STANDARD_PROBLEM);
        return problem;
	}
}
