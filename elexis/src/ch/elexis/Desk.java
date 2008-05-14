/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Desk.java 3916 2008-05-11 14:48:53Z rgw_ch $
 *******************************************************************************/

package ch.elexis;

import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.preferences.PreferenceInitializer;
import ch.rgw.IO.FileTool;
import ch.rgw.tools.StringTool;

public class Desk implements IApplication {
	public static Display theDisplay=null;
    public static FormToolkit theToolkit=null;
    public static FontRegistry theFontRegistry=null;
    public static ImageRegistry theImageRegistry=null;
    public static ColorRegistry theColorRegistry=null;
    
    public static final String COL_RED="rot";
    public static final String COL_GREEN="gruen";
    public static final String COL_BLUE="blau";
    public static final String COL_SKYTBLUE="himmelblau";
    public static final String COL_LIGHTBLUE="hellblau";
    public static final String COL_BLACK="schwarz";
    public static final String COL_GREY="grau";
    public static final String COL_WHITE="weiss";
    public static final String COL_DARKGREY="dunkelgrau";
    public static final String COL_LIGHTGREY="hellgrau";
    public static final String COL_GREY60="grau60";
    public static final String COL_GREY20="grau20";
    
    /** Returning to some home place */
    public static final String IMG_HOME="home"; //$NON-NLS-1$
    /** An Address label */
    public static final String IMG_ADRESSETIKETTE="adressetikette"; //$NON-NLS-1$
    /** a label with patient data */
    public static final String IMG_PATIENTETIKETTE="patientetikette"; //$NON-NLS-1$
    /** a label with some identity number (e.g. for lab orders) */
    public static final String IMG_VERSIONEDETIKETTE="auftragsetikette"; //$NON-NLS-1$
    /** deleting items */
    public static final String IMG_DELETE="delete"; //$NON-NLS-1$
    /** a male */
    public static final String IMG_MANN="mann"; //$NON-NLS-1$
    /** a female */
    public static final String IMG_FRAU="frau"; //$NON-NLS-1$
    /** a Very Important Person */
    public static final String IMG_VIP= "vip"; //$NON-NLS-1$
    
    /** a printer */
    public static final String IMG_PRINTER="printer"; //$NON-NLS-1$
    //public static final String IMG_PRINT="print"; //$NON-NLS-1$
    
    /** a filter */
    public static final String IMG_FILTER="filter"; //$NON-NLS-1$
    /** creating a new Object */
    public static final String IMG_NEW="new"; //$NON-NLS-1$
    /** importing items */
    public static final String IMG_IMPORT="import"; //$NON-NLS-1$
    /** exporting items */
    public static final String IMG_EXPORT="export"; //$NON-NLS-1$
    
    /** the 48x48 pixel version of the elexis(tm) logo */
    public static final String IMG_LOGO48="elexislogo48"; //$NON-NLS-1$
    /** editing an item */
    public static final String IMG_EDIT="edit"; //$NON-NLS-1$
    /** warning */
    public static final String IMG_ACHTUNG="achtung"; //$NON-NLS-1$
    /** ok */
    public static final String IMG_OK="AllesOK"; //$NON-NLS-1$
    /** tick */
    public static final String IMG_TICK="abgehakt"; //$NON-NLS-1$
    /** error */
	public static final String IMG_FEHLER = "fehler"; //$NON-NLS-1$
	/** refresh/reload */
	public static final String IMG_REFRESH="refresh"; //$NON-NLS-1$
	/** wizard/doing things automagically */
	public static final String IMG_WIZARD="wizard"; //$NON-NLS-1$
	/** add something to an existing object */
	public static final String IMG_ADDITEM="addItem"; //$NON_NLS-1$
	/** remove something from an existing object */
	public static final String IMG_REMOVEITEM="minus";	//$NON_NLS-1$
	/** excalamation mark red*/
	public static final String IMG_AUSRUFEZ_ROT="ausrufezeichen_rot"; //$NON_NLS-1$
	/** exclamantion mark */
	public static final String IMG_AUSRUFEZ="ausrufezeichen"; //$NON_NLS-1$
	/** computer network */
	public static final String IMG_NETWORK="netzwerk"; //$NON_NLS-1$
	/** a book */
	public static final String IMG_BOOK= "buch";
	/* a person */
	public static final String IMG_PERSON = "person"; //$NON_NLS-1$
	public static final String IMG_PERSON_OK="personOK";  //$NON_NLS-1$
	public static final String IMG_DISK= "diskette";  //$NON_NLS-1$
	public static final String IMG_LOCK_CLOSED="schloss_zu";  //$NON_NLS-1$
	
	public static final String FONT_SMALL="small";
    
	public Desk(){
		getDisplay();
		getImageRegistry();
		getColorRegistry();
		getFontRegistry();
		getToolkit();
	}
	public Object start(IApplicationContext context) throws Exception {
		Map<String, String> args=context.getArguments();
		if(args.containsKey("--clean-all")){
			String p=PreferenceInitializer.getDefaultDBPath();
			FileTool.deltree(p);
			Hub.localCfg.clear();
			Hub.localCfg.flush();
		}
		try {
			// 	Wir wollen die schicken runden Tabs von Eclipse 3.x
			PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
			// Aber die Animationen sind eher nervend, nicht?
			PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);
			
			int returnCode = PlatformUI.createAndRunWorkbench(theDisplay, new ApplicationWorkbenchAdvisor());
			// Die Funktion kehrt erst beim Programmende zurück.
			Hub.heart.suspend();
			System.out.println(Messages.Desk_37);
			Hub.localCfg.flush();
			if(Hub.globalCfg!=null){
				Hub.globalCfg.flush();
			}
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {				// aufräumen
			if(theToolkit!=null){
				theToolkit.dispose();
			}
			if(theImageRegistry!=null){
				theImageRegistry.dispose();
			}
			if((theDisplay!=null) && (!theDisplay.isDisposed())){
				theDisplay.dispose();
			}
		}

	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	static String getImageBase(){
		String imageBase=Hub.localCfg.get(PreferenceConstants.USR_PLAF,"rsc/");
		return imageBase;
	}
	public ImageRegistry getImageRegistry(){
		if(theImageRegistry==null){
			theImageRegistry=new ImageRegistry(theDisplay);
			String imageBase=getImageBase();
			synchronized(theImageRegistry){
				theImageRegistry.put(IMG_HOME,Hub.getImageDescriptor(imageBase+"home.ico")); //$NON-NLS-1$
				theImageRegistry.put(IMG_ADRESSETIKETTE,Hub.getImageDescriptor(imageBase+"adretikette.ico")); //$NON-NLS-1$
				theImageRegistry.put(IMG_PATIENTETIKETTE,Hub.getImageDescriptor(imageBase+"patetikette.ico")); //$NON-NLS-1$
				theImageRegistry.put(IMG_VERSIONEDETIKETTE, Hub.getImageDescriptor(imageBase+"patvetikette.ico")); //$NON-NLS-1$
				theImageRegistry.put(IMG_DELETE,Hub.getImageDescriptor(imageBase+"delete.gif")); //$NON-NLS-1$
				theImageRegistry.put(IMG_MANN,Hub.getImageDescriptor(imageBase+"mann.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_FRAU,Hub.getImageDescriptor(imageBase+"frau.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_VIP,Hub.getImageDescriptor(imageBase+"vip.png")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_PRINTER,Hub.getImageDescriptor(imageBase+"printer.png")); //$NON-NLS-1$
		        //theImageRegistry.put(IMG_PRINT,Hub.getImageDescriptor(imageBase+"print.gif")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_FILTER,Hub.getImageDescriptor(imageBase+"filter_ps.gif")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_NEW,Hub.getImageDescriptor(imageBase+"new2.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_LOGO48,Hub.getImageDescriptor(imageBase+"elexis48.png")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_IMPORT,Hub.getImageDescriptor(imageBase+"import.gif")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_EDIT, Hub.getImageDescriptor(imageBase+"schreiben.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_ACHTUNG, Hub.getImageDescriptor(imageBase+"achtung.png")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_OK, Hub.getImageDescriptor(imageBase+"ok.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_TICK, Hub.getImageDescriptor(imageBase+"tick.png")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_FEHLER,Hub.getImageDescriptor(imageBase+"fehler.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_REFRESH, Hub.getImageDescriptor(imageBase+"refresh.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_WIZARD, Hub.getImageDescriptor(imageBase+"wizard.ico")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_ADDITEM, Hub.getImageDescriptor(imageBase+"add.gif")); //$NON-NLS-1$
		        theImageRegistry.put(IMG_EXPORT, Hub.getImageDescriptor(imageBase+"page_go.png")); //$NON_NLS-1$
		        theImageRegistry.put(IMG_AUSRUFEZ, Hub.getImageDescriptor(imageBase+"ausrufez.png")); //$NON_NLS-1$
		        theImageRegistry.put(IMG_AUSRUFEZ_ROT, Hub.getImageDescriptor(imageBase+"ausrufez_rot.ico")); //$NON_NLS-1$
		        theImageRegistry.put(IMG_REMOVEITEM, Hub.getImageDescriptor(imageBase+"minus.ico")); //$NON_NLS-1$
		        theImageRegistry.put(IMG_NETWORK, Hub.getImageDescriptor(imageBase+"netzwerk.ico")); //$NON_NLS-1$
		        theImageRegistry.put(IMG_BOOK, Hub.getImageDescriptor(imageBase+"book.png"));  //$NON_NLS-1$
		        theImageRegistry.put(IMG_PERSON, Hub.getImageDescriptor(imageBase+"person.ico"));  //$NON_NLS-1$
		        theImageRegistry.put(IMG_PERSON_OK, Hub.getImageDescriptor(imageBase+"personok.ico"));  //$NON_NLS-1$
		        theImageRegistry.put(IMG_DISK, Hub.getImageDescriptor(imageBase+"floppy.png"));  //$NON_NLS-1$
		        theImageRegistry.put(IMG_LOCK_CLOSED, Hub.getImageDescriptor(imageBase+"emblem-readonly.png"));  //$NON_NLS-1$
			}
		}
		return theImageRegistry;
	}
	public static ImageDescriptor getImageDescriptor(String imagename){
		ImageDescriptor ret=theImageRegistry.getDescriptor(imagename);
		if(ret==null){
			ret=Hub.getImageDescriptor(getImageBase()+imagename+".png");
			if(ret==null){
				ret=Hub.getImageDescriptor(getImageBase()+imagename+".gif");
			}
			if(ret==null){
				ret=Hub.getImageDescriptor(getImageBase()+imagename+".ico");
			}
			if(ret!=null){
				theImageRegistry.put(imagename, ret);
			}
		}
		return ret;
	}
	
	public static Image getImage(String name){
		Image ret=theImageRegistry.get(name);
		if(ret==null){
			ImageDescriptor id=getImageDescriptor(name);
			if(id!=null){
				ret=id.createImage();
				theImageRegistry.put(name, ret);
			}
		}
		return ret;
	}
	public ColorRegistry getColorRegistry(){

		if(theColorRegistry==null){
			theColorRegistry=new ColorRegistry(theDisplay,true);
		}
		return theColorRegistry;
	}
	
	public FormToolkit getToolkit(){
		if(theToolkit==null){
			theToolkit=new FormToolkit(theDisplay);
		}
		return theToolkit;
	}
	
	public Display getDisplay(){
		if(theDisplay==null){
			theDisplay = PlatformUI.createDisplay();
		}
		return theDisplay;
	}
	
	public FontRegistry getFontRegistry(){
		if(theFontRegistry==null){
			theFontRegistry=new FontRegistry(theDisplay,true);
		}
		return theFontRegistry;
	}
	/** 
	 * Eine Color aus einer RGB-Beschreibung als Hex-String herstellen
	 * @param coldesc Die Farbe als Beschreibung in Hex-Form
	 * @return die Farbe als Color, ist in Regisry gespeichert
	 */
	public static Color getColorFromRGB(final String coldesc){
		String col=StringTool.pad(SWT.LEFT, '0', coldesc, 6);
		if(!theColorRegistry.hasValueFor(col)){
			RGB rgb=new RGB(Integer.parseInt(col.substring(0,2),16),
					Integer.parseInt(col.substring(2,4),16),
					Integer.parseInt(col.substring(4,6),16));
			theColorRegistry.put(col, rgb);
		}
		return theColorRegistry.get(col);
	}
	
	/**
	 * Eine Hex-String Beeschreibung einer Farbe liefern
	 * @param rgb Die Farbe in RGB-Form
	 * @return
	 */
	public static String createColor(final RGB rgb){
		StringBuilder sb=new StringBuilder();
		sb.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.red),2))
			.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.green),2))
			.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.blue),2));
		String srgb=sb.toString();
		theColorRegistry.put(srgb, rgb);
		return srgb;
	}
	
	public static Shell getTopShell(){
		Shell ret=null;
		if(theDisplay!=null){
			ret=theDisplay.getActiveShell();
		}
		if(ret==null){
			ret=Hub.getActiveShell();
		}	
		if(ret==null){
			ret=new Shell(theDisplay);
		}
		return ret;
	}
}
