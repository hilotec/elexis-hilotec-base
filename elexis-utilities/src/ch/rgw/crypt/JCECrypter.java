package ch.rgw.crypt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import ch.rgw.tools.ExHandler;

public class JCECrypter implements Cryptologist {
	
	public byte[] decrypt(byte[] encrypted, char[] pwd){
		// TODO Auto-generated method stub
		return null;
	}
	
	public byte[] encrypt(byte[] source, String receiverKeyName){
		// TODO Auto-generated method stub
		return null;
	}
	
	public KeyPair generateKeys(){
		
		return null;
	}
	
	public byte[] sign(byte[] source, char[] pwd){
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean verify(byte[] data, byte[] signature, String signerKeyName){
		// TODO Auto-generated method stub
		return false;
	}
	
	public KeyPair createKeyPair(String ident){
		KeyPairGenerator kpGen;
		try {
			kpGen = KeyPairGenerator.getInstance("DSA", "SUN");
			kpGen.initialize(1024, new SecureRandom());
			return kpGen.generateKeyPair();
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return null;
	}
	
}
