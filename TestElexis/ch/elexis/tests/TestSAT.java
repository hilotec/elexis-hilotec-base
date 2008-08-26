package ch.elexis.tests;

import java.io.File;
import java.util.HashMap;

import ch.elexis.pro.SAT;
import ch.elexis.util.Result;
import ch.rgw.IO.FileTool;
import ch.rgw.tools.GnuPG;
import junit.framework.TestCase;

public class TestSAT extends TestCase {
	static String homedir;
	static GnuPG gpg;
	static String encrypted;

	public TestSAT(){
		homedir=System.getenv("TEMP")+File.separator+"gpghome";
		File file=new File(homedir);
		if(file.exists()){
			FileTool.deltree(file.getAbsolutePath());
		}
		file.mkdirs();
		gpg=new GnuPG(false);
		gpg.setExecutable("c:/tools/gnupg/gpg.exe");
		gpg.setHomedir(homedir);
		gpg.generateKey("Alice Elexistesterin", "alice@elexis.ch", "aliceelexis".toCharArray(), "alice");
		gpg.generateKey("Bob Elexistester", "bob@elexis.ch", "bobelexis".toCharArray(), "bob");
	
	}
	
	public void testWrap() throws Exception{
		SAT sat=new SAT(gpg,"alice@elexis.ch");
		HashMap<String, Object> hash=new HashMap<String, Object>();
		hash.put("test", "Ein Testtext");
		String result=sat.wrap(hash, "bob@elexis.ch", "aliceelexis".toCharArray());
		assertNotNull(result);
		System.out.println(new String(result));
		encrypted=result;
	}
	
	public void testUnwrap() throws Exception{
		SAT sat=new SAT(gpg,"bob@elexis.ch");
		Result<HashMap<String,Object>> result=sat.unwrap(encrypted,"bobelexis".toCharArray());
		assertTrue(result.isOK());
		HashMap<String, Object> res=result.get();
		String val=(String)res.get("test");
		assertEquals(val, "Ein Testtext");
	}
}
