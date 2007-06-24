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
 * $Id: TarmedCodeSelectorFactory.java 1625 2007-01-19 20:01:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;

import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.LazyTreeLoader;
import ch.elexis.data.Query;
import ch.elexis.data.TarmedLeistung;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.TreeContentProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class TarmedCodeSelectorFactory extends CodeSelectorFactory {
	private AbstractDataLoaderJob dataloader;
	
	public TarmedCodeSelectorFactory() {
		dataloader=(AbstractDataLoaderJob)JobPool.getJobPool().getJob("Tarmed"); //$NON-NLS-1$
		
		if(dataloader==null){
			dataloader=new LazyTreeLoader<TarmedLeistung>("Tarmed",new Query<TarmedLeistung>(TarmedLeistung.class),"Parent",new String[]{"ID","Text"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			JobPool.getJobPool().addJob(dataloader);
		}
		JobPool.getJobPool().activate("Tarmed",Job.SHORT); //$NON-NLS-1$
	}

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		ViewerConfigurer vc=new ViewerConfigurer(
				new TreeContentProvider(cv,dataloader),
				new ViewerConfigurer.TreeLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"Text"}), //$NON-NLS-1$
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE,null)
				);
		return vc;
	}

	@Override
	public Class getElementClass() {
		return TarmedLeistung.class;
	}

	@Override
	public void dispose() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	@Override
	public String getCodeSystemName() {
		return "Tarmed"; //$NON-NLS-1$
	}

}
