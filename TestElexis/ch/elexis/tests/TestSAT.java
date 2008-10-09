package ch.elexis.tests;

import java.util.HashMap;

import junit.framework.TestCase;
import ch.rgw.crypt.Cryptologist;
import ch.rgw.crypt.JCECrypter;
import ch.rgw.crypt.SAT;
import ch.rgw.tools.Result;

public class TestSAT extends TestCase {
	static String homedir;
	static Cryptologist crypt;
	static byte[] encrypted;
	
		
	public void testCreate() throws Exception{
		crypt=new JCECrypter(null,null,"admin","adminkey".toCharArray());
		assertTrue(crypt.hasKeyOf("admin"));
	}
	
	public void testCreateKeys() throws Exception{
		if(!crypt.hasKeyOf("alice")){
			/*KeyPair kp= */crypt.generateKeys("alice","alicepwd".toCharArray(),null,null);
			
		}
		if(!crypt.hasKeyOf("bob")){
			/*KeyPair kp= */crypt.generateKeys("bob","bobpwd".toCharArray(),null,null);
		}
	}
	public void testWrap() throws Exception{
		SAT sat = new SAT(crypt, "alice");
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("test", "Ein Testtext");
		byte[] result = sat.wrap(hash, "bob", "alicepwd".toCharArray());
		assertNotNull(result);
		System.out.println(new String(result));
		encrypted = result;
	}
	
	public void testUnwrap() throws Exception{
		SAT sat = new SAT(crypt, "bob@elexis.ch");
		Result<HashMap<String, Object>> result = sat.unwrap(encrypted, "bobelexis".toCharArray());
		assertTrue(result.isOK());
		HashMap<String, Object> res = result.get();
		String val = (String) res.get("test");
		assertEquals(val, "Ein Testtext");
		assertEquals("alice@elexis.ch", res.get(SAT.ADM_SIGNED_BY));
	}
}
