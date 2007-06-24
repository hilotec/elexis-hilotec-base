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
 *  $Id: Transporter.java 2621 2007-06-24 11:05:57Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.Desk;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.exchange.XChangeExporter;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.util.Result;
import ch.rgw.tools.BinConverter;
import ch.rgw.tools.ExHandler;
import ch.sgam.informatics.exchange.ui.GPGExportDialog;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * A Transporter is a vehicle, that is able to create and maintain an eXChange -document.
 * Therafter, it can encrypt, save and/or transfer the document.
 * @author gerry
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class Transporter extends XChangeExporter{
	
	public boolean doExport() {
		Element eDoc=eRoot.getChild("document",ns); //$NON-NLS-1$
		String id=eDoc.getAttributeValue("id"); //$NON-NLS-1$
		Format format=Format.getPrettyFormat();
		format.setEncoding("utf-8"); //$NON-NLS-1$
		XMLOutputter xmlo=new XMLOutputter(format);
		String xmlAspect=xmlo.outputString(doc);
	
		try {
			File temp=File.createTempFile("xchange", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(temp));
			ZipEntry entry=new ZipEntry(id+".xml"); //$NON-NLS-1$
			byte[] data=xmlAspect.getBytes("utf-8"); //$NON-NLS-1$
			entry.setSize(data.length);
			zos.putNextEntry(entry);
			zos.write(data);
			zos.closeEntry();
			Iterator<Entry<String, byte[]>> it=getBinaries();
			while(it.hasNext()){
				Entry<String,byte[]> binEntry=it.next();
				byte[] val=binEntry.getValue();
				if(val!=null){
					zos.putNextEntry(new ZipEntry(binEntry.getKey()));
					zos.write(binEntry.getValue());
					zos.closeEntry();
				}
			}
			zos.close();
			new GPGExportDialog(Desk.theDisplay.getActiveShell(),temp,id).open();
			return true;
		} catch (IOException e) {
			ExHandler.handle(e);
			return false;
		}
		
	}
	
	
	/**
	 * A possible generator for the various UID's that are used in xChange
	 */
	public static long sequence=Math.round(Math.random()*Long.MAX_VALUE);
	public static String createUID(String provider){
		try{
			MessageDigest sha=MessageDigest.getInstance("SHA"); //$NON-NLS-1$
			sha.update(provider.getBytes());
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements())
            {   NetworkInterface  ni  = (NetworkInterface)nis.nextElement();
                Enumeration       ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {   InetAddress  ia  = (InetAddress)ias.nextElement();
                    sha.update(ia.getHostName().getBytes());
                }
            }
			byte[] ti=new byte[8];
			BinConverter.longToByteArray(new Date().getTime(),ti,0);
			sha.update(ti);
			BinConverter.longToByteArray(sequence++, ti, 0);
			sha.update(ti);
			return new String(Base64.encode(sha.digest()));
		}catch(Exception ex){
			ExHandler.handle(ex);
			return null;
		}
	}
	public boolean finalizeExport() {
		return doExport();
	}
	public Result<Element> store(Object output) {
		if(output instanceof Patient){
			ContactElement ret=addContact((Kontakt)output,true);
			return new Result<Element>(ret.getElement());
		}
		return null;
	}
	
}
