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
 *    $Id: KonsExtension.java 4135 2008-07-13 19:18:15Z rgw_ch $
 *******************************************************************************/

package ch.elexis.images;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.Desk;
import ch.elexis.text.EnhancedTextField;
import ch.elexis.util.IKonsExtension;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class KonsExtension implements IKonsExtension {
	EnhancedTextField mine;
	public String connect(EnhancedTextField tf) {
		mine=tf;
		return "bildanzeige";
	}

	public boolean doLayout(StyleRange n, String provider, String id) {
		n.background=Desk.getColor(Desk.COL_GREEN);
		return true;
	}

	public boolean doXRef(String refProvider, String refID) {
		Bild bild=Bild.load(refID);
		new BildanzeigeFenster(Desk.getTopShell(),bild).open();
		return true;
	}

	public IAction[] getActions() {
		IAction[] ret=new IAction[1];
		ret[0]= new Action("Bild einfügen..."){
			@Override
			public void run() {
				FileDialog fd=new FileDialog(Desk.getTopShell());
				String iName=fd.open();
				if(iName!=null){
					try{
						ImageLoader iml=new ImageLoader();
						iml.load(iName);
						BildImportDialog bid=new BildImportDialog(Desk.getTopShell(),iml);
						if(bid.open()==Dialog.OK){
							Bild bild=bid.result;
							mine.insertXRef(-1, "Bild: "+bild.get("Titel"), "bildanzeige", bild.getId());
						}
						
					}catch(Throwable t){
						ExHandler.handle(t);
						SWTHelper.showError("Fehler beim Laden", "Das Bild konnte nicht geladen werden "+t.getMessage());
					}
				}
			}
			
		};
		return ret;
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void removeXRef(String refProvider, String refID) {
		Bild bild=Bild.load(refID);
		bild.delete();
	}

	public void insert(Object o, int pos) {
		// TODO Auto-generated method stub
		
	}


}
