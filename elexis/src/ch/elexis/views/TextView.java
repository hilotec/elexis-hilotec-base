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
 *  $Id: TextView.java 4109 2008-07-06 19:35:50Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.dialogs.DocumentSelectDialog;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.util.*;
import ch.rgw.tools.ExHandler;

public class TextView extends ViewPart implements ActivationListener{
	public final static String ID="ch.elexis.TextView";
	TextContainer txt;
	//CommonViewer cv;
	Composite textContainer=null;
	private Brief actBrief;
	private Log log=Log.get("TextView");
	private IAction briefLadenAction, loadTemplateAction, loadSysTemplateAction, saveTemplateAction, 
					showMenuAction, showToolbarAction, importAction, newDocAction;
	private ViewMenus menus;
	
	
	public TextView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		txt=new TextContainer(getViewSite());
		textContainer=txt.getPlugin().createContainer(parent,new SaveHandler());
		if(textContainer==null){
			SWTHelper.showError("Konnte TextView nicht erstellen", "Das Textplugin konnte nicht korrekt geladen werden");
		}else{
			makeActions();
			menus=new ViewMenus(getViewSite());
		    //menus.createToolbar(briefNeuAction);
		    menus.createMenu(newDocAction,briefLadenAction,loadTemplateAction,loadSysTemplateAction,saveTemplateAction,showMenuAction,showToolbarAction,importAction);
		    GlobalEvents.getInstance().addActivationListener(this,this);
		    setName();
		}
	}

	@Override
	public void setFocus() {
		if(textContainer!=null){
			textContainer.setFocus();
		}
	}
	public TextContainer getTextContainer(){
		return txt;
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this,this);
		if(txt!=null){
			txt.dispose();
		}
		actBrief=null;
		super.dispose();
	}
	
	public boolean openDocument(Brief doc){
		if(txt.open(doc)==true){
			actBrief=doc;
			setName();
			return true;
		} else {
			actBrief = null;
			setName();
			return false;
		}
	}
	/**
	 * Ein Document von Vorlage erstellen. 
	 * @param template die Vorlage
	 * @param subject Titel, kann null sein
	 * @return true bei erfolg
	 */
	public boolean createDocument(Brief template,String subject){
		if(template==null){
			SWTHelper.showError("Keine Vorlage ausgesucht", "Bitte wählen Sie eine Vorlage für das Dokument aus");
			return false;
		}
		actBrief=txt.createFromTemplate(Konsultation.getAktuelleKons(),template,Brief.UNKNOWN,null, subject);
		setName();
		if(actBrief==null){
			return false;
		}
		return true;
	}
	private void makeActions(){
		briefLadenAction=new Action("Brief öffnen..."){
			@Override
			public void run() {
				Patient actPatient=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
				DocumentSelectDialog bs=new DocumentSelectDialog(getViewSite().getShell(),actPatient,DocumentSelectDialog.TYPE_LOAD_DOCUMENT);
				if(bs.open()==Dialog.OK){
					openDocument(bs.getSelectedDocument());
				}
			}
			
		};
		
		loadSysTemplateAction=new Action("Systemvorlage öffnen"){
			@Override
			public void run(){
				DocumentSelectDialog bs=new DocumentSelectDialog(getViewSite().getShell(),Hub.actMandant,DocumentSelectDialog.TYPE_LOAD_SYSTEMPLATE);
				if(bs.open()==Dialog.OK){
					openDocument(bs.getSelectedDocument());
				}
			}
		};
		loadTemplateAction=new Action("Vorlage öffnen"){
			@Override
			public void run(){
				DocumentSelectDialog bs=new DocumentSelectDialog(getViewSite().getShell(),Hub.actMandant,DocumentSelectDialog.TYPE_LOAD_TEMPLATE);
				if(bs.open()==Dialog.OK){
					openDocument(bs.getSelectedDocument());
				}
			}
		};
		saveTemplateAction=new Action("Als Vorlage speichern"){
			@Override
			public void run(){
				if(actBrief!=null){
					txt.saveTemplate(actBrief.get("Betreff"));
				}else{
					txt.saveTemplate(null);
				}
			}
		};
		
		showMenuAction=new Action("Menu anzeigen",Action.AS_CHECK_BOX){
			public void run(){
				txt.getPlugin().showMenu(isChecked());
			}
		};
		showToolbarAction=new Action("Werkzeugleiste",Action.AS_CHECK_BOX){
			public void run(){
				txt.getPlugin().showToolbar(isChecked());
			}
		};
		importAction=new Action("Text importieren"){
			@Override
			public void run(){
				try{
					FileDialog fdl=new FileDialog(getViewSite().getShell());
					String filename=fdl.open();
					if(filename!=null){
						File file=new File(filename);
						if(file.exists()){
							actBrief = null;
							setPartName(filename);
							FileInputStream fis=new FileInputStream(file);
							txt.getPlugin().loadFromStream(fis, false);
						}
						 
					}

				}catch(Throwable ex){
					ExHandler.handle(ex);
				}
			}
		};
		
		newDocAction=new Action("Neues Dokument"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
			}
			public void run(){
				actBrief = null;
				setName();
				txt.getPlugin().createEmptyDocument();
			}
			
		};
        briefLadenAction.setImageDescriptor(Hub.getImageDescriptor("rsc/kuvert.ico"));
        briefLadenAction.setToolTipText("Brief zum Bearbeiten öffnen");
        //briefNeuAction.setImageDescriptor(Hub.getImageDescriptor("rsc/schreiben.gif"));
        //briefNeuAction.setToolTipText("Einen neuen Brief erstellen");
        showMenuAction.setToolTipText("Menuleiste anzeigen");
        showMenuAction.setImageDescriptor(Hub.getImageDescriptor("rsc/menubar.ico"));
        showToolbarAction.setImageDescriptor(Hub.getImageDescriptor("rsc/toolbar.ico"));
        showToolbarAction.setToolTipText("Werkzeugleiste anzeigen");
    }
	
		class SaveHandler implements ITextPlugin.ICallback{

		public void save() {
			log.log("Save",Log.DEBUGMSG);
			if(actBrief!=null){
				actBrief.save(txt.getPlugin().storeToByteArray(),txt.getPlugin().getMimeType());
			}
		}
		public boolean saveAs(){
			log.log("Save As",Log.DEBUGMSG);
			InputDialog il=new InputDialog(getViewSite().getShell(),"Text speichern","Geben Sie bitte einen Titel für den Text ein","",null);
			if(il.open()==Dialog.OK){
				actBrief.setBetreff(il.getValue());
				return actBrief.save(txt.getPlugin().storeToByteArray(),txt.getPlugin().getMimeType());
			}
			return false;
		}
		
	}
	
	public void activation(boolean mode) {
		if(mode==false){
			if(actBrief!=null){
				actBrief.save(txt.getPlugin().storeToByteArray(),txt.getPlugin().getMimeType());
			}
			//txt.getPlugin().clear();	
		}else{
			loadSysTemplateAction.setEnabled(Hub.acl.request(AccessControlDefaults.DOCUMENT_SYSTEMPLATE));
			saveTemplateAction.setEnabled(Hub.acl.request(AccessControlDefaults.DOCUMENT_TEMPLATE));
		}
	}

	public void visible(boolean mode) {
		
	}
	void setName(){
		String n="";
		if(actBrief==null){
			setPartName("Kein Brief ausgewählt");
		}else{
			Person pat=actBrief.getPatient();
			if(pat!=null){
				n=pat.getLabel()+": ";
			}
			n+=actBrief.getBetreff();
			setPartName(n);
		}
	}

}
