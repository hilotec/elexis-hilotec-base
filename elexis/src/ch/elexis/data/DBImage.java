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
 *    $Id: DBImage.java 3786 2008-04-19 09:57:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

import ch.elexis.Desk;

public class DBImage extends PersistentObject {
	public static final String DBVERSION="1.0.0";
	public static final String TABLENAME="DBIMAGE";
		
	static{
		addMapping(
			TABLENAME, "Datum=S:D:Datum","Titel=Title","Bild"
		);
	}
	@Override
	public String getLabel() {
		return get("Datum")+" - "+get("Titel");
	}

	public String getName(){
		return get("Titel");
	}
	public DBImage(String name, InputStream source){
		ImageLoader iml=new ImageLoader();
		iml.load(source);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		iml.save(baos, SWT.IMAGE_PNG);
		create(null);
		set("Titel",name);
		setBinary("Bild", baos.toByteArray());
	}

	public Image getImage(){
		byte[] in=getBinary("Bild");
		ByteArrayInputStream bais=new ByteArrayInputStream(in);
		Image ret=new Image(Desk.theDisplay,bais);
		return ret;
	}
	
	public static DBImage load(String id){
		DBImage ret= new DBImage(id);
		if(!ret.exists()){
			return null;
		}
		return ret;
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	
	
	protected DBImage(String id){
		super(id);
	}
	private DBImage(){}

}
