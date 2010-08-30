/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Mandanten.java 6352 2010-05-12 17:04:22Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
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
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.preferences.inputs.PrefAccessDenied;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.LabeledInputField.IContentProvider;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;
import ch.elexis.util.SWTHelper;

public class Mandanten extends PreferencePage implements IWorkbenchPreferencePage {
	private LabeledInputField.AutoForm lfa;
	private InputData[] def;
	
	private Hashtable<String, Mandant> hMandanten = new Hashtable<String, Mandant>();
	
	@Override
	protected Control createContents(Composite parent){
		if (Hub.acl.request(AccessControlDefaults.ACL_USERS)) {
			FormToolkit tk = Desk.getToolkit();
			Form form = tk.createForm(parent);
			Composite body = form.getBody();
			body.setLayout(new GridLayout(1, false));
			Combo mandanten = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
			Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
			List<Mandant> list = qbe.execute();
			for (Mandant m : (List<Mandant>) list) {
				mandanten.add(m.getLabel());
				hMandanten.put(m.getLabel(), m);
			}
			mandanten.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e){
					Combo source = (Combo) e.getSource();
					String m = (source.getItem(source.getSelectionIndex()));
					Mandant man = hMandanten.get(m);
					lfa.reload(man);
				}
				
			});
			GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			// gd.horizontalSpan=2;
			mandanten.setLayoutData(gd);
			tk.adapt(mandanten);
			lfa = new LabeledInputField.AutoForm(body, def);
			lfa.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			tk.paintBordersFor(body);
			return form;
		} else {
			return new PrefAccessDenied(parent);
		}
	}
	
	public void init(IWorkbench workbench){
		String grp = Hub.globalCfg.get(PreferenceConstants.ACC_GROUPS, Messages.Mandanten_0);
		
		def =
			new InputData[] {
				new InputData(Messages.Mandanten_kuerzel, "Label", Typ.STRING, null), //$NON-NLS-1$
				new InputData(Messages.Mandanten_password, PersistentObject.FLD_EXTINFO, Typ.STRING, "UsrPwd"),  //$NON-NLS-1$
				// -> KSK, NIF und EAN gehören zu Tarmed.
				// new InputData("KSK-Nr","ExtInfo",Typ.STRING,"KSK"),
				// new InputData("NIF","ExtInfo",Typ.STRING,"NIF"),
				// new InputData("EANr","ExtInfo",Typ.STRING,"EAN"),
				new InputData(Messages.Mandanten_groups, PersistentObject.FLD_EXTINFO, "Groups", grp.split(",")), //$NON-NLS-1$ //$NON-NLS-2$ 
				new InputData(Messages.Mandanten_biller, PersistentObject.FLD_EXTINFO, new IContentProvider() { 
					
					public void displayContent(PersistentObject po, InputData ltf){
						Mandant m = (Mandant) po;
						Kontakt r = m.getRechnungssteller();
						ltf.setText(r.getLabel());
					}
					
					public void reloadContent(PersistentObject po, InputData ltf){
						Kontakt rsi = (Kontakt) po;
						KontaktSelektor ksl =
							new KontaktSelektor(getShell(), Kontakt.class,
								Messages.Mandanten_selectBiller,
								Messages.Mandanten_pleaseSelectBiller,new String[]{Kontakt.FLD_NAME1,Kontakt.FLD_NAME2});
						if (ksl.open() == Dialog.OK) {
							rsi = (Kontakt) ksl.getSelection();
						}
						((Mandant) po).setRechnungssteller(rsi);
						ltf.setText(rsi.getLabel());
					}
				})
			};
	}
	
}
