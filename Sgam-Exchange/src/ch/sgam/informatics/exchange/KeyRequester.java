/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Sgam.informatics
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Transporter.java 1143 2006-10-21 19:06:51Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange;

import javax.mail.Message;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

import ch.elexis.Desk;
import ch.elexis.mail.Mailer;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;

import com.valhalla.misc.GnuPG;

/**
 * A Class that handles the request for a public and the answer to that request
 * @author gerry
 *
 */
public class KeyRequester {
	String keyname;
	
	public KeyRequester(String myKey){
		keyname=myKey;
	}
	
	public Result<String> sendKey(String address, String subject, String message){
		GnuPG gpg=new GnuPG();
		if((SWTHelper.blameEmptyString(address, "Empfängeradresse")==false) ||
				(SWTHelper.blameEmptyString(subject, "Nachrichtentitel")==false) ||
				(SWTHelper.blameEmptyString(message, "Nachrichteninhalt")==false) ||
				(SWTHelper.blameEmptyString(keyname, "Absenderschlüssel (Einstellungen)")==false)){
			return new Result<String>(Log.ERRORS,2,"Falscher Parameter","",true);
		}
		if(gpg.getKey(keyname)){
			String key=gpg.getResult();
			Mailer mailer=new Mailer();
			Message msg=mailer.createMultipartMessage(subject, keyname);
			mailer.addTextPart(msg, message);
			mailer.addTextPart(msg, key, "key.gpg"); //$NON-NLS-1$
			Result<String> res=mailer.send(msg, address);
			res.add(0, 0, "ok", gpg.getErrorString(), false); //$NON-NLS-1$
			return res;
		}
		return new Result<String>(Log.ERRORS,1,gpg.getErrorString(),"",true); //$NON-NLS-1$
	}
	
	public Result<String> importKeyFromClipboard(){
		GnuPG gpg=new GnuPG();
		Clipboard clip=new Clipboard(Desk.theDisplay);
		String cont=(String)clip.getContents(TextTransfer.getInstance());
		if(gpg.importKeyFromClipboard(cont)){
			return new Result<String>("OK");
		}
		return new Result<String>(Log.ERRORS,2,gpg.getErrorString(),"",true); //$NON-NLS-1$
	}
	public Result<String> importKey(String filename){
		if(SWTHelper.blameEmptyString(filename, "Dateiname")==false){
			return new Result<String>(Log.ERRORS,3,"Kein Dateiname","",true);
		}
		GnuPG gpg=new GnuPG();
		if(gpg.importKey(filename)){
			return new Result<String>("OK"); //$NON-NLS-1$
		}
		return new Result<String>(Log.ERRORS,2,gpg.getErrorString(),"",true); //$NON-NLS-1$
	}
}
