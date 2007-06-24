// $Id: FileTool.java 2514 2007-06-12 05:11:38Z rgw_ch $
/*
  */
package ch.rgw.IO;



import java.io.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.util.Log;

import ch.rgw.tools.ExHandler;

/**
 * @author Gerry
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileTool {
    public static String Version(){return "1.3.0";}
    private static final Log log=Log.get("FileTool");
    
    /**
     * Gibt das Basisverzeichnis von clazz resp. des Jars, in dem diese Klasse 
     * sich befindet zur�ck.
     * Holt hierf�r die URL der Klass und unterscheidet folgende F�lle:
     * jar:file://netzlaufwerk/pfad/MyApp.jar
     * file://netzlaufwerk/pfad/MyApp.class
     * jar:file:/X:/pfad/MyApp.jar
     * file://X:/pfad/MyApp.class 
     * @return
     */
    public static String getBasePath(Class clazz)
    {	
    	String raw=getClassPath(clazz);
    	if(raw==null) {
    		return ".";
    	}
    	String found=null;
    	Pattern p=
    		Pattern.compile(".*?file:(\\/{1,2})(.+?)[^\\\\\\/]+\\.(jar|class).*");
    	Matcher m=p.matcher(raw);
    	if(m.matches()){
    	    found=m.group(2);
    	    if(found.matches("[a-zA-Z]:.+")){
    	        return found;
    	    }
    	    return m.group(1)+found;
    	}

		return found;
    }
    public static String getClassPath( Class clazz )
	{	ClassLoader loader = clazz.getClassLoader();
		if ( loader == null ) {
	      return null;
		}
	    URL url = loader.getResource(clazz.getName().replace('.','/')
	                                 + ".class");
	    return ( url != null ) ? url.toString() : null;
	}	
    public static final int REPLACE_IF_EXISTS=0;
    public static final int BACKUP_IF_EXISTS=1;
    public static final int FAIL_IF_EXISTS=2;
    public static boolean copyFile(File src,File dest,int if_exists)
    {
        if(src.canRead()==false) {
            log.log(Messages.getString("FileTool.cantReadSource"),Log.ERRORS); //$NON-NLS-1$
            return false;
        }
        if(dest.exists()) {
            String pname=dest.getAbsolutePath();
            switch(if_exists) {
                case REPLACE_IF_EXISTS:
                    if(dest.delete()==false) {
                        log.log(Messages.getString("FileTool.cantDeleteTarget"),Log.ERRORS); //$NON-NLS-1$
                        return false;
                    }
                    break;
                case BACKUP_IF_EXISTS:
                    File bak=new File(pname+".bak");
                    if(bak.exists()==true) {
                        if(bak.delete()==false) {
                            log.log(Messages.getString("FileTool.backupExists"),Log.ERRORS); //$NON-NLS-1$
                            return false;
                        }
                    }
                    if(dest.renameTo(bak)==false) {
                        log.log(Messages.getString("FileTool.cantRenameTarget"),Log.ERRORS); //$NON-NLS-1$
                        return false;
                    }
                    dest=new File(pname);
                    break;
                case FAIL_IF_EXISTS:
                    log.log(Messages.getString("FileTool.targetExists"),Log.ERRORS); //$NON-NLS-1$
                    return false;
                default:
                    log.log(Messages.getString("FileTool.badCopyMode"),Log.ERRORS); //$NON-NLS-1$
                    return false;
            }
        }
        try {
            if(dest.createNewFile()==false){
                log.log(Messages.getString("FileTool.couldnotcreate")+dest.getAbsolutePath()+Messages.getString("FileTool.fil"),Log.ERRORS); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
            if(dest.canWrite()==false) {
                log.log(Messages.getString("FileTool.cantWriteTarget"),Log.ERRORS); //$NON-NLS-1$
                return false;
            }
       
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(dest));
            BufferedInputStream bis=new BufferedInputStream(new FileInputStream(src));
            byte[] buffer=new byte[131072];
            while(true){
                int r=bis.read(buffer);
                if(r==-1){
                    break;
                }
                bos.write(buffer,0,r);
            }
            bis.close();
            bos.close();
        }catch(Throwable ex) {
            ExHandler.handle(ex);
            log.log(ex.getMessage(),Log.ERRORS);
            return false;
        }
        return true;
    }
    
    public static void copyStreams(InputStream is, OutputStream os) throws IOException{
        BufferedOutputStream bos=new BufferedOutputStream(os);
        BufferedInputStream bis=new BufferedInputStream(is);
        byte[] buffer=new byte[131072];
        while(true){
            int r=bis.read(buffer);
            if(r==-1){
                break;
            }
            bos.write(buffer,0,r);
        }
        //bis.close();
        //bos.close();
    }
    public static String readFile(File name){
    	try{
    		FileReader fr=new FileReader(name);
    		char[] cnt=new char[(int)name.length()];
    		fr.read(cnt);
			fr.close();
    		return new String(cnt);
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		return null;
    	}
    	
    }
    public static boolean writeFile(File name, String cnt){
    	try{
    		FileWriter fw=new FileWriter(name);
    		fw.write(cnt);
    		fw.close();
    		return true;
    		
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		return false;
    	}
    }
    public static boolean deltree(String d){
    	File f=new File(d);
    	boolean res=true;
    	if(f.exists()){
    		if(f.isDirectory()){
    			String[] subs=f.list();
    			for(String sub:subs){
    				if(deltree(f.getAbsolutePath()+File.separator+sub)==false){
    					res=false;
    				}
    			}
    		}
    		if(f.delete()==false){
    			res=false;
    		}
    	}
    	return res;
    }
    
    public static void readWrite(Reader in, Writer out){
    	
    }
}
