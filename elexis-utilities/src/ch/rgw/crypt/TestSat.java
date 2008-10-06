/**
 * 
 */
package ch.rgw.crypt;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.HashMap;

import junit.framework.TestCase;

/**
 * @author user
 * 
 */
public class TestSat extends TestCase {

	static Cryptologist c;
	static KeyPair rootkeys;
	static Certificate rootCert;
	static final byte[] testArr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	static KeyPair destKeys;
	static KeyPair origKeys;

	public TestSat(String name) {
		super(name);

	}

	public void testCreateKey() throws Exception {
		c = new JCECrypter(null,null,"alice","alicepwd".toCharArray());
		assertTrue(km.load(true));
		if(!km.existsPrivate("root")){
			rootkeys = km.generateKey("root");
			assertNotNull(rootkeys);
			km.addKeyPair(rootkeys,"root","rootpwd".toCharArray());
//			rootCert = km.generateCertificate(rootkeys.getPublic(), rootkeys
//				.getPrivate(),"issuerTest","root",null,null);
//			assertNotNull(rootCert);

		}
		if(!km.existsPrivate("bob")){
			destKeys = km.generateKey("bob");
			assertNotNull(destKeys);
			km.addKeyPair(destKeys,"bob","bobpwd".toCharArray());
						
		}
		if(!km.existsPrivate("alice")){
			origKeys = km.generateKey("alice");
			assertNotNull(origKeys);
			km.addKeyPair(origKeys,"alice","alicepwd".toCharArray());
				
		}
	
	}

	public void testEncryption() throws Exception {
		SAT sat = new SAT(c, "test");
		HashMap<String, Object> raw = new HashMap<String, Object>();
		raw.put("payload", testArr);
		sat.wrap(raw, "bob", "alicepwd".toCharArray());
	}

}
