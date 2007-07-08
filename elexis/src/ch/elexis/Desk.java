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
 *    $Id: Desk.java 2762 2007-07-08 20:35:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.preferences.PreferenceInitializer;
import ch.rgw.IO.FileTool;
import ch.rgw.tools.StringTool;


/**
 * Haupt-Ablaufsteuerung
 * Dies ist eine Basisklasse von Eclipse, welche die Workbench startet. 
 * Enthält ausserdem Registries für einige Ressourcen
 */
public class Desk implements IPlatformRunnable {
    public static Display theDisplay;
    public static FormToolkit theToolkit;
    public static FontRegistry theFontRegistry;
    public static ImageRegistry theImageRegistry;
    public static ColorRegistry theColorRegistry;
    public static final String COL_RED="rot";
    public static final String COL_GREEN="grün";
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
    
    
    public static final String IMG_HOME="home"; //$NON-NLS-1$
    public static final String IMG_ADRESSETIKETTE="adressetikette"; //$NON-NLS-1$
    public static final String IMG_PATIENTETIKETTE="patientetikette"; //$NON-NLS-1$
    public static final String IMG_VERSIONEDETIKETTE="versionedetikette"; //$NON-NLS-1$
    public static final String IMG_DELETE="delete"; //$NON-NLS-1$
    public static final String IMG_MANN="mann"; //$NON-NLS-1$
    public static final String IMG_FRAU="frau"; //$NON-NLS-1$
    public static final String IMG_VIP= "vip"; //$NON-NLS-1$
    public static final String IMG_PRINTER="printer"; //$NON-NLS-1$
    public static final String IMG_PRINT="print"; //$NON-NLS-1$
    public static final String IMG_FILTER="filter"; //$NON-NLS-1$
    public static final String IMG_NEW="new"; //$NON-NLS-1$
    public static final String IMG_IMPORT="import"; //$NON-NLS-1$
    public static final String IMG_LOGO48="elexislogo48"; //$NON-NLS-1$
    public static final String IMG_EDIT="edit"; //$NON-NLS-1$
    public static final String IMG_ACHTUNG="achtung"; //$NON-NLS-1$
    public static final String IMG_OK="AllesOK"; //$NON-NLS-1$
	public static final String IMG_FEHLER = "fehler"; //$NON-NLS-1$
	public static final String IMG_REFRESH="refresh"; //$NON-NLS-1$
	public static final String IMG_WIZARD="wizard"; //$NON-NLS-1$
	public static final String IMG_ADDITEM="addItem"; //$NON_NLS-1$
	public static final String IMG_REMOVEITEM="minus";	//$NON_NLS-1$
	public static final String IMG_EXPORT="export";	//$NON_NLS-1$
	public static final String IMG_AUSRUFEZ_ROT="ausrufezeichen_rot"; //$NON_NLS-1$
	public static final String IMG_AUSRUFEZ="ausrufezeichen"; //$NON_NLS-1$
	public static final String IMG_NETWORK="netzwerk"; //$NON_NLS-1$
	public static final String IMG_BOOK= "buch";
	
	public static final String FONT_SMALL="small";
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	/**
	 * Diese Funktion startet die Workbench
	 * (Nach der Initialisierung des Programms und Hub#start()
	 */
	public Object run(Object args) throws Exception {
		for(String arg:(String[])args){
			if(arg.equalsIgnoreCase("--clean_all")){ //$NON-NLS-1$
				String p=PreferenceInitializer.getDefaultDBPath();
				FileTool.deltree(p);
				Hub.localCfg.clear();
				Hub.localCfg.flush();
			}
		}
		if(theDisplay==null){
			theDisplay = PlatformUI.createDisplay();
		}
		if(theToolkit==null){
			theToolkit=new FormToolkit(theDisplay);
		}
		if(theFontRegistry==null){
			theFontRegistry=new FontRegistry(theDisplay,true);
		}
		if(theColorRegistry==null){
			theColorRegistry=new ColorRegistry(theDisplay,true);
		}
		if(theImageRegistry==null){
			theImageRegistry=new ImageRegistry(theDisplay);
			theImageRegistry.put(IMG_HOME,Hub.getImageDescriptor("rsc/home.ico")); //$NON-NLS-1$
			theImageRegistry.put(IMG_ADRESSETIKETTE,Hub.getImageDescriptor("rsc/adretikette.ico")); //$NON-NLS-1$
			theImageRegistry.put(IMG_PATIENTETIKETTE,Hub.getImageDescriptor("rsc/patetikette.ico")); //$NON-NLS-1$
			theImageRegistry.put(IMG_VERSIONEDETIKETTE, Hub.getImageDescriptor("rsc/patvetikette.ico")); //$NON-NLS-1$
			theImageRegistry.put(IMG_DELETE,Hub.getImageDescriptor("rsc/delete.gif")); //$NON-NLS-1$
			theImageRegistry.put(IMG_MANN,Hub.getImageDescriptor("rsc/mann.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_FRAU,Hub.getImageDescriptor("rsc/frau.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_VIP,Hub.getImageDescriptor("rsc/vip.png")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_PRINTER,Hub.getImageDescriptor("rsc/printer.png")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_PRINT,Hub.getImageDescriptor("rsc/print.gif")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_FILTER,Hub.getImageDescriptor("rsc/filter_ps.gif")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_NEW,Hub.getImageDescriptor("rsc/new2.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_LOGO48,Hub.getImageDescriptor("rsc/elexis48.png")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_IMPORT,Hub.getImageDescriptor("rsc/import.gif")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_EDIT, Hub.getImageDescriptor("rsc/schreiben.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_ACHTUNG, Hub.getImageDescriptor("rsc/achtung.png")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_OK, Hub.getImageDescriptor("rsc/ok.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_FEHLER,Hub.getImageDescriptor("rsc/fehler.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_REFRESH, Hub.getImageDescriptor("rsc/refresh.png")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_WIZARD, Hub.getImageDescriptor("rsc/wizard.ico")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_ADDITEM, Hub.getImageDescriptor("rsc/add.gif")); //$NON-NLS-1$
	        theImageRegistry.put(IMG_EXPORT, Hub.getImageDescriptor("rsc/page_go.png")); //$NON_NLS-1$
	        theImageRegistry.put(IMG_AUSRUFEZ, Hub.getImageDescriptor("rsc/ausrufez.png")); //$NON_NLS-1$
	        theImageRegistry.put(IMG_AUSRUFEZ_ROT, Hub.getImageDescriptor("rsc/ausrufez_rot.ico")); //$NON_NLS-1$
	        theImageRegistry.put(IMG_REMOVEITEM, Hub.getImageDescriptor("rsc/minus.ico")); //$NON_NLS-1$
	        theImageRegistry.put(IMG_NETWORK, Hub.getImageDescriptor("rsc/netzwerk.ico")); //$NON_NLS-1$
	        theImageRegistry.put(IMG_BOOK, Hub.getImageDescriptor("rsc/book.png"));  //$NON_NLS-1$
		}
		try {
				// 	Wir wollen die schicken runden Tabs von Eclipse 3.x
				PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
				
				int returnCode = PlatformUI.createAndRunWorkbench(theDisplay, new ApplicationWorkbenchAdvisor());
				// Die Funktion kehrt erst beim Programmende zurück.
				Hub.heart.suspend();
				System.out.println(Messages.Desk_37);
				Hub.localCfg.flush();
				if(Hub.globalCfg!=null){
					Hub.globalCfg.flush();
				}
				if (returnCode == PlatformUI.RETURN_RESTART) {
					return IPlatformRunnable.EXIT_RESTART;
			}
			return IPlatformRunnable.EXIT_OK;
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
	/** 
	 * Eine Color aus einer RGB-Beschreibung als Hex-String herstellen
	 * @param coldesc Die Farbe als Beschreibung in Hex-Form
	 * @return die Farbe als Color, ist in Regisry gespeichert
	 */
	public static Color getColorFromRGB(String coldesc){
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
	public static String createColor(RGB rgb){
		StringBuilder sb=new StringBuilder();
		sb.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.red),2))
			.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.green),2))
			.append(StringTool.pad(SWT.LEFT, '0', Integer.toHexString(rgb.blue),2));
		String srgb=sb.toString();
		theColorRegistry.put(srgb, rgb);
		return srgb;
	}
}
