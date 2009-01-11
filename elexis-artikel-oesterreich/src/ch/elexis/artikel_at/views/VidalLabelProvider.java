/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: VidalLabelProvider.java 4930 2009-01-11 17:33:49Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_at.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.data.Artikel;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.Log;

public class VidalLabelProvider extends DefaultLabelProvider implements ITableColorProvider {
	public VidalLabelProvider(){
		if (Desk.getImage("VidalRed") == null) {
			ImageDescriptor imd = getImageDescriptor("rsc/redbox.ico");
			if (imd == null) {
				Hub.log.log("No red icon found", Log.ERRORS);
			}
			Desk.getImageRegistry().put("VidalRed", getImageDescriptor("rsc/redbox.ico"));
		}
		if (Desk.getImage("VidalGreen") == null) {
			Desk.getImageRegistry().put("VidalGreen", getImageDescriptor("rsc/greenbox.ico"));
		}
		if (Desk.getImage("VidalYellow") == null) {
			Desk.getImageRegistry().put("VidalYellow", getImageDescriptor("rsc/yellowbox.ico"));
		}
	}
	
	public static ImageDescriptor getImageDescriptor(String path){
		return AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.artikel_at", path); //$NON-NLS-1$
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		if (element instanceof Medikament) {
			String box = ((Medikament) element).get("Codeclass");
			// Hub.log.log("Box: *"+box+"*", Log.INFOS);
			if (box != null) {
				if (box.startsWith("R")) {
					// Hub.log.log("Red", Log.INFOS);
					Image img = Desk.getImage("VidalRed");
					if (img == null) {
						Hub.log.log("Image is null", Log.ERRORS);
					}
					// Hub.log.log(img.toString(), Log.INFOS);
				} else if (box.startsWith("G")) {
					return Desk.getImage("VidalGreen");
				} else if (box.startsWith("Y")) {
					return Desk.getImage("VidalYellow");
				}
			} else {
				Hub.log.log("Box is Null!", Log.ERRORS);
			}
		} else {
			return Desk.getImage(Desk.IMG_ACHTUNG);
		}
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex){
		if (element instanceof Medikament) {
			Medikament art = (Medikament) element;
			StringBuilder ret = new StringBuilder();
			ret.append(art.getLabel());
			
			ret.append("/").append(art.getRemb());
			
			if (art.isLagerartikel()) {
				ret.append("(").append(Integer.toString(art.getTotalCount())).append(")");
			}
			return ret.toString();
		}
		return super.getColumnText(element, columnIndex);
	}
	
	public Color getBackground(Object element, int columnIndex){
		// TODO Auto-generated method stub
		return null;
	}
	
	public Color getForeground(Object element, int columnIndex){
		if (element instanceof Artikel) {
			if (((Artikel) element).isLagerartikel()) {
				return Desk.getColor(Desk.COL_BLUE);
			}
		}
		return null;
	}
	
}
