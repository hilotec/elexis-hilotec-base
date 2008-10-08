// $Id: KeyManager.java 253 2005-03-14 17:21:12Z gerry $

/***********************************************************
 **  Copyright (c) 2002-2008 G. Weirich     	          **
 ***********************************************************/

package ch.rgw.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V1CertificateGenerator;

import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Vereinfachtes API für die Java Kryptographie-Klassen KeyManager stellt die
 * Verbindung zu einem keystore her und lässt auf die darin befindlichen
 * Schlüssel zugreifen.
 */

public class JCEKeyManager {
	public static String Version() {
		return "0.1.5";
	}

	private KeyStore ks;
	private static SecureRandom _srnd;
	private static Logger log;

	@SuppressWarnings("unused")
	private JCEKeyManager() {
	}

	char[] storePwd = null;
	String ksType;
	String ksFile;

	static {
		log = Logger.getLogger("KeyManager");
		Security
				.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // Add
		// provider
		// .
		// _srnd = SecureRandom.getInstance("SHA1PRNG","SUN"); // Create random
		// number generator.

		_srnd = new SecureRandom();
	}

	/**
	 * The Constructor does not actually create or access a keystore but only
	 * defines the access rules The keystore ist valid after a successful call
	 * to create() or load()
	 * 
	 * @param keystoreFile
	 *            path and name of the keystore to use if null:
	 *            {user.home}/.keystore is used.
	 * @param type
	 *            type of the keystore. If NULL: jks
	 * @param keystorePwd
	 *            password for the keystore must not be null.
	 */
	public JCEKeyManager(String keystoreFile, String type, char[] keystorePwd) {
		if (StringTool.isNothing(keystoreFile)) {
			ksFile = System.getProperty("user.home") + "/.keystore";
		} else {
			ksFile = FileTool.resolveFile(keystoreFile).getAbsolutePath();
		}
		log.log(Level.FINE, "ksPathName: " + ksFile);

		if (StringTool.isNothing(type)) {
			ksType = "jks";
		} else {
			ksType = type;
		}
		storePwd = keystorePwd;
	}

	/**
	 * Keystore laden
	 */
	public boolean load(boolean bCreateIfNotExists) {
		try {
			File ksf=new File(ksFile);
			if(!ksf.exists()){
				return create(false);
			}
			ks = KeyStore.getInstance(ksType);
			ks.load(new FileInputStream(ksFile), storePwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.log(Level.SEVERE,
					"No Keystore found or coudl not open Keystore");
			return false;
		}
		return true;
	}

	public boolean create(boolean bDeleteIfExists) {
		File ksF = new File(ksFile);
		if(ksF.exists()){
			if(bDeleteIfExists){
				if(!ksF.delete()){
					return false;
				}
			}else{
				return false;
			}
		}
		if (ks == null) {
			try {
				ks = KeyStore.getInstance(ksType);
				ks.load(null, null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return save();
	}

	public boolean save() {
		try {
			ks.store(new FileOutputStream(ksFile), storePwd);
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return false;
	}

	public boolean isKeystoreLoaded() {
		return (ks == null) ? false : true;
	}

	/**
	 * Public key mit dem Alias alias holen. Es wird auf Gültigkeit des
	 * Zertifiktats getestet
	 * 
	 * @param alias
	 *            Name des gesuchten Schlüssels
	 * @return den gesuchten Schlüssel oder null - nicht gefunden
	 * */
	public PublicKey getPublicKey(String alias) {
		if (ks == null) {
			log.log(Level.WARNING, "Keystore nicht geladen");
			if (!load(true)) {
				return null;
			}
		}
		try {

			java.security.cert.Certificate cert = ks.getCertificate(alias);
			if (cert == null) {
				log.log(Level.WARNING, "No certificate \"" + alias + "\"found");
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
	public PublicKey getPublicKey(InputStream is) {
		try {
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory
					.getInstance("X.509");
			java.security.cert.Certificate cert = cf.generateCertificate(is);
			return cert.getPublicKey();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}

	/**
	 * Private key mit dem Alias alias holen
	 * 
	 * @param alias
	 *            Zu holender Schlüssel
	 * @param pwd
	 *            Schlüssel-Passwort
	 * @return den Schlüssel oder null
	 */
	public PrivateKey getPrivateKey(String alias, char[] pwd) {

		try {
			if (StringTool.isNothing(alias) || (!ks.isKeyEntry(alias))) {
				log.log(Level.WARNING, "Alias falsch oder fehlend");
				return null;
			}
			return (PrivateKey) ks.getKey(alias, pwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.log(Level.SEVERE, "Kann Key nicht laden");
			return null;
		}
	}

	/**
	 * Zertifikat dem keystore zufügen
	 * 
	 * @param cert
	 *            Ein X.509 Zertifikat
	 * @return true bei Erfolg
	 */
	public boolean addCertificate(X509Certificate cert) {

		try {
			ks.setCertificateEntry(cert.getSubjectX500Principal().getName(),
					cert);
			return save();
		} catch (KeyStoreException e) {
			ExHandler.handle(e);
			return false;
		}
	}

	/*
	 * public Certificate createCertificate(PublicKey pk, PrivateKey
	 * signingKey){ CertificateFactory
	 * cf=CertificateFactory.getInstance("X.509"); } throws InvalidKeyException,
	 * NoSuchProviderException, SignatureException {
	 */

	/**
	 * Generate a certificate from a public key and a signing private key.
	 * 
	 * @param pk
	 *            the key to make a certficate from
	 * @param signingKey
	 *            the signer's private key
	 * @param name
	 *            of the issuer
	 * @param name
	 *            of the certificate holder
	 * @return the signed certificate.
	 * @throws KeyStoreException
	 * 
	 */
	public X509Certificate generateCertificate(PublicKey pk,
			PrivateKey signingKey, String issuer, String subject,
			TimeTool ttFrom, TimeTool ttUntil) throws InvalidKeyException,
			NoSuchProviderException, SignatureException,
			CertificateEncodingException, IllegalStateException,
			NoSuchAlgorithmException, KeyStoreException {

		// generate the certificate
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=" + issuer));
		if (ttFrom == null) {
			ttFrom = new TimeTool();
		}
		if (ttUntil == null) {
			ttUntil = new TimeTool(ttFrom);
			ttUntil.add(TimeTool.YEAR, 2);
		}
		certGen.setNotBefore(ttFrom.getTime());
		certGen.setNotAfter(ttUntil.getTime());
		certGen.setSubjectDN(new X500Principal("CN=" + subject));
		certGen.setPublicKey(pk);
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
		X509Certificate cert = certGen.generate(signingKey, "BC");
		ks.setCertificateEntry(subject, cert);
		return cert;
	}

	public boolean addKeyPair(PrivateKey kpriv, X509Certificate cert, char[] keyPwd)
			throws Exception {
		String alias=cert.getSubjectDN().getName();
		ks.setKeyEntry(alias, kpriv, keyPwd, new Certificate[] { cert });

		return true;
	}

	public boolean existsPrivate(String alias) {
		try {
			return ks.isKeyEntry(alias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean existsCertificate(String alias) {
		try {
			return ks.isCertificateEntry(alias);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}

	public KeyPair generateKeys() {
		try {
			KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA", "BC");
			kp.initialize(1024, _srnd);
			return kp.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	/*
	 * public DHParameterSpec createParams() throws Exception{
	 * AlgorithmParameterGenerator paramGen =
	 * AlgorithmParameterGenerator.getInstance("DH"); paramGen.init(512);
	 * AlgorithmParameters params = paramGen.generateParameters();
	 * DHParameterSpec dhps = (DHParameterSpec)
	 * params.getParameterSpec(DHParameterSpec.class); return dhps; }
	 * 
	 * public KeyPair createKeyPair(DHParameterSpec params){ try {
	 * KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman"); if
	 * (params != null) { kpg.initialize(params); } KeyPair kp =
	 * kpg.generateKeyPair();
	 * 
	 * return kp; } catch (Exception ex) { ExHandler.handle(ex); return null; }
	 * }
	 */

}
