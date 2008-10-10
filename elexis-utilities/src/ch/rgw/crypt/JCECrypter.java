package ch.rgw.crypt;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class JCECrypter implements Cryptologist {
	private JCEKeyManager km;
	private String userKey;
	private char[] pwd;
	
	public JCECrypter(String keystore, char[] kspwd, String mykey, char[] keypwd) throws Exception{
		userKey=mykey;
		pwd=keypwd;
		if(keystore==null){
			keystore=System.getProperty("user.home")+File.separator+".JCECrypter";
			if(kspwd==null){
				kspwd="JCECrypterDefault".toCharArray();
			}
		}
		km=new JCEKeyManager(keystore,null,kspwd);
		if(km.load(true)){
			if(!km.existsPrivate(mykey)){
				KeyPair kp=km.generateKeys();
				X509Certificate cert=km.generateCertificate(kp.getPublic(), kp.getPrivate(), userKey, userKey, null, null);
				km.addKeyPair(kp.getPrivate(),cert,pwd);
			}
		}else{
			km=null;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(pwd!=null){
			for(int i=0;i<pwd.length;i++){
				pwd[i]=0;
			}
		}
		super.finalize();
	}

	public byte[] decrypt(byte[] encrypted){
		// TODO Auto-generated method stub
		return encrypted;
	}
	
	public byte[] encrypt(byte[] source, String receiverKeyName){
		try{
			return source;
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return null;
	}
	
	public byte[] sign(byte[] source){
		try {
			Signature sig=Signature.getInstance("SHA1withRSA", "BC");
			PrivateKey pk=km.getPrivateKey(userKey, pwd);
			SecureRandom sr=new SecureRandom();
			sig.initSign(pk, sr);
			sig.update(source);
			return sig.sign();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		} 
		
		return null;
	}
	
	public Result<String> verify(byte[] data, byte[] signature, String signerKeyName){
		try{
			Signature sig=Signature.getInstance("RSA", "BC");
			PublicKey pk=km.getPublicKey(signerKeyName);
			if(pk==null){
				return new Result<String>(Result.SEVERITY.WARNING,1,"No key found", signerKeyName,true);
			}
			sig.initVerify(pk);
			sig.update(data);
			if(sig.verify(signature)){
				return new Result<String>("OK");
			}else{
				return new Result<String>(Result.SEVERITY.ERROR,2,"Signature failed", signerKeyName,true);
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return new Result<String>(Result.SEVERITY.FATAL,3,"could not verify",signerKeyName,true);
	}
	public boolean hasCertificateOf(String alias){
		return km.existsCertificate(alias);
	}
	public boolean hasKeyOf(String alias){
		return km.existsPrivate(alias);
	}
	public boolean addCertificate(X509Certificate cert){
		if(km.addCertificate(cert)){
			return km.save();
		}
		return false;
	}
	public KeyPair generateKeys( String alias, char[] keypwd, TimeTool validFrom, TimeTool validUntil){
		KeyPair ret= km.generateKeys();
		if(alias!=null){
			X509Certificate cert=generateCertificate(ret.getPublic(), alias, validFrom, validUntil);
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
	
	public X509Certificate generateCertificate(PublicKey pk, String alias, TimeTool validFrom, TimeTool validUntil){
		PrivateKey priv=km.getPrivateKey(userKey, pwd);
		try {
			X509Certificate ret= km.generateCertificate(pk, priv, userKey, alias, validFrom, validUntil);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
