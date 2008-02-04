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
 * $Id: ICalTransfer.java 3607 2008-02-04 14:45:15Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.agenda.Messages;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ImporterPage.DBBasedImporter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class ICalTransfer {
	String[] bereiche;
	
	public ICalTransfer(){
		bereiche=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_14).split(",");	
	}
	
	public void doExport(TimeTool von, TimeTool bis, String bereich){
		if(von==null){
			von=new TimeTool();
		}
		if(bis==null){
			bis=new TimeTool();
		}
		if(bereich==null){
			bereich=bereiche[0];
		}
		new ICalExportDlg(von,bis,bereich).open();
	}
	
	public void doImport(String bereich){
		if(bereich==null){
			bereich=bereiche[0];
		}
		new ICalImportDlg(bereich).open();
	}
	class ICalImportDlg extends TitleAreaDialog{
		String m;
		public ICalImportDlg(String bereich){
			super(Desk.getTopShell());
			m=bereich;
		}
		
	}
	class ICalExportDlg extends TitleAreaDialog{
		final String NOFILESELECTED=" - keine Datei ausgewählt -"; 
		TimeTool von,bis;
		String m;
		DatePickerCombo dpVon, dpBis;
		Combo cbBereiche;
		FileDialog fileDialog;
		Label lFile;
		
		public ICalExportDlg(TimeTool from, TimeTool until, String bereich) {
			super(Desk.getTopShell());
			von=new TimeTool(from);
			bis=new TimeTool(until);
			m=bereich;
	
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout(3,false));
			new Label(ret,SWT.NONE).setText("Von");
			new Label(ret,SWT.NONE).setText("Bis");
			new Label(ret,SWT.NONE).setText("Bereich");
			dpVon=new DatePickerCombo(ret,SWT.NONE);
			dpBis=new DatePickerCombo(ret,SWT.NONE);
			cbBereiche=new Combo(ret,SWT.NONE);
			cbBereiche.setItems(bereiche);
			cbBereiche.setText(m);
			Button bChose=new Button(ret,SWT.PUSH);
			bChose.setText("Datei");
			bChose.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					fileDialog=new FileDialog(getShell(),SWT.SAVE);
					fileDialog.setFilterExtensions(new String[]{"*.ics"});
					fileDialog.setFilterNames(new String[]{"iCal Dateien"});
					String fileName=fileDialog.open();
					if(fileName==null){
						lFile.setText(NOFILESELECTED);
						getButton(IDialogConstants.OK_ID).setEnabled(false);
					}else{
						lFile.setText(fileName);
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					}
				}
				
			});
			lFile=new Label(ret,SWT.NONE);
			lFile.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lFile.setText(NOFILESELECTED);
			dpVon.setDate(von.getTime());
			dpBis.setDate(bis.getTime());
			return ret;
			
		}
		@Override
		public void create() {
			super.create();
			getShell().setText("Agenda exportieren");
			setTitle("Agenda-Bereich exportieren");
			setMessage("Wählen Sie bitte Beginndatum, Enddatum und Bereich für den Export");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		@Override
		protected void okPressed() {
			von.setTimeInMillis(dpVon.getDate().getTime());
			bis.setTimeInMillis(dpBis.getDate().getTime());
			Query<Termin> qbe=new Query<Termin>(Termin.class);
			qbe.add("Tag", ">=", von.toString(TimeTool.DATE_COMPACT));
			qbe.add("Tag", "<=", bis.toString(TimeTool.DATE_COMPACT));
			qbe.add("BeiWem", "=", m);
			List<Termin> termine=qbe.execute();
			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			TimeZone timezone = registry.getTimeZone("Europe/Zurich");
			VTimeZone tz = timezone.getVTimeZone();
			Calendar calendar=new Calendar();
			calendar.getProperties().add(new ProdId("-//ch.elexis//Elexis v"+Hub.Version));
			calendar.getProperties().add(Version.VERSION_2_0);
			calendar.getProperties().add(CalScale.GREGORIAN);
			for(Termin t:termine){
				if((t.getStartMinute()==0) && (t.getType().equals(Termin.typReserviert()))){
					continue;
				}
				if((t.getStartMinute()+t.getDurationInMinutes()==(23*60)+59) && (t.getType().equals(Termin.typReserviert()))){
					continue;
				}
				TimeTool tt=new TimeTool(t.getStartTime());
				DateTime start=new DateTime(tt.getTime());
				tt.addMinutes(t.getDurationInMinutes());
				DateTime end=new DateTime(tt.getTime());
				VEvent vTermin=new VEvent(start,end,t.getPersonalia());
				vTermin.getProperties().add(tz.getTimeZoneId());
				vTermin.getProperties().add(new Description(t.getText()));
				Uid uid=new Uid(t.getId());
				vTermin.getProperties().add(uid);
				calendar.getComponents().add(vTermin);
			}
			try{
				FileOutputStream fout = new FileOutputStream(lFile.getText());
				CalendarOutputter outputter = new CalendarOutputter();
				outputter.output(calendar, fout);
				
			}catch(Exception ex){
				ExHandler.handle(ex);
				SWTHelper.alert("I/O-Fehler", "Konnte Datei "+lFile.getText()+" nicht schreiben");
			}
			super.okPressed();
		}
		
	}
}
