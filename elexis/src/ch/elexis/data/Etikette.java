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
 *    $Id: Etikette.java 3823 2008-04-20 16:43:50Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Desk;


/**
 * Eine Markierung für im Prinzip beliebige Objekte. Ein Objekt, das eine
 * Etikette hat, kann diese Etikette zur Darstellung verwenden
 * @author gerry
 *
 */
public class Etikette extends PersistentObject implements Comparable<Etikette>{
	static final String TABLENAME="ETIKETTEN";
	static final String LINKTABLE="ETIKETTEN_OBJECT_LINK";
		
	static{
		addMapping(
			TABLENAME, "Datum=S:D:Datum","BildID=Image","vg=foreground","bg=background","Name",
				"wert=importance"
			
		);
	}
	
	public Etikette(String name, Color fg, Color bg){
		create(null);
		if(fg==null){
			fg=Desk.theColorRegistry.get(Desk.COL_BLACK);
		}
		if(bg==null){
			bg=Desk.theColorRegistry.get(Desk.COL_GREY20);
		}
		set(new String[]{"Name","vg", "bg"}, new String[]{
				name,
				Desk.createColor(fg.getRGB()),
				Desk.createColor(bg.getRGB())
		});
	}
	public Composite createForm(Composite parent){
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(2,false));
		Image img=getImage();
		Composite cImg=new Composite(ret,SWT.BORDER);
		cImg.setBackgroundImage(img);
		GridData gd1=new GridData(img.getBounds().width,img.getBounds().height);
		cImg.setLayoutData(gd1);
		Label lbl=new Label(ret,SWT.NONE);
		GridData gd2=new GridData(Integer.MAX_VALUE,img.getBounds().height);
		lbl.setLayoutData(gd2);
		lbl.setText(getLabel());
		lbl.setForeground(getForeground());
		lbl.setBackground(getBackground());
		return ret;
	}
	public Image getImage(){
		DBImage image=DBImage.load(get("BildID"));
		if(image!=null){
			Image ret=Desk.theImageRegistry.get(image.getName());
			if(ret==null){
				ret=image.getImage();
				Desk.theImageRegistry.put(image.getName(), ret);
			}
			return ret;
		}
		return null;
	}
	
	public void setImage(DBImage image){
		set("BildID",image.getId());
	}
	
	public void setForeground( String fg){
		set("vg",fg);
	}
	public void setForeground(Color fg){
		if(fg!=null){
			set("vg",Desk.createColor(fg.getRGB()));
		}
	}
	public Color getForeground(){
		String vg=get("vg");
		return Desk.getColorFromRGB(vg);

	}
	
	public void setBackground(String bg){
		set("bg",bg);
	}
	public void setBackground(Color bg){
		if(bg!=null){
			set("bg",Desk.createColor(bg.getRGB()));
		}
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

	public int getWert(){
		return checkZero(get("wert"));
	}
	public void setWert(int w){
		set("wert",Integer.toString(w));
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
	protected Etikette(){}
	public int compareTo(Etikette o) {
		return o.getWert()-getWert();
	}
}
