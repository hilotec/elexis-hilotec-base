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
 *  $Id: UserPreferences.java 3862 2008-05-05 16:14:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.io.*;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.data.NamedBlob;
import ch.elexis.util.SWTHelper;
import ch.rgw.IO.FileTool;
import ch.rgw.IO.InMemorySettings;
import ch.rgw.IO.Settings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class UserPreferences extends PreferencePage implements
		IWorkbenchPreferencePage {

	Button bLoad, bSave, bWorkspaceLoad, bWorkspaceSave;
	Text tLoad, tSave, tWorkspaceLoad, tWorkspaceSave;
	
	public UserPreferences(){
		noDefaultAndApplyButton();
	}
	@Override
	protected Control createContents(Composite parent) {
		final String layoutfile=Platform.getInstanceLocation().getURL().getPath()+File.separator+".metadata"+
			File.separator+".plugins"+File.separator+"org.eclipse.ui.workbench"+File.separator+"workbench.xml";
		Composite ret=new Composite(parent,SWT.NONE);
		GridLayout gl=new GridLayout(2,false);
		gl.verticalSpacing=10;
		ret.setLayout(gl);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Label desc=new Label(ret,SWT.WRAP);
		desc.setText("Die Einstellungen in diesem Zweig sind anwenderspezifisch. Sie können die\n"+
				"Einstellungen für sich spezifisch einstellen, oder aus einem gespeicherten Satz laden.\n"+
				"Sie können auch Ihre Einstellungen in einem benannten Datensatz speichern."); 
		desc.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		bLoad=new Button(ret,SWT.PUSH);
		bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bLoad.setText("Einstellungen laden von...     ");
		bLoad.setLayoutData(new GridData(GridData.FILL));
		bLoad.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String name=tLoad.getText();
				if(StringTool.isNothing(name)){
					SWTHelper.showInfo("Kein Name angegeben", "Bitte geben Sie den Namen der Konfiguration an, die Sie laden möchten");
				}else if(NamedBlob.exists("UserCfg:"+name)){
					NamedBlob blob=NamedBlob.load("UserCfg:"+name);
					InMemorySettings ims=new InMemorySettings(blob.getHashtable());
					Hub.userCfg.overlay(ims, Settings.OVL_REPLACE);
				}else{
					SWTHelper.showError("Konfiguration nicht gefunden", "Die Konfiguration mit Name "+name+" wurde nicht gefunden");
				}
			}
			
		});
		tLoad=new Text(ret,SWT.BORDER);
		tLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bSave=new Button(ret,SWT.PUSH);
		bSave.setText("Einstellungen speichern nach...");
		bSave.setLayoutData(new GridData(GridData.FILL));
		bSave.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String name=tSave.getText();
				if(StringTool.isNothing(name)){
					SWTHelper.showInfo("Kein Name angegeben", "Bitte geben Sie einen Namen für die zu speichernde Konfiguration ein");
				}else{
					NamedBlob blob=NamedBlob.load("UserCfg:"+name);
					InMemorySettings ims=new InMemorySettings();
					ims.overlay(Hub.userCfg, Settings.OVL_REPLACE);
					blob.put(ims.getNode());
					SWTHelper.showInfo("Konfiguration gespeochert", "Die aktuelle Konfiguratiion wurde unter dem Namen "+name+" gespeichert");
					tSave.setText("");
				}
			}
		});
		tSave=new Text(ret,SWT.BORDER);
		tSave.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bWorkspaceLoad=new Button(ret,SWT.PUSH);
		bWorkspaceLoad.setText("Arbeitsplatzeinstellung laden von:");
		bWorkspaceLoad.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String name=tWorkspaceLoad.getText();


				if(StringTool.isNothing(name)){
					SWTHelper.showInfo("Kein Name angegeben", "Bitte geben Sie den Namen der Konfiguration an, die Sie laden möchten");
				}else if(NamedBlob.exists("Workspace:"+name)){
					NamedBlob blob=NamedBlob.load("Workspace:"+name);
					InMemorySettings ims=new InMemorySettings(blob.getHashtable());
					final String  newloc=ims.get("perspectivelayout", null);
					if(newloc!=null){
						Hub.ShutdownJob job=new Hub.ShutdownJob(){

							public void doit() {
								try{
									File file=new File(layoutfile);
									FileTool.copyFile(file, new File(layoutfile+".bak"), FileTool.REPLACE_IF_EXISTS);
									file.delete();
									FileWriter fout=new FileWriter(file);
									fout.write(newloc);
									fout.close();
								}catch(Exception ex){
									ExHandler.handle(ex);
								}
								
							}};
						Hub.addShutdownJob(job);
						SWTHelper.showInfo("Konfiguration geladen", "Die Konfiguration wird beim nächsten Start geändert");
					}
				}else{
					SWTHelper.showError("Konfiguration nicht gefunden", "Die Konfiguration mit Name "+name+" wurde nicht gefunden");
				}
			}
			
		});
		bWorkspaceLoad.setLayoutData(new GridData(GridData.FILL));
		tWorkspaceLoad=new Text(ret,SWT.BORDER);
		tWorkspaceLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bWorkspaceSave=new Button(ret,SWT.PUSH);
		bWorkspaceSave.setText("Arbeitsplatzeinstellungen speichern nach");
		bWorkspaceSave.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String name=tWorkspaceSave.getText();

				if(StringTool.isNothing(name)){
					SWTHelper.showInfo("Kein Name angegeben", "Bitte geben Sie den Namen der Konfiguration an, die Sie speichern möchten");
				}else{
					try{
						File file=new File(layoutfile);
						FileReader reader=new FileReader(file);
						StringBuilder sb=new StringBuilder(1000);
						char[] load=new char[4096];
						while(true){
							int x=reader.read(load);
							if(x==-1){
								break;
							}
							sb.append(load, 0, x);
						}
						reader.close();
						NamedBlob blob=NamedBlob.load("Workspace:"+name);
						InMemorySettings ims=new InMemorySettings();
						ims.set("perspectivelayout", sb.toString());
						blob.put(ims.getNode());
					}catch(Exception ex){
						ExHandler.handle(ex);
					}
					
				}
			}
			
		});
		bWorkspaceSave.setLayoutData(new GridData(GridData.FILL));
		tWorkspaceSave=new Text(ret,SWT.BORDER);
		tWorkspaceSave.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
}