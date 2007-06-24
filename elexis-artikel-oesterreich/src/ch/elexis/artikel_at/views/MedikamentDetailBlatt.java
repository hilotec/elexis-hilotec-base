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
 *  $Id: MedikamentDetailBlatt.java 2365 2007-05-10 14:04:26Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_at.views;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.InputData;
import ch.rgw.tools.ExHandler;

public class MedikamentDetailBlatt extends Composite {
	InputData[] fields=new InputData[]{
			new InputData("Pharma-ZNr","ExtInfo",InputData.Typ.STRING,"PhZNr"),
			new InputData("ZNr","ExtInfo",InputData.Typ.STRING,"ZNr"),
			//new InputData("ZNrNum","ExtInfo",InputData.Typ.STRING,"ZNrNum"),
			//new InputData("SUnit","ExtInfo",InputData.Typ.STRING,"SUnit"),
			new InputData("DoLC","ExtInfo",InputData.Typ.STRING,"DoLC"),
			//new InputData("Storage","ExtInfo",InputData.Typ.STRING,"Storage"),
			//new InputData("Quantity","ExtInfo",InputData.Typ.STRING,"Quantity"),
			//new InputData("Unit","ExtInfo",InputData.Typ.STRING,"Unit"),
			//new InputData("EnhUnitDesc","ExtInfo",InputData.Typ.STRING,"EnhUnitDesc"),
			new InputData("KVP","ExtInfo",InputData.Typ.CURRENCY,"KVP"),
			new InputData("AVP","ExtInfo",InputData.Typ.CURRENCY,"AVP"),
			new InputData("ZInh","ExtInfo",InputData.Typ.STRING,"ZInh"),
			new InputData("Remb","ExtInfo",InputData.Typ.STRING,"Remb")
			
		};
	LabeledInputField.AutoForm fld;
	ScrolledForm form;
	Text fullName;
	Text tLagerung;
	Text tUnit;
	Text tIndikation, tRules, tRemarks;
	Group gRsigns, gSsigns;
	Button[] bRsigns, bSsigns;
	Composite texte;
	Composite parent;
	
	public MedikamentDetailBlatt(Composite pr){
		super(pr,SWT.NONE);
		parent=pr;
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form=Desk.theToolkit.createScrolledForm(this);
		Composite ret=form.getBody();
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		fullName=SWTHelper.createText(Desk.theToolkit, ret, 3, SWT.BORDER|SWT.READ_ONLY|SWT.WRAP);
		fullName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Group g0=new Group(ret,SWT.NONE);
		g0.setText("Packungs- und Lagerungsangaben");
		g0.setLayout(new GridLayout());
		g0.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tUnit=Desk.theToolkit.createText(g0, "",SWT.BORDER|SWT.READ_ONLY);
		tUnit.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tLagerung=Desk.theToolkit.createText(g0, "",SWT.BORDER|SWT.READ_ONLY);
		tLagerung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		fld=new LabeledInputField.AutoForm(ret,fields);
		fld.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		fld.setEnabled(false);
		Desk.theToolkit.adapt(fld);
		gRsigns=new Group(ret,SWT.NONE);
		gRsigns.setText("RSigns");
		ColumnLayout cl1=new ColumnLayout();
		cl1.topMargin=15;
		cl1.bottomMargin=15;
		cl1.minNumColumns=3;
		cl1.maxNumColumns=10;
		gRsigns.setLayout(cl1);
		gRsigns.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bRsigns=new Button[Medikament.RSIGNS.length];
		for(int i=0;i<Medikament.RSIGNS.length;i++){
			bRsigns[i]=Desk.theToolkit.createButton(gRsigns, Medikament.RSIGNS[i], SWT.CHECK);
		}
		gRsigns.setEnabled(false);
		Desk.theToolkit.adapt(gRsigns);
		gSsigns=new Group(ret,SWT.NONE);
		gSsigns.setText("SSigns");
		gSsigns.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ColumnLayout cl2=new ColumnLayout();
		cl2.topMargin=15;
		cl2.bottomMargin=15;
		cl2.minNumColumns=3;
		cl2.maxNumColumns=10;
		bSsigns=new Button[Medikament.SSIGNS.length];
		gSsigns.setLayout(cl2);
		for(int i=0;i<Medikament.SSIGNS.length;i++){
			bSsigns[i]=Desk.theToolkit.createButton(gSsigns, Medikament.SSIGNS[i], SWT.CHECK);
		}
		gSsigns.setEnabled(false);
		Desk.theToolkit.adapt(gSsigns);
		texte=Desk.theToolkit.createComposite(ret);
		texte.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		texte.setLayout(new GridLayout());
		tIndikation=SWTHelper.createText(Desk.theToolkit, texte, 4, SWT.READ_ONLY|SWT.WRAP);
		tRules=SWTHelper.createText(Desk.theToolkit, texte, 4, SWT.READ_ONLY|SWT.WRAP);
		tRemarks=SWTHelper.createText(Desk.theToolkit, texte, 4, SWT.READ_ONLY|SWT.WRAP);
		Hyperlink hl=Desk.theToolkit.createHyperlink(ret, "Zeichenerklärung", SWT.NONE);
		hl.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				IWorkbenchPage rnPage=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					rnPage.showView(ZeichenErklaerung.ID);
				} catch (PartInitException e1) {
					ExHandler.handle(e1);
				}
			}
			
		});

	}
	public void display(Medikament med) {
		form.setText(med.getExt("SName"));
		fullName.setText(med.getExt("OName"));
		StringBuilder sb=new StringBuilder();
		sb.append(med.getExt("Quantity")).append(" ").append(med.getExt("Unit")).append(" (")
			.append(med.getExt("EnhUnitDesc")).append(")");
		tUnit.setText(sb.toString());
		tLagerung.setText(med.getExt("Storage"));
		fld.reload(med);
		Hashtable extInfo=med.getHashtable("ExtInfo");
		Hashtable<String,String> ssigns=(Hashtable<String,String>)extInfo.get("SSigns");
		if(ssigns!=null){
			for(int i=0;i<Medikament.SSIGNS.length;i++){
				bSsigns[i].setSelection(ssigns.get(Medikament.SSIGNS[i]).equals("1"));
			}	
		}
		Hashtable<String,String> rsigns=(Hashtable<String,String>)extInfo.get("RSigns");
		if(rsigns!=null){
			for(int i=0;i<Medikament.RSIGNS.length;i++){
				String val=rsigns.get(Medikament.RSIGNS[i]);
				bRsigns[i].setSelection(val.equals("1"));
			}
		}
		
		Point s=getSize();
		GridData gd=(GridData)texte.getLayoutData();
		gd.widthHint=s.x;
		texte.setLayoutData(gd);

		
		String t=med.getExt("RuleText");
		tRules.setText(t==null ? "" : t);
		t=med.getExt("RemarkText");
		tRemarks.setText(t==null ? "" : t);
		t=med.getExt("INDText");
		tIndikation.setText(t==null ? ""  : t);
	}
	
}
