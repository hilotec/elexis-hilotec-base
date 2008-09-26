// $Id: KeyManager.java 253 2005-03-14 17:21:12Z gerry $

/***********************************************************
 **  Copyright (c) 2002-2008 G. Weirich     	          **
 ***********************************************************/

package ch.rgw.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.spec.DHParameterSpec;

import ch.rgw.IO.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import chapter6.X509V1CreateExample;

/**
 * Vereinfachtes API für die Java Kryptographie-Klassen KeyManager stellt die Verbindung zu einem
 * keystore her und lässt auf die darin befindlichen Schlüssel zugreifen.
 */

public class JCEKeyManager {
	public static String Version(){
		return "0.1.5";
	}
	
	private KeyStore ks;
	private static SecureRandom _srnd;
	private static Logger log;
	
	@SuppressWarnings("unused")
	private JCEKeyManager(){}
	
	char[] storePwd = null;
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
	public JCEKeyManager(String keystoreFile, String type, char[] keystorePwd){
		if (StringTool.isNothing(keystoreFile)) {
			ksFile = System.getProperty("user.home") + "/.keystore";
		} else {
			ksFile = FileTool.resolveFile(keystoreFile).getAbsolutePath();
		}
		log.log(Level.FINE, "ksPathName: " + ksFile);
		
		if(StringTool.isNothing(type)){
			ksType="jks";
		}else{
			ksType=type;
		}
		storePwd=keystorePwd;
	}
	
	/**
	 * Keystore laden 
	 */
	public boolean load(){
		try {
			ks = KeyStore.getInstance(ksType);
			ks.load(new FileInputStream(ksFile), storePwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
				log.log(Level.SEVERE, "No Keystore found or coudl not open Keystore");
				return false;
		}
		return true;
	}
	
	public boolean create(){
		return load() && save();
	}
	
	public boolean save(){
		try {
			ks.store(new FileOutputStream(ksFile), storePwd);
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return false;
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

	public boolean addCertificate(X509Certificate cert){
		
		try {
			ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
			return save();
		} catch (KeyStoreException e) {
			ExHandler.handle(e);
			return false;
		}
	}
	
	public Certificate createCertificate(PublicKey pk, PrivateKey signingKey){
		CertificateFactory cf=CertificateFactory.getInstance("X.509");
		X509Certificate x5=
	}
	public boolean addKeyPair(KeyPair kp){
		PrivateKey privk=kp.getPrivate();
		PublicKey pubk=kp.getPublic();
		CertificateFactory cf=CertificateFactory.getInstance("X.509");
		
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bOut.write(X509V1CreateExample.generateV1Certificate(kp).getEncoded());
        
        bOut.close();
        
        InputStream in = new ByteArrayInputStream(bOut.toByteArray());
        
        // create the certificate factory
        CertificateFactory fact = CertificateFactory.getInstance("X.509","BC");
        
        // read the certificate
        X509Certificate    x509Cert = (X509Certificate)fact.generateCertificate(in);
        
		ks.setKeyEntry(alias, key, pwd, chain)
	}
	/*
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
	*/
	
}
