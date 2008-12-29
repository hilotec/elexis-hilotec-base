package ch.rgw.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class JCECrypter implements Cryptologist {
	public static short VERSION=0x0100;
	public static short MAGIC=(short)0xefde;
	public static short  KEY_MARKER=0x10;
	public static short  IV_MARKER=0x20;
	public static short  DATA_MARKER=0x30;
	
	private JCEKeyManager km;
	private String userKey;
	private char[] pwd;

	/**
	 * Create a new Crypter. If the named keystore does not exist, it well
	 * created newly and a key for the named user will be created as well.
	 * @param keystore keystore to use or NULL for default keystore
	 * @param kspwd keystore password or NULL for default password
	 * @param mykey identifier for user's key in the named keystore
	 * @param keypwd password for the user's key
	 * @throws Exception
	 */
	public JCECrypter(String keystore, char[] kspwd, String mykey, char[] keypwd)
			throws Exception {
	
		
		userKey = mykey;
		pwd = keypwd;
		Security
		.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // Add

		if (keystore == null) {
			keystore = System.getProperty("user.home") + File.separator
					+ ".JCECrypter";
			if (kspwd == null) {
				kspwd = "JCECrypterDefault".toCharArray();
			}
		}
		km = new JCEKeyManager(keystore, null, kspwd);
		if (km.load(true)) {
			if (!km.existsPrivate(mykey)) {
				KeyPair kp = km.generateKeys();
				X509Certificate cert = km.generateCertificate(kp.getPublic(),
						kp.getPrivate(), userKey, userKey, null, null);
				km.addKeyPair(kp.getPrivate(), cert, pwd);
				km.save();
			}
		} else {
			km = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (pwd != null) {
			for (int i = 0; i < pwd.length; i++) {
				pwd[i] = 0;
			}
		}
		super.finalize();
	}

	public Result<byte[]> decrypt(byte[] encrypted) {

		try {
			PrivateKey pk = km.getPrivateKey(userKey, pwd);
			//Cipher rsaCip = Cipher.getInstance("RSA/None/OAEPPadding", "BC");
			Cipher rsaCip=Cipher.getInstance("RSA/ECB/PKCS1Padding");
			rsaCip.init(Cipher.DECRYPT_MODE, pk);
			ByteArrayInputStream bais=new ByteArrayInputStream(encrypted);
			DataInputStream di=new DataInputStream(bais);
			int magic=di.readShort();
			if(magic!=MAGIC){
				return new Result<byte[]>(Result.SEVERITY.ERROR,1,"Bad format",null,true);
			}
			int version=di.readShort();
			int mark=di.readShort();
			if(mark!=KEY_MARKER){
				return new Result<byte[]>(Result.SEVERITY.ERROR,2,"unexpected block marker",null,true);
			}
			int len=di.readInt();
			byte[] d=new byte[len];
			di.readFully(d);
			Key bfKey = (SecretKey)new SecretKeySpec(rsaCip.doFinal(d), "Blowfish");
            /*
			mark=di.readShort();
            if(mark!=IV_MARKER){
            	return new Result<byte[]>(Result.SEVERITY.ERROR,3,"unexpected block marker",null,true);
            }
        	len=di.readInt();
			d=new byte[len];
			di.readFully(d);
			byte[] iv=rsaCip.doFinal(d);
			*/
			Cipher aesCip=Cipher.getInstance("Blowfish");
			aesCip.init(Cipher.DECRYPT_MODE, bfKey /*, new IvParameterSpec(iv)*/);
			mark=di.readShort();
			if(mark!=DATA_MARKER){
            	return new Result<byte[]>(Result.SEVERITY.ERROR,4,"unexpected block marker",null,true);
            }
			len=di.readInt();
			d=new byte[len];
			di.readFully(d);
			return new Result<byte[]>(aesCip.doFinal(d));
			
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return null;
	}

	public byte[] encrypt(byte[] source, String receiverKeyName) {
		try {
			PublicKey cert = km.getPublicKey(receiverKeyName);
			Cipher bfCip = Cipher.getInstance(
					"Blowfish");
			byte[] bfKey=generateBlowfishKey();
			SecretKeySpec spec=new SecretKeySpec(bfKey,"Blowfish");
			bfCip.init(Cipher.ENCRYPT_MODE, spec);
	        
	        //Cipher rsaCip=Cipher.getInstance("RSA/None/OAEPPadding", "BC");
			Cipher rsaCip=Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        rsaCip.init(Cipher.ENCRYPT_MODE, cert);
	
	        ByteArrayOutputStream baos=new ByteArrayOutputStream();
	        DataOutputStream dao=new DataOutputStream(baos);
	        
	        dao.writeShort(MAGIC);
	        dao.writeShort(VERSION);
	        writeBlock(dao,rsaCip.doFinal(bfKey),KEY_MARKER);
	        //writeBlock(dao,rsaCip.doFinal(aes_iv),IV_MARKER);
	       // aesCip.init(Cipher.ENCRYPT_MODE, aesKey,new IvParameterSpec(aes_iv));
	        writeBlock(dao,bfCip.doFinal(source),DATA_MARKER);
	        dao.flush();
			return baos.toByteArray();
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}

	private void writeBlock(DataOutputStream o, byte[] block, int marker) throws Exception{
		o.writeShort(marker);
		o.writeInt(block.length);
		o.write(block);
		for(int i=0;i<block.length;i++){
			block[i]=0;
		}
	}
	public byte[] sign(byte[] source) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA", "BC");
			PrivateKey pk = km.getPrivateKey(userKey, pwd);
			SecureRandom sr = new SecureRandom();
			sig.initSign(pk, sr);
			sig.update(source);
			return sig.sign();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}

		return null;
	}

	public Result<String> verify(byte[] data, byte[] signature,
			String signerKeyName) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA", "BC");
			PublicKey pk = km.getPublicKey(signerKeyName);
			if (pk == null) {
				return new Result<String>(Result.SEVERITY.WARNING, 1,
						"No key found", signerKeyName, true);
			}
			sig.initVerify(pk);
			sig.update(data);
			if (sig.verify(signature)) {
				return new Result<String>("OK");
			} else {
				return new Result<String>(Result.SEVERITY.ERROR, 2,
						"Signature failed", signerKeyName, true);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return new Result<String>(Result.SEVERITY.FATAL, 3, "could not verify",
				signerKeyName, true);
	}

	public boolean hasCertificateOf(String alias) {
		return km.existsCertificate(alias);
	}

	public boolean hasKeyOf(String alias) {
		return km.existsPrivate(alias);
	}

	public boolean addCertificate(X509Certificate cert) {
		if (km.addCertificate(cert)) {
			return km.save();
		}
		return false;
	}

	public KeyPair generateKeys(String alias, char[] keypwd,
			TimeTool validFrom, TimeTool validUntil) {
		KeyPair ret = km.generateKeys();
		if (alias != null) {
			X509Certificate cert = generateCertificate(ret.getPublic(), alias,
					validFrom, validUntil);
			try {
				km.addKeyPair(ret.getPrivate(), cert, keypwd);
				km.save();
			} catch (Exception ex) {
				ExHandler.handle(ex);
				return null;
			}
		}
		return ret;
	}

	public X509Certificate generateCertificate(PublicKey pk, String alias,
			TimeTool validFrom, TimeTool validUntil) {
		PrivateKey priv = km.getPrivateKey(userKey, pwd);
		try {
			X509Certificate ret = km.generateCertificate(pk, priv, userKey,
					alias, validFrom, validUntil);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getUser() {
		return userKey;
	}

	private byte[] generateBlowfishKey(){
		try{
			KeyGenerator key_gen = KeyGenerator.getInstance("Blowfish"); 
			SecretKey key= key_gen.generateKey();
			return key.getEncoded();
		}catch(Exception ex){
			ExHandler.handle(ex);
			return null;
		}
	}
	private Key generateAESKey() {
		try{
			KeyGenerator key_gen = KeyGenerator.getInstance("AES", "BC"); 
			key_gen.init(128, km.getRandom()); 
			Key aes_key = key_gen.generateKey();
			return aes_key;
		}catch(Exception ex){
			ExHandler.handle(ex);
			return null;
		}
	}

	public X509Certificate getCertificate(String alias) {
		return km.getCertificate(alias);
	}

	public boolean isFunctional() {
		return true;
	}
}
