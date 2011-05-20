/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and medelexis AG
 * All rights reserved.
 * $Id: Labor2009Selector.java 135 2009-06-15 20:16:21Z  $
 *******************************************************************************/

package ch.elexis.labortarif2009.ui;

import org.eclipse.swt.SWT;

import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.PersistentObjectLoader;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.labortarif2009.data.Labor2009Tarif;
import ch.elexis.selectors.FieldDescriptor;
import ch.elexis.selectors.FieldDescriptor.Typ;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.SelectorPanelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class Labor2009Selector extends CodeSelectorFactory {
	PersistentObjectLoader fdl;
	CommonViewer cv;
	Query<Labor2009Tarif> qbe = new Query<Labor2009Tarif>(Labor2009Tarif.class);
	SelectorPanelProvider slp;
	FieldDescriptor<?>[] fields =
		{
			new FieldDescriptor<Labor2009Tarif>(Messages.Labor2009Selector_code,
				Labor2009Tarif.FLD_CODE, Typ.STRING, null),
			new FieldDescriptor<Labor2009Tarif>(Messages.Labor2009Selector_text,
				Labor2009Tarif.FLD_NAME, Typ.STRING, null)
		};
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		fdl = new FlatDataLoader(cv, qbe);
		slp = new SelectorPanelProvider(fields, true);
		this.cv = cv;
		return new ViewerConfigurer(fdl, new DefaultLabelProvider(), slp,
			new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
				SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
	}
	
	@Override
	public void dispose(){
		cv.dispose();
		fdl.dispose();
	}
	
	@Override
	public String getCodeSystemName(){
		return Labor2009Tarif.CODESYSTEM_NAME;
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Labor2009Tarif.class;
	}
	
}
