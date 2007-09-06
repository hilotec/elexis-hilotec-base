/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MiGelSelector.java 3104 2007-09-06 18:58:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.artikel_ch.views;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;

import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.ListLoader;
import ch.elexis.artikel_ch.data.ArtikelFactory;
import ch.elexis.artikel_ch.data.MiGelArtikel;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LazyContentProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.artikel.ArtikelContextMenu;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class MiGelSelector extends CodeSelectorFactory {
	AbstractDataLoaderJob dataloader;
	
	public MiGelSelector() {
		dataloader=(AbstractDataLoaderJob) JobPool.getJobPool().getJob("MiGeL");
		if(dataloader==null){
			dataloader=new ListLoader("MiGeL",new Query<MiGelArtikel>(MiGelArtikel.class),new String[]{"SubID","Name"});
			JobPool.getJobPool().addJob(dataloader);
			JobPool.getJobPool().activate("MiGeL",Job.SHORT);
		}
	}

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		new ArtikelContextMenu((MiGelArtikel)new ArtikelFactory().createTemplate(MiGelArtikel.class),cv);
		return new ViewerConfigurer(
				new LazyContentProvider(cv,dataloader,null),
				new DefaultLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"SubID=Code","Name"}),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,null)
		);
	}

	@Override
	public Class getElementClass() {
		return MiGelArtikel.class;
	}

	@Override
	public void dispose() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	@Override
	public String getCodeSystemName() {
		return "MiGeL";
	}

}
