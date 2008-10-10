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
	
	private static final String adminname="admin@elexistest.ch";
	private static final String alicename="alice@elexistest.ch";
	private static final String bobname="bob@elexistest.ch";
	private static final String adminpwd="adminpwd";
	private static final String alicepwd="alicepwd";
	private static final String bobpwd="bobpwd";
		
	public void testCreate() throws Exception{
		crypt=new JCECrypter(null,null,adminname,adminpwd.toCharArray());
		assertTrue(crypt.hasKeyOf(adminname));
	}
	
	public void testCreateKeys() throws Exception{
		if(!crypt.hasKeyOf("alice")){
			/*KeyPair kp= */crypt.generateKeys(alicename,alicepwd.toCharArray(),null,null);
			
		}
		if(!crypt.hasKeyOf("bob")){
			/*KeyPair kp= */crypt.generateKeys(bobname,bobpwd.toCharArray(),null,null);
		}
	}
	public void testWrap() throws Exception{
		crypt=new JCECrypter(null,null,alicename,alicepwd.toCharArray());
		SAT sat = new SAT(crypt);
		HashMap<String, Object> hash = new HashMap<String, Object>();
		hash.put("test", "Ein Testtext");
		byte[] result = sat.wrap(hash, bobname);
		assertNotNull(result);
		System.out.println(new String(result));
		encrypted = result;
	}
	
	public void testUnwrap() throws Exception{
		crypt=new JCECrypter(null,null,bobname,bobpwd.toCharArray());
		SAT sat = new SAT(crypt);
		Result<HashMap<String, Object>> result = sat.unwrap(encrypted);
		assertTrue(result.isOK());
		HashMap<String, Object> res = result.get();
		String val = (String) res.get("test");
		assertEquals(val, "Ein Testtext");
		assertEquals(alicename, res.get(SAT.ADM_SIGNED_BY));
	}
}
