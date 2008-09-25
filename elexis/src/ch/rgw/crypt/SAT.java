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
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.SoapConverter;
import ch.rgw.tools.Result.SEVERITY;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Secure Authenticated Transmission
 * The Transmitter stores a hashtable in an encrypted and signed wrapper.
 * For best compatibility we use SOAP messages encrypted with GnuPG
 * @author Gerry
 *
 */
public class SAT {
	public static final String ADM_TIMESTAMP="ADM_timestamp";
	public static final String ADM_SIGNED_BY="ADM_user";
	static final Pattern signatureEN=Pattern.compile(".+gpg: good signature from .+<(.+)>.*",Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	static final Pattern signatureDE=Pattern.compile(".+gpg: Korrekte Unterschrift von .+<(.+)>.*",Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	GnuPG gpg;
	String userKey;
	/**
	 * Create a new SAT actor
	 * @param gpg a fully configured GnuPG
	 * @param sender the keyname of the sender.
	 */
	public SAT(GnuPG gpg, String userKey){
		this.gpg=gpg;
		this.userKey=userKey;
	}

	
	public Result<HashMap<String, Object>>unwrap(String encrypted, char[] pwd){
		if(gpg.decrypt(encrypted, new String(pwd))){
			String dec=gpg.getResult();
			String msg=gpg.getErrorString();
			Matcher matcher=signatureEN.matcher(msg);
			String user=null;
			if(matcher.matches()){
				user=matcher.group(1);
			}else{
				matcher=signatureDE.matcher(msg);
				if(matcher.matches()){
					user=matcher.group(1).trim();
				}
			}
			try{
				SoapConverter sc=new SoapConverter();
				sc.load(dec);
				HashMap<String, Object> hash=sc.getParameters();
				Long ts=(Long)hash.get(ADM_TIMESTAMP);
				if(ts==null || ((System.currentTimeMillis()-ts)>300000)){
					return new Result<HashMap<String, Object>>(Result.SEVERITY.ERROR,3,"Timeout",null,false);
				}
				if(user==null){
					hash.remove(ADM_SIGNED_BY);
				}else{
					hash.put(ADM_SIGNED_BY, user);
				}
				return new Result<HashMap<String, Object>>(hash);
			}catch(Exception ex){
				ExHandler.handle(ex);
				return new Result<HashMap<String, Object>>(Result.SEVERITY.ERROR,2,"Deserialize error "+ex.getMessage(),null,true);
			}
		}
		return new Result<HashMap<String, Object>>(Result.SEVERITY.ERROR,1,"Decode error",null,true);
	}
	
	/**
	 * Transmit a hashtable. 
	 * @param hash a hashtable containing arbitrary String/Object pairs. All objects must be 
	 * Serializables. Keynames starting with ADM_ are reserved and must not be used.
	 * @param dest the receiver. The Object will be encoded with the receiver's public key
	 * @param pwd Password for the sender's private key
	 * @return a String containig the ASCII-armored encrypted and signed hashtable or null on error 
	 */
	public String wrap(HashMap<String, Object> hash, String dest, char[] senderPwd){
		try{
			hash.put(ADM_TIMESTAMP, System.currentTimeMillis());
			SoapConverter sc=new SoapConverter();
			sc.create("xidClient","0.0.1","elexis.ch");
			sc.addHashMap("params", hash);
			if(gpg.signAndEncrypt(sc.toXML(), userKey, dest, new String(senderPwd))){
				return gpg.getResult();
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return null;
	}
	
	public String sendRequest(String hostaddress, String request){
		String output="";
		try{
			// Connect to server
		    URLConnection conn = (new URL(hostaddress)).openConnection();
		    
		    // Get output stream
		    conn.setDoOutput(true);
		    OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		    
		    String rx=new BASE64Encoder().encode(request.getBytes("utf-8"));
		    out.write("request="+rx);
		    out.close();
		    byte[] in=new BASE64Decoder().decodeBuffer(conn.getInputStream());
	    	return new String(in,"utf-8");
		    
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
	    return output;
	}
}
