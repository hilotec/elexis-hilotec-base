/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ReplaceJob.java 3536 2008-01-16 12:06:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.elexis.Hub;
import ch.elexis.Hub.ShutdownJob;
import ch.elexis.update.AutoUpdate.ReplaceInfo;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.IO.FileTool;
import ch.rgw.tools.ExHandler;

public class ReplaceJob implements ShutdownJob {

	private List<ReplaceInfo> downloads;
	private File basedir;
	
	public ReplaceJob(File basedir, List<ReplaceInfo> downloads){
		this.downloads=downloads;
		this.basedir=basedir;
	}
	/**
	 * We replace the Plug-Ins just here because we cannot do it while running normally
	 * 
	 */
	public void doit() throws Exception{
		if(downloads.size()==0){
			AutoUpdate.log.log("Updater: Nothing to to", Log.INFOS);
			return;
		}
		AutoUpdate.log.log("Updater: replacing files", Log.INFOS);
		
		for(ReplaceInfo ri:downloads){
			
			String rfname=ri.file.getName();			// filename as in ch.elexis.plugin_some_1.2.1.20080101.zip
			Hub.log.log("trying "+rfname, Log.INFOS);
			int off=rfname.lastIndexOf(".");		
			String dname=rfname.substring(0, off);		// basename without .zip
			off=dname.lastIndexOf(".");	
			String version=dname;
			if(off!=-1){
				version=version.substring(off);			// date-part
			}
			File destdir=new File(basedir.getAbsolutePath()+File.separator+dname);
			destdir.mkdir();							// create directory with name as 'ch.elexis.plugin.some_1.2.1.20080101'
			try{
				ZipInputStream zis=new ZipInputStream(new FileInputStream(ri.file));
				ZipEntry zip;
				while((zip=zis.getNextEntry())!=null){	
					String id=zip.getName();				// extract all files into that directory
					File file=new File(destdir.getAbsolutePath()+File.separator+id);
					if(zip.isDirectory()){
						file.mkdirs();
					}else{
						FileOutputStream fos=new FileOutputStream(file);
						if(id.toLowerCase().endsWith("manifest.mf")){		// adapt manifest to actual version
							OutputStreamWriter writer=new OutputStreamWriter(fos);
							StringBuilder sb=new StringBuilder();
							int c;
							while((c=zis.read())!=-1){
								sb.append((char)c);
							}
							String[] manifest=sb.toString().split("\\n");
							for(String line:manifest){
								if(line.startsWith("Bundle-Version:")){
									line+=version;
								}
								writer.write(line+"\n");
							}
							writer.close();
						}else{
							byte[] buffer=new byte[8192];
				        	while(true){
				        		int r=zis.read(buffer);
				                if(r==-1){
				                    break;
				                }
				                fos.write(buffer,0,r);
				            }
						}
				        fos.close();
					}
				}
				zis.close();
			
				AutoUpdate.log.log("Success "+rfname, Log.INFOS);
				if((ri.mode==UpdateClient.FILE_REVISION) ||
						(ri.mode==UpdateClient.FILE_BUILD)){
					if(!FileTool.deltree(basedir.getAbsolutePath()+File.separator+ri.sub)){
						AutoUpdate.log.log("Could not delete "+ri.sub, Log.ERRORS);
					}
				}
				if(!ri.file.delete()){
					AutoUpdate.log.log("Could not delete "+ri.file.getName(), Log.ERRORS);
				}
			}catch(Throwable t){
				ExHandler.handle(t);
				AutoUpdate.log.log("Update error: "+ rfname+" could not be unpacked", Log.ERRORS);
				FileTool.deltree(destdir.getAbsolutePath());
			}
		}
		// clean configuration area
		File elexisroot=basedir.getParentFile();
		File configdir=new File(elexisroot.getAbsolutePath()+File.separator+"configuration");
		if(configdir.isDirectory()){
			for(File cfgsub:configdir.listFiles()){
				if(cfgsub.isDirectory()){
					FileTool.deltree(cfgsub.getAbsolutePath());
				}
			}
			
		}
		AutoUpdate.log.log("Updater terminated successfully", Log.INFOS);
	}


}
