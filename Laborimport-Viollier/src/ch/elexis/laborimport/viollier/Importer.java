/**
 * (c) 2007 by G. Weirich
 * All rights reserved
 * $Id: Importer.java 116 2007-06-07 07:06:44Z gerry $
 */

package ch.elexis.laborimport.viollier;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.openmedical.JMedTransfer.JMedTransfer;
import ch.rgw.tools.TimeTool;

public class Importer extends ImporterPage {
	public static final String MY_LAB="Viollier";
	public static final String PLUGIN_ID="ch.elexis.laborimport.viollier";
	
	public Importer() {
	}

	@Override
	public Composite createPage(Composite parent) {
		//parentShell=parent.getShell();
		Composite ret=new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		FileBasedImporter fbi=new ImporterPage.FileBasedImporter(ret,this);
		ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		File jar=new File(getBasePath()+File.separator+"JMedTransfer0.jar");
		final String ini=getBasePath()+File.separator+"MedTransfer.ini";
		if(jar.canRead() && new File(ini).canRead()){
			Button bDirect=new Button(ret,SWT.PUSH);
			bDirect.setText("Labordaten direkt abholen");
			bDirect.addSelectionListener(new SelectionAdapter(){
	
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String temp=Hub.localCfg.get(PreferencePage.DL_DIR, System.getenv("temp"));
					int res=JMedTransfer.download(new String[]{"-d", temp, "-i", ini, "--verbose", "INF",
							"-#OpenMedicalKey#"});
					System.out.println(res+" files downoladed");
					if(res<1){
						SWTHelper.showInfo("Verbindung mit Labor "+MY_LAB+" erfolgreich", "Es sind keine Resultate zum Abholen vorhanden");
					}else{
						File tmpdir=new File(temp);
						if(tmpdir.isDirectory()){
							String[] files=tmpdir.list(new FilenameFilter(){
								
								public boolean accept(File path,String name) {
									if(name.toLowerCase().endsWith(".hl7")){
										return true;
									}
									return false;
								}});
							for(String file:files){
								Result rs=importFile(file);
								if(!rs.isOK()){
									rs.display("Fehler beim Import");
								}
							}
							SWTHelper.showInfo("Verbindung mit Labor "+MY_LAB+" erfolgreich", "Es wurden "+Integer.toString(res)+
									"Dateien verarbeitet");
						}else{
							SWTHelper.showError("Falsches Verzeichnis", "Bitte kontrollieren Sie die Einstellungen für das Download-Verzeichnis");
						}
					}
				}
				
			});
		}
		return ret;
	}

	private Result<String> parse(HL7 hl7){
		HL7.OBR obr=hl7.firstOBR();
		int nummer=0;
		String dat=new TimeTool().toString(TimeTool.DATE_GER);
		while(obr!=null){
			HL7.OBX obx=obr.firstOBX();
			while(obx!=null){
				String itemname=obx.getItemName();
				Query<LabItem> qbe=new Query<LabItem>(LabItem.class);
				qbe.add("LaborID", "=", hl7.getLabor().get().getId());
				qbe.add("titel", "=", itemname);
				qbe.add("kuerzel", "=", obx.getItemCode());
				List<LabItem> list=qbe.execute();
				LabItem li=null;
				if(list.size()<1){
					LabItem.typ typ=LabItem.typ.NUMERIC;
					if(obx.isFormattedText()){
						typ=LabItem.typ.TEXT;
					}
					li=new LabItem(obx.getItemCode(),itemname,hl7.getLabor().get(),
							obx.getRefRange(),obx.getRefRange(),obx.getUnits(),typ,
							"Z Automatisch_"+dat,Integer.toString(nummer++));
				}else{
					li=list.get(0);
				}
				LabResult lr;
				Query<LabResult> qr=new Query<LabResult>(LabResult.class);
				qr.add("PatientID", "=", hl7.getPatient(false).get().getId());
				qr.add("Datum", "=", obx.getDate().toString(TimeTool.DATE_GER));
				qr.add("ItemID", "=", li.getId());
				if(qr.execute().size()!=0){
					if(SWTHelper.askYesNo("Dieser Laborwert wurde schon importiert", "Weitermachen?")){
						obx=obr.nextOBX(obx);
						continue;
					}else{
						return new Result<String>("Cancelled");
					}
				}
				if(obx.isFormattedText()){
					lr=new LabResult(hl7.getPatient(false).get(),obx.getDate(),li,"text",obx.getResultValue());
				}else{
					lr=new LabResult(hl7.pat,obx.getDate(),li,obx.getResultValue(),obx.getComment());
				}
				if(obx.isPathologic()){
					lr.setFlag(LabResult.PATHOLOGIC, true);
				}
				obx=obr.nextOBX(obx);
			}
			obr=obr.nextOBR(obr);
		}
		return new Result<String>("OK");
	}
	
	private Result importFile(String file){
		HL7 hl7=new HL7(null,null);
		Result<String> r=hl7.load(file);
		if(r.isOK()){
			Result<Patient> res=hl7.getPatient(true);
			if(res.isOK()){
				Result<Kontakt> rk=hl7.getLabor();
				if(rk.isOK()){
					Result ret=parse(hl7);
					GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
					return ret;
				}else{
					return rk;
				}
			}else{
				res.display("Fehler beim Import");
				return res;
			}
		}
		return r;

	}
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		return importFile(results[0]).asStatus();
	}

	@Override
	public String getDescription() {
		return "Bitte wählen Sie eine Datei im HL7-Format oder die Direktübertragung zum Import aus";
	}

	@Override
	public String getTitle() {
		return "Labor "+MY_LAB;
	}
	
	String getBasePath(){
	  try {
	        URL url = Platform.getBundle(PLUGIN_ID).getEntry("/");
	        url  = FileLocator.toFileURL(url);
	        String bundleLocation = url.getPath();
	        File file = new File(bundleLocation);
	        bundleLocation = file.getAbsolutePath();
	        return bundleLocation;
	      }
	      catch(Throwable throwable) {
	        return null;
	      }
	}
}
