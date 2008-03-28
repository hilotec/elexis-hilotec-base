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
 * $Id: AutoUpdate.java 3751 2008-03-28 11:44:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.update;
import java.io.File;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class AutoUpdate {
	static Log log=Log.get("AutoUpdate");
	File basedir;
	int filecounter=0;
	LinkedList<ReplaceInfo> downloads=new LinkedList<ReplaceInfo>();
	private static final String updateURL="http://www.rgw.ch/update12.php";
	
	public boolean doUpdate(){
		try {
			UpdateJob job=null;
			String base=Hub.getBasePath();
			//String base="d:/apps/elexis-1.2.0/plugins/ch.elexis_1.2.1";
			//String base="c:/programme/elexis-1.2.1/plugins/ch.elexis_1.2.1_20080124";
			basedir=new File(base).getParentFile();
			log.log("Dir: "+basedir.getAbsolutePath(),Log.INFOS);  // =plugins
			if(basedir.isDirectory()){
				if(!basedir.canWrite()){
					SWTHelper.showError("Update nicht möglich", "Kein Schreibrecht auf Elexis-Verzeichnis");
				}
				String[] subs=basedir.list();		
				job=new UpdateJob(subs);
				job.schedule();
				Hub.addShutdownJob(new ReplaceJob(basedir, downloads));
			}
			

		} catch (Exception e) {
				ExHandler.handle(e);
				return false;
		}
		return true;
	}
	
	class UpdateJob extends Job{
		String[] dirs;
		UpdateClient ucl=new UpdateClient();
		
		UpdateJob(final String [] d){
			super("Elexis-Update");
			dirs=d;
		}
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask("Update", dirs.length*2);
			String tempdir=Hub.localCfg.get(Preferences.TEMPDIR, System.getenv("TEMP"));
			//String url=Hub.globalCfg.get(Preferences.UPDATE_SITE, "http://www.rgw.ch/update.php");
			
			for(String sub:dirs){
				String f=sub+".zip";
				monitor.subTask(f);
				
				try{
					log.log("check: "+f, Log.INFOS);
					int newer=ucl.checkUpdate(f, updateURL);
					log.log("Result: "+Integer.toString(newer), Log.INFOS);
					monitor.worked(1);
					if(newer>UpdateClient.FILE_SAME){
						monitor.subTask(sub);
						File rFile=ucl.download(f, tempdir, updateURL);
						if(rFile!=null){
							downloads.add(new ReplaceInfo(rFile,sub,newer));
							log.log(f+" ok.", Log.INFOS);
							filecounter++;
						}else{
							log.log("Error downloading "+f, Log.ERRORS);
						}
					}
					monitor.worked(1);
					if(monitor.isCanceled()){
						return Status.CANCEL_STATUS;
					}
				}catch(Exception ex){
					log.log("Exception during update "+ex.getMessage(), Log.ERRORS);
					ExHandler.handle(ex);
				}
			} // for
			monitor.done();
			if(filecounter!=0){
				SWTHelper.showInfo("Update abgeschlossen", "Es wurden "+filecounter+" plugins heruntergeladen. Bitte neu starten.");
			}
			return Status.OK_STATUS;
		}
		
	}
	static class ReplaceInfo{
		ReplaceInfo(final File f, final String s, final int m){
			file=f; sub=s; mode=m;
		}
		public File file;		// New Zip-File to insert
		public String sub;		// Subdirectory to replace with zip-content
		public int mode;		// One of the constants in UpdateClient
	}
}
