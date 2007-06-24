/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MoneyInput.java 2376 2007-05-15 16:35:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A class to display and let the user enter or change currency strings
 * @author gerry
 *
 */
public class MoneyInput extends Composite {
	Text text;
	List<SelectionListener> listeners=new LinkedList<SelectionListener>();
	
	public MoneyInput(Composite parent){
		super(parent,SWT.NONE);
		setLayout(new FillLayout());
		text=new Text(this,SWT.BORDER);
		prepare();
	}
	public MoneyInput(Composite parent, String label){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		new Label(this,SWT.NONE).setText(label);
		text=new Text(this,SWT.BORDER);
		prepare();
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	public MoneyInput(Composite parent, String label, Money money){
		this(parent,label);
		text.setText(money.getAmountAsString());
	}
	
	private void prepare(){
		text.addFocusListener(new FocusAdapter(){

			@Override
			public void focusLost(FocusEvent e) {
				try{
					String t=text.getText();
					if(t.length()==0){
						text.setText(new Money().getAmountAsString());
					}else{
						Money.checkInput(t);
					}
					for(SelectionListener lis:listeners){
						Event ev=new Event();
						ev.widget=e.widget;
						ev.display=e.display;
						lis.widgetSelected(new SelectionEvent(ev));
					}
				}catch(ParseException px){
					SWTHelper.alert("Ungültiger Betrag", "Der eingegebene Betrag kann nicht interpretiert werden");
				}
			}});
		/*
		text.addVerifyListener(new VerifyListener(){
			public void verifyText(VerifyEvent e) {
				if(e.character==SWT.DEL || e.character==SWT.BS){
					e.doit=true;
				}else{
					String t=text.getText()+e.character;
					if(t.length()<2 || t.matches("[0-9]+[\\.,]?[0-9]{0,2}")){
						e.doit=true;
					}else{
						e.doit=false;
					}
				}
			}});
			*/
		
	}
	public Money getMoney(){
		String t=text.getText();
		if(StringTool.isNothing(t)){
			return new Money();
		}
		try{
			return new Money(t);
		}catch(ParseException px){
			ExHandler.handle(px);
			return null;				// sollte nicht passieren
		}
	}
	
	public void setMoney(String m){
		text.setText(m);
	}
	public Text getControl(){
		return text;
	}
	public void addSelectionListener(SelectionListener lis){
		listeners.add(lis);
	}
	public void removeSelectionListener(SelectionListener lis){
		listeners.remove(lis);
	}
}
