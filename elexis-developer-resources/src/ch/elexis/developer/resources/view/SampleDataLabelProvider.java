/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id: SampleDataLabelProvider.java 6108 2010-02-11 18:26:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.developer.resources.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.developer.resources.model.SampleDataType;

/**
 * A LabelProvider renders visual Representations of Objects held in a Viewer
 * 
 * @author gerry
 * 
 */
public class SampleDataLabelProvider extends LabelProvider {

	/**
	 * We can provide any image here, maybe depending on context. Or we can
	 * return null, then no image will be shown. (We could leave away this
	 * method, then)
	 */
	@Override
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * A textual Representation of the object
	 */
	@Override
	public String getText(Object element) {
		SampleDataType sdt = (SampleDataType) element; // it will always be of
														// this type
		return sdt.getLabel(); // We keep things simple here
	}

}
