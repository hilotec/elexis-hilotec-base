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

package ch.elexis.exchange;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.exchange.SoapConverter;
import ch.elexis.util.Result;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.GnuPG;

/**
 * Secure Authenticated Transmission
 * The Transmitter stores a hashtable in an encrypted and signed wrapper.
 * For best compatibility we use SOAP messages encrypted with GnuPG
 * @author Gerry
 *
 */
public class SAT {
	static final Pattern signature=Pattern.compile(".+pgp: good signature from:(.+)");
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
			Matcher matcher=signature.matcher(msg);
			String user=null;
			if(matcher.matches()){
				user=matcher.group(1);
			}
			try{
				SoapConverter sc=new SoapConverter();
				sc.load(dec);
				HashMap<String, Object> hash=sc.getParameters();
				Long ts=(Long)hash.get("ADM_timestamp");
				if(ts==null || ((System.currentTimeMillis()-ts)>300000)){
					return new Result<HashMap<String, Object>>(Level.WARNING,3,"Timeout",null,false);
				}
				if(user==null){
					hash.remove("ADM_user");
				}else{
					hash.put("ADM_user", user);
				}
				return new Result<HashMap<String, Object>>(hash);
			}catch(Exception ex){
				ExHandler.handle(ex);
				return new Result<HashMap<String, Object>>(Level.WARNING,2,"Deserialize error "+ex.getMessage(),null,true);
			}
		}
		return new Result<HashMap<String, Object>>(Level.WARNING.intValue(),1,"Decode error",null,true);
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
			hash.put("ADM_timestamp", System.currentTimeMillis());
			SoapConverter sc=new SoapConverter();
			sc.create();
			sc.addHashMap("params", hash);
			if(gpg.signAndEncrypt(sc.toXML(), userKey, dest, new String(senderPwd))){
				return gpg.getResult();
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return null;
	}
}
