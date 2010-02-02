/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id: DBImage.java 6051 2010-02-02 17:25:48Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

import ch.elexis.Desk;
import ch.elexis.StringConstants;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

/**
 * A DBImage is an Image stored in the database and retrievable by its name
 * @author gerry
 *
 */
public class DBImage extends PersistentObject {
	public static final String FLD_PREFIX = "Prefix";
	private static final String FLD_TITLE = "Titel";
	public static final String DATE = "Datum";
	public static final String FLD_IMAGE = "Bild";
	public static final String DBVERSION="1.0.0";
	public static final String TABLENAME="DBIMAGE";
	
	static{
		addMapping(
			TABLENAME, DATE_FIELD,FLD_PREFIX,"Titel=Title",FLD_IMAGE
		);
	}
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		synchronized (sb){
			sb.append(get(DATE)).append(" - ").append(get(FLD_TITLE))
			.append(StringConstants.OPENBRACKET).append(get(FLD_PREFIX))
			.append(StringConstants.CLOSEBRACKET);
			return sb.toString();
		}
	}
	
	public String getName(){
		return get(FLD_TITLE);
	}
	public DBImage(String prefix, String name, InputStream source){
		ImageLoader iml=new ImageLoader();
		try{
			iml.load(source);
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			iml.save(baos, SWT.IMAGE_PNG);
			create(null);
			set(FLD_TITLE,name);
			setBinary(FLD_IMAGE, baos.toByteArray());
		}catch(Exception ex){
			SWTHelper.showError("Image error", "Bild ungültig","Das Bild konnte nicht geladen werden "+ex.getMessage());
			ExHandler.handle(ex);
		}
	}
	
	public Image getImage(){
		byte[] in=getBinary(FLD_IMAGE);
		ByteArrayInputStream bais=new ByteArrayInputStream(in);
		try{
			Image ret=new Image(Desk.getDisplay(),bais);
			return ret;
		}catch(Exception ex){
			SWTHelper.showError("Image Error", "Ungültiges Bild", "Das Bild ist ungültig "+ex.getMessage());
			return null;
		}
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
	protected DBImage(){}
	
}
