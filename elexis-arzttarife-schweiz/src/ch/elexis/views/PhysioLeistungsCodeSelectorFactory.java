/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * $Id: PhysioLeistungsCodeSelectorFactory.java 5139 2009-02-16 21:10:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.swt.SWT;

import ch.elexis.actions.FlatDataLoader;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PhysioLeistung;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.FieldDescriptor;
import ch.elexis.util.viewers.SelectorPanelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class PhysioLeistungsCodeSelectorFactory extends CodeSelectorFactory {
	
	public PhysioLeistungsCodeSelectorFactory(){
	// TODO Auto-generated constructor stub
	}
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		FieldDescriptor<?>[] fd =
			new FieldDescriptor<?>[] {
				new FieldDescriptor<PhysioLeistung>("Ziffer", "Ziffer", null),
				new FieldDescriptor<PhysioLeistung>("Text", "Text", null),
			};
		FlatDataLoader fdl =
			new FlatDataLoader(cv, new Query<PhysioLeistung>(PhysioLeistung.class));
		SelectorPanelProvider slp = new SelectorPanelProvider(fd, true);
		ViewerConfigurer vc =
			new ViewerConfigurer(fdl, new DefaultLabelProvider(), slp,
				new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, cv));
		return vc;
		
	}
	
	@Override
	public void dispose(){
	// TODO Auto-generated method stub
	
	}
	
	@Override
	public String getCodeSystemName(){
		return "Physiotherapie";
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return PhysioLeistung.class;
	}

	@Override
	public PersistentObject findElement(String code){
		String id=new Query<PhysioLeistung>(PhysioLeistung.class).findSingle("Ziffer", "=", code);
		return PhysioLeistung.load(id);
	}
	
}
