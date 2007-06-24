/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: LaborleistungCodeSelectorFactory.java 1625 2007-01-19 20:01:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;

import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.ListLoader;
import ch.elexis.data.LaborLeistung;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LazyContentProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class LaborleistungCodeSelectorFactory extends CodeSelectorFactory {
	private AbstractDataLoaderJob dataloader;
	private ViewerConfigurer vc;
	
	public LaborleistungCodeSelectorFactory() {
	dataloader=(AbstractDataLoaderJob)JobPool.getJobPool().getJob("Labortarif"); //$NON-NLS-1$
		
		if(dataloader==null){
			dataloader=new ListLoader<LaborLeistung>("Labortarif",new Query<LaborLeistung>(LaborLeistung.class),new String[]{"Code","Text"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			JobPool.getJobPool().addJob(dataloader);
		}	
		JobPool.getJobPool().activate("Labortarif",Job.SHORT); //$NON-NLS-1$
	}

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		vc=new ViewerConfigurer(
				new LazyContentProvider(cv,dataloader,null),
				new DefaultLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"Code","Text"}), //$NON-NLS-1$ //$NON-NLS-2$
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,null)
				);
		return vc;
	}

	@Override
	public Class getElementClass() {
		return LaborLeistung.class;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public String getCodeSystemName() {
		return "Analysetarif"; //$NON-NLS-1$
	}

}
