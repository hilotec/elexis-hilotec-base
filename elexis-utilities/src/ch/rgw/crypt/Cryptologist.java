package ch.rgw.crypt;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * A Cryptologist knows how to create keys and certificates, and encrypt, decrypt, sign and verify
 * byte arrays. 
 * 
 * @author gerry
 * 
 */
public interface Cryptologist {
	
	/**
	 * encrypt a byte array
	 * 
	 * @param source
	 *            the plain bytes
	 * @param receiverKeyName
	 *            name of the receiver's public key
	 * 
	 * @return the encrypted bytes or null if encryption failed
	 */
	public byte[] encrypt(byte[] source, String receiverKeyName);
	
	/**
	 * Sign a byte array (create and sign a MAC)
	 * 
	 * @param source
	 *            the bytes to sign
	 * @return the signature
	 */
	public byte[] sign(byte[] source);
	
	/**
	 * decrypt a byte array
	 * 
	 * @param encrypted
	 *            the encrypted bytes
	 * @return the plain array or null of decryption failed
	 */
	public Result<byte[]> decrypt(byte[] encrypted);
	
	/**
	 * Verify a MAC
	 * 
	 * @param data
	 *            the signed data
	 * @param signature
	 *            the signed digest
	 * @param signerKeyName
	 *            name of the signer's public key
	 * @return
	 */
	public Result<String> verify(byte[] data, byte[] signature, String signerKeyName);
	
	public boolean hasCertificateOf(String alias);
	
	public boolean hasKeyOf(String alias);
	
	public boolean addCertificate(X509Certificate cert);
	
	public KeyPair generateKeys(String alias, char[] pwd, TimeTool validFrom, TimeTool validUntil);
	
	public X509Certificate getCertificate(String alias);
	
	public X509Certificate generateCertificate(PublicKey pk, String alias, TimeTool validFrom,
		TimeTool validUntil);
	
	public String getUser();
	
	public boolean isFunctional();
}
