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
 *  $Id: ESRView.java 3982 2008-05-31 10:58:47Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.*;
import ch.elexis.util.*;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ESRView extends ViewPart implements ActivationListener{
	CommonViewer cv;
	ViewerConfigurer vc;
	ESRLoader esrloader;
    public final static String DISPLAY_ESR="DisplayESR";
	Query<ESRRecord> qbe;
	private Action loadESRFile;
	private ViewMenus menus;
	private ESRSelectionListener esrl;
	
	public ESRView() {
		Hub.acl.grantForSelf(DISPLAY_ESR);
	}
	
	@Override
	public void dispose(){
		Hub.acl.revokeFromSelf(DISPLAY_ESR);
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		cv=new CommonViewer();
		qbe=new Query<ESRRecord>(ESRRecord.class);
		esrloader=(ESRLoader)JobPool.getJobPool().getJob("ESR-Loader");
		if(esrloader==null){
			esrloader=new ESRLoader(qbe);
			JobPool.getJobPool().addJob(esrloader);
		}

		vc=new ViewerConfigurer(
				new LazyContentProvider(cv,esrloader,DISPLAY_ESR),
				new ESRLabelProvider(),
				new DefaultControlFieldProvider(cv,new String[]{"Datum"}),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST,SWT.NONE,cv)
				);
		cv.create(vc, parent, SWT.None, getViewSite());
		JobPool.getJobPool().activate("ESR-Loader", Job.SHORT);
		makeActions();
		menus=new ViewMenus(getViewSite());
		menus.createToolbar(loadESRFile);
		menus.createMenu(loadESRFile);
		esrl=new ESRSelectionListener(getViewSite());
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener(){
			public void doubleClicked(PersistentObject obj, CommonViewer cv) {
				ESRRecordDialog erd=new ESRRecordDialog(getViewSite().getShell(),(ESRRecord)obj);
				if(erd.open()==Dialog.OK){
					cv.notify(CommonViewer.Message.update);
				}
			}
			
		});
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	class ESRLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider{
		DecimalFormat df=new DecimalFormat("###0.00");
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof ESRRecord){
				ESRRecord rec=(ESRRecord)element;
				if(rec.getTyp().equals(ESRRecord.MODE.Summenrecord)){
					return "-- "+rec.getFile()+" "+rec.getBetrag()+" --";
				}else if(rec.getId().equals("1")){
					return "RechnungNr  Eingelesen/Verrechnet/Gutgeschrieben  Patient  Buchung";
				}
				StringBuilder sb=new StringBuilder(100);
				Rechnung rn=rec.getRechnung();
				if(rn!=null){
					sb.append(rn.getNr()).append(": ");
				}
				String betrag=rec.getBetrag().getAmountAsString();
				sb.append(rec.getEinlesedatatum()).append("/").append(rec.getVerarbeitungsdatum()).append("/").append(rec.getValuta())
					.append(" - ").append(rec.getPatient().getLabel()).append(" - ").append(betrag);
				String dat=rec.getGebucht();
				if(StringTool.isNothing(dat)){
					sb.append(" Nicht verbucht!");
				}else{
					sb.append(" Verbucht: ").append(new TimeTool(dat).toString(TimeTool.DATE_GER));
				}
				return sb.toString();
			}
			return null;
		}
		
		
		
		public Color getForeground(Object element, int columnIndex) {
			return Desk.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		}
		
		
		public Color getBackground(Object element, int columnIndex) {
			if(element instanceof ESRRecord){
				ESRRecord rec=(ESRRecord)element;
				if(rec.getTyp().equals(ESRRecord.MODE.Summenrecord)){
					return Desk.getDisplay().getSystemColor(SWT.COLOR_GREEN);
				}
				String buch=rec.getGebucht();
				if(rec.getRejectCode().equals(ESRRecord.REJECT.OK)){
					if(StringTool.isNothing(buch)){
						return Desk.getDisplay().getSystemColor(SWT.COLOR_GRAY);
					}
					return Desk.getDisplay().getSystemColor(SWT.COLOR_WHITE);
				}
				return Desk.getDisplay().getSystemColor(SWT.COLOR_RED);
			}
			return Desk.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
		}
		
		
		
	}
	class ESRLoader extends AbstractDataLoaderJob{
		Query<ESRRecord> qbe;
		ESRLoader(Query<ESRRecord> qbe){
			super("ESR-Loader",qbe,new String[]{"Datum"});
			this.qbe=qbe;
		}
		@Override
		public IStatus execute(IProgressMonitor monitor) {
			monitor.beginTask("Lade ESR", SWT.INDETERMINATE);
		
			qbe.clear();
			if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
				if(Hub.actMandant==null){
					return null;
				}
				qbe.add("MandantID", "=", Hub.actMandant.getId());
			}
			
			vc.getControlFieldProvider().setQuery(qbe);
			qbe.orderBy(true,new String[]{"Datum","Gebucht"});
			List<ESRRecord> list=qbe.execute();
			result=list.toArray();
			monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public int getSize() {
			return PersistentObject.getConnection().queryInt("SELECT COUNT(0) FROM ESRRECORDS");

		}
		
	}
	private void makeActions(){
		loadESRFile=new Action("ESR-Datei einlesen"){
			{
				setToolTipText("Auswahl einer von der Bank heruntergeladenen ESR-Datei zum Einlesen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_IMPORT));
			}
			@Override
			public void run(){
				FileDialog fld=new FileDialog(getViewSite().getShell(),SWT.OPEN);
				fld.setText("ESR Datei auswählen");
				String filename=fld.open();
				if(filename!=null){
					ESRFile esrf=new ESRFile();
					Result<List<ESRRecord>> result=esrf.read(filename);
					if(result.isOK()){
						for(ESRRecord rec:result.get()){
							if(rec.getRejectCode().equals(ESRRecord.REJECT.OK)){
								if(rec.getTyp().equals(ESRRecord.MODE.Summenrecord)){
									Hub.log.log("ESR eingelesen. Summe "+rec.getBetrag(), Log.INFOS);
								}else if( (rec.getTyp().equals(ESRRecord.MODE.Storno_edv)) || (rec.getTyp().equals(ESRRecord.MODE.Storno_Schalter))){
									Rechnung rn=rec.getRechnung();
									Money zahlung=rec.getBetrag().negate();
									rn.addZahlung(zahlung, "Storno für rn "+rn.getNr()+" / "+rec.getPatient().getPatCode());
									rec.setGebucht(null);
								}else{
									Rechnung rn=rec.getRechnung();
									if(rn.getStatus()==RnStatus.BEZAHLT){
										if(MessageDialog.openConfirm(getViewSite().getShell(), "Rechnung schon bezahlt", "Rechnung "+rn.getNr()+" ist bereits bezahlt. Trotzdem buchen?")==false){
											continue;
										}
									}
									Money zahlung=rec.getBetrag();
									Money offen=rn.getOffenerBetrag();
									if(zahlung.isMoreThan(offen)){
										if(MessageDialog.openConfirm(getViewSite().getShell(), "Betrag zu hoch", "Die Zahlung für Rechnung "+rn.getNr()+" übersteigt den offenen Betrag. Trotzdem buchen?")==false){
											continue;
										}
									}
									
									rn.addZahlung(zahlung, "VESR für rn "+rn.getNr()+" / "+rec.getPatient().getPatCode());
									rec.setGebucht(null);
								}
							}
						}
					}else{
						result.display("Fehler beim ESR-Einlesen:");
					}
				}
				JobPool.getJobPool().activate("ESR-Loader", Job.SHORT);
				//cv.notify(CommonViewer.Message.update);
			}
		};
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
		esrl.activate(mode);
	}

}
