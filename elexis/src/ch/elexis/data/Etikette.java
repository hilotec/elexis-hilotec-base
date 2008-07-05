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
 *    $Id: Etikette.java 4096 2008-07-05 05:09:31Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Desk;
import ch.rgw.tools.JdbcLink.Stm;


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
			fg=Desk.getColor(Desk.COL_BLACK);
		}
		if(bg==null){
			bg=Desk.getColor(Desk.COL_WHITE);
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
		GridData gd1=null;
		GridData gd2=null;;
		Composite cImg=new Composite(ret,SWT.NONE);
		if(img!=null){
			cImg.setBackgroundImage(img);
			gd1=new GridData(img.getBounds().width,img.getBounds().height);
			gd2=new GridData(SWT.DEFAULT,img.getBounds().height);
		}else{
			gd1=new GridData(10,10);
			gd2=new GridData(SWT.DEFAULT,SWT.DEFAULT);
		}
		cImg.setLayoutData(gd1);
		Label lbl=new Label(ret,SWT.NONE);
		lbl.setLayoutData(gd2);
		lbl.setText(getLabel());
		lbl.setForeground(getForeground());
		lbl.setBackground(getBackground());
		return ret;
	}
	public Image getImage(){
		DBImage image=DBImage.load(get("BildID"));
		if(image!=null){
			Image ret=Desk.getImage(image.getName());
			if(ret==null){
				ret=image.getImage();
				Desk.getImageRegistry().put(image.getName(), ret);
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
		Desk.getImageRegistry().put(get("Name"), new DBImageDescriptor(get("Name")));
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
	
	@Override
	public boolean delete() {
		StringBuilder sb=new StringBuilder();
    	Stm stm=getConnection().getStatement();
		
    	sb.append("DELETE FROM ")
    		.append(Etikette.LINKTABLE).append(" WHERE ")
    		.append("etikette = '").append(getId()).append("'");
    	stm.exec(sb.toString());
    	getConnection().releaseStatement(stm);
		return super.delete();
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
		if(o != null){
			return o.getWert()-getWert();
		}
		return 1;
	}
}
