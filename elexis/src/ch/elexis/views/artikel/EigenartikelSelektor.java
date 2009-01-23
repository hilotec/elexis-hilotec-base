/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: EigenartikelSelektor.java 5024 2009-01-23 16:36:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.artikel;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;

import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.ListLoader;
import ch.elexis.data.Eigenartikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.Query;
import ch.elexis.util.*;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultControlFieldProvider;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.LazyContentProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class EigenartikelSelektor extends CodeSelectorFactory {
	AbstractDataLoaderJob dataloader;
	
	public EigenartikelSelektor(){
		dataloader = (AbstractDataLoaderJob) JobPool.getJobPool().getJob("Eigenartikel");
		if (dataloader == null) {
			dataloader =
				new ListLoader<Eigenartikel>("Eigenartikel", new Query<Eigenartikel>(
					Eigenartikel.class), new String[] {
					"Name"
				});
			JobPool.getJobPool().addJob(dataloader);
		}
		JobPool.getJobPool().activate("Eigenartikel", Job.SHORT);
		
	}
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		new ArtikelContextMenu((Eigenartikel) new PersistentObjectFactory()
			.createTemplate(Eigenartikel.class), cv);
		return new ViewerConfigurer(new LazyContentProvider(cv, dataloader, null),
			new DefaultLabelProvider(), new DefaultControlFieldProvider(cv, new String[] {
				"Name"
			}), new ViewerConfigurer.DefaultButtonProvider(cv, Eigenartikel.class),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
		
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	@Override
	public void dispose(){
	// TODO Automatisch erstellter Methoden-Stub
	
	}
	
	@Override
	public String getCodeSystemName(){
		return "Eigenartikel";
	}
	
}
