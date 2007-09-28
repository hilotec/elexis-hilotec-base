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
 * $Id: BAGMediLabelProvider.java 3229 2007-09-28 16:41:20Z rgw_ch $
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
import ch.rgw.tools.StringTool;

public class BAGMediLabelProvider extends DefaultLabelProvider implements
		ITableColorProvider {

	
	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if(element instanceof BAGMedi){
			BAGMedi bm=(BAGMedi)element;
			StringBuilder sb=new StringBuilder();
			sb.append(bm.getLabel())
				.append(" <").append(bm.getVKPreis().getAmountAsString()).append(">");
			
			List<Substance> conts=bm.getSubstances();
			if(conts.size()>0){
				sb.append("[");
				for(Substance s:conts){
					sb.append(s.getLabel()).append("; ");
				}
				sb.append("]");
			}
			if(bm.isLagerartikel()){
				sb.append(" (").append(bm.getTotalCount()).append(")");
			}

			return sb.toString();
		}
		return super.getColumnText(element, columnIndex);
	}

	public Color getBackground(final Object element, final int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getForeground(final Object element, final int columnIndex) {
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
	public Image getColumnImage(final Object element, final int columnIndex) {
		if(element instanceof BAGMedi){
			BAGMedi bm=(BAGMedi) element;
			String g=StringTool.unNull(bm.get("Generikum"));
			if(g.startsWith("G")){
				return Desk.theImageRegistry.get(BAGMedi.IMG_GENERIKUM);
			}else if(g.startsWith("O")){
				return Desk.theImageRegistry.get(BAGMedi.IMG_HAS_GENERIKA);
			}else{
				return Desk.theImageRegistry.get(BAGMedi.IMG_ORIGINAL);
			}
		}
		
		return super.getColumnImage(element, columnIndex);
	}

	
}
