/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: SelectorField.java 5029 2009-01-24 16:34:46Z rgw_ch $
 *******************************************************************************/

package ch.elexis.selectors;

import java.util.LinkedList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;

public class SelectorField extends Composite {
	Label lbl;
	Text text;
	int len;
	
	private LinkedList<SelectorListener> listeners=new LinkedList<SelectorListener>();
	
	public SelectorField(Composite parent, String label){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		lbl=new Label(this,SWT.NONE);
		lbl.setText(label);
		//Font font=JFaceResources.getDefaultFont());
		lbl.setForeground(Desk.getColor(Desk.COL_BLUE));
		text=new Text(this,SWT.BORDER);
		lbl.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseUp(MouseEvent e){
				for(SelectorListener sl:listeners){
					sl.titleClicked(SelectorField.this);
				}
		
			}
			
			
		});
		lbl.setCursor(Desk.getCursor(Desk.CUR_HYPERLINK));
		text.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e){
				String fld=text.getText();
				int l2=fld.length();
				if((l2>2) || (len>2)){
				}
				len=l2;
				
			}});
		len=0;
	}
	
	public void addSelectorListener(SelectorListener listen){
		listeners.add(listen);
	}
	
	public void removeSelectorListener(SelectorListener listen){
		listeners.remove(listen);
	}

	public void clear(){
		text.setText("");
	}
	public String getText(){
		return text.getText();
	}
	
	public void setText(String txt){
		text.setText(txt);
	}
	String getLabel(){
		return lbl.getText();
	}
}
