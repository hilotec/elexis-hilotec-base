// $Id: KeyManager.java 253 2005-03-14 17:21:12Z gerry $

/***********************************************************
 **  Copyright (c) 2002-2008 G. Weirich     	          **
 ***********************************************************/

package ch.rgw.crypt;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.spec.DHParameterSpec;

import ch.rgw.IO.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Vereinfachtes API für die Java Kryptographie-Klassen KeyManager stellt die Verbindung zu einem
 * keystore her und lässt auf die darin befindlichen Schlüssel zugreifen.
 */

public class KeyManager {
	public static String Version(){
		return "0.1.4";
	}
	
	private KeyStore ks;
	private static SecureRandom _srnd;
	private static Logger log;
	
	private KeyManager(){}
	
	char[] pwd = null;
	String ksType;
	String ksFile;
	
	static {
		log = Logger.getLogger("KeyManager");
		_srnd = new SecureRandom();
	}
	
	/**
	 * The Constructor does not actually create or access a keystore but only defines the access
	 * rules The keystore ist valid after a successful call to create() or load()
	 * 
	 * @param keystoreFile
	 *            path and name of the keystore to use if null: {user.home}/.keystore is used.
	 * @param type
	 *            type of the keystore. If NULL: jks
	 * @param keystorePwd
	 *            password for the keystore must not be null.
	 */
	public KeyManager(String keystoreFile, String type, char[] keystorePwd){
		if (StringTool.isNothing(keystoreFile)) {
			ksFile = System.getProperty("user.home") + "/.keystore";
		} else {
			ksFile = FileTool.resolveFile(keystoreFile).getAbsolutePath();
		}
		log.log(Level.FINE, "ksPathName: " + ksFile);
		
		if(StringTool.isNothing(type)){
			ksType="jks";
		}else{
			ksType="jces";
		}
		
	}
	
	/**
	 * Keystore laden 
	 */
	public boolean load(){
		try {
			ks = KeyStore.getInstance(ksType);
			ks.load(new FileInputStream(ksFile), pwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
				log.log(Level.SEVERE, "No Keystore found or coudl not open Keystore");
				return false;
		}
		return true;
	}
	
	public boolean isKeystoreLoaded(){
		return (ks == null) ? false : true;
	}
	
	/** Public key mit dem Alias alias holen */
	public PublicKey getPublicKey(String alias){
		if (ks == null) {
			log.log(Level.WARNING,"Keystore nicht geladen");
			if(!load()){
				return null;
			}
		}
		try {
			
			java.security.cert.Certificate cert = ks.getCertificate(alias);
			if (cert == null) {
				log.log(Level.WARNING,"No certificate \"" + alias + "\"found");
				return null;
			} else {
				return cert.getPublicKey();
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	/** Public key aus einem Input Stream lesen */
	public PublicKey getPublicKey(InputStream is){
		try {
			java.security.cert.CertificateFactory cf =
				java.security.cert.CertificateFactory.getInstance("X.509");
			java.security.cert.Certificate cert = cf.generateCertificate(is);
			return cert.getPublicKey();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Private key mit dem Alias alias holen
	 * @param alias Zu holender Schlüssel
	 * @param pwd Schlüssel-Passwort
	 * @return den Schlüssel oder null
	 */
	public PrivateKey getPrivateKey(String alias, char[] pwd){
		
		try {
			if (StringTool.isNothing(alias) || (!ks.isKeyEntry(alias))) {
				log.log(Level.WARNING,"Alias falsch oder fehlend");
				return null;
			}
			return (PrivateKey) ks.getKey(alias, pwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.log(Level.SEVERE,"Kann Key nicht laden");
			return null;
		}
	}
	
	public DHParameterSpec createParams() throws Exception{
		AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
		paramGen.init(512);
		AlgorithmParameters params = paramGen.generateParameters();
		DHParameterSpec dhps = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
		return dhps;
		
	}
	
	public KeyPair createKeyPair(DHParameterSpec params){
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
			if (params != null) {
				kpg.initialize(params);
			}
			KeyPair kp = kpg.generateKeyPair();
			
			return kp;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	public byte[] createSecretKey(int bitsize, PrivateKey encoder){
		return null;
	}
}
