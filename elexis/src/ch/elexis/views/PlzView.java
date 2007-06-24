/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PlzView.java 770 2006-08-21 16:56:45Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LazyContentProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;

public class PlzView extends ViewPart implements AbstractDataLoaderJob.FilterProvider{
	public static final String ID="ch.elexis.Plz";
	String[] fields={"Plz","Ort"};
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private AbstractDataLoaderJob dataloader=(AbstractDataLoaderJob)Hub.jobPool.getJob("Plz");
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		cv=new CommonViewer();
		dataloader.addFilterProvider(this);
		 vc=new ViewerConfigurer(
	         		//new ViewerConfigurer.DefaultContentProvider(cv, Anschrift.class),
	         		new LazyContentProvider(cv,dataloader, null),
	         		new DefaultLabelProvider(),
	         		new DefaultControlFieldProvider(cv, fields),
	         		new ViewerConfigurer.DefaultButtonProvider(null,null),
	         		new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,cv)
	         );
      cv.create(vc,parent,SWT.NONE,getViewSite());
      //vc.getControlFieldProvider().addChangeListener(this);
      cv.getViewerWidget().addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
      ((LazyContentProvider)vc.getContentProvider()).startListening();
	}

	@Override
	public void dispose() {
		((LazyContentProvider)vc.getContentProvider()).stopListening();
		dataloader.removeFilterProvider(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/*
	public void changed(String[] fields, String[] values) {
		JobPool.getJobPool().activate(dataloader.getJobname(),Job.INTERACTIVE);
	}
*/
	public void applyFilter() {
		Query qbe=dataloader.getQuery();
		vc.getControlFieldProvider().setQuery(qbe);
	}

 /*
   public void reorder(String field)
    {
        // TODO Auto-generated method stub
        
    }
*/
}
