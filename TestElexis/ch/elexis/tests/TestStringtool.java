package ch.elexis.tests;

import java.io.*;
import java.util.Hashtable;

import ch.rgw.IO.FileTool;
import ch.rgw.tools.StringTool;

import junit.framework.TestCase;


public class TestStringtool extends TestCase {
	String in;
	Hashtable hash;
	
	protected void setUp() throws Exception {
		String path=FileTool.getBasePath(TestStringtool.class);
		File file=new File(path+"/hashtable.txt");
		long l=file.length();
		byte[] arr=new byte[(int)l];
		FileInputStream fis=new FileInputStream(file);
		fis.read(arr);
		in=new String(arr);
	}

	/** 
	 * Statdard-Vefrahren? 
	 * @throws Exception
	 */
	public void testFold() throws Exception{
		hash=StringTool.foldStrings(in);
		assertNotNull(hash);
		byte[] flat=StringTool.flatten(hash,StringTool.BZIP,null);
		assertNotNull(flat);
		Hashtable check=StringTool.fold(flat,StringTool.GUESS,null);
		assertNotNull(check);
	}
}
