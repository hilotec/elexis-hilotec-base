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
 *  $Id: Importer.java 2621 2007-06-24 11:05:57Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Hub;
import ch.elexis.exchange.XChangeImporter;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

import com.valhalla.misc.GnuPG;

/**
 * This class implements an Importer for Sgam.xChange-Files. These are GPG-encrypted
 * zip-Files. The importer decrypts them and feeds them to the elexis-exchange system.
 * @author Gerry
 *
 */
public class Importer extends ImporterPage {
	Text passwd;
	String decodepwd;
	
	public Importer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new FileBasedImporter(ret,this).setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.getString("Importer.pleaseEnterPassword")); //$NON-NLS-1$
		passwd=new Text(ret,SWT.PASSWORD|SWT.BORDER);
		passwd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}

	@Override
	public void collect() {
		decodepwd=passwd.getText();
		super.collect();
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		File file=new File(results[0]);
		if(file.exists()){
			GnuPG gpg=new GnuPG();
			long l=file.length();
			if(l>Integer.MAX_VALUE){
				l=Integer.MAX_VALUE;
			}
			
			String inter=Hub.localCfg.get(PreferenceConstants.ABL_BASEPATH, "")+File.separator+StringTool.unique("zip")+".zip"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(gpg.decrypt(file, inter, decodepwd)){
				XChangeImporter xChange=new XChangeImporter();
				ZipInputStream zis=new ZipInputStream(new FileInputStream(inter));
				ZipEntry zip;
				while((zip=zis.getNextEntry())!=null){
					String id=zip.getName();
					ByteArrayOutputStream baos=new ByteArrayOutputStream();
		            byte[] buffer=new byte[8192];
		            while(true){
		                int r=zis.read(buffer);
		                if(r==-1){
		                    break;
		                }
		                baos.write(buffer,0,r);
		            }
		            
		            baos.close();
		            byte [] cnt=baos.toByteArray();
		            if(id.endsWith(".xml")){ //$NON-NLS-1$
						xChange.load(new String(cnt,"utf-8")); //$NON-NLS-1$
					}else{
						xChange.addBinary(id, cnt);
					}
					if(!xChange.isValid()){ 
						zis.close();
						return new Status(Status.ERROR,"ch.sgam.informatics",1,Messages.getString("Importer.noValidXChange"),null); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
				}
				zis.close();
				xChange.doImport();
				return Status.OK_STATUS;
			}
			return new Status(Status.ERROR,"ch.sgam.informatics",1,Messages.getString("Importer.9")+file.getAbsolutePath()+Messages.getString("Importer.10")+gpg.getErrorString(),null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return new Status(Status.ERROR,Messages.getString("Importer.11"),1,Messages.getString("Importer.12")+file.getAbsolutePath()+Messages.getString("Importer.13"),null);		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getDescription() {
		return Messages.getString("Importer.import"); //$NON-NLS-1$
		
	}

	@Override
	public String getTitle() {
		return "xChange"; //$NON-NLS-1$
	}

}
