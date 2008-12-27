/*******************************************************************************
 * Copyright (c) 2008, G. Weirich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: OffenePostenListe.java 1057 2008-12-22 22:25:21Z  $
 *******************************************************************************/
package ch.elexis.buchhaltung.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import ch.unibe.iam.scg.archie.annotations.GetProperty;
import ch.unibe.iam.scg.archie.annotations.SetProperty;
import ch.unibe.iam.scg.archie.model.AbstractDataProvider;
import ch.unibe.iam.scg.archie.model.SetDataException;
import ch.unibe.iam.scg.archie.ui.FieldTypes;

/**
 * Find all bills that are payable at a given date
 * @author user
 *
 */
public class OffenePostenListe extends AbstractDataProvider {
	private static final String NAME = "Offene Posten";
	private TimeTool stichtag = new TimeTool();
	private TimeTool startTag = new TimeTool();
	
	public OffenePostenListe(){
		super(NAME);
		startTag.set(TimeTool.MONTH,TimeTool.JANUARY);
		startTag.set(TimeTool.DAY_OF_MONTH,1);
	}
	
	public void setStartTag(TimeTool starttag){
		this.startTag.set(starttag);
	}
	
	public TimeTool getStartTag(){
		return new TimeTool(startTag);
	}
	
	public void setStichtag(TimeTool stichtag){
		this.stichtag.set(stichtag);
	}
	
	public TimeTool getStichtag(){
		return new TimeTool(stichtag);
	}
	
	@GetProperty(name = "Ausgangsdatum", fieldType = FieldTypes.TEXT_DATE)
	public String metaGetStarttag(){
		return getStartTag().toString(TimeTool.DATE_SIMPLE);
	}
	
	@SetProperty(name = "Ausgangsdatum", index = -2)
	public void metaSetStarttag(String tag) throws SetDataException{
		TimeTool tt = new TimeTool(tag);
		this.setStartTag(tt);
	}
	
	@GetProperty(name = "Stichtag", fieldType = FieldTypes.TEXT_DATE)
	public String metaGetStichtag(){
		return getStichtag().toString(TimeTool.DATE_SIMPLE);
	}
	
	@SetProperty(name = "Stichtag")
	public void metaSetStichtag(String stichtag) throws SetDataException{
		TimeTool tt = new TimeTool(stichtag);
		this.setStichtag(tt);
	}
	
	@Override
	protected IStatus createContent(IProgressMonitor monitor){
		int totalwork = 1000000;
		monitor.beginTask("Offene Rechnungen per " + getStichtag().toString(TimeTool.DATE_SIMPLE),
			totalwork);
		monitor.subTask("Datenbankabfrage");
		Query<Rechnung> qbe = new Query<Rechnung>(Rechnung.class);
		qbe.add("RnDatum", "<=", getStichtag().toString(TimeTool.DATE_COMPACT));
		qbe.add("RnDatum", ">=", getStartTag().toString(TimeTool.DATE_COMPACT));
		List<Rechnung> rnn = qbe.execute();
		monitor.worked(1000);
		int step = totalwork / rnn.size();
		monitor.subTask("Analysiere Rechnungen");
		final ArrayList<Comparable<?>[]> result = new ArrayList<Comparable<?>[]>();
		TimeTool now = getStichtag();
		for (Rechnung rn : rnn) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			Fall fall = rn.getFall();
			if (fall != null) {
				Patient pat = fall.getPatient();
				Money betrag = rn.getBetrag();
				
				if ((pat != null) && (betrag != null) && (!betrag.isNeglectable())) {
					int status = rn.getStatusAtDate(now);
					if (RnStatus.isActive(status)) {
						Comparable[] row = new Comparable[this.getDataSet().getHeadings().size()];
						row[0] = Integer.parseInt(pat.get("PatientNr"));
						row[1] = rn.getNr();
						List<Zahlung> zahlungen = rn.getZahlungen();
						for (Zahlung z : zahlungen) {
							TimeTool tt = new TimeTool(z.getDatum());
							if (tt.isAfter(now)) {
								continue;
							}
							betrag.subtractMoney(z.getBetrag());
						}
						row[3] = betrag.getAmountAsString();
						row[2] = RnStatus.getStatusText(status);
						result.add(row);
					}
					
				}
			}
			monitor.worked(step);
		}
		this.dataSet.setContent(result);
		
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	protected List<String> createHeadings(){
		List<String> ret = new ArrayList<String>();
		ret.add("Patient-Nr");
		ret.add("Rechnungs-Nr");
		ret.add("Rechnungs-Status");
		ret.add("Offener Betrag");
		return ret;
	}
	
	@Override
	public String getDescription(){
		return "Offene Posten";
	}
	
}
