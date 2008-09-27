package ch.elexis.tests;

import java.io.File;
import java.util.HashMap;

import junit.framework.TestCase;
import ch.rgw.IO.FileTool;
import ch.rgw.crypt.Cryptologist;
import ch.rgw.crypt.GnuPG;
import ch.rgw.crypt.JCECrypter;
import ch.rgw.crypt.SAT;
import ch.rgw.tools.Result;

public class TestSAT extends TestCase {
	static String homedir;
	Cryptologist crypt;
	static byte[] encrypted;

	public TestSAT(){
		homedir=System.getenv("TEMP")+File.separator+"crypt";
		File file=new File(homedir);
		if(file.exists()){
			FileTool.deltree(file.getAbsolutePath());
		}
		file.mkdirs();
		/*
		gpg=new GnuPG(false);
		gpg.setExecutable("d:/apps/gpg/gpg.exe");
		gpg.setHomedir(homedir);
		gpg.generateKey("Alice Elexistesterin", "alice@elexis.ch", "aliceelexis".toCharArray(), "alice");
		gpg.generateKey("Bob Elexistester", "bob@elexis.ch", "bobelexis".toCharArray(), "bob");
		*/
		crypt=new JCECrypter();
	}
	
	public void testWrap() throws Exception{
		SAT sat=new SAT(crypt,"alice@elexis.ch");
		HashMap<String, Object> hash=new HashMap<String, Object>();
		hash.put("test", "Ein Testtext");
		byte[] result=sat.wrap(hash, "bob@elexis.ch", "aliceelexis".toCharArray());
		assertNotNull(result);
		System.out.println(new String(result));
		encrypted=result;
	}
	
	public void testUnwrap() throws Exception{
		SAT sat=new SAT(crypt,"bob@elexis.ch");
		Result<HashMap<String,Object>> result=sat.unwrap(encrypted,"bobelexis".toCharArray());
		assertTrue(result.isOK());
		HashMap<String, Object> res=result.get();
		String val=(String)res.get("test");
		assertEquals(val, "Ein Testtext");
		assertEquals("alice@elexis.ch", res.get(SAT.ADM_SIGNED_BY));
	}
}
