/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Starter.java 1182 2006-10-29 14:48:00Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Extensions;
import ch.rgw.tools.ExHandler;

public class Starter extends ViewPart {
	private Composite contents;
	public static final String ID="Starter";
	private SwitchPerspective switcher=new SwitchPerspective();
	
	public Starter() {
		super();
        this.setPartName(Messages.getString("Starter.partname")); //$NON-NLS-1$
	}

	public void createPartControl(Composite parent) {
	
		contents=new Composite(parent,SWT.NONE);
		contents.setLayout(new FillLayout(SWT.VERTICAL));
		String sbdef=Hub.localCfg.get(PreferenceConstants.SIDEBAR,"");
		if(sbdef.equals("")){
			List<IConfigurationElement> ex=Extensions.getExtensions("ch.elexis.Sidebar");
			for(IConfigurationElement el:ex){
				sbdef+=el.getAttribute("name")+":"+el.getAttribute("ID")+",";
			}
			sbdef=sbdef.replaceFirst(",$","");
			Hub.localCfg.set(PreferenceConstants.SIDEBAR,sbdef);
			Hub.localCfg.flush();
		}
		String[] pers=sbdef.split(",");
		for(String per:pers){
			String[] def=per.split(":");
	    	if(PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(def[1])==null){
	    		sbdef=sbdef.replaceFirst(per,"");
	    		sbdef=sbdef.replaceAll(",,",",");
	    		Hub.localCfg.set(PreferenceConstants.SIDEBAR,sbdef);
	    		continue;
	    	}
			createPushButton(contents,def[0],1,def[1]);
		}
	}

	public void setFocus() {

	}
    Button createPushButton(Composite parent, String text, int col, String perspektiveID)
    {
        Button r=new Button(parent,SWT.PUSH);
        r.setText(text);
        // r.setBackground(r.getDisplay().getSystemColor(col));
        r.addSelectionListener(switcher);
        r.setData(perspektiveID);
        return r;
    }
    class SwitchPerspective extends SelectionAdapter{
    	public void widgetSelected(SelectionEvent e) {
			Button b=(Button)e.getSource();
			String p=(String)b.getData();
			try{
				IWorkbenchWindow win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				PlatformUI.getWorkbench().showPerspective(p,win);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}
    }

}
