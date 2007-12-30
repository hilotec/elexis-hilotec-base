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
 * $Id: RnPrintView.java 3489 2007-12-30 13:28:16Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.Hub;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.arzttarife_schweiz.Messages;
import ch.elexis.banking.ESR;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.ReplaceCallback;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.XMLTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class RnPrintView extends ViewPart {
	public static final String ID="elexis-arzttarife-schweiz.printView"; //$NON-NLS-1$
	/*public static final int LinesFirstPage=15;
	public static final int LinesMiddlePage=25;
	public static final int LinesLastPage=22;*/
	private double cmAvail=21.4;
	private static double cmPerLine=0.65;		// Höhe pro Zeile
	private static double cmFirstPage=13.0;		// Platz auf der ersten Seite
	private static double cmMiddlePage=21.0;	// Platz auf Folgeseiten
	private static double cmFooter=4.5;			// Platz für Endabrechnung
	private int existing; 
	private final Log log=Log.get("RnPrint");
	private String paymentMode;
	//TextContainer text;
	TarmedACL ta=TarmedACL.getInstance();
	CTabFolder ctab;
	
	
	public RnPrintView() {
		
	}

	@Override
	public void createPartControl(final Composite parent) {
		ctab=new CTabFolder(parent,SWT.BOTTOM);
		ctab.setLayout(new FillLayout());
	}

	CTabItem addItem(final String template, final String title, final Kontakt adressat){
		CTabItem ret=new CTabItem(ctab,SWT.NONE);				
		TextContainer text=new TextContainer(getViewSite());
		ret.setControl(text.getPlugin().createContainer(ctab,new ICallback(){
			public void save() {}
			public boolean saveAs() {
				return false;
			}
			
		}));
		// don't associate a Konsultation
		Brief actBrief=text.createFromTemplateName(null,template,Brief.RECHNUNG,adressat, Messages.RnPrintView_tarmedBill);
		ret.setData("brief",actBrief); //$NON-NLS-1$
		ret.setData("text",text); //$NON-NLS-1$
		ret.setText(title);
		ret.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(final DisposeEvent e) {
				CTabItem item=(CTabItem)e.getSource();
				TextContainer text=(TextContainer)item.getData("text"); //$NON-NLS-1$
				Brief brief=(Brief)item.getData("brief"); //$NON-NLS-1$
				if ((brief != null) && brief.exists()) {
					text.saveBrief(brief,Brief.RECHNUNG);
					brief.delete();
					item.setData("brief", null);
					text.dispose();
				}		
			
			}});
		return ret;
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public void dispose() {
		clearItems();
		super.dispose();
	}

	public void clearItems(){
		for(int i=0;i<ctab.getItems().length;i++){
			if (!ctab.getItem(i).isDisposed()) {
				useItem(i,null, null);
				ctab.getItem(i).dispose();
			}
		}
	}
	public void useItem(final int idx, final String template, final Kontakt adressat){
		CTabItem item=ctab.getItem(idx);
		TextContainer text=(TextContainer)item.getData("text"); //$NON-NLS-1$
		String betreff = Messages.RnPrintView_tarmedBill;
		
		// save and delete old brief
		Brief brief=(Brief)item.getData("brief"); //$NON-NLS-1$
		if ((brief != null) && brief.exists()) {
			text.saveBrief(brief,Brief.RECHNUNG);
			brief.delete();
			item.setData("brief", null);
		}
		if(template!=null){
			// don't associate a Konsultation
			Brief actBrief=text.createFromTemplateName(null,template,Brief.RECHNUNG,adressat,betreff);
			item.setData("brief",actBrief); //$NON-NLS-1$
		}
	}
	/**
	 * Druckt die Rechnung auf eine Vorlage, deren Ränder alle auf 0.5cm eingestellt sein müssen, und die unterhalb
	 * von 170 mm leer ist. (Papier mit EZ-Schein wird erwartet)
	 * Zweite und Folgeseiten müssen gem Tarmedrechnung formatiert sein.
	 * @param rn die Rechnung
	 * @param withForms 
	 * @param monitor 
	 * @return
	 */ 
	@SuppressWarnings("unchecked") 
	public boolean doPrint(final Rechnung rn, final IRnOutputter.TYPE rnType, final boolean withESR, final boolean withForms, final boolean doVerify, final IProgressMonitor monitor){
		Mandant mSave=Hub.actMandant;
		monitor.subTask(rn.getLabel());
		GlobalEvents.getInstance().fireSelectionEvent(rn);
		existing=ctab.getItems().length;
		String printer=null;
		//String tray=null;
		XMLExporter xmlex=new XMLExporter();
		DecimalFormat df=new DecimalFormat("0.00"); //$NON-NLS-1$
		Document xmlRn=xmlex.doExport(rn,null, rnType, doVerify);
		if(rn.getStatus()==RnStatus.FEHLERHAFT){
			return false;
		}
		Element invoice=xmlRn.getRootElement().getChild("invoice", XMLExporter.ns);
		Element balance=invoice.getChild("balance", XMLExporter.ns);
		paymentMode="TG"; //fall.getPaymentMode();
		Element eTiers=invoice.getChild("tiers_garant", XMLExporter.ns);
		if(eTiers==null){
			eTiers=invoice.getChild("tiers_payant",XMLExporter.ns);
			paymentMode="TP";
		}
		
		Mandant mnd=rn.getMandant();
		Hub.setMandant(mnd);
		Rechnungssteller rs=mnd.getRechnungssteller();
		GlobalEvents.getInstance().fireSelectionEvent(rs);
		Fall fall=rn.getFall();
		
		// make sure the Textplugin can replace all fields
		fall.setInfoString("payment", paymentMode);
		fall.setInfoString("Gesetz", TarmedRequirements.getGesetz(fall));
		mnd.setInfoElement("EAN", TarmedRequirements.getEAN(mnd));
		rs.setInfoElement("EAN", TarmedRequirements.getEAN(rs));
		mnd.setInfoElement("KSK", TarmedRequirements.getKSK(mnd));
		mnd.setInfoElement("NIF", TarmedRequirements.getNIF(mnd));
		if(!mnd.equals(rs)){
			rs.setInfoElement("EAN", TarmedRequirements.getEAN(rs));
			rs.setInfoElement("KSK", TarmedRequirements.getKSK(rs));
			rs.setInfoElement("NIF", TarmedRequirements.getNIF(rs));
		}
		
		GlobalEvents.getInstance().fireSelectionEvent(fall);
		Patient pat=fall.getPatient();
		Kontakt adressat;
		
		
		if(paymentMode.equals("TP")){ //$NON-NLS-1$
			adressat=fall.getRequiredContact(TarmedRequirements.INSURANCE);
		}else{
			adressat=fall.getGarant();
		}
		if((adressat==null) || (!adressat.exists())){
			adressat=pat;
		}
		adressat.getPostAnschrift(true); // damit sicher eine existiert
		String userdata=rn.getRnId();
		ESR esr=new ESR(rs.getInfoString(ta.ESRNUMBER),rs.getInfoString(ta.ESRSUB),userdata,ESR.ESR27);
		Money mDue=XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount_due"));
		Money mPaid=XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount_prepaid"));
		String offenRp=mDue.getCentsAsString();
		//Money mEZDue=new Money(xmlex.mTotal);
		Money mEZDue=XMLTool.xmlDoubleToMoney(balance.getAttributeValue("amount_obligations"));
		if(withESR==true){
			CTabItem ctEZ;
			TextContainer text;
			String tmpl = "Tarmedrechnung_EZ"; //$NON-NLS-1$
			if ((rn.getStatus() == RnStatus.MAHNUNG_1) || (rn.getStatus() == RnStatus.MAHNUNG_1_GEDRUCKT)) {
				tmpl="Tarmedrechnung_M1"; //$NON-NLS-1$
			}else if((rn.getStatus() == RnStatus.MAHNUNG_2) || (rn.getStatus() == RnStatus.MAHNUNG_2_GEDRUCKT)){
				tmpl="Tarmedrechnung_M2"; //$NON-NLS-1$
			}else if((rn.getStatus() == RnStatus.MAHNUNG_3) || (rn.getStatus() == RnStatus.MAHNUNG_3_GEDRUCKT)){
				tmpl = "Tarmedrechnung_M3"; //$NON-NLS-1$
			}
			if(--existing<0){
				ctEZ=addItem(tmpl,"ESR",adressat); //$NON-NLS-1$
			}else{
				ctEZ=ctab.getItem(0);
				useItem(0,tmpl, adressat);
			}
			List<Zahlung> extra=rn.getZahlungen();
			text=(TextContainer) ctEZ.getData("text"); //$NON-NLS-1$
			Kontakt bank=Kontakt.load(rs.getInfoString(ta.RNBANK));
			final StringBuilder sb=new StringBuilder();
			String sTarmed=balance.getAttributeValue("amount_tarmed");
			String sMedikament=balance.getAttributeValue("amount_drug");
			String sAnalysen=balance.getAttributeValue("amount_lab");
			String sMigel=balance.getAttributeValue("amount_migel");
			String sPhysio=balance.getAttributeValue("amount_physio");
			String sOther=balance.getAttributeValue("amount_unclassified");
			sb.append(Messages.RnPrintView_tarmedPoints).append(sTarmed).append("\n"); 
			sb.append(Messages.RnPrintView_medicaments).append(sMedikament).append("\n"); 
			sb.append(Messages.RnPrintView_labpoints).append(sAnalysen).append("\n"); 
			sb.append(Messages.RnPrintView_migelpoints).append(sMigel).append("\n"); 
			sb.append(Messages.RnPrintView_physiopoints).append(sPhysio).append("\n"); 
			sb.append(Messages.RnPrintView_otherpoints).append(sOther).append("\n"); 
			/*
			sb.append(Messages.RnPrintView_tarmedPoints).append(xmlex.mTarmed.getAmountAsString()).append("\n"); //$NON-NLS-2$
			sb.append(Messages.RnPrintView_medicaments).append(xmlex.mMedikament.getAmountAsString()).append("\n"); //$NON-NLS-2$
			sb.append(Messages.RnPrintView_labpoints).append(xmlex.mAnalysen.getAmountAsString()).append("\n"); //$NON-NLS-2$
			sb.append(Messages.RnPrintView_migelpoints).append(xmlex.mMigel.getAmountAsString()).append("\n"); //$NON-NLS-2$
			sb.append(Messages.RnPrintView_physiopoints).append(xmlex.mPhysio.getAmountAsString()).append("\n"); //$NON-NLS-2$
			sb.append(Messages.RnPrintView_otherpoints).append(xmlex.mUebrige.getAmountAsString()).append("\n"); //$NON-NLS-2$
			*/
			for(Zahlung z:extra){
				Money betrag=new Money(z.getBetrag()).multiply(-1.0);
				if(!betrag.isNegative()){
					sb.append(z.getBemerkung()).append(":\t").append(betrag.getAmountAsString()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
					mEZDue.addMoney(betrag);
				}
			}
			sb.append("--------------------------------------").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			
			sb.append(Messages.RnPrintView_sum).append(mEZDue);
			
			if(!mPaid.isZero()){
				sb.append(Messages.RnPrintView_prepaid).append(mPaid.getAmountAsString()).append("\n"); 
				//sb.append("Noch zu zahlen:\t").append(xmlex.mDue.getAmountAsString()).append("\n");
				sb.append(Messages.RnPrintView_topay).append(mEZDue.subtractMoney(mPaid).roundTo5().getAmountAsString()).append("\n"); 
			}
		
			
			text.getPlugin().setFont("Serif",SWT.NORMAL, 9); //$NON-NLS-1$
			text.replace("\\[Leistungen\\]",sb.toString());
		
			if(esr.printBESR(bank,adressat,rs,mEZDue.roundTo5().getCentsAsString(),text)==false){
				// avoid dead letters
				clearItems();
				Hub.setMandant(mSave);
				return false;
			}
			printer=Hub.localCfg.get("Drucker/A4ESR/Name",null); //$NON-NLS-1$
			//tray=Hub.localCfg.get("Drucker/A4ESR/Schacht",null); //$NON-NLS-1$
			// Das mit der Tray- Einstellung funktioniert sowieso nicht richtig.
			// OOo nimmt den Tray aus der Druckformatvorlage. Besser wir setzen ihn hier auf
			// null vorläufig.
			if(text.getPlugin().print(printer,null, false)==false){
			//if(text.getPlugin().print(printer,tray, false)==false){
				SWTHelper.showError("Fehler beim Drucken", "Konnte den Drucker nicht starten");
				rn.addTrace(Rechnung.REJECTED, "Druckerfehler");
				// avoid dead letters
				clearItems();
				Hub.setMandant(mSave);
				return false;
			}
			
			monitor.worked(2);
		}
		if(withForms==false){
			// avoid dead letters
			clearItems();
			Hub.setMandant(mSave);
			return true;
		}
		printer=Hub.localCfg.get("Drucker/A4/Name",null); //$NON-NLS-1$
		//tray=Hub.localCfg.get("Drucker/A4/Schacht",null); //$NON-NLS-1$
		CTabItem ctP1;
		if(--existing<0){
			ctP1=addItem("Tarmedrechnung_S1",Messages.RnPrintView_page1,adressat); //$NON-NLS-1$
		}else{
			ctP1=ctab.getItem(1);
			useItem(1,"Tarmedrechnung_S1", adressat); //$NON-NLS-1$
		}
		TextContainer text=(TextContainer) ctP1.getData("text"); //$NON-NLS-1$
		StringBuilder sb=new StringBuilder();
		Element root=xmlRn.getRootElement();
		Namespace ns=root.getNamespace();
		//Element invoice=root.getChild("invoice",ns); //$NON-NLS-1$
		if(invoice.getAttributeValue("resend").equalsIgnoreCase("true")){ //$NON-NLS-1$ //$NON-NLS-2$
			text.replace("\\[F5\\]",Messages.RnPrintView_yes); //$NON-NLS-1$
		}else{
			text.replace("\\[F5\\]",Messages.RnPrintView_no); //$NON-NLS-1$
		}

		// Vergütungsart F17
		// replaced with Fall.payment
	
		if(fall.getAbrechnungsSystem().equals("UVG")){ //$NON-NLS-1$
			text.replace("\\[F58\\]",fall.getBeginnDatum()); //$NON-NLS-1$
		}else{
			text.replace("\\[F58\\]",""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		Element detail=invoice.getChild("detail",ns); //$NON-NLS-1$
		Element diagnosis=detail.getChild("diagnosis",ns); //$NON-NLS-1$
		String type=diagnosis.getAttributeValue(Messages.RnPrintView_62);
		
		// TODO Cheap workaround, fix
		if(type.equals("by_contract")){ //$NON-NLS-1$
			type="TI-Code"; //$NON-NLS-1$
		}
		text.replace("\\[F51\\]",type); //$NON-NLS-1$
		if(type.equals("freetext")){ //$NON-NLS-1$
			text.replace("\\[F52\\]",""); //$NON-NLS-1$ //$NON-NLS-2$
			text.replace("\\[F53\\]",diagnosis.getText()); //$NON-NLS-1$
		}else{
			text.replace("\\[F52\\]",diagnosis.getAttributeValue("code")); //$NON-NLS-1$ //$NON-NLS-2$
			text.replace("\\[F53\\]",""); //$NON-NLS-1$ //$NON-NLS-2$
		}


		Element services=detail.getChild("services",ns); //$NON-NLS-1$
		List<Element> ls=services.getChildren();
		Element remark=invoice.getChild("remark"); //$NON-NLS-1$
		if(remark!=null){
			final String rem=remark.getText();
			text.getPlugin().findOrReplace(Messages.RnPrintView_remark,new ReplaceCallback(){
				public String replace(final String in) {
					return Messages.RnPrintView_remarksp+rem;
				}
			});
		}
		replaceHeaderFields(text,rn);
		text.replace("\\[F.+\\]",""); //$NON-NLS-1$ //$NON-NLS-2$
		Object cursor=text.getPlugin().insertText("[Rechnungszeilen]","\n",SWT.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
		TimeTool r=new TimeTool();
		int page=1;
		double seitentotal=0.0;
		double sumPfl=0.0;
		double sumNpfl=0.0;
		double mwst0=0.0;
		double mwst1=0.0;
		double mwst2=0.0;
		double sumMwst=0.0;
		double sumTotal=0.0;
		ITextPlugin tp=text.getPlugin();
		cmAvail=cmFirstPage;
		monitor.worked(2);
		for(Element s:ls){
			tp.setFont("Helvetica",SWT.BOLD, 7); //$NON-NLS-1$
			cursor=tp.insertText(cursor,"\t"+s.getText()+"\n",SWT.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
			tp.setFont("Helvetica",SWT.NORMAL, 8); //$NON-NLS-1$
			sb.setLength(0);
			if(r.set(s.getAttributeValue("date_begin"))==false){ //$NON-NLS-1$
				continue;
			}
			sb.append("■ "); //$NON-NLS-1$
			sb.append(r.toString(TimeTool.DATE_GER)).append("\t"); //$NON-NLS-1$
			sb.append(getValue(s,"tariff_type")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"code")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"ref_code")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"number")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"side")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"quantity")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			String val=s.getAttributeValue("unit.mt"); //$NON-NLS-1$
			if(StringTool.isNothing(val)){
				val=s.getAttributeValue("unit"); //$NON-NLS-1$
				if(StringTool.isNothing(val)){
					val="\t"; //$NON-NLS-1$
				}
			}
			sb.append(val).append("\t"); //$NON-NLS-1$
			sb.append(getValue(s,"scale_factor.mt")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			val=s.getAttributeValue("unit_factor.mt"); //$NON-NLS-1$
			if(StringTool.isNothing(val)){
				val=s.getAttributeValue("unit_factor"); //$NON-NLS-1$
				if(StringTool.isNothing(val)){
					val="\t"; //$NON-NLS-1$
				}
			}
			sb.append(val).append("\t"); //$NON-NLS-1$
			sb.append(getValue(s,"unit.tt")).append("\t\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(getValue(s,"unit_factor.tt")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("1\t1\t"); //$NON-NLS-1$
			String pfl=s.getAttributeValue("obligation"); //$NON-NLS-1$
			String vat=getValue(s,"vat_rate"); //$NON-NLS-1$
			String am=s.getAttributeValue("amount"); //$NON-NLS-1$
			//double dLine=Double.parseDouble(am);
			double dLine;
			try {
				dLine = XMLTool.xmlDoubleToMoney(am).getAmount();
			} catch (NumberFormatException ex) {
				// avoid dead letters
				clearItems();
				log.log("Fehlerhaftes Format für amount bei "+sb.toString(), Log.ERRORS);
				Hub.setMandant(mSave);
				return false;
			}
			sumTotal+=dLine;
			if(pfl.equalsIgnoreCase("true")){ //$NON-NLS-1$
				sb.append("0\t"); //$NON-NLS-1$
				sumPfl+=dLine;
			}else{
				sb.append("1\t"); //$NON-NLS-1$
				sumNpfl+=dLine;
			}
			if(vat.equals("0")){ //$NON-NLS-1$
				mwst0+=dLine;
			}else if(vat.equals("1")){ //$NON-NLS-1$
				mwst1+=dLine;
				sumMwst+=(0.074*dLine);
			}else{
				mwst2+=dLine;
				sumMwst+=(0.024*dLine);
			}
			sb.append(vat).append("\t"); //$NON-NLS-1$
			
			sb.append(am);
			seitentotal+=dLine;
			sb.append("\n"); //$NON-NLS-1$
			cursor=tp.insertText(cursor,sb.toString(),SWT.LEFT);
			cmAvail-=cmPerLine;
			if(cmAvail <= 0){
				StringBuilder footer=new StringBuilder();
				cursor=tp.insertText(cursor,"\n\n",SWT.LEFT); //$NON-NLS-1$
				footer.append("■ Zwischentotal\t\tCHF\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t").append(df.format(seitentotal)); //$NON-NLS-1$
				tp.setFont("Helvetica",SWT.BOLD, 7); //$NON-NLS-1$
				cursor=tp.insertText(cursor,footer.toString(),SWT.LEFT);
				seitentotal=0.0;
				esr.printESRCodeLine(text.getPlugin(),offenRp,null);
				
				if(text.getPlugin().print(printer,/*tray*/null, false)==false){
					// avoid dead letters
					clearItems();
					Hub.setMandant(mSave);
					return false;
				}
				
				text=insertPage(++page,adressat,text,rn);
				cursor=text.getPlugin().insertText("[Rechnungszeilen]","\n",SWT.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
				cmAvail=cmMiddlePage;
				monitor.worked(2);
			}
			
		}
		cursor=tp.insertText(cursor,"\n",SWT.LEFT); //$NON-NLS-1$
		if(cmAvail<cmFooter){
			esr.printESRCodeLine(text.getPlugin(),offenRp,null);
			if(text.getPlugin().print(printer,/*tray*/null, false)==false){
				// avoid dead letters
				clearItems();
				Hub.setMandant(mSave);
				return false;
			}
			text=insertPage(++page,adressat,text,rn);
			cursor=text.getPlugin().insertText("[Rechnungszeilen]","\n",SWT.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
			monitor.worked(2);
		}
		StringBuilder footer=new StringBuilder(100);
		//Element balance=invoice.getChild("balance",ns); //$NON-NLS-1$
		
	
		cursor=text.getPlugin().insertTextAt(0,220,190,45," ",SWT.LEFT); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\tTARMED AL \t"); //$NON-NLS-1$
		footer.append(balance.getAttributeValue("amount_tarmed.mt")) //$NON-NLS-1$
		.append("  (").append(balance.getAttributeValue("unit_tarmed.mt")).append(")\t"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cursor=print(cursor,tp,false,footer.toString());
		cursor=print(cursor,tp,true,"Physio \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_physio")); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\tMiGeL \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_migel")); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\tÜbrige \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_unclassified")); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\n\tTARMED TL \t"); //$NON-NLS-1$
		footer.setLength(0);
		footer.append(balance.getAttributeValue("amount_tarmed.tt")) //$NON-NLS-1$
		.append("  (").append(balance.getAttributeValue("unit_tarmed.tt")).append(")\t"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cursor=print(cursor,tp,false,footer.toString());
		cursor=print(cursor,tp,true,"Labor \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_lab")); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\tMedi \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_drug")); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"\tKantonal \t"); //$NON-NLS-1$
		cursor=print(cursor,tp,false,getValue(balance,"amount_cantonal")); //$NON-NLS-1$
		
		footer.setLength(0);
		footer.append("\n\n").append("■ Gesamtbetrag\t\tCHF\t\t").append(df.format(sumTotal)) //$NON-NLS-1$ //$NON-NLS-2$
			.append("\tdavon PFL \t").append(df.format(sumPfl)).append("\tAnzahlung \t") //$NON-NLS-1$ //$NON-NLS-2$
			.append(mPaid.getAmountAsString()).append("\tFälliger Betrag \t").append(mDue.getAmountAsString()) //$NON-NLS-1$
			.append("\n\n■ MwSt.Nr. \t\t"); //$NON-NLS-1$
		cursor=print(cursor,tp,true,footer.toString());
		cursor=print(cursor,tp,false,"keine\n\n"); //$NON-NLS-1$
		cursor=print(cursor,tp,true,"  Code\tSatz\t\tBetrag\tMwSt\n"); //$NON-NLS-1$
		tp.setFont("Helvetica",SWT.NORMAL,9); //$NON-NLS-1$
		footer.setLength(0);
		footer.append("■ 0\t0\t\t").append(df.format(mwst0)).append("\t 0.00\n") //$NON-NLS-1$ //$NON-NLS-2$
			.append("■ 1\t7.4\t\t").append(df.format(mwst1)).append("\t").append(df.format(0.074*mwst1)).append("\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.append("■ 2\t2.4\t\t").append(df.format(mwst2)).append("\t").append(df.format(0.024*mwst2)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cursor=print(cursor,tp,false,footer.toString());
		cursor=print(cursor,tp,true,"\n Total\t\t\t"); //$NON-NLS-1$
		footer.setLength(0);
		footer.append(mDue.getAmountAsString()).append("\t").append(df.format(sumMwst)); //$NON-NLS-1$
		tp.setFont("Helvetica",SWT.BOLD,9); //$NON-NLS-1$
		tp.insertText(cursor,footer.toString(),SWT.LEFT);
		esr.printESRCodeLine(text.getPlugin(),offenRp,null);
		
		if(text.getPlugin().print(printer,/*tray*/ null, false)==false){
			// avoid dead letters
			clearItems();
			Hub.setMandant(mSave);
		 	return false;
		}
		monitor.worked(2);
		// avoid dead letters
		clearItems();
		Hub.setMandant(mSave);
		return true;
	}
	private TextContainer insertPage(final int page, final Kontakt adressat, TextContainer text, final Rechnung rn){
		CTabItem ctF;
		if(--existing<0){
			ctF=addItem("Tarmedrechnung_S2",Messages.RnPrintView_page+page,adressat); //$NON-NLS-1$
		}else{
			ctF=ctab.getItem(page);
			useItem(page,"Tarmedrechnung_S2", adressat); //$NON-NLS-1$
		}
		text=(TextContainer) ctF.getData("text"); //$NON-NLS-1$
		replaceHeaderFields(text, rn);
		text.replace("\\[Seite\\]",StringTool.pad(SWT.LEFT,'0',Integer.toString(page),2)); //$NON-NLS-1$
		return text;
		
	}
	private Object print(final Object cur, final ITextPlugin p, final boolean small, final String text){
		if(small){
			p.setFont("Helvetica",SWT.BOLD,7); //$NON-NLS-1$
		}else{
			p.setFont("Helvetica",SWT.NORMAL,9); //$NON-NLS-1$
		}
		return p.insertText(cur,text,SWT.LEFT);
	}
	private String getValue(final Element s,final String field){
		String ret=s.getAttributeValue(field);
		if(StringTool.isNothing(ret)){
			return " "; //$NON-NLS-1$
		}
		return ret;
	}
	
	private void replaceHeaderFields(final TextContainer text, final Rechnung rn){
		Fall fall=rn.getFall();
		Mandant m=rn.getMandant();
		text.replace("\\[F1\\]",rn.getRnId()); //$NON-NLS-1$
		
		String titel;
		String titelMahnung;
		
		if(paymentMode.equals("TP")){ //$NON-NLS-1$
			titel = Messages.RnPrintView_tbBill;
			
			switch(rn.getStatus()){
			case RnStatus.MAHNUNG_1_GEDRUCKT:
			case RnStatus.MAHNUNG_1:
				titelMahnung=Messages.RnPrintView_firstM;
				break;
			case RnStatus.MAHNUNG_2:
			case RnStatus.MAHNUNG_2_GEDRUCKT:
				titelMahnung=Messages.RnPrintView_secondM;
				break;
			case RnStatus.IN_BETREIBUNG:
			case RnStatus.TEILVERLUST:
			case RnStatus.TOTALVERLUST:
			case RnStatus.MAHNUNG_3:
			case RnStatus.MAHNUNG_3_GEDRUCKT:
				titelMahnung=Messages.RnPrintView_thirdM;
				break;
			default:
				titelMahnung = ""; //$NON-NLS-1$
			};
		} else {
			titel = Messages.RnPrintView_getback;
			titelMahnung = ""; //$NON-NLS-1$
		}
		
		text.replace("\\[Titel\\]",titel); //$NON-NLS-1$
		text.replace("\\[TitelMahnung\\]", titelMahnung); //$NON-NLS-1$
		
		if(fall.getAbrechnungsSystem().equals("IV")){ //$NON-NLS-1$
			text.replace("\\[NIF\\]",m.getNif()); //$NON-NLS-1$
			text.replace("\\[F60]\\",fall.getVersNummer()); //$NON-NLS-1$
		}else{
			text.replace("\\[NIF\\]",m.getKsk()); //$NON-NLS-1$
			text.replace("\\[F60\\]",""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
	/*
	public void save() {
		if(actBrief!=null){
			actBrief.save(text.getPlugin().storeToByteArray());
		}

		
	}
	*/
	public boolean saveAs() {
		// TODO Auto-generated method stub
		return false;
	}

}
