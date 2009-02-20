/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Fall.java 5153 2009-02-20 11:50:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Extensions;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Fall ist eine Serie von zusammengehörigen Behandlungen. Ein Fall hat einen Garanten, ein
 * Anfangsdatum ein Enddatum, eine Bezeichnung und allenfalls ein Enddatum
 * 
 * @author Gerry
 * 
 */
public class Fall extends PersistentObject {
	
	public static final String TYPE_DISEASE = "Krankheit";
	public static final String TYPE_ACCIDENT = "Unfall";
	public static final String TYPE_MATERNITY = "Mutterschaft";
	public static final String TYPE_PREVENTION = "Prävention";
	public static final String TYPE_BIRTHDEFECT = "Geburtsgebrechen";
	public static final String TYPE_OTHER = "Anderes";
	
	@Override
	protected String getTableName(){
		return "FAELLE";
	}
	
	static {
		addMapping("FAELLE", "PatientID", "res=Diagnosen", "DatumVon=S:D:DatumVon",
			"DatumBis=S:D:DatumBis", "GarantID", "Behandlungen=LIST:FallID:BEHANDLUNGEN:Datum",
			"Bezeichnung", "Grund", "xGesetz=Gesetz", "Kostentraeger=KostentrID", "VersNummer",
			"FallNummer", "RnPlanung=BetriebsNummer", "ExtInfo");
	}
	
	/**
	 * Vorgeschlagenen Zeitpunkt für Rechnungsstellung holen (Eine Vorgabe die im fall gemacht wird)
	 * 
	 * @return
	 */
	public TimeTool getBillingDate(){
		String r = get("RnPlanung");
		if (StringTool.isNothing(r)) {
			return null;
		}
		TimeTool ret = new TimeTool();
		if (ret.set(r)) {
			return ret;
		}
		return null;
	}
	
	/**
	 * Zeitpunkt für Rechnungsvorschlag setzen
	 * 
	 * @param dat
	 *            Ein Zeitpunkt oder null
	 */
	public void setBillingDate(TimeTool dat){
		set("RnPlanung", dat == null ? null : dat.toString(TimeTool.DATE_GER));
	}
	
	@Override
	public boolean isValid(){
		
		if (!super.isValid()) {
			return false;
		}
		Patient p = Patient.load(get("PatientID"));
		if ((p == null) || (!p.isValid())) {
			return false;
		}
		
		// Check whether all user-defined requirements for this billing system are met
		String reqs = getRequirements(getAbrechnungsSystem());
		if (reqs != null) {
			for (String req : reqs.split(";")) {
				String[] r = req.split(":");
				String localReq = getInfoString(r[0]);
				if (StringTool.isNothing(localReq)) {
					return false;
				}
				if (r[1].equals("K")) {
					Kontakt k = Kontakt.load(localReq);
					if (!k.isValid()) {
						return false;
					}
				}
			}
		}
		// check whether the outputter could output a bill
		IRnOutputter outputter = getOutputter();
		if (outputter == null) {
			return false;
		} else {
			if (!outputter.canBill(this)) {
				return false;
			}
		}
		return true;
	}
	
	protected Fall(){/* leer */}
	
	protected Fall(final String id){
		super(id);
	}
	
	/**
	 * Einen neuen Fall zu einem Patienten mit einer Bezeichnung erstellen (Garant muss später noch
	 * ergänzt werden; Datum wird von heute genommen
	 * 
	 * @param PatientID
	 * @param Bezeichnung
	 */
	Fall(final String PatientID, final String Bezeichnung, final String Grund,
		String Abrechnungsmethode){
		create(null);
		set(new String[] {
			"PatientID", "Bezeichnung", "Grund", "DatumVon"
		}, PatientID, Bezeichnung, Grund, new TimeTool().toString(TimeTool.DATE_GER));
		if (Abrechnungsmethode == null) {
			String[] billings = getAbrechnungsSysteme();
			Abrechnungsmethode = billings[0];
		}
		setAbrechnungsSystem(Abrechnungsmethode);
		GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.create);
	}
	
	/** Einen Fall anhand der ID aus der Datenbank laden */
	public static Fall load(final String id){
		Fall ret = new Fall(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	/** Anfangsdatum lesen (in der Form dd.mm.yy) */
	public String getBeginnDatum(){
		return checkNull(get("DatumVon"));
	}
	
	public String getBezeichnung(){
		return checkNull(get("Bezeichnung"));
	}
	
	public void setBezeichnung(final String t){
		set("Bezeichnung", t);
	}
	
	/**
	 * Anfangsdatum setzen Zulässige Formate: dd.mm.yy, dd.mm.yyyy, yyyymmdd, yy-mm-dd
	 */
	public void setBeginnDatum(final String dat){
		set("DatumVon", dat);
	}
	
	/** Enddatum lesen oder null: Fall noch nicht abgeschlossen */
	public String getEndDatum(){
		return checkNull(get("DatumBis"));
	}
	
	/** Enddatum setzen. Setzt zugleich den Fall auf abgeschlossen */
	public void setEndDatum(final String dat){
		set("DatumBis", dat);
	}
	
	/**
	 * Den Rechnungsempfänger liefern
	 * 
	 * @return
	 */
	public Kontakt getGarant(){
		Kontakt ret = Kontakt.load(get("GarantID"));
		if ((ret == null) || (!ret.isValid())) {
			ret = getPatient();
		}
		return ret;
	}
	
	public void setGarant(final Kontakt garant){
		set("GarantID", garant.getId());
	}
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString("RechnungsstellerID"));
		if (!ret.isValid()) {
			ret = null;
		}
		return ret;
	}
	
	public void setRechnungssteller(final Kontakt r){
		setInfoString("RechnungsstellerID", r.getId());
	}
	
	/**
	 * Retrieve a required Kontakt from this Fall's Billing system's requirements
	 * 
	 * @param name
	 *            the requested Kontakt's name
	 * @return the Kontakt or Null if no such Kontakt was found
	 */
	public Kontakt getRequiredContact(final String name){
		String kid = getInfoString(name);
		if (kid.equals("")) {
			return null;
		}
		return Kontakt.load(kid);
	}
	
	public void setRequiredContact(final String name, final Kontakt k){
		String r = getRequirements();
		if (!StringTool.isNothing(r)) {
			String[] req = r.split(";");
			int idx = StringTool.getIndex(req, name + ":K");
			if (idx != -1) {
				if (req[idx].endsWith(":K")) {
					setInfoString(name, k.getId());
				}
			}
		}
	}
	
	/**
	 * Retrieve a required String Value from this billing system's definition. If no variable with
	 * that name is found, the billings system constants will be searched
	 * 
	 * @param name
	 * @return a string that might be empty but will never be null.
	 */
	public String getRequiredString(final String name){
		String kid = getInfoString(name);
		if (StringTool.isNothing(kid)) {
			kid = getBillingSystemConstant(getAbrechnungsSystem(), name);
		}
		return kid;
	}
	
	public void setRequiredString(final String name, final String val){
		String[] req = getRequirements().split(";");
		int idx = StringTool.getIndex(req, name + ":T");
		if (idx != -1) {
			setInfoString(name, val);
		}
	}
	
	/**
	 * This is an update only for swiss installations that takes the old tarmed cases to the new
	 * system
	 */
	private static void update(){
		// String is=getInfoString("Kostenträger");
		Query<Fall> qbe = new Query<Fall>(Fall.class);
		for (Fall fall : qbe.execute()) {
			if (fall.getInfoString("Kostenträger").equals("")) {
				fall.setInfoString("Kostenträger", checkNull(fall.get("Kostentraeger")));
			}
			if (fall.getInfoString("Rechnungsempfänger").equals("")) {
				fall.setInfoString("Rechnungsempfänger", checkNull(fall.get("GarantID")));
			}
			if (fall.getInfoString("Versicherungsnummer").equals("")) {
				fall.setInfoString("Versicherungsnummer", checkNull(fall.get("VersNummer")));
			}
			if (fall.getInfoString("Fallnummer").equals("")) {
				fall.setInfoString("Fallnummer", checkNull(fall.get("FallNummer")));
			}
			if (fall.getInfoString("Unfallnummer").equals("")) {
				fall.setInfoString("Unfallnummer", checkNull(fall.get("FallNummer")));
			}
		}
	}
	
	@Deprecated
	public Kontakt getArbeitgeber(){
		String id = getInfoString("Arbeitgeber");
		Kontakt ret = null;
		if (StringTool.isNothing(id) || ((ret = Kontakt.load(id)).exists() == false)) {
			return null;
		}
		return ret;
	}
	
	@Deprecated
	public String getArbeitgeberName(){
		return getArbeitgeber().getLabel();
	}
	
	/**
	 * Versichertennummer holen
	 * 
	 * @deprecated user getRequiredString instead
	 */
	@Deprecated
	public String getVersNummer(){
		
		return checkNull(getInfoString("Versicherungsnummer"));
	}
	
	/**
	 * Versichertennummer setzen public void setVersNummer(final String nr){ set("VersNummer",nr); }
	 */
	/** Fallnummer lesen */
	public String getFallNummer(){
		return checkNull(get("FallNummer"));
	}
	
	/** Fallnummer setzen */
	public void setFallNummer(final String nr){
		set("FallNummer", nr);
	}
	
	/** Feststellen, ob der Fall noch offen ist */
	public boolean isOpen(){
		if (getEndDatum().equals("")) {
			return true;
		}
		return false;
	}
	
	public void setAbrechnungsSystem(final String system){
		setInfoString("billing", system);
	}
	
	public String getAbrechnungsSystem(){
		String ret = getInfoString("billing");
		if (StringTool.isNothing(ret)) {
			String[] systeme = getAbrechnungsSysteme();
			String altGesetz = get("xGesetz");
			int idx = StringTool.getIndex(systeme, altGesetz);
			if (idx == -1) {
				ret = systeme[0];
			} else {
				ret = systeme[idx];
			}
			setAbrechnungsSystem(ret);
		}
		return ret;
	}
	
	public String getCodeSystemName(){
		return getCodeSystem(getAbrechnungsSystem());
	}
	
	/**
	 * Retrieve requirements of this Cases billing system
	 * 
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date
	 */
	
	public String getRequirements(){
		String req = getRequirements(getAbrechnungsSystem());
		return req == null ? "" : req;
	}
	
	/**
	 * Retrieve the name of the outputter of this case's billing system
	 * 
	 * @return
	 */
	public String getOutputterName(){
		return getDefaultPrintSystem(getAbrechnungsSystem());
	}
	
	/**
	 * Retrieve the ooutputter for this case's billing system
	 * 
	 * @return the IRnOutputter that will be used or null if none was found
	 */
	public IRnOutputter getOutputter(){
		String outputterName = getOutputterName();
		if (outputterName.length() > 0) {
			List<IConfigurationElement> list =
				Extensions.getExtensions("ch.elexis.RechnungsManager");
			for (IConfigurationElement ic : list) {
				if (ic.getAttribute("name").equals(outputterName)) {
					try {
						IRnOutputter ret = (IRnOutputter) ic.createExecutableExtension("outputter");
						return ret;
					} catch (CoreException e) {
						ExHandler.handle(e);
					}
				}
			}
		}
		return null;
	}
	
	/** Behandlungen zu diesem Fall holen */
	public Konsultation[] getBehandlungen(final boolean sortReverse){
		List<String> list = getList("Behandlungen", sortReverse);
		int i = 0;
		Konsultation[] ret = new Konsultation[list.size()];
		for (String id : list) {
			ret[i++] = Konsultation.load(id);
		}
		// Arrays.sort(ret,new Konsultation.BehandlungsComparator(sortReverse));
		return ret;
	}
	
	public Konsultation getLetzteBehandlung(){
		List<String> list = getList("Behandlungen", true);
		if (list.size() > 0) {
			return Konsultation.load(list.get(0));
		}
		return null;
	}
	
	/** Neue Konsultation zu diesem Fall anlegen */
	public Konsultation neueKonsultation(){
		if (isOpen() == false) {
			MessageDialog.openError(null, "Fall geschlossen",
				"Zu einem abgeschlossenen Fall kann keine neue Konsultation erstellt werden");
			return null;
		}
		if ((Hub.actMandant == null) || (!Hub.actMandant.exists())) {
			SWTHelper
				.showError(
					"Kein Mandant ausgewält",
					"Sie müssen erst einen Mandanten erstellen und auswählen, bevor Sie eine Konsultation erstellen können");
			return null;
		}
		return new Konsultation(this);
	}
	
	public Patient getPatient(){
		return Patient.load(get("PatientID"));
	}
	
	public String getGrund(){
		return checkNull(get("Grund"));
	}
	
	public void setGrund(final String g){
		set("Grund", g);
	}
	
	@Override
	public String getLabel(){
		String[] f = new String[] {
			"Grund", "Bezeichnung", "DatumVon", "DatumBis"
		};
		String[] v = new String[f.length];
		get(f, v);
		StringBuilder ret = new StringBuilder();
		if (!isOpen()) {
			ret.append("-GESCHLOSSEN- ");
		}
		String ges = getAbrechnungsSystem();
		ret.append(ges).append(": ").append(v[0]).append(" - ");
		ret.append(v[1]).append("(");
		String ed = v[3];
		if ((ed == null) || StringTool.isNothing(ed.trim())) {
			ed = " offen ";
		}
		ret.append(v[2]).append("-").append(ed).append(")");
		return ret.toString();
	}
	
	@Override
	public boolean delete(){
		return delete(false);
	}
	
	/**
	 * Mark this Fall as deleted. This will fail if there exist Konsultationen fpr this Fall, unless
	 * force is set
	 * 
	 * @param force
	 *            delete even if KOnsultationene xist (in that case, all Konsultationen will be
	 *            deleted as well)
	 * @return true if this Fall could be (and has been) deleted.
	 */
	public boolean delete(final boolean force){
		Konsultation[] bh = getBehandlungen(false);
		if ((bh.length == 0)
			|| ((force == true) && (Hub.acl.request(AccessControlDefaults.DELETE_FORCED) == true))) {
			for (Konsultation b : bh) {
				b.delete(true);
			}
			delete_dependent();
			return super.delete();
		}
		return false;
	}
	
	private boolean delete_dependent(){
		Query<AUF> qAUF = new Query<AUF>(AUF.class);
		qAUF.add("FallID", "=", getId());
		for (AUF auf : qAUF.execute()) {
			auf.delete();
		}
		Query<Rechnung> qRn = new Query<Rechnung>(Rechnung.class);
		qRn.add("FallID", "=", getId());
		for (Rechnung rn : qRn.execute()) {
			rn.delete();
		}
		
		return true;
	}
	
	/**
	 * retrieve a string from ExtInfo.
	 * 
	 * @param name
	 *            the requested parameter
	 * @return the value of that parameter (which might be empty but will never be null)
	 */
	@SuppressWarnings("unchecked")
	public String getInfoString(final String name){
		Hashtable extinfo = getHashtable("ExtInfo");
		return checkNull((String) extinfo.get(name));
	}
	
	@SuppressWarnings("unchecked")
	public void setInfoString(final String name, final String wert){
		Hashtable<String, String> extinfo = getHashtable("ExtInfo");
		extinfo.put(name, wert);
		setHashtable("ExtInfo", extinfo);
	}
	
	@SuppressWarnings("unchecked")
	public void clearInfoString(final String string){
		Hashtable<String, String> extinfo = getHashtable("ExtInfo");
		extinfo.remove(string);
		setHashtable("ExtInfo", extinfo);
		
	}
	
	@SuppressWarnings("unchecked")
	public Object getInfoElement(final String name){
		Hashtable extinfo = getHashtable("ExtInfo");
		return extinfo.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public void setInfoElement(final String name, final Object elem){
		Hashtable extinfo = getHashtable("ExtInfo");
		extinfo.put(name, elem);
		setHashtable("ExtInfo", extinfo);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	public static String getDefaultCaseLabel(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASELABEL,
			PreferenceConstants.USR_DEFCASELABEL_DEFAULT);
	}
	
	public static String getDefaultCaseReason(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASEREASON,
			PreferenceConstants.USR_DEFCASEREASON_DEFAULT);
	}
	
	public static String getDefaultCaseLaw(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFLAW, getAbrechnungsSysteme()[0]);
	}
	
	/**
	 * Find all installed billing systems. If we do not find any, we assume that this is an old
	 * installation and try to update. If we find a tarmed-Plugin installed, we create
	 * default-tarmed billings.
	 * 
	 * @return an Array with the names of all configured billing systems
	 */
	public static String[] getAbrechnungsSysteme(){
		String[] ret = Hub.globalCfg.nodes(Leistungscodes.CFG_KEY);
		if ((ret == null) || (ret.length == 0)) {
			List<IConfigurationElement> list =
				Extensions.getExtensions("ch.elexis.RechnungsManager");
			for (IConfigurationElement ic : list) {
				if (ic.getAttribute("name").startsWith("Tarmed")) {
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/KVG/name", "KVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/KVG/gesetz", "KVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/KVG/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/KVG/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/KVG/bedingungen",
						"Kostenträger:K;Versicherungsnummer:T");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/UVG/name", "UVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/UVG/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/UVG/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/UVG/bedingungen",
						"Kostenträger:K;Unfallnummer:T;Unfalldatum:D");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/UVG/gesetz", "UVG");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/IV/name", "IV");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/IV/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/IV/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/IV/bedingungen",
						"Kostenträger:K;AHV-Nummer:T;Fallnummer:T");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/IV/gesetz", "IVG");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/MV/name", "MV");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/MV/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/MV/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/MV/bedingungen", "Kostenträger:K");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/MV/gesetz", "MVG");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/privat/name", "privat");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/privat/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/privat/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/privat/gesetz", "VVG");
					// Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/privat/bedingungen",
					// "Rechnungsempfänger:K");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/VVG/name", "VVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/VVG/leistungscodes",
						"TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/VVG/standardausgabe",
						"Tarmed-Drucker");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/VVG/bedingungen",
						"Kostenträger:K;Versicherungsnummer:T");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/VVG/gesetz", "VVG");
					
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='UVG' WHERE typ='ch.elexis.data.TarmedLeistungUVG'");
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='KVG' WHERE typ='ch.elexis.data.TarmedLeistungKVG'");
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='IV' WHERE typ='ch.elexis.data.TarmedLeistungIV'");
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='MV' WHERE typ='ch.elexis.data.TarmedLeistungMV'");
					update();
					break;
				}
			}
			ret = Hub.globalCfg.nodes(Leistungscodes.CFG_KEY);
			if (ret == null) {
				return new String[] {
					"undefiniert"
				};
			}
		}
		return ret;
	}
	
	public static void createAbrechnungssystem(final String systemname, final String codesystem,
		final String ausgabe, final String... requirements){
		String key = Leistungscodes.CFG_KEY + "/" + systemname;
		Hub.globalCfg.set(key + "/name", systemname);
		Hub.globalCfg.set(key + "/leistungscodes", codesystem);
		Hub.globalCfg.set(key + "/standardausgabe", ausgabe);
		Hub.globalCfg.set(key + "/bedingungen", StringTool.join(requirements, ";"));
	}
	
	public static void removeAbrechnungssystem(final String systemName){
		Hub.globalCfg.remove(Leistungscodes.CFG_KEY + "/" + systemName);
		Hub.globalCfg.flush();
	}
	
	public static String getCodeSystem(final String billingSystem){
		String ret =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/leistungscodes",
				null);
		if (ret == null) { // compatibility
			getAbrechnungsSysteme();
			ret =
				Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/leistungscodes",
					"?");
		}
		return ret;
	}
	
	public static String getDefaultPrintSystem(final String billingSystem){
		String ret =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/standardausgabe",
				null);
		if (ret == null) { // compatibility
			getAbrechnungsSysteme();
			ret =
				Hub.globalCfg.get(
					Leistungscodes.CFG_KEY + "/" + billingSystem + "/standardausgabe", "?");
		}
		return ret;
	}
	
	public static String[] getBillingSystemConstants(final String billingSystem){
		String bc =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/constants", null);
		if (bc == null) {
			return new String[0];
		} else {
			return bc.split("#");
		}
	}
	
	public static String getBillingSystemConstant(final String billingSystem, final String constant){
		String[] c = getBillingSystemConstants(billingSystem);
		for (String bc : c) {
			String[] val = bc.split("=");
			if (val[0].equalsIgnoreCase(constant)) {
				return val[1];
			}
		}
		return "";
	}
	
	/**
	 * add a billing system constant
	 * 
	 * @param billingSystem
	 *            the Billing system
	 * @param constant
	 *            a String of the form name=value
	 * 
	 */
	public static void addBillingSystemConstant(final String billingSystem, final String constant){
		if (constant.indexOf('=') != -1) {
			String bc =
				Hub.globalCfg
					.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/constants", null);
			if (bc != null) {
				bc += "#" + constant;
			} else {
				bc = constant;
			}
			Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/" + billingSystem + "/constants", bc);
		}
	}
	
	public static void removeBillingSystemConstant(final String billingSystem, final String constant){
		String bc =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/constants", null);
		bc = bc.replaceAll(constant, "");
		bc = bc.replaceAll("##", "#");
		bc = bc.replaceFirst("#$", "");
		bc = bc.replaceFirst("^#", "");
		Hub.globalCfg.set(Leistungscodes.CFG_KEY + "/" + billingSystem + "/constants", bc);
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @param attr
	 * @return
	 * @deprecated use getBillingSystemConstant
	 */
	@Deprecated
	public static String getBillingSystemAttribute(final String billingSystem, final String attr){
		String ret =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/" + attr, "");
		return ret;
	}
	
	/**
	 * Retrieve requirements of a given billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date
	 */
	public static String getRequirements(final String billingSystem){
		String ret =
			Hub.globalCfg.get(Leistungscodes.CFG_KEY + "/" + billingSystem + "/bedingungen", null);
		return ret;
	}
	
	/**
	 * Return the referenced field as a PersistentObject. For fields not representing
	 * PersistentObjects, this method returns null.
	 * 
	 * This method is mainly used to replace indirect fields in text templates (e. g.
	 * [Fall.Kostenträger.Bezeichnung1])
	 * 
	 * Actually, this method should be defined by the class PersistentObject and implemented by all
	 * subclasses. A subclass should de-reference all its field it defines. If the sublcass extends
	 * another sublcass, it should also call the superclass' method. All of this is not yet
	 * implemented.
	 * 
	 * TODO: implement further fields of Fall, e. g. PatientID and GarantID
	 * 
	 * @param field
	 *            the field to resolve. This must represent a Persistent Object, else null is
	 *            returned.
	 * @return the referenced object, or null if it could not be found
	 */
	public PersistentObject getReferencedObject(String field){
		// first consider the billing system requirements
		Kontakt kontakt = getRequiredContact(field);
		if (kontakt != null) {
			if (kontakt.exists()) {
				if (kontakt.istPerson()) {
					kontakt = Person.load(kontakt.getId());
				} else if (kontakt.istOrganisation()) {
					kontakt = Organisation.load(kontakt.getId());
				}
				return kontakt;
			} else {
				return null;
			}
		}
		
		// then try our own fields
		// TODO
		
		return null;
	}
}
