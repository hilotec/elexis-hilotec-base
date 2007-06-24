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
 *  $Id: Mandanten.java 1849 2007-02-19 07:52:43Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.preferences.inputs.PrefAccessDenied;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;

public class Mandanten extends PreferencePage implements
IWorkbenchPreferencePage {
	private LabeledInputField.AutoForm lfa;
	private InputData[] def;
	
	
	
	private Hashtable<String,Mandant> hMandanten=new Hashtable<String,Mandant>();
	@SuppressWarnings("unchecked")
	@Override
	protected Control createContents(Composite parent) {
		if(Hub.acl.request(AccessControlDefaults.ACL_USERS)){
			FormToolkit tk=new FormToolkit(Desk.theDisplay);
			Form form=tk.createForm(parent);
			Composite body=form.getBody();
			body.setLayout(new GridLayout(1,false));
			Combo mandanten=new Combo(body,SWT.DROP_DOWN|SWT.READ_ONLY);
			Query qbe=new Query(Mandant.class);
			List list=qbe.execute();
			for(Mandant m:(List<Mandant>)list){
				mandanten.add(m.getLabel());
				hMandanten.put(m.getLabel(),m);
			}
			mandanten.addSelectionListener(new SelectionAdapter(){
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					Combo source=(Combo)e.getSource();
					String m=(source.getItem(source.getSelectionIndex()));
					Mandant man=hMandanten.get(m);
					lfa.reload(man);
				}
				
			});
			GridData gd=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
			//gd.horizontalSpan=2;
			mandanten.setLayoutData(gd);
			tk.adapt(mandanten);
			lfa=new LabeledInputField.AutoForm(body,def);
			lfa.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			tk.paintBordersFor(body);
			return form;
		}else{
			return new PrefAccessDenied(parent);
		}
	}

	public void init(IWorkbench workbench) {
		String grp=Hub.globalCfg.get(PreferenceConstants.ACC_GROUPS, "Admin");
		
		def=new InputData[]{
				new InputData("Kürzel","Label",Typ.STRING,null),
				new InputData("Passwort","ExtInfo",Typ.STRING,"UsrPwd"),
				// -> KSK, NIF und EAN gehören zu Tarmed.
				// new InputData("KSK-Nr","ExtInfo",Typ.STRING,"KSK"),
				// new InputData("NIF","ExtInfo",Typ.STRING,"NIF"),
				// new InputData("EANr","ExtInfo",Typ.STRING,"EAN"),
				new InputData("Gruppen","ExtInfo","Groups",grp.split(","))
		};	
	}

}
