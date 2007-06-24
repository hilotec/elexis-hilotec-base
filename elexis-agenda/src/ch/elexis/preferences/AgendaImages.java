/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: AgendaImages.java 1287 2006-11-17 10:41:01Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.data.Termin;
import ch.elexis.util.Plannables;
import ch.rgw.IO.FileTool;
import ch.rgw.tools.ExHandler;

import ch.elexis.agenda.Messages;

public class AgendaImages extends PreferencePage implements
		IWorkbenchPreferencePage {
	private SettingsPreferenceStore prefs;
	
	public AgendaImages() {
		prefs=new SettingsPreferenceStore(Hub.userCfg);
        setPreferenceStore(prefs);
        setDescription(Messages.AgendaImages_imagesForAgenda); 
	}



	@Override
	protected Control createContents(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(3,false));
		for(String t:Termin.TerminTypes){
			Image img=Plannables.getTypImage(t);
			Label lImg=new Label(ret,SWT.NONE);
			lImg.setImage(img);
			Label lTx=new Label(ret,SWT.NONE);
			lTx.setText(t);
			Button bCh=new Button(ret,SWT.PUSH);
			bCh.setText(Messages.AgendaImages_change); 
			bCh.setData(t);
			bCh.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fdl=new FileDialog(parent.getShell(),SWT.OPEN);
					String dpath=Activator.getBasePath().replaceFirst("\\\\bin", "")+File.separator+"icons"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fdl.setFilterPath(dpath);
					String name=fdl.open();
					File src=new File(name);
					try {
						Image img=new Image(Desk.theDisplay,new FileInputStream(src));
						if(img!=null){					// File bezeichnet ein lesbares Bild
							File dest=new File(dpath+File.separator+src.getName());
							if(!dest.getAbsolutePath().equalsIgnoreCase(src.getAbsolutePath())){	

								// Ist noch nicht im richtigen Verzeichnis
								if(FileTool.copyFile(src, dest, FileTool.FAIL_IF_EXISTS)==false){
									MessageDialog.openError(parent.getShell(), Messages.AgendaImages_cannotCopy,  
											Messages.AgendaImages_6+name+Messages.AgendaImages_7); 
									return;
								}
							}
							String t=(String)((Button)e.getSource()).getData();
							Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX+t, Messages.AgendaImages_8+dest.getName()); 
						}
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
				
			});
		}
		return ret;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
