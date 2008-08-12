// (c) 2008 by G. Weirich
// $Id: LockFile.java 4262 2008-08-12 16:13:00Z rgw_ch $

package ch.rgw.IO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.rgw.tools.TimeTool;

/**
 * A file that creates and detects lockfile to provide cooperative detection od running
 * instances of an applicatiion (or for other purposes)
 * @author gerry
 *
 */
public class LockFile {
	File baseDir;
	String baseName;
	int maxNum;
	int timeOutSeconds;
	
	/**
	 * Create an instance of a lockfile. Lock() will create a file in the given dir and
	 * with the given name and a suffix ranging from 1 to maxNum.
	 * @param dir the dir where the lockfile(s) should be created
	 * @param basename the basename for the files
	 * @param maxNum maximum number of instances of this lock that may be aquired simulatously
	 * @param timeoutSeconds after what time will a lock treaded as invalid
	 */
	public LockFile(File dir, String basename, int maxNum, int timeoutSeconds){
		baseDir=dir;
		baseName=basename;
		this.maxNum=maxNum;
		this.timeOutSeconds=timeoutSeconds;
	}
	
	/**
	 * create a lockfile with the given patterns. There will be created at most maxNUm
	 * lockfiles. Each of them will expire after timeoutSeconds, or after the application exits.
	 * @return true on success, false if there are already maxNum lockfiles
	 * @throws IOException id something went wrong
	 */
	public boolean lock() throws IOException{
		int n=1;

		while(n<=maxNum){
			File file=new File(baseDir,constructFilename(n));
			if(!isLockValid(file)){
				return createLockfile(file);
			}
			n++;
		}
		return false;
	}
	
	private boolean isLockValid(File file) throws IOException{
		if(!file.exists()){
			return false;
		}
		TimeTool now=new TimeTool();
		DataInputStream dais=new DataInputStream(new FileInputStream(file));
		String ts=dais.readUTF();
		TimeTool tt=new TimeTool();
		dais.close();
		if(tt.set(ts)){
			if(tt.secondsTo(now)>timeOutSeconds){
				if(file.delete()){
					return false;
				}
			}else{
				return true;
			}
		}
		if(file.delete()){
			return false;
		}
		throw(new IOException("Can not delete "+file.getAbsolutePath()));
	}
	private boolean createLockfile(File file) throws IOException{
		if(!file.createNewFile()){
			return false;
		}
		file.deleteOnExit();
		DataOutputStream daos=new DataOutputStream(new FileOutputStream(file));
		daos.writeUTF(new TimeTool().toString(TimeTool.FULL_ISO));
		daos.close();
		return true;
	}
	
	private String constructFilename(int n){
		return new StringBuilder().append(baseName).append(".")
			.append(Integer.toString(n)).toString();
	}
	/**
	 * check if at least one lockfile with the given pattern exists
	 * @return true if one ore more lockfiles exist
	 * @throws IOException 
	 */
	public boolean existsLock() throws IOException{
		int n=1;
		while(n<=maxNum){
			File file=new File(baseDir,constructFilename(n));
			if(isLockValid(file)){
				return true;
			}
		}
		return false;
	}
}
