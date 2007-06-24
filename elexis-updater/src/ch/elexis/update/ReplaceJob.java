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
import ch.rgw.IO.FileTool;

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
		AutoUpdate.log.log("Updater: replacing files", Log.INFOS);
		for(ReplaceInfo ri:downloads){
			
			String rfname=ri.file.getName();
			Hub.log.log("trying "+rfname, Log.INFOS);
			int off=rfname.lastIndexOf(".");
			String dname=rfname.substring(0, off);
			off=dname.lastIndexOf(".");
			String version=dname;
			if(off!=-1){
				version=version.substring(off);
			}
			File destdir=new File(basedir.getAbsolutePath()+File.separator+dname);
			destdir.mkdir();
			ZipInputStream zis=new ZipInputStream(new FileInputStream(ri.file));
			ZipEntry zip;
			while((zip=zis.getNextEntry())!=null){
				String id=zip.getName();
				File file=new File(destdir.getAbsolutePath()+File.separator+id);
				if(zip.isDirectory()){
					file.mkdirs();
				}else{
					FileOutputStream fos=new FileOutputStream(file);
					if(id.toLowerCase().endsWith("manifest.mf")){
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
				FileTool.deltree(ri.sub);
			}
			ri.file.delete();
		}
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
