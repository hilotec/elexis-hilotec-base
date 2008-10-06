package ch.rgw.crypt;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class JCECrypter implements Cryptologist {
	private JCEKeyManager km;
	private String userKey;
	private char[] pwd;
	
	public JCECrypter(String keystore, char[] kspwd, String mykey, char[] keypwd) throws Exception{
		userKey=mykey;
		pwd=keypwd;
		if(keystore==null){
			keystore=System.getenv("user.home")+File.separator+".JCECrypter";
			if(kspwd==null){
				kspwd="JCECrypterDefault".toCharArray();
			}
		}
		km=new JCEKeyManager(keystore,null,kspwd);
		if(!km.existsPrivate(mykey)){
			KeyPair kp=km.generateKey(mykey);
			km.addKeyPair(kp, mykey, keypwd);
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

	public byte[] decrypt(byte[] encrypted, char[] pwd){
		// TODO Auto-generated method stub
		return null;
	}
	
	public byte[] encrypt(byte[] source, String receiverKeyName){
		try{
			
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return null;
	}
	
	public byte[] sign(byte[] source, char[] pwd){
		try {
			Signature sig=Signature.getInstance("RSA", "BC");
			PrivateKey pk=km.getPrivateKey(userKey, pwd);
			SecureRandom sr=SecureRandom.getInstance("DSA", "BC");
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
	
}
