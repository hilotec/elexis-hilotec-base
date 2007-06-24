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
 *  $Id: Rechnungslauf.java 2378 2007-05-16 05:12:19Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.rechnung;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.Money;
import ch.rgw.tools.TimeTool;

public class Rechnungslauf implements IRunnableWithProgress {
	
	TimeTool ttFirstBefore, ttLastBefore, ttHeute,limitQuartal;
	Money mLimit;
	boolean bQuartal;
	Hashtable<Konsultation, Patient> hKons;
	KonsZumVerrechnenView kzv;
	
	public Rechnungslauf(KonsZumVerrechnenView kzv, TimeTool ttFirstBefore, TimeTool ttLastBefore, Money mLimit, boolean bQuartal){
		this.ttFirstBefore=ttFirstBefore;
		this.ttLastBefore=ttLastBefore;
		this.mLimit=mLimit;
		this.bQuartal=bQuartal;
		hKons=new Hashtable<Konsultation,Patient>(1000);
		ttHeute=new TimeTool();
		limitQuartal=new TimeTool();
		String heute=ttHeute.toString(TimeTool.DATE_COMPACT).substring(4);
		if(heute.compareTo("0930")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,9);	// 1.10.
		}else if(heute.compareTo("0630")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,6);
		}else if(heute.compareTo("0331")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,3);
		}else{
			limitQuartal.set(TimeTool.MONTH,1);
		}
		this.kzv=kzv;
	}
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
			Query<Konsultation> qbe=new Query<Konsultation>(Konsultation.class);
			qbe.add("RechnungsID", "", null);
			if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
				qbe.add("MandantID", "=", Hub.actMandant.getId());
			}
			monitor.beginTask("Analysiere Konsultationen", IProgressMonitor.UNKNOWN);
			monitor.subTask("Lese Konsultationen ein");
			List<Konsultation> list=qbe.execute();
			TimeTool cmp=new TimeTool();
			for(Konsultation k:list){
				monitor.worked(1);
				if(hKons.get(k)!=null){
					continue;
				}
				Fall kFall=k.getFall();
				if((kFall==null) || (!kFall.exists())){
					continue;
				}
				String kfID=kFall.getId();
				Patient kPatient=kFall.getPatient();
				if((kPatient==null) || (!kPatient.exists())){
					continue;
				}
				if(ttFirstBefore!=null){
					cmp.set(k.getDatum());
					if(cmp.isBefore(ttFirstBefore)){
						for(Konsultation k2:list){
							String fid=k2.get("FallID");
							if((fid!=null) && (fid.equals(kfID))){
									hKons.put(k2, kPatient);
							}
						}
					}
				}
				
				if(ttLastBefore!=null){
					cmp.set(k.getDatum());
					if(cmp.isBefore(ttFirstBefore)){
						for(Konsultation k2:list){
							String fid=k2.get("FallID");
							if((fid!=null) && (fid.equals(kfID))){
								hKons.put(k2, kPatient);
							}
						}
					}
				}
				if(mLimit!=null){
					Money sum=new Money();
					Map<Konsultation,Patient> list2=new HashMap<Konsultation,Patient>(100);
					for(Konsultation k2:list){
					String fid=k2.get("FallID");
					if((fid!=null) && (fid.equals(kfID))){
						list2.put(k2,kPatient);
							sum.addAmount(k2.getUmsatz()/100.0);
						}
					}
					if(sum.isMoreThan(mLimit)){
						hKons.putAll(list2);
					}
				}
			
				
				if(bQuartal){
					cmp.set(k.getDatum());
					if(cmp.isBefore(limitQuartal)){
						hKons.put(k, kPatient);
					}
				}
			}
			monitor.subTask("erstelle Listen");
			Enumeration<Konsultation> en=hKons.keys();
			while(en.hasMoreElements()){
				kzv.selectKonsultation(en.nextElement());
				monitor.worked(1);
			}
			monitor.done();

	}
	

}
