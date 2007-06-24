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
 *  $Id: Zugriff.java 2234 2007-04-17 13:04:25Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.IACLContributor;
import ch.elexis.preferences.inputs.ACLPreferenceTree;
import ch.elexis.preferences.inputs.PrefAccessDenied;
import ch.elexis.util.Extensions;

/** Einstellungen für die Zugriffsregelung. anwender, Passworte usw. */

public class Zugriff extends PreferencePage implements IWorkbenchPreferencePage{
	ACLPreferenceTree apt;
	public Zugriff(){
		super("Zugriffsrechte");
	}
	@SuppressWarnings("unchecked")
	@Override
	protected Control createContents(Composite parent) {
		if(Hub.acl.request(AccessControlDefaults.ACL_USERS)){
			 List<IACLContributor> acls=Extensions.getClasses("ch.elexis.ACLContribution", "ACLContributor");
			 ArrayList<String> lAcls=new ArrayList<String>(100);
			 for(IACLContributor acl:acls){
				 for(String s:acl.getACL()){
					  lAcls.add(s);
					  // TODO collision detection
				 }
			 }
	
			apt= new ACLPreferenceTree(parent,(String[])lAcls.toArray(new String[0]));
			return apt;
		}else{
			return new PrefAccessDenied(parent);
		}
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean performOk() {
		apt.flush();
		return super.performOk();
	}
	@Override
    protected void performDefaults()
    {  
		apt.reload();
    }
	
}
/*
public class Zugriff extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

    private static final String[] fields={ACTION_EXIT,ACTION_PREFS,ACTION_LOGIN,
    	ACTION_CONNECT,
        LEISTUNGEN_VERRECHNEN,READ_LETZTE_BEHANDLUNG,WRITE_LETZTE_BEHANDLUNG,
        LIST_PATIENTEN,DISPLAY_KONTAKT,DISPLAY_PERSON,DISPLAY_PATIENT,DISPLAY_ORGANISATION,
        DISPLAY_ANWENDER,DISPLAY_MANDANT};
    
    private static final String[] texte={
        "Programm beenden: ","Einstellungsdialog: ","Benutzer wechseln",
        "Datenbankverbindung ändern",
        "Leistungen verrechnen","Behandlungen lesen","Behandlungen schreiben",
        "Patienten auflisten","Kontaktdetails editieren","Personendaten editieren",
        "Patientendaten editieren","Organisationsdaten editieren","Anwenderdaten editieren",
        "Mandantendaten editieren"};
    
	public Zugriff() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(new InMemorySettings()));
		setDescription("Zuweisung von Rechten");
	}

	@Override
	protected void createFieldEditors() {
		String grps=Hub.globalCfg.get(PreferenceConstants.ACC_GROUPS, "Admin");
		String[] grp=grps.split(",");
        for(int i=0;i<fields.length;i++){
            addField(new ComboFieldEditor(fields[i],texte[i],grp,getFieldEditorParent()));
            //addField(new StringFieldEditor(fields[i],texte[i],getFieldEditorParent()));
        }
    }
	public void init(IWorkbench workbench) {
        for(String s:fields){
            List<String> g=Hub.acl.groupsForGrant(s);
            getPreferenceStore().setValue(s,StringTool.join(g,","));
        }
    }

        @Override
	public boolean performOk() {
		super.performOk();
      
        for(String grant:fields){
            String pref=getPreferenceStore().getString(grant);
            for(String s:pref.split(",")){
                Hub.acl.deleteGrant(grant);
                Hub.acl.grant(s,grant);
            }
        }
		Hub.acl.flush();
		return true;
	}

    
	@Override
    protected void performDefaults()
    {  
       Hub.acl.reset();
       this.initialize();
    }
	
}*/
