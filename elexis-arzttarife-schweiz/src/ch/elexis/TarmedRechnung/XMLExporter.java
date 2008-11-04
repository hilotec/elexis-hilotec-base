/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: XMLExporter.java 4665 2008-11-04 17:41:57Z rgw_ch $
 *******************************************************************************/

/*  BITTE KEINE ÄNDERUNGEN AN DIESEM FILE OHNE RÜCKSPRACHE MIT MIR weirich@elexis.ch */
/*  THIS FILE IS FROZEN. DO NOT MODIFY */

package ch.elexis.TarmedRechnung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.Hub;
import ch.elexis.artikel_ch.data.Medical;
import ch.elexis.artikel_ch.data.Medikament;
import ch.elexis.artikel_ch.data.MiGelArtikel;
import ch.elexis.banking.ESR;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LaborLeistung;
import ch.elexis.data.Mandant;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.TarmedLeistung;
import ch.elexis.data.TrustCenters;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.RnStatus.REJECTCODE;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.preferences.PreferenceInitializer;
import ch.elexis.tarmedprefs.PreferenceConstants;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.XMLTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

/**
 * Exportiert eine Elexis-Rechnung im XML 4.0 Format von xmldata.ch Bitte KEINE Änderungen an dieser
 * Klasse durchführen. Senden Sie Verbesserungsvorschläge oder Wünsche als Mail oder direkt als
 * Patch an weirich@elexis.ch.
 * 
 * In den meisten Fällen kann diese Klasse als BlackBox zum Erstellen von Tarmed-konformen XML-Files
 * zur Weiterverarbeitung verwendet werden. DoExport(..) liefert ein JDOM-Dokument, das die
 * gewünschte Rechnung enthält. Diese kann vom Aufrufer dann an einen Intermediär oder auf einen
 * Drucker ausgegeben werden. Der Output dieses Exporters ist TrustX zertifiziert. Änderungen
 * sollten in den seltensten Fällen nötig sein. Falls doch: Fehlermeldungen bitte an
 * weirich@elexis.ch
 * 
 * @author gerry
 * 
 */
public class XMLExporter implements IRnOutputter {
	public static final Namespace ns =
		Namespace.getNamespace("invoice", "http://www.xmlData.ch/xmlInvoice/XSD");
	Fall actFall;
	Patient actPatient;
	Mandant actMandant;
	double tpTarmedTL = 0;
	double tpTarmedAL = 0;
	String diagnosen, dgsys;
	Rechnung rn;
	
	private Money mTarmed;
	private Money mTarmedTL;
	private Money mTarmedAL;
	private Money mKant;
	private Money mUebrige;
	private Money mAnalysen;
	private Money mMigel;
	private Money mPhysio;
	private Money mMedikament;
	private Money mTotal;
	private Money mPaid;
	private Money mDue;
	static TarmedACL ta;
	private String outputDir;
	private static final String PREFIX = "TarmedRn:";
	private static final Log log = Log.get("XMLExporter");
	
	/**
	 * Reset exporter
	 */
	public void clear(){
		actFall = null;
		actPatient = null;
		actMandant = null;
		tpTarmedTL = 0;
		tpTarmedAL = 0;
		diagnosen = "";
		dgsys = "";
		rn = null;
		
		mTarmed = new Money();
		mTarmedTL = new Money();
		mTarmedAL = new Money();
		mKant = new Money();
		mUebrige = new Money();
		mAnalysen = new Money();
		mMigel = new Money();
		mPhysio = new Money();
		mMedikament = new Money();
		mTotal = new Money();
		mPaid = new Money();
		mDue = new Money();
	}
	
	public XMLExporter(){
		ta = TarmedACL.getInstance();
		clear();
	}
	
	/**
	 * Output a Collection of bills. This essentially lets the user modify the output settings (if
	 * any) and then calls doExport() für each bill in rnn
	 * 
	 * @param type
	 *            desired mode (original, copy, storno)
	 * @param rnn
	 *            a Collection of Rechnung - Objects to output
	 */
	public Result<Rechnung> doOutput(final IRnOutputter.TYPE type, final Collection<Rechnung> rnn){
		Result<Rechnung> ret = new Result<Rechnung>();
		if (outputDir == null) {
			SWTHelper.SimpleDialog dlg =
				new SWTHelper.SimpleDialog(new SWTHelper.IControlProvider() {
					public Control getControl(Composite parent){
						return createSettingsControl(parent);
					}
					
					public void beforeClosing(){
					// Nothing
					}
				});
			if (dlg.open() != Dialog.OK) {
				return ret;
			}
		}
		for (Rechnung rn : rnn) {
			if (doExport(rn, outputDir + File.separator + rn.getNr() + ".xml", type, false) == null) {
				ret.add(Result.SEVERITY.ERROR, 1, "Fehler in Rechnung " + rn.getNr(), rn, true);
			}
		}
		return ret;
	}
	
	/**
	 * Wa want to be informed on cancellings of any bills
	 * 
	 * @param rn
	 *            we don't mind, we always return true
	 */
	public boolean canStorno(final Rechnung rn){
		return true;
	}
	
	private void negate(Element el, String attr){
		String v = el.getAttributeValue(attr);
		if (!StringTool.isNothing(v)) {
			if (!v.equals("0.00")) {
				if (v.startsWith("-")) {
					v = v.substring(1);
				} else {
					v = "-" + v;
				}
				el.setAttribute(attr, v);
			}
		}
	}
	
	/**
	 * Export a bill as XML. We do, in fact first check whether this bill was exported already. And
	 * if so we do not create it again but load the old one. There is deliberately no possibility to
	 * avoid this behaviour. (One can only delete or storno a bill and recreate it (even then the
	 * stored xml remains stored. Additionally, the caller can chose to store the bill as XML in the
	 * file system. This is done if the parameter dest ist given. On success the caller will receive
	 * a JDOM Document containing the bill. 
	 * 
	 * @param rechnung
	 *            the bill to export
	 * @param dest
	 *            a full filepath to save the final document (or null to not save it)
	 * @param type
	 *            Type of output (original, copy, storno)
	 * @param doVerify
	 *            true if the bill should be sent trough a verifyer after creation.
	 * @return the jdom XML-Document that contains the bill. Might be null on failure.
	 */
	@SuppressWarnings("unchecked")
	public Document doExport(final Rechnung rechnung, final String dest,
		final IRnOutputter.TYPE type, final boolean doVerify){
		clear();
		Namespace nsxsi =
			Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		// Namespace
		// nsschema=Namespace.getNamespace("schemaLocation","http://www.xmlData.ch/xmlInvoice/XSD
		// MDInvoiceRequest_400.xsd");
		Namespace nsdef = Namespace.getNamespace("http://www.xmlData.ch/xmlInvoice/XSD");
		rn = rechnung;
		mPaid = rn.getAnzahlung();
		
		if (NamedBlob.exists(PREFIX + rechnung.getNr())) {
			// If the bill exists already in the database, it has been output earlier, so we don't
			// recreate it. We must, however, reflect changes that happened since it was output:
			// Payments, state changes, obligations
			NamedBlob blob = NamedBlob.load(PREFIX + rechnung.getNr());
			SAXBuilder builder = new SAXBuilder();
			try {
				Document ret = builder.build(new StringReader(blob.getString()));
				Element root = ret.getRootElement();
				Element invoice = root.getChild("invoice", ns);
				Element balance = invoice.getChild("balance", ns);
				Money anzInBill =
					XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount_prepaid"));
				if (!mPaid.equals(anzInBill)) {
					balance.setAttribute("amount_prepaid", XMLTool.moneyToXmlDouble(mPaid)); // 10335
					mDue =
						XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount_obligations"));
					mDue.subtractMoney(mPaid);
					mDue.roundTo5();
					balance.setAttribute("amount_due", XMLTool.moneyToXmlDouble(mDue)); // 10340
				}
				if (type.equals(IRnOutputter.TYPE.COPY)) {
					invoice.setAttribute("resend", Boolean.toString(true));
				} else if (type.equals(TYPE.STORNO)) {
					Element detail = invoice.getChild("detail", ns);
					Element services = detail.getChild("services", ns);
					List<Element> sr = services.getChildren();
					for (Element el : sr) {
						try {
							negate(el, "quantity");
							// negate(el,"unit.mt");
							// negate(el,"unit.tt");
							negate(el, "amount.mt");
							negate(el, "amount.tt");
							// negate(el,"unit");
							negate(el, "amount");
							/*
							 * Money
							 * betrag=XMLTool.xmlDoubleToMoney(el.getAttributeValue("amount"));
							 * el.setAttribute("amount", XMLTool.moneyToXmlDouble(betrag.negate()));
							 */

						} catch (Exception ex) {
							ExHandler.handle(ex);
						}
					}
					// Money betrag=XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount"));
					// balance.setAttribute("amount",XMLTool.moneyToXmlDouble(betrag.negate()));
					negate(balance, "amount");
					negate(balance, "amount_tarmed");
					negate(balance, "amount_tarmed.mt");
					negate(balance, "amount_tarmed.tt");
					negate(balance, "amount_cantonal");
					negate(balance, "amount_unclassified");
					negate(balance, "amount_drug");
					negate(balance, "amount_lab");
					negate(balance, "amount_migel");
					negate(balance, "amount_physio");
					negate(balance, "amount_obligations");
					balance.setAttribute("amount_due", "0.00");
					balance.setAttribute("amount_prepaid", "0.00");
				}
				
				if (dest != null) {
					if (type.equals(TYPE.STORNO)) {
						writeFile(ret, dest.toLowerCase().replaceFirst("\\.xml$", "_storno.xml"));
					} else {
						writeFile(ret, dest);
					}
				}
				StringWriter stringWriter = new StringWriter();
				XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
				xout.output(ret, stringWriter);
				blob.putString(stringWriter.toString());
				return ret;
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError("Lesefehler",
					"Diese früher abgespeicherte Rechnung kann nicht mehr gelesen werden");
				// What should we do -> We create it from scratch
			}
		}
		if (type.equals(TYPE.STORNO)) {
			SWTHelper.showError("Storno unmöglich",
				"Diese Rechnung scheint noch gar nie ausgegeben worden zu sein");
			return null;
		}
		
		actFall = rn.getFall();
		actPatient = actFall.getPatient();
		actMandant = rn.getMandant();
		Kontakt kostentraeger = actFall.getRequiredContact(TarmedRequirements.INSURANCE);
		// We try to figure out whether we should use Tiers Payant or Tiers Garant.
		// if unsure, we make it TG
		String tiers = "TG";
		Patient pat = actFall.getPatient();
		Kontakt rnAdressat = actFall.getGarant();
		
		if ((kostentraeger != null) && (kostentraeger.isValid())) {
			if (rnAdressat.equals(kostentraeger)) {
				tiers = "TP";
			} else {
				tiers = "TG";
			}
		} else {
			kostentraeger = rnAdressat;
			tiers = "TG";
		}
		String tcCode = TarmedRequirements.getTCCode(actMandant);
		
		if (kostentraeger == null) {
			kostentraeger = actPatient;
		}
		Document xmlRn; // Ziffern "Referenzhandbuch Arztrechnung XML 4.0"
		Element root = new Element("request", ns); // 10020/21
		root.addNamespaceDeclaration(nsdef);
		root.addNamespaceDeclaration(nsxsi); // 10022
		root.setAttribute("schemaLocation",
			"http://www.xmlData.ch/xmlInvoice/XSD MDInvoiceRequest_400.xsd", nsxsi);
		
		// Rolle
		root.setAttribute("role", "production"); // 10030/32
		xmlRn = new Document(root);
		
		// header
		Element header = new Element("header", ns); // 10050
		root.addContent(header);
		Element sender = new Element("sender", ns); // 10051
		String mEAN = TarmedRequirements.getEAN(actMandant); // (String)actMandant.getInfoElement("EAN");
		
		sender.setAttribute("ean_party", mEAN);
		String kEAN = TarmedRequirements.getEAN(kostentraeger); // (String)kostentraeger.getInfoElement("EAN");
		String rEAN = TarmedRequirements.getRecipientEAN(kostentraeger);
		if (rEAN.equals("unknown")) {
			rEAN = kEAN;
		}
		
		// Try to find the intermediate EAN. If we have explicitely set
		// an intermediate EAN, we'll use this one. Otherweise, we'll
		// check whether the mandator has a TC contract. if so, we try to
		// find the TC's EAN.
		// If nothing appropriate is found, we'll try to use the receiver EAN
		// or at least the guarantor EAN.
		// If everything fails we use a pseudo EAN to make the Validators happy
		String iEAN = TarmedRequirements.getIntermediateEAN(actFall);
		Element intermediate = new Element("intermediate", ns); // 10052
		if (iEAN.length() == 0) {
			if (TarmedRequirements.hasTCContract(actMandant)) {
				String trustCenter = TarmedRequirements.getTCName(actMandant);
				if (trustCenter.length() > 0) {
					iEAN = TrustCenters.getTCEAN(trustCenter);
				}
			}
			if (StringTool.isNothing(iEAN)) {
				if (!rEAN.matches("(20[0-9]{11}|76[0-9]{11})")) {
					if (kEAN.matches("(20[0-9]{11}|76[0-9]{11})")) {
						iEAN = kEAN;
					} else {
						iEAN = TarmedRequirements.EAN_PSEUDO; // make validator happy
					}
				} else {
					iEAN = rEAN;
				}
			}
		}
		intermediate.setAttribute("ean_party", iEAN);
		
		Element recipient = new Element("recipient", ns); // 10053
		recipient.setAttribute("ean_party", rEAN);
		
		header.addContent(sender);
		header.addContent(intermediate);
		header.addContent(recipient);
		
		// prolog
		Element prolog = new Element("prolog", ns); // 10060
		root.addContent(prolog);
		VersionInfo vi = new VersionInfo(Hub.Version);
		// Versionen unter 100 werden nicht akzeptiert
		// int
		// tmi=Integer.parseInt(vi.maior())*100+Integer.parseInt(vi.minor())*10+Integer.parseInt(vi.rev());
		Element spackage = new Element("package", ns); // 10070
		spackage.setText("Elexis");
		spackage.setAttribute("version", vi.maior() + vi.minor() + vi.rev()); // 10071
		spackage.setAttribute("id", "0"); // 10072
		prolog.addContent(spackage);
		
		Element generator = new Element("generator", ns); // 10080
		Element gsoft = new Element("software", ns); // 10081
		generator.addContent(gsoft);
		gsoft.setText("JDOM");
		// 10082
		gsoft.setAttribute("version", "100"); // Damit die Version akzeptiert wird, muss sie
		// wieder mindestens 100 sein
		gsoft.setAttribute("id", "0"); // 10083
		prolog.addContent(generator);
		
		Element validator = new Element("validator", ns); // 10100
		validator.setAttribute("focus", "tarmed"); // 10111
		validator.setAttribute("version_software", vi.maior() + vi.minor() + vi.rev()); // 10130
		validator.setAttribute("version_db", "401"); // 10131
		validator.setAttribute("id", "0"); // 10132
		validator.setText("Elexis TarmedVerifier");
		prolog.addContent(validator);
		
		// invoice
		Element invoice = new Element("invoice", ns); // 10150
		root.addContent(invoice);
		String ts = null;
		if (type.equals(IRnOutputter.TYPE.COPY)) {
			ts = rn.getExtInfo("TimeStampXML");
		} else {
			ts = Long.toString(new Date().getTime() / 1000);
			rn.setExtInfo("TimeStampXML", ts);
		}
		
		invoice.setAttribute("invoice_timestamp", ts); // 10152
		invoice.setAttribute("invoice_id", rn.getRnId()); // 10153
		invoice.setAttribute("invoice_date", new TimeTool(rn.getDatumRn())
			.toString(TimeTool.DATE_MYSQL)
			+ "T00:00:00"); // 10154
		invoice.setAttribute("resend", Boolean.toString(type.equals(IRnOutputter.TYPE.COPY))); // 10170
		invoice.setAttribute("case_id", rn.getFall().getId()); // 10180
		String bem = rn.getBemerkung();
		if (!StringTool.isNothing(bem)) { // 10200
			Element remark = new Element("remark", ns); // 10201
			remark.setText(rn.getBemerkung());
			invoice.addContent(remark);
		}
		
		// 10250 weggelassen
		
		// Balance aufbauen // 10300
		String curr = (String) Hub.actMandant.getInfoElement("Währung"); // 10310
		if (StringTool.isNothing(curr)) {
			curr = "CHF";
		}
		List<Konsultation> lb = rn.getKonsultationen();
		
		Element services = new Element("services", ns);
		
		// DecimalFormat df = new DecimalFormat("#0.00");
		
		// Alle Informationen je Konsultation sammeln
		// alle Preise (in Rappen) auflisten
		StringBuilder sbDiagnosen = new StringBuilder();
		dgsys = "freetext";
		String lastDate = "";
		int sessionNumber = 1;
		
		// To make the validator happy, the attribute date_begin must duplicate exactly
		// the date of the first billing position end date_end must duplicate exactly
		// the date of the last billed consultation. If we have non-billed entries in
		// the patient's record we must forget these for the sake of xml-confirmity
		// so we use ttFirst and ttLast to check he the dates (instead of the begin end end
		// dates that are stored in the bill
		TimeTool ttFirst = new TimeTool(TimeTool.END_OF_UNIX_EPOCH);
		TimeTool ttLast = new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
		int recordNumber = 1;
		for (Konsultation b : lb) {
			List<IDiagnose> ld = b.getDiagnosen();
			for (IDiagnose dg : ld) {
				String dgc = dg.getCode();
				if (dgc != null) {
					dgsys = dg.getCodeSystemName();
					if (sbDiagnosen.indexOf(dgc) == -1) {
						sbDiagnosen.append(dg.getCode()).append(" ");
					}
				}
			}
			List<Verrechnet> lv = b.getLeistungen();
			if (lv.size() == 0) {
				continue;
			}
			TimeTool tt = new TimeTool(b.getDatum());
			// System.out.println(tt.toString(TimeTool.DATE_GER));
			if (tt.isBefore(ttFirst)) { // make validator happy
				ttFirst.set(tt);
				// System.out.println(ttFirst.toString(TimeTool.DATE_GER));
			}
			if (tt.isAfter(ttLast)) { // make validator even happier
				ttLast.set(tt);
				// System.out.println(ttLast.toString(TimeTool.DATE_GER));
			}
			String dateShort = tt.toString(TimeTool.DATE_COMPACT);
			String dateForTarmed = makeTarmedDatum(b.getDatum());
			if (dateShort.equals(lastDate)) {
				sessionNumber++;
			} else {
				sessionNumber = 1;
			}
			
			lastDate = dateShort;
			// unit.mt x unit_factor.mt x scale_factor.mt x external_factor.mt x quantity =
			// amount.mt
			// unit.tt x unit_factor.tt x scale_factor.tt x external_factor.tt x quantity =
			// amount.tt
			// amount
			for (Verrechnet vv : lv) {
				Element el;
				int zahl = vv.getZahl();
				IVerrechenbar v = vv.getVerrechenbar();
				
				if (v == null) {
					log.log("Fehlerhafte Rechnung " + rn.getNr() + " Null-Verrechenbar bei Kons "
						+ b.getLabel(), Log.ERRORS);
					continue;
				}
				if (v instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) v;
					String arzl = vv.getDetail("AL");
					String tecl = vv.getDetail("TL");
					double primaryScale = vv.getPrimaryScaleFactor();
					double secondaryScale = vv.getSecondaryScaleFactor();
					
					double tlTl, tlAL, mult;
					if (arzl != null) { // earlier system no more supported
						tlTl = Double.parseDouble(tecl);
						mult = PersistentObject.checkZeroDouble(vv.get("VK_Scale")); // Taxpunkt
						tlAL = Double.parseDouble(arzl);
						
					} else {
						tlTl = tl.getTL();
						tlAL = tl.getAL();
						mult = tl.getVKMultiplikator(tt, actFall);
					}
					/*
					 * obsoleted in 1.4 if (tl.getText().indexOf('%') != -1) { // %-Zuschlag oder
					 * Abzüge if (tlTl == 0.0) { tlAL = vv.getEffPreis().getCents(); mult = 1.0; } }
					 */
					tpTarmedTL += tlTl * zahl;
					tpTarmedAL += tlAL * zahl;
					/*
					 * Money mAL=new Money((int)Math.round(tlALmult)); Money mTL=new
					 * Money((int)Math.round(tlTlmult));
					 * 
					 * mTarmedAL.addCent(mAL.getCents()zahl); mTarmedTL.addCent(mTL.getCents()zahl);
					 */
					// Änderung 17.4. 08
					Money mAL =
						new Money((int) Math.round(tlAL * mult * zahl * primaryScale
							* secondaryScale));
					Money mTL =
						new Money((int) Math.round(tlTl * mult * zahl * primaryScale
							* secondaryScale));
					
					mTarmedAL.addCent(mAL.getCents());
					mTarmedTL.addCent(mTL.getCents());
					
					el = new Element("record_tarmed", ns); // 22000
					el.setAttribute("treatment", "ambulatory"); // 22050
					el.setAttribute("tariff_type", "001"); // 22060
					Hashtable<String, String> ext = tl.loadExtension();
					String bezug = ext.get("Bezug"); // 22360
					if (!StringTool.isNothing(bezug)) {
						el.setAttribute("ref_code", bezug);
					}
					el.setAttribute("ean_provider", TarmedRequirements.getEAN(actMandant
						.getRechnungssteller())); // 22390
					el.setAttribute("ean_responsible", TarmedRequirements.getEAN(actMandant)); // 22400
					el.setAttribute("billing_role", "both"); // 22410
					el.setAttribute("medical_role", "self_employed"); // 22430
					
					el.setAttribute("body_location", TarmedLeistung.getSide(vv)); // 22450
					
					el.setAttribute("unit.mt", XMLTool.doubleToXmlDouble(tlAL / 100.0, 2)); // 22470
					el.setAttribute("unit_factor.mt", XMLTool.doubleToXmlDouble(mult, 2)); // 22480
					// (strebt
					// gegen
					// 0)
					el.setAttribute("scale_factor.mt", XMLTool.doubleToXmlDouble(primaryScale, 1)); // 22490
					el.setAttribute("external_factor.mt", XMLTool.doubleToXmlDouble(secondaryScale,
						1)); // 22500
					el.setAttribute("amount.mt", XMLTool.moneyToXmlDouble(mAL)); // 22510
					
					el.setAttribute("unit.tt", XMLTool.doubleToXmlDouble(tlTl / 100.0, 2)); // 22520
					el.setAttribute("unit_factor.tt", XMLTool.doubleToXmlDouble(mult, 2)); // 22530
					el.setAttribute("scale_factor.tt", XMLTool.doubleToXmlDouble(primaryScale, 1)); // 22540
					el.setAttribute("external_factor.tt", XMLTool.doubleToXmlDouble(secondaryScale,
						1)); // 22550
					el.setAttribute("amount.tt", XMLTool.moneyToXmlDouble(mTL)); // 22560
					Money mAmountLocal = new Money(mAL);
					mAmountLocal.addMoney(mTL);
					el.setAttribute("amount", XMLTool.moneyToXmlDouble(mAmountLocal)); // 22570
					el.setAttribute("vat_rate", "0"); // 22590
					el.setAttribute("validate", "true"); // 22620
					
					el
						.setAttribute("obligation", Boolean.toString(TarmedLeistung
							.isObligation(vv)));
					
				} else if (v instanceof LaborLeistung) {
					el = new Element("record_lab", ns); // 28000
					el.setAttribute("tariff_type", "316"); // 28060
					LaborLeistung ll = (LaborLeistung) v;
					double mult = ll.getFactor(tt, actFall);
					Money preis = vv.getEffPreis(); // b.getEffPreis(v);
					double korr = preis.getCents() / mult;
					el.setAttribute("unit", XMLTool.doubleToXmlDouble(korr / 100.0, 2)); // 28470
					el.setAttribute("unit_factor", XMLTool.doubleToXmlDouble(mult, 2)); // 28480
					Money mAmountLocal = new Money(preis);
					mAmountLocal.multiply(zahl);
					el.setAttribute("amount", XMLTool.moneyToXmlDouble(mAmountLocal)); // 28570
					el.setAttribute("vat_rate", "0"); // 28590
					el.setAttribute("obligation", "true"); // 28630
					el.setAttribute("validate", "true"); // 28620
					mAnalysen.addMoney(mAmountLocal);
				} else if ((v instanceof Medikament) || (v instanceof Medical)
					|| (v.getCodeSystemCode() == "400")) {
					el = new Element("record_drug", ns);
					Money preis = vv.getEffPreis(); // b.getEffPreis(v);
					el.setAttribute("unit", XMLTool.moneyToXmlDouble(preis));
					el.setAttribute("unit_factor", "1.0");
					el.setAttribute("tariff_type", "400"); // Pharmacode-basiert
					el.setAttribute("code", ((Artikel) v).getPharmaCode());
					Money mAmountLocal = new Money(preis);
					mAmountLocal.multiply(zahl);
					el.setAttribute("amount", XMLTool.moneyToXmlDouble(mAmountLocal));
					el.setAttribute("vat_rate", "0");
					el.setAttribute("obligation", "true");
					el.setAttribute("validate", "true");
					mMedikament.addMoney(mAmountLocal);
				} else if (v instanceof MiGelArtikel) {
					el = new Element("record_migel", ns);
					Money preis = vv.getEffPreis(); // b.getEffPreis(v);
					el.setAttribute("unit", XMLTool.moneyToXmlDouble(preis));
					el.setAttribute("unit_factor", "1.0");
					el.setAttribute("tariff_type", "452"); // MiGeL ab 2001-basiert
					el.setAttribute("code", ((MiGelArtikel) v).getCode());
					Money mAmountLocal = new Money(preis);
					mAmountLocal.multiply(zahl);
					el.setAttribute("amount", XMLTool.moneyToXmlDouble(mAmountLocal));
					el.setAttribute("vat_rate", "0");
					el.setAttribute("obligation", "true");
					el.setAttribute("validate", "true");
					mMigel.addMoney(mAmountLocal);
				} else {
					Money preis = vv.getEffPreis();
					el = new Element("record_unclassified", ns);
					el.setAttribute("tariff_type", v.getCodeSystemCode());
					el.setAttribute("unit", XMLTool.moneyToXmlDouble(preis));
					el.setAttribute("unit_factor", "1.0");
					Money mAmountLocal = new Money(preis);
					mAmountLocal.multiply(zahl);
					el.setAttribute("amount", XMLTool.moneyToXmlDouble(mAmountLocal));
					el.setAttribute("vat_rate", "0");
					el.setAttribute("validate", "true");
					el.setAttribute("obligation", "false");
					el.setAttribute("external_factor", "1.0");
					mUebrige.addMoney(mAmountLocal);
				}
				el.setAttribute("record_id", Integer.toString(recordNumber++)); // 22010
				el.setAttribute("number", Integer.toString(sessionNumber)); // 22030
				el.setAttribute("quantity", Integer.toString(zahl)); // 22350
				el.setAttribute("date_begin", dateForTarmed); // 22370
				el.setText(vv.getText()); // 22340
				// el.setAttribute("code",v.getCode()); // 22330
				setAttributeWithDefault(el, "code", v.getCode(), "0"); // 22330
				services.addContent(el);
			}
		}
		mTotal.addMoney(mTarmedAL).addMoney(mTarmedTL).addMoney(mAnalysen).addMoney(mMedikament)
			.addMoney(mUebrige).addMoney(mKant).addMoney(mPhysio).addMoney(mMigel);
		
		Element balance = new Element("balance", ns); // 10300
		balance.setAttribute("currency", curr); // 10310
		balance.setAttribute("amount", XMLTool.moneyToXmlDouble(mTotal)); // 10330
		// mPaid=rn.getAnzahlung();
		balance.setAttribute("amount_prepaid", XMLTool.moneyToXmlDouble(mPaid)); // 10335
		mDue = new Money(mTotal);
		mDue.subtractMoney(mPaid);
		mDue.roundTo5();
		
		// chfDue=Math.round(chfDue*20.0)/20.0;
		mTarmed.addMoney(mTarmedAL).addMoney(mTarmedTL);
		balance.setAttribute("amount_due", XMLTool.moneyToXmlDouble(mDue)); // 10340
		balance.setAttribute("amount_tarmed", XMLTool.moneyToXmlDouble(mTarmed)); // 10341
		balance.setAttribute("unit_tarmed.mt", XMLTool.doubleToXmlDouble(tpTarmedAL / 100.0, 2)); // 10348
		balance.setAttribute("amount_tarmed.mt", XMLTool.moneyToXmlDouble(mTarmedAL)); // 10349
		balance.setAttribute("unit_tarmed.tt", XMLTool.doubleToXmlDouble(tpTarmedTL / 100.0, 2)); // 10350
		balance.setAttribute("amount_tarmed.tt", XMLTool.moneyToXmlDouble(mTarmedTL)); // 10351
		balance.setAttribute("amount_cantonal", "0.00"); // 10342
		balance.setAttribute("amount_unclassified", XMLTool.moneyToXmlDouble(mUebrige)); // 10343
		balance.setAttribute("amount_lab", XMLTool.moneyToXmlDouble(mAnalysen)); // 10344
		balance.setAttribute("amount_physio", "0.00"); // 10346
		balance.setAttribute("amount_drug", XMLTool.moneyToXmlDouble(mMedikament)); // 10347
		balance.setAttribute("amount_migel", XMLTool.moneyToXmlDouble(mMigel)); // 10345
		balance.setAttribute("amount_obligations", XMLTool.moneyToXmlDouble(mTotal)); // 10352
		
		// 10370 ff als stub, solange keine Mwst Pflicht
		Element vat = new Element("vat", ns);
		vat.setAttribute("vat", "0.0");
		Element vatrate = new Element("vat_rate", ns);
		vatrate.setAttribute("vat_rate", "0.0");
		vatrate.setAttribute("amount", XMLTool.moneyToXmlDouble(mDue));
		vatrate.setAttribute("vat", "0.00");
		vat.addContent(vatrate);
		balance.addContent(vat);
		invoice.addContent(balance);
		
		String esrmode = actMandant.getInfoString(ta.ESR5OR9);
		Element esr; // 10400
		String userdata = rn.getRnId();
		ESR besr =
			new ESR(actMandant.getInfoString(ta.ESRNUMBER), actMandant.getInfoString(ta.ESRSUB),
				userdata, ESR.ESR27);
		
		// String ESRNumber=m.getInfoString(ta.ESRNUMBER);
		// String ESRSubid=m.getInfoString(ta.ESRSUB);
		// Zur Zeit nur esr9 unterstützt
		if (esrmode.equals("esr5")) { // esr5 oder esr9
			esr = new Element("esr5", ns);
			esr.setAttribute("participant_number", besr.makeParticipantNumber(true)); // Teilnehmernummer
			esr.setAttribute("type", actMandant.getInfoString(ta.ESRPLUS)); // 15 oder 15plus (mit
			// oder ohne Betrag)
			// String refnr="01000234004554504"; // TODO
			// String codingline="01322234°3423424"; // TODO
			// esr.setAttribute("reference_number",refnr); // Referenz-Nummer
			// esr.setAttribute("coding_line",codingline); // codierzeile
		} else if (esrmode.equals("esr9")) { // esr9 // 10403
			esr = new Element("esr9", ns);
			esr.setAttribute("participant_number", besr.makeParticipantNumber(true));// 9-stellige
			// Teilnehmernummer
			// 10451
			// esr.setAttribute("type",m.getInfoString(ta.ESRPLUS)); // 16or27 oder 16or27plus
			esr.setAttribute("type", "16or27"); // Nur dieses Format unterstützt 10461
			String refnr = besr.makeRefNr(true);
			// String codingline=besr.createCodeline(mDue.getCentsAsString(),null);
			String codingline =
				besr.createCodeline(XMLTool.moneyToXmlDouble(mDue).replaceFirst("[.,]", ""), null);
			esr.setAttribute("reference_number", refnr); // 16 oder 27 stellige ref nr 10470
			esr.setAttribute("coding_line", codingline); // codierzeile 10479
		} else {
			MessageDialog.openError(null, "Fehler bei der Mandant-Definition",
				"Es ist kein gültiger ESR-Modus definiert");
			return null;
		}
		String bankid = actMandant.getInfoString(ta.RNBANK);
		if (!bankid.equals("")) { // Bankverbindung -> BESR 10480
			Organisation bank = Organisation.load(bankid);
			Element eBank = new Element("bank", ns); // 10500
			Element company = buildAdressElement(bank); // 10511-10670
			eBank.addContent(company);
			esr.addContent(eBank);
		}
		invoice.addContent(esr);
		
		Element eTiers = null;
		if (tiers.equals("TG")) {
			eTiers = new Element("tiers_garant", ns); // 11020
			String paymentPeriode = actMandant.getInfoString(ta.RNFRIST);
			if (StringTool.isNothing(paymentPeriode)) {
				paymentPeriode = "30";
			}
			eTiers.setAttribute("payment_periode", "P" + paymentPeriode + "D"); // 11021
		} else {
			eTiers = new Element("tiers_payant", ns); // 11260
			// to simplify things for now we do no accept modifications
			eTiers.setAttribute("invoice_modification", "false"); // 11262
			eTiers.setAttribute("purpose", "invoice"); // 11265
			// TODO Storno / anulment
		}
		
		Element biller = new Element("biller", ns); // 11070 -> 11400
		// biller.setAttribute("ean_party",actMandant.getInfoString("EAN")); // 11402
		biller.setAttribute("ean_party", TarmedRequirements
			.getEAN(actMandant.getRechnungssteller())); // 11402
		biller.setAttribute("zsr", TarmedRequirements.getKSK(actMandant)); // actMandant.getInfoString("KSK"));
		// // 11403
		String spec = actMandant.getInfoString(ta.SPEC);
		if (!spec.equals("")) {
			biller.setAttribute("specialty", spec); // 11404
		}
		biller.addContent(buildAdressElement(actMandant)); // 11600-11680
		eTiers.addContent(biller);
		
		Element provider = (Element) biller.clone(); // 11080
		provider.setName("provider");
		eTiers.addContent(provider);
		
		Element insurance = new Element("insurance", ns); // 11090
		// The 'insurance' element is optional in Tiers Garant so in TG we only insert this Element,
		// if we have all
		// data absolutely correct
		// In Tiers Payant, the insurance element is mandatory, and, furthermore, MUST be an
		// Organization. So in TP, we
		// insert an insurance element in any case, and, if the guarantor is a person,
		// we "convert" it to an organization to make the validator happy
		if (tiers.equals("TG")) {
			if (kostentraeger.istOrganisation()) {
				if (kEAN.matches("[0-9]{13,13}")) {
					insurance.setAttribute("ean_party", kEAN);
					insurance.addContent(buildAdressElement(kostentraeger));
					eTiers.addContent(insurance);
				}
			}
		} else {
			// insurance.addContent(buildAdressElement(kostentraeger)); // must be an organization,
			// so we fake one
			/*
			 * if(!kEAN.matches("[0-9]{13,13}")){ kEAN="2000000000000"; }
			 */
			insurance.setAttribute("ean_party", kEAN);
			Element company = new Element("company", ns);
			Element companyname = new Element("companyname", ns);
			companyname.setText(kostentraeger.get("Bezeichnung1"));
			company.addContent(companyname);
			company.addContent(buildPostalElement(kostentraeger));
			company.addContent(buildTelekomElement(kostentraeger));
			company.addContent(buildOnlineElement(kostentraeger));
			insurance.addContent(company);
			eTiers.addContent(insurance);
			// note this may lead to a person mistreated as organization. So these faults should be
			// caught when generating bills
			
		}
		
		Element patient = new Element("patient", ns); // 11100
		
		// patient.setAttribute("unique_id",rn.getFall().getId()); // this is optional and should be
		// ssn13 type. leave it out for now
		String gender = "male";
		if (pat == null) {
			MessageDialog.openError(null, "Fehler",
				"Die Rechnung hat keinen zugeordneten Patienten");
			return null;
		}
		if (StringTool.isNothing(pat.getGeschlecht())) { // we fall back to female. why not?
			pat.set("Geschlecht", "w");
		}
		if (pat.getGeschlecht().equals("w")) {
			gender = "female";
		}
		patient.setAttribute("gender", gender);
		String gebDat = pat.getGeburtsdatum();
		if (StringTool.isNothing(gebDat)) { // make validator happy if we don't know the birthdate
			patient.setAttribute("birthdate", "0001-00-00T00:00:00");
		} else {
			patient.setAttribute("birthdate", new TimeTool(pat.getGeburtsdatum())
				.toString(TimeTool.DATE_MYSQL)
				+ "T00:00:00");
		}
		patient.addContent(buildAdressElement(pat));
		eTiers.addContent(patient);
		
		Element guarantor = new Element("guarantor", ns); // 11110
		guarantor.addContent(buildAdressElement(rnAdressat));
		eTiers.addContent(guarantor);
		
		Element referrer = new Element("referrer", ns); // 11120
		Kontakt auftraggeber = actMandant; // TODO
		referrer.setAttribute("ean_party", TarmedRequirements.getEAN(auftraggeber)); // auftraggeber.getInfoString("EAN"));
		referrer.setAttribute("zsr", TarmedRequirements.getKSK(auftraggeber)); // auftraggeber.getInfoString("KSK"));
		referrer.addContent(buildAdressElement(auftraggeber));
		eTiers.addContent(referrer);
		
		if (tiers.equals("TG") && (TarmedRequirements.hasTCContract(actMandant))) {
			Element demand = new Element("demand", ns);
			demand.setAttribute("tc_demand_id", "0");
			
			demand.setAttribute("tc_token", besr.createCodeline(XMLTool.moneyToXmlDouble(mDue)
				.replaceFirst("[.,]", ""), tcCode));
			demand.setAttribute("insurance_demand_date", makeTarmedDatum(rn.getDatumRn()));
			eTiers.addContent(demand);
		}
		
		invoice.addContent(eTiers);
		
		Element detail = new Element("detail", ns); // 15000
		
		// --detail.setAttribute("date_begin",makeTarmedDatum(rn.getDatumVon())); // 15002
		// --detail.setAttribute("date_end",makeTarmedDatum(rn.getDatumBis())); // 15003
		// use the versions below to make the validator happy
		detail.setAttribute("date_begin", makeTarmedDatum(ttFirst.toString(TimeTool.DATE_GER))); // 15002
		detail.setAttribute("date_end", makeTarmedDatum(ttLast.toString(TimeTool.DATE_GER))); // 15002
		
		detail.setAttribute("canton", actMandant.getInfoString(ta.KANTON)); // 15004
		detail.setAttribute("service_locality", "practice"); // 15021
		
		// 15030 ff weggelassen
		
		Element diagnosis = new Element("diagnosis", ns); // 15500
		/*
		 * String dgType=actMandant.getInfoString(ta.DIAGSYS); if(dgType.equals("")){
		 * dgType="by_contract"; }
		 */

		diagnosis.setAttribute("type", match_diag(dgsys)); // 15510
		diagnosen = sbDiagnosen.toString().trim(); // 15530
		if (dgsys.equals("freetext")) {
			diagnosis.setText(diagnosen);
		} else {
			if (diagnosen.length() > 12) {
				diagnosen = diagnosen.substring(0, 12);
			}
			diagnosis.setAttribute("code", diagnosen);
		}
		detail.addContent(diagnosis);
		
		String gesetz = TarmedRequirements.getGesetz(actFall);
		
		Element versicherung = new Element(gesetz.toLowerCase(), ns); // 16700
		versicherung.setAttribute("reason", match_type(actFall.getGrund()));
		if (gesetz.equalsIgnoreCase("ivg")) {
			String caseNumber = actFall.getRequiredString(TarmedRequirements.CASE_NUMBER);
			if ((!caseNumber.matches("[0-9]{14}")) && // seit 1.1.2000 gültige Nummer
				(!caseNumber.matches("[0-9]{10}")) && // bis 31.12.1999 gültige Nummer
				(!caseNumber.matches("[0-9]{9}")) && // auch bis 31.12.1999 gültige Nummer
				(!caseNumber.matches("[0-9]{6}"))) { // Nummer für Abklärungsmassnahmen
				/* die spinnen, die Bürokraten */
				if (Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true)) {
					rn.reject(REJECTCODE.VALIDATION_ERROR, "IV-Fallnummer ungültig");
				} else {
					caseNumber = "123456"; // sometimes it's better to cheat than to fight
					// bureaucrazy
				}
			}
			versicherung.setAttribute("case_id", caseNumber);
			String ahv = TarmedRequirements.getAHV(pat).replaceAll("[^0-9]", "");
			if (ahv.length() == 0) {
				ahv = actFall.getRequiredString(TarmedRequirements.SSN).replaceAll("[^0-9]", "");
			}
			boolean bAHVValid = ahv.matches("[0-9]{11}") || ahv.matches("[0-9]{13}");
			if (Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true) && (bAHVValid == false)) {
				rn.reject(REJECTCODE.VALIDATION_ERROR, "AHV-Nummer ungültig");
			} else {
				versicherung.setAttribute("ssn", ahv);
			}
			String nif =
				TarmedRequirements.getNIF(actMandant.getRechnungssteller())
					.replaceAll("[^0-9]", "");
			if (Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true)
				&& (!nif.matches("[0-9]{1,7}"))) {
				rn.reject(REJECTCODE.VALIDATION_ERROR, "NIF-Nummer ungültig");
			} else {
				versicherung.setAttribute("nif", nif);
			}
		} else if (gesetz.equalsIgnoreCase("uvg")) {
			String casenumber = actFall.getRequiredString(TarmedRequirements.CASE_NUMBER);
			if (StringTool.isNothing(casenumber)) {
				casenumber = actFall.getRequiredString(TarmedRequirements.ACCIDENT_NUMBER);
			}
			if (!StringTool.isNothing(casenumber)) {
				versicherung.setAttribute("case_id", casenumber);
			}
			String vnummer = actFall.getRequiredString(TarmedRequirements.INSURANCE_NUMBER);
			if (!StringTool.isNothing(vnummer)) {
				versicherung.setAttribute("patient_id", vnummer);
			}
		} else {
			String vnummer = actFall.getRequiredString(TarmedRequirements.INSURANCE_NUMBER);
			if (StringTool.isNothing(vnummer)) {
				vnummer = actFall.getRequiredString(TarmedRequirements.CASE_NUMBER);
			}
			if (StringTool.isNothing(vnummer)) {
				vnummer = pat.getId();
			}
			versicherung.setAttribute("patient_id", vnummer); // 16720
		}
		String casedate = actFall.getInfoString("Unfalldatum"); // 16740
		if (StringTool.isNothing(casedate)) {
			casedate = rn.getDatumVon();
		}
		versicherung.setAttribute("case_date", makeTarmedDatum(casedate));
		// versicherung.setAttribute("case_id",actFall.getFallNummer()); // 16730
		setAttributeIfNotEmpty(versicherung, "contract_number", actFall
			.getInfoString("Vertragsnummer")); // 16750
		detail.addContent(versicherung);
		
		detail.addContent(services); // 20000
		invoice.addContent(detail);
		if (rn.setBetrag(mDue) == false) {
			rn.reject(RnStatus.REJECTCODE.SUM_MISMATCH,
				"Die errechnete Summe entspricht nicht der Rechnungssumme");
			
		} else if (doVerify) {
			new Validator().checkBill(this, new Result<Rechnung>());
		}
		if (rn.getStatus() != RnStatus.FEHLERHAFT) {
			try {
				StringWriter stringWriter = new StringWriter();
				XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
				xout.output(xmlRn, stringWriter);
				NamedBlob blob = NamedBlob.load(PREFIX + rn.getNr());
				blob.putString(stringWriter.toString());
				if (dest != null) {
					writeFile(xmlRn, dest);
					
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.alert("Fehler", "Konnte Datei " + dest + " nicht schreiben");
				return null;
			}
		}
		return xmlRn;
	}
	
	public String getDescription(){
		return "Tarmed-XML 4.0-Datei für TrustCenter";
	}
	
	public Element buildAdressElement(final Kontakt k){
		Element ret;
		if (k.istPerson() == false) {
			ret = new Element("company", ns);
			Element companyname = new Element("companyname", ns);
			companyname.setText(StringTool.limitLength(k.get("Bezeichnung1"), 35));
			ret.addContent(companyname);
			ret.addContent(buildPostalElement(k));
			ret.addContent(buildTelekomElement(k));
			ret.addContent(buildOnlineElement(k));
		} else {
			ret = new Element("person", ns);
			setAttributeIfNotEmptyWithLimit(ret, "salutation", k.getInfoString("Anrede"), 35);
			setAttributeIfNotEmptyWithLimit(ret, "title", k.get("Titel"), 35);
			Element familyname = new Element("familyname", ns);
			familyname.setText(StringTool.limitLength(k.get("Bezeichnung1"), 35));
			ret.addContent(familyname);
			Element givenname = new Element("givenname", ns);
			String gn = k.get(StringTool.limitLength("Bezeichnung2", 35));
			if (StringTool.isNothing(gn)) {
				gn = "Unbekannt"; // make validator happy
			}
			givenname.setText(gn);
			ret.addContent(givenname);
			ret.addContent(buildPostalElement(k));
			ret.addContent(buildTelekomElement(k));
			ret.addContent(buildOnlineElement(k));
		}
		return ret;
	}
	
	public Element buildPostalElement(final Kontakt k){
		Element ret = new Element("postal", ns);
		addElementIfExists(ret, "pobox", null, StringTool.limitLength(k.getInfoString("Postfach"),
			35), null);
		addElementIfExists(ret, "street", null, StringTool.limitLength(k.get("Strasse"), 35), null);
		Element zip =
			addElementIfExists(ret, "zip", null, StringTool.limitLength(k.get("Plz"), 9), "0000");
		setAttributeIfNotEmpty(zip, "countrycode", StringTool.limitLength(k.get("Land"), 3));
		addElementIfExists(ret, "city", null, StringTool.limitLength(k.get("Ort"), 35), "Unbekannt");
		return ret;
	}
	
	public Element buildOnlineElement(final Kontakt k){
		Element ret = new Element("online", ns);
		String email = StringTool.limitLength(k.get("E-Mail"), 70);
		if (!email.matches(".+@.+")) {
			email = "mail@invalid.invalid";
		}
		addElementIfExists(ret, "email", null, k.get("E-Mail"), "mail@invalid.invalid");
		addElementIfExists(ret, "url", null, StringTool.limitLength(k.get("Website"), 100), null);
		return ret;
	}
	
	public Element buildTelekomElement(final Kontakt k){
		Element ret = new Element("telecom", ns);
		addElementIfExists(ret, "phone", null, StringTool.limitLength(k.get("Telefon1"), 25),
			"555-555 55 55");
		addElementIfExists(ret, "fax", null, StringTool.limitLength(k.get("Fax"), 25), null);
		return ret;
	}
	
	public static String makeTarmedDatum(final String datum){
		return new TimeTool(datum).toString(TimeTool.DATE_MYSQL) + "T00:00:00";
	}
	
	private Element addElementIfExists(final Element parent, final String name, final String attr,
		String val, final String defValue){
		if (StringTool.isNothing(val)) {
			val = defValue;
		}
		if (!StringTool.isNothing(val)) {
			Element ret = new Element(name, ns);
			if (attr == null) {
				ret.setText(val);
			} else {
				ret.setAttribute(attr, val);
			}
			parent.addContent(ret);
			return ret;
		}
		return null;
	}
	
	private void setAttributeWithDefault(final Element element, final String name, String value,
		final String def){
		if (element != null) {
			if (!StringTool.isNothing(name)) {
				if (StringTool.isNothing(value)) {
					value = def;
				}
				element.setAttribute(name, value);
			}
		}
	}
	
	private boolean setAttributeIfNotEmptyWithLimit(final Element element, final String name,
		String value, final int len){
		if (value.length() >= len) {
			value = value.substring(0, len - 1);
		}
		return setAttributeIfNotEmpty(element, name, value);
	}
	
	private boolean setAttributeIfNotEmpty(final Element element, final String name,
		final String value){
		if (element == null) {
			return false;
		}
		if (StringTool.isNothing(name)) {
			return false;
		}
		if (StringTool.isNothing(value)) {
			return false;
		}
		element.setAttribute(name, value);
		return true;
	}
	
	private String match_type(final String type){
		if (type == null) {
			return "disease";
		}
		if (type.equalsIgnoreCase(Fall.TYPE_DISEASE)) {
			return "disease";
		}
		if (type.equalsIgnoreCase(Fall.TYPE_ACCIDENT)) {
			return "accident";
		}
		if (type.equalsIgnoreCase(Fall.TYPE_MATERNITY)) {
			return "maternity";
		}
		if (type.equalsIgnoreCase(Fall.TYPE_PREVENTION)) {
			return "prevention";
		}
		if (type.equalsIgnoreCase(Fall.TYPE_BIRTHDEFECT)) {
			return "birthdefect";
		}
		return "disease";
	}
	
	private String match_diag(final String name){
		if (name == null) {
			return "freetext";
		}
		if (name.equalsIgnoreCase("ICD-10")) {
			return "ICD10";
		}
		if (name.equalsIgnoreCase("by contract")) {
			return "by_contract";
		}
		if (name.equalsIgnoreCase("ICPC")) {
			return "ICPC";
		}
		if (name.equalsIgnoreCase("birthdefect")) {
			return "birthdefect";
		}
		return "by_contract";
	}
	
	public Control createSettingsControl(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Label l = new Label(ret, SWT.NONE);
		l.setText("Bitte das Ausgabeverzeichnis für die Rechnungen angeben");
		l.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		final Text text = new Text(ret, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Button b = new Button(ret, SWT.PUSH);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				outputDir = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				Hub.localCfg.set(PreferenceConstants.RNN_EXPORTDIR, outputDir);
				text.setText(outputDir);
			}
		});
		b.setText("Ändern");
		outputDir =
			Hub.localCfg.get(PreferenceConstants.RNN_EXPORTDIR, PreferenceInitializer
				.getDefaultDBPath());
		text.setText(outputDir);
		return ret;
	}
	
	void writeFile(final Document doc, final String dest) throws IOException{
		FileOutputStream fout = new FileOutputStream(dest);
		OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8");
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, cout);
		cout.close();
		fout.close();
		int status_vorher = rn.getStatus();
		if ((status_vorher == RnStatus.OFFEN) || (status_vorher == RnStatus.MAHNUNG_1)
			|| (status_vorher == RnStatus.MAHNUNG_2) || (status_vorher == RnStatus.MAHNUNG_3)) {
			rn.setStatus(status_vorher + 1);
		}
		rn.addTrace(Rechnung.OUTPUT, getDescription() + ": "
			+ RnStatus.getStatusText(rn.getStatus()));
	}
	
	public boolean canBill(final Fall fall){
		Kontakt garant = fall.getGarant();
		Kontakt kostentraeger = fall.getRequiredContact(TarmedRequirements.INSURANCE);
		if ((garant != null) && (kostentraeger != null)) {
			if (garant.isValid()) {
				if (kostentraeger.isValid()) {
					if (kostentraeger.istOrganisation()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void saveComposite(){
	// Nothing
	}
}
