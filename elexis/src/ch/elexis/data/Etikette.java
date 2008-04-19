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
 *    $Id: Etikette.java 3790 2008-04-19 17:14:49Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import ch.elexis.Desk;


/**
 * Eine Markierung für im Prinzip beliebige Objekte. Ein Objekt, das eine
 * Etikette hat, kann diese Etikette zur Darstellung verwenden
 * @author gerry
 *
 */
public class Etikette extends PersistentObject{
	static final String TABLENAME="ETIKETTEN";
	static final String LINKTABLE="ETIKETTEN_OBJECT_LINK";
		
	static{
		addMapping(
			TABLENAME, "Datum=S:D:Datum","BildID=Image","vg=foreground","bg=background","Name"
			
		);
	}
	
	public Image getImage(){
		DBImage image=DBImage.load("BildID");
		if(image!=null){
			Image ret=Desk.theImageRegistry.get(image.getName());
			if(ret==null){
				Desk.theImageRegistry.put(image.getName(), image.getImage());
			}
			return ret;
		}
		return null;
	}
	
	public Color getForeground(){
		String vg=get("vg");
		return Desk.getColorFromRGB(vg);

	}
	
	public void register(){
		Desk.theImageRegistry.put(get("Name"), new DBImageDescriptor(get("Name")));
	}
	
	public Color getBackground(){
		String bg=get("bg");
		return Desk.getColorFromRGB(bg);
	}
	@Override
	public String getLabel() {
		return get("Name");
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	 
	public static Etikette load(String id){
		Etikette ret=new Etikette(id);
		if(!ret.exists()){
			return null;
		}
		return ret;
	}
	protected Etikette(String id){
		super(id);
	}
}
