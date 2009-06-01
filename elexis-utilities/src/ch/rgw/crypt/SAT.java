/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import ch.rgw.crypt.Cryptologist.VERIFY_RESULT;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.SoapConverter;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.Var;

/**
 * Secure Authenticated Transmission The Transmitter stores a hashtable in an
 * encrypted and signed wrapper.
 * 
 * @author Gerry
 * 
 */
public class SAT {
	public static final String RESULT_BAD_SIGNATURE = "Bad signature";
	public static final String ADM_TIMESTAMP = "ADM_timestamp";
	public static final String ADM_SIGNED_BY = "ADM_user";
	public static final String ADM_PAYLOAD = "ADM_payload";
	public static final String ADM_SIGNATURE = "ADM_signature";
	public static final String ERR_SERVER = "Server error: ";
	public static final String ERR_DECRYPT = "Decrypt error: ";

	Cryptologist crypt;

	/**
	 * Create a new SAT actor
	 * 
	 * @param c
	 *            a fully configured Cryptologist
	 */
	public SAT(Cryptologist c) {
		crypt = c;
	}

	/**
	 * Decrypt, and verify a packet (using our preconfigured Cryptologist)
	 * 
	 * @param encrypted
	 *            the encrypted packet
	 * @return a hashmap with the Parameters and an additional parameter
	 *         "ADM_SIGNED_BY" containing the sender's ID
	 */
	public Var unwrap(byte[] encrypted) throws Exception{
		if (encrypted == null) {
			throw new Exception("Null packet from server");
		}
		if (encrypted.length < 25) { // is probably an error message
			throw new Exception(new String(encrypted));
		}
		Result<byte[]> dec = crypt.decrypt(encrypted);
		if ((dec == null) || (!dec.isOK())) {
			return new Var(Var.KEY_ERROR,"Could not decrypt");
		}
		byte[] decrypted = dec.get();
		SoapConverter sc = new SoapConverter();
		if (sc.load(decrypted)) {
			Map<String, Object> fields = sc.getParameters();
			String user = (String) fields.get(ADM_SIGNED_BY);
			Long ts = (Long) fields.get(ADM_TIMESTAMP);
			byte[] signature = (byte[]) fields.get(ADM_SIGNATURE);
			if ((StringTool.isNothing(user)) || (signature == null)) {
				throw new Exception("Bad protocol");
			}
			if (ts == null || ((System.currentTimeMillis() - ts) > 300000)) {
				throw new Exception("timeout");
			}
			Map<String, Object> ret = (Map<String, Object>) fields
					.get(ADM_PAYLOAD);
			byte[] digest = calcDigest(sc);
			if(crypt.verify(digest, signature, user)==VERIFY_RESULT.OK){
				ret.put(ADM_SIGNED_BY, user);
				return new Var(ret);
			} else {
				return new Var(Var.KEY_ERROR,RESULT_BAD_SIGNATURE);
			}
		} else {
			return new Var(Var.KEY_ERROR,"Invalid Message");
		}

	}

	/**
	 * Sign and encrypt a HashMap. We sign the unencrypted data and encrypt
	 * later. This imposes more load on verifying (since it must be decrypted
	 * prior to verify the signature) but improves stability against replay
	 * attacks.
	 * 
	 * @param hash
	 *            a hashtable containing arbitrary String/Object pairs. All
	 *            objects must be Serializables. Keynames starting with ADM_ are
	 *            reserved and must not be used.
	 * @param dest
	 *            the receiver. The Object will be encoded with the receiver's
	 *            public key
	 * @return a byte array containing the signed and encrypted Hashmap. This
	 *         will remain valid for 5 Minutes.
	 * @throws Exception 
	 */
	public byte[] wrap(Var var, String dest) throws Exception {

		SoapConverter sc = new SoapConverter();
		sc.create("xidClient", "0.1.0", "elexis.ch");
		sc.addMap(null, ADM_PAYLOAD, var);
		sc.addIntegral(ADM_TIMESTAMP, System.currentTimeMillis());
		sc.addString(ADM_SIGNED_BY, crypt.getUser());
		byte[] digest = calcDigest(sc);
		byte[] signature = crypt.sign(digest);
		sc.addArray(ADM_SIGNATURE, signature);
		String xml = sc.toString();
		byte[] wrapped = crypt.encrypt(StringTool.getBytes(xml), dest);
		return wrapped;
	}

	@Deprecated
	public String sendRequest(String hostaddress, String request) {

		try {
			// Connect to server
			URLConnection conn = (new URL(hostaddress)).openConnection();

			// Get output stream
			conn.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(conn
					.getOutputStream());

			String rx = new String(Base64Coder.encodeString(request));
			out.write("request=" + rx);
			out.close();
			StringBuilder sb = new StringBuilder();
			InputStream is = conn.getInputStream();
			int in;
			while (((in = is.read()) != -1)) {
				sb.append((byte) in);
			}
			return Base64Coder.decodeString(sb.toString()); // .decodeBuffer(conn.getInputStream());

		} catch (Exception ex) {
			ExHandler.handle(ex);
			return "";
		}

	}

	public byte[] sendRequest(String hostaddress, byte[] request) {
		try {
			// Connect to server
			URLConnection conn = (new URL(hostaddress)).openConnection();

			// Get output stream
			conn.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(conn
					.getOutputStream());

			// String rx = new String(Base64Coder.encode(request));
			String rx = StringTool.enPrintableStrict(request);
			out.write("request=" + rx);
			out.close();
			// StringBuilder sb = new StringBuilder();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = conn.getInputStream();
			int in;
			while (((in = is.read()) != -1)) {
				// sb.append((byte) in);
				baos.write(in);
			}
			// return Base64Coder.decode(sb.toString()); //
			// .decodeBuffer(conn.getInputStream());
			return baos.toByteArray();

		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
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
			if (type.equalsIgnoreCase(SoapConverter.TYPE_MAP)) {
				addParameters(el, digest);
			} else if (name.equalsIgnoreCase(ADM_SIGNATURE)) {
				continue;
			} else {
				digest.update(StringTool.getBytes(type));
				digest.update(StringTool.getBytes(name));
				digest.update(StringTool.getBytes(el.getTextTrim()));
			}
		}
	}
}
