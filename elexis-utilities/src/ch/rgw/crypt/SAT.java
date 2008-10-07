/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: SAT.java 4317 2008-08-27 05:19:24Z rgw_ch $
 *******************************************************************************/

package ch.rgw.crypt;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.SoapConverter;
import ch.rgw.tools.StringTool;

/**
 * Secure Authenticated Transmission The Transmitter stores a hashtable in an
 * encrypted and signed wrapper. For best compatibility we use SOAP messages
 * encrypted with GnuPG
 * 
 * @author Gerry
 * 
 */
public class SAT {
	public static final String ADM_TIMESTAMP = "ADM_timestamp";
	public static final String ADM_SIGNED_BY = "ADM_user";
	public static final String ADM_PAYLOAD = "ADM_payload";
	public static final String ADM_SIGNATURE = "ADM_signature";

	// static final Pattern signatureEN=Pattern.compile(".+gpg: good signature
	// from
	// .+<(.+)>.*",Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	// static final Pattern signatureDE=Pattern.compile(".+gpg: Korrekte
	// Unterschrift von
	// .+<(.+)>.*",Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	Cryptologist crypt;
	String userKey;

	/**
	 * Create a new SAT actor
	 * 
	 * @param c
	 *            a fully configured Cryptologist
	 * @param userKey
	 *            the keyname of the sender.
	 */
	public SAT(Cryptologist c, String userKey) {
		crypt = c;
		this.userKey = userKey;
	}

	/**
	 * Decrypt, and verify a packet
	 * 
	 * @param encrypted
	 *            the encrypted packet
	 * @param pwd
	 *            the password to decrypt
	 * @return a hashmap with the Parameters and an additional parameter
	 *         "ADM_user" containing the sender's ID
	 */
	public Result<HashMap<String, Object>> unwrap(byte[] encrypted, char[] pwd) {
		byte[] decrypted = crypt.decrypt(encrypted, pwd);
		SoapConverter sc = new SoapConverter();
		if (sc.load(decrypted)) {
			HashMap<String, Object> fields = sc.getParameters();
			String user = (String) fields.get(ADM_SIGNED_BY);
			Long ts = (Long) fields.get(ADM_TIMESTAMP);
			byte[] signature = (byte[]) fields.get(ADM_SIGNATURE);
			if ((StringTool.isNothing(user)) || (signature == null)) {
				return new Result<HashMap<String, Object>>(
						Result.SEVERITY.ERROR, 4, "Bad protocol", null, true);
			}
			if (ts == null || ((System.currentTimeMillis() - ts) > 300000)) {
				return new Result<HashMap<String, Object>>(
						Result.SEVERITY.ERROR, 3, "Timeout", null, true);
			}
			byte[] digest = calcDigest(sc);
			Result<String> res=crypt.verify(digest, signature, user);
			if (res.isOK()) {
				HashMap<String, Object> ret = (HashMap<String, Object>) fields
						.get(ADM_PAYLOAD);
				ret.put(ADM_SIGNED_BY, user);
				return new Result<HashMap<String, Object>>(ret);
			} else {
				return new Result<HashMap<String, Object>>(
						Result.SEVERITY.ERROR, 6, "Bad protocol", null, true);
			}
		} else {
			return new Result<HashMap<String, Object>>(Result.SEVERITY.ERROR,
					5, "Bad signature", null, true);
		}

	}

	/**
	 * Transmit a hashtable.
	 * 
	 * @param hash
	 *            a hashtable containing arbitrary String/Object pairs. All
	 *            objects must be Serializables. Keynames starting with ADM_ are
	 *            reserved and must not be used.
	 * @param dest
	 *            the receiver. The Object will be encoded with the receiver's
	 *            public key
	 * @param pwd
	 *            Password for the sender's private key
	 * @return a byte array containing the signed and encrypted Hashmap
	 */
	public byte[] wrap(HashMap<String, Object> hash, String dest,
			char[] senderPwd) {
		try {
			SoapConverter sc = new SoapConverter();
			sc.create("xidClient", "0.0.1", "elexis.ch");
			sc.addHashMap(null, ADM_PAYLOAD, hash);
			sc.addIntegral(ADM_TIMESTAMP, System.currentTimeMillis());
			sc.addString(ADM_SIGNED_BY, userKey);
			byte[] digest = calcDigest(sc);
			byte[] signature = crypt.sign(digest, senderPwd);
			sc.addArray(ADM_SIGNATURE, signature);
			String xml = sc.toString();
			byte[] wrapped = crypt.encrypt(StringTool.getBytes(xml), dest);
			return wrapped;
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}

	public String sendRequest(String hostaddress, String request) {
		String output = "";
		try {
			// Connect to server
			URLConnection conn = (new URL(hostaddress)).openConnection();

			// Get output stream
			conn.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(conn
					.getOutputStream());

			String rx = new BASE64Encoder().encode(request.getBytes("utf-8"));
			out.write("request=" + rx);
			out.close();
			byte[] in = new BASE64Decoder().decodeBuffer(conn.getInputStream());
			return new String(in, "utf-8");

		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return output;
	}

	private byte[] calcDigest(SoapConverter sc) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			Document doc = sc.getXML();
			Element eRoot = doc.getRootElement();
			Element body = eRoot.getChild("Body", SoapConverter.ns);
			addParameters(body, digest);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			ExHandler.handle(e);
		}
		return null;
	}

	private void addParameters(Element e, MessageDigest digest) {
		List<Element> params = e.getChildren("parameter", SoapConverter.ns);
		for (Element el : params) {
			String type = el.getAttributeValue("type");
			String name = el.getAttributeValue("name");
			if (type.equalsIgnoreCase(SoapConverter.TYPE_HASH)) {
				addParameters(el, digest);
			} else if (type.equalsIgnoreCase(SoapConverter.TYPE_SIGNATURE)) {
				continue;
			} else {
				digest.update(StringTool.getBytes(type));
				digest.update(StringTool.getBytes(name));
				digest.update(StringTool.getBytes(el.getTextTrim()));
			}
		}
	}
}
