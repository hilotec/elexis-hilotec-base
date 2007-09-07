/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: BestellBlatt.java 3108 2007-09-07 11:03:34Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Bestellung.Item;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;

public class BestellBlatt extends ViewPart implements ICallback{
	public final static String ID="ch.elexis.BestellBlatt";
	TextContainer text;
	Brief actBest;
	private final static String TEMPLATENAME="Bestellung";

	@Override
	public void createPartControl(final Composite parent) {
		text=new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent,this);
	}

	public void createOrder(final Kontakt adressat, final List<Item> items){
		String[][] tbl=new String[items.size()+2][];
		int i=1;
		Money sum=new Money();
		tbl[0]=new String[]{"Anzahl","Pharmacode","Name","Einzelpreis","Zeilenpreis"};
		DecimalFormat df=new DecimalFormat("\u00a4\u00a4  #.00");
		for(Item it:items){
			String[] row=new String[5];
			row[0]=Integer.toString(it.num);
			row[1]=it.art.getPharmaCode();
			row[2]=it.art.getName();
			row[3]=it.art.getEKPreis().getAmountAsString(); //Integer.toString(it.art.getEKPreis());
			//int amount=it.num*it.art.getEKPreis();
			Money amount=it.art.getEKPreis().multiply(it.num);
			row[4]=amount.getAmountAsString();
			sum.addMoney(amount);
			tbl[i++]=row;
		}
		tbl[i]=new String[]{"Summe","","","",sum.getAmountAsString()};
		actBest=text.createFromTemplateName(null,TEMPLATENAME,Brief.BESTELLUNG,adressat, null);
		if(actBest==null){
			SWTHelper.showError("Konnte Bestellung nicht erstellen", "Druckvorlage '"+TEMPLATENAME+"' konnte nicht geladen werden");
		}else{
			actBest.setPatient(Hub.actUser);
			text.getPlugin().insertTable("["+TEMPLATENAME+"]",ITextPlugin.FIRST_ROW_IS_HEADER|ITextPlugin.GRID_VISIBLE,tbl, null);
		}
	}
	@Override
	public void setFocus() {
		// TODO Automatisch erstellter Methoden-Stub

	}

	public void save() {
		if(actBest!=null){
			actBest.save(text.getPlugin().storeToByteArray(),text.getPlugin().getMimeType());
		}
	}

	public boolean saveAs() {
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}

}
