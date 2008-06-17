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
 *    $Id: Etiketten.java 4046 2008-06-17 11:47:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Desk;
import ch.elexis.data.Etikette;
import ch.elexis.data.Query;
import ch.elexis.dialogs.ImageChooser;
import ch.elexis.util.SWTHelper;

public class  Etiketten extends PreferencePage implements
		IWorkbenchPreferencePage {

	Combo combo;
	Canvas cImage,cFore,cBack;
	Etikette act;
	List<Etikette> lEtiketten;
	Button bNew, bRemove;
	Spinner spWert;
	
	void setEtikette(Etikette et){
		act=et;
		if(et==null){
			cImage.setBackground(Desk.getColor(Desk.COL_WHITE));
			cFore.setBackground(Desk.getColor(Desk.COL_BLACK));
			cBack.setBackground(Desk.getColor(Desk.COL_LIGHTGREY));
			spWert.setSelection(0);
		}else{
			cFore.setBackground(et.getForeground());
			cBack.setBackground(et.getBackground());
			spWert.setSelection(act.getWert());
		}
		cImage.redraw();
		cFore.redraw();
		cBack.redraw();

	}
	@Override
	protected Control createContents(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		combo=new Combo(ret,SWT.SIMPLE);
		combo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		combo.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i=combo.getSelectionIndex();
				if(i>-1){
					setEtikette(lEtiketten.get(i));
					bRemove.setEnabled(true);
				}
			}
			
		});
		combo.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				if(combo.getText().length()==0){
					bNew.setEnabled(false);
				}else{
					bNew.setEnabled(true);
				}
				
			}});
		for(Etikette et:lEtiketten){
			combo.add(et.getLabel());
		}
		//new Label(ret,SWT.NONE).setText("Anzeige");
		Composite bottom=new Composite(ret,SWT.NONE);
		bottom.setLayout(new GridLayout(2,false));
		bNew=new Button(bottom,SWT.PUSH);
		bNew.setText("Neue Etikette");
		bNew.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				String name=combo.getText();
				Etikette n=new Etikette(name,null,null);
				lEtiketten.add(n);
				combo.add(n.getLabel());
			}
			
		});
		bRemove=new Button(bottom,SWT.PUSH);
		bRemove.setText("Etikette löschen");
		bRemove.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx=combo.getSelectionIndex();
				if(idx>-1){
					//String n=combo.getItem(idx);
					combo.remove(idx);
					Etikette eti=lEtiketten.get(idx);
					lEtiketten.remove(idx);
					eti.delete();
				}
			}
		});
		new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cImage=new Canvas(bottom,SWT.BORDER);
		cImage.addPaintListener(new PaintListener(){

			public void paintControl(PaintEvent e) {
				GC gc=e.gc;
				if(act!=null){
					Image img=act.getImage();
					if(img!=null){
						gc.drawImage(img, 0, 0);
						return;
					}
				}
				gc.setForeground(Desk.getColor(Desk.COL_GREY20));
				gc.fillRectangle(0, 0, 32, 32);
			}});
		GridData gdImage=new GridData(32,32);
		cImage.setLayoutData(gdImage);
		/*
		Composite cImage=new Composite(bottom,SWT.NONE);
		cImage.setLayout(new FillLayout());
		Button bExistingImage=new Button(cImage, SWT.PUSH);
		bExistingImage.setText("Bild aus Archiv...");
		bExistingImage.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				ImageChooser imc=new ImageChooser(getShell());
				if(imc.open()==Dialog.OK){
					act.setImage(imc.getSelection());
					setEtikette(act);
				}
			}
			
		});
		*/
		Button bNewImage=new Button(bottom,SWT.PUSH);
		bNewImage.setText("Bild...");
		bNewImage.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(act!=null){
					ImageChooser imc=new ImageChooser(getShell());
					if(imc.open()==Dialog.OK){
						act.setImage(imc.getSelection());
						setEtikette(act);
					}	
				}
			}
		});
		cFore=new Canvas(bottom,SWT.BORDER);
		GridData gdFore=new GridData(32,16);
		cFore.setLayoutData(gdFore);
		Button bFore=new Button(bottom,SWT.PUSH);
		bFore.setText("Textfarbe");
		bFore.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				if(act!=null){
					ColorDialog cd=new ColorDialog(getShell(),SWT.NONE);
					RGB rgb=cd.open();
					if(rgb!=null){
						act.setForeground(Desk.createColor(rgb));
					}
					setEtikette(act);
				}
			}
			
		});
		cBack=new Canvas(bottom,SWT.BORDER);
		GridData gdBack=GridDataFactory.copyData(gdFore);
		cBack.setLayoutData(gdBack);
		Button bBack=new Button(bottom,SWT.PUSH);
		bBack.setText("Hintergrund");
		bBack.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				if(act!=null){
					ColorDialog cd=new ColorDialog(getShell(),SWT.NONE);
					RGB rgb=cd.open();
					if(rgb!=null){
						act.setBackground(Desk.createColor(rgb));
					}
					setEtikette(act);
				}
			}
			
		});
		spWert=new Spinner(bottom,SWT.NONE);
		spWert.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				if(act!=null){
					act.setWert(spWert.getSelection());
				}
				
			}});
		new Label(bottom,SWT.NONE).setText("'Wert' des Etiketts");
		bNew.setEnabled(false);
		bRemove.setEnabled(false);
		return ret;
	}

	public void init(IWorkbench workbench) {
		Query<Etikette> qbe=new Query<Etikette>(Etikette.class);
		lEtiketten=qbe.execute();
		if(lEtiketten!=null){
			
		}else{
			lEtiketten=new LinkedList<Etikette>();
		}
		
	}

	
}
