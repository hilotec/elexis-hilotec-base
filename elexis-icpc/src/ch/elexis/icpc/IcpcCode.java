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
 *    $Id: IcpcCode.java 2739 2007-07-07 14:07:55Z rgw_ch $
 *******************************************************************************/
package ch.elexis.icpc;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Desk;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.Tree;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.VersionInfo;

public class IcpcCode extends PersistentObject implements IDiagnose {
	static final String TABLENAME="CH_ELEXIS_ICPC";
	static final String VERSION="1.1.0";
	public static final String createDB="CREATE TABLE "+TABLENAME+" ("+
	"ID			CHAR(3) primary key,"+
	"deleted	CHAR(1) default '0',"+
	"component	CHAR(2),"+
	"short		VARCHAR(80),"+
	"icd10		    TEXT,"+
	"txt			TEXT,"+	
	"criteria		TEXT,"+
	"inclusion 		TEXT,"+
	"exclusion		TEXT,"+
	"consider		TEXT,"+
	"note			TEXT);"+
	"create index "+TABLENAME+"_IDX1 ON "+TABLENAME+" (component);"+
	"INSERT INTO "+TABLENAME+" (ID,txt) VALUES ('ver','"+VERSION+"');";
	
	private static Tree root;
	private String realCode;
	
	static{
		addMapping(TABLENAME,"component","text=txt","short","icd10","criteria","inclusion",
				"exclusion","consider","note");
	}
	public static boolean initialize(){
		try{
			ByteArrayInputStream bais=new ByteArrayInputStream(createDB.getBytes("UTF-8"));
			return j.execScript(bais,true, false);
		}catch(Exception ex){
			ExHandler.handle(ex);
			return false;
		}
	}
	public static Tree getRoot(){
		if(root==null){
			reload();
		}
		return root;
	}
	@Override
	public String getLabel() {
		if(realCode==null){
			return getId()+" "+get("short");
		}
		return realCode+" "+get("short");
	}
	public void setLabel(String l){
		realCode=l;
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public String getCode() {
		return realCode==null ? getId() : realCode;
	}

	public String getCodeSystemCode() {
		return "999";
	}

	public String getCodeSystemName() {
		return "Icpc";
	}

	public String getText() {
		return get("text");
	}

	protected IcpcCode(){}
	protected IcpcCode(String id){
		super(id);
	}
	public static IcpcCode load(String id){
		return new IcpcCode(id);
	}
	
	public static final String[] classes={
		"A: General and unspecified",
		"B: Blood, blood-forming organs and immune mechanism(spleen, bone marrow)",
		"D: Digestive",
		"F: Eye",
		"H: Ear (Hearing)",
		"K: Circulatory",
		"L: Musculoskeletal (Locomotion)",
		"N: Neurological",
		"P: Psychological",
		"R: Respiratory",
		"S: Skin",
		"T: Endocrine, metabolic and nutritional",
		"U: Urological",
		"W: Pregnancy, child-bearing, family planning",
		"X: Female genital",
		"Y: Male genital",
		"Z Social Problems"
	};
	
	public static final String[] components={
		"1: Complaint and symptom component",
		"2: Diagnostic, screening and preventive component",
		"3: Medication, treatment, procedure component",
		"4: Test result component",
		"5: Administrative component",
		"6: Referrals and other reasons for encounter",
		"7: Diagnosis/disease component"
	};
	
	@SuppressWarnings("unchecked")
	public static void reload(){
		IcpcCode ic=IcpcCode.load("ver");
		if(!ic.exists()){
			if(!IcpcCode.initialize()){
				MessageDialog.openError(Desk.theDisplay.getActiveShell(), "Fehler bei ICPC-Code", "Konnte die Datenbank nicht erstellen");
				return;
			}
		}else{
			VersionInfo vi=new VersionInfo(ic.getText());
			if(vi.isOlder(VERSION)){
				if(vi.isOlder("1.1.0")){
					PersistentObject.j.exec("ALTER TABLE "+TABLENAME+" ADD deleted CHAR(1) default '0';");
					ic.set("text", VERSION);
				}
			}
		}
		IcpcCode.root=new Tree(null,null);
		Query<IcpcCode> qbe=new Query<IcpcCode>(IcpcCode.class);
		for(int i=classes.length-1;i>=0;i--){
			String cl=classes[i];
			Tree tClass=new Tree(IcpcCode.root,cl);
			for(int j=components.length-1;j>=0;j--){
				String cmp=components[j];
				Tree tComp=new Tree(tClass,cmp);
				qbe.clear();
				qbe.add("component", "=", cmp.substring(0,1));
				qbe.startGroup();
				qbe.add("ID", "Like", "*%");
				qbe.or();
				qbe.add("ID", "Like", cl.substring(0,1)+"%");
				qbe.endGroup();
				qbe.orderBy(true, new String[]{"ID"});
				List<IcpcCode> list=qbe.execute();
				for(IcpcCode code:list){
					code.setLabel(cl.substring(0,1)+code.getId().substring(1));
					new Tree(tComp,code);
				}
			}
			
		}
		
	}
	@Override
	public boolean isDragOK() {
		if(getId().length()==3){
			return true;
		}
		return super.isDragOK();
	}
	@Override
	public String storeToString(){
		StringBuilder sb=new StringBuilder();
		sb.append(getClass().getName()).append("::").append(getId()).append("::");
		sb.append(realCode==null ? "" : realCode);
		return sb.toString();
	}
	public Iterable<IAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}
}
