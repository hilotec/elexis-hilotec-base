/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: BAGMediLabelProvider.java 3112 2007-09-08 04:41:00Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import java.util.List;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Artikel;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.DefaultLabelProvider;

public class BAGMediLabelProvider extends DefaultLabelProvider implements
		ITableColorProvider {

	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof BAGMedi){
			BAGMedi bm=(BAGMedi)element;
			StringBuilder sb=new StringBuilder();
			sb.append(bm.getLabel()).append(" [");
			List<Substance> conts=bm.getSubstances();
			for(Substance s:conts){
				sb.append(s.getLabel()).append("; ");
			}
			sb.append("]");
			if(bm.isLagerartikel()){
				sb.append(" (").append(bm.getTotalCount()).append(")");
			}

			return sb.toString();
		}
		return super.getColumnText(element, columnIndex);
	}

	public Color getBackground(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
    	if (element instanceof Artikel) {
    		Artikel art = (Artikel) element;
    		
    		if (art.isLagerartikel()) {
    			int trigger = Hub.globalCfg.get(PreferenceConstants.INVENTORY_ORDER_TRIGGER, PreferenceConstants.INVENTORY_ORDER_TRIGGER_DEFAULT);

    			int ist = art.getIstbestand();
    			int min = art.getMinbestand();

    			boolean order = false;
    			switch (trigger) {
    			case PreferenceConstants.INVENTORY_ORDER_TRIGGER_BELOW:
    				order = (ist < min);
    				break;
    			case PreferenceConstants.INVENTORY_ORDER_TRIGGER_EQUAL:
    				order = (ist <= min);
    				break;
    			default:
    				order = (ist < min);
    			}

    			if (order) {
    				return Desk.theColorRegistry.get(Desk.COL_RED);
    			} else {
    				return Desk.theColorRegistry.get(Desk.COL_BLUE);
    			}
    		}
    	}
    	
    	return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if(element instanceof BAGMedi){
			BAGMedi bm=(BAGMedi) element;
			String g=bm.getExt("Generika");
			if(g.equals("G")){
				return Desk.theImageRegistry.get(BAGMedi.IMG_GENERIKUM);
			}else if(g.equals("O")){
				return Desk.theImageRegistry.get(BAGMedi.IMG_HAS_GENERIKA);
			}else{
				return Desk.theImageRegistry.get(BAGMedi.IMG_ORIGINAL);
			}
		}
		
		return super.getColumnImage(element, columnIndex);
	}

	
}
