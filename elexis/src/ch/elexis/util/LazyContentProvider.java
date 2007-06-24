/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: LazyContentProvider.java 2208 2007-04-13 09:05:39Z danlutz $
 *******************************************************************************/

package ch.elexis.util;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.Hub;
import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.BackgroundJob;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.data.Query;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;
import ch.elexis.util.ViewerConfigurer.ControlFieldProvider;

/**
 * Ein Content-Provider, der benötigte Daten aus einem BackgroundJob bezieht
 * und einem TableViewer nur gerade die jeweils benötigten Datne liefern kann.
 * Registriert sich beim Dataloader selbst als listener und startet diesen auch,
 * wenn Daten benötigt werden.
 * @author Gerry
 */
public class LazyContentProvider implements CommonContentProvider, ILazyContentProvider, 
	BackgroundJobListener, AbstractDataLoaderJob.FilterProvider{
    AbstractDataLoaderJob dataloader;
    CommonViewer tableviewer;
    String	required;
    
    public LazyContentProvider(CommonViewer viewer, AbstractDataLoaderJob job, String rights){
        dataloader=job;
        job.addListener(this);
        job.addFilterProvider(this);
        tableviewer=viewer;
        required=rights;
    }
    public void dispose()
    {   dataloader.removeListener(this);
    	dataloader.removeFilterProvider(this);
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    public void updateElement(int index)
    {
    	if(Hub.acl.request(required)==false){
    		((TableViewer)tableviewer.getViewerWidget()).replace(" --- ",index); //$NON-NLS-1$
    		return;
    	}
        if(dataloader.isValid()){
            Object[] res=(Object[])dataloader.getData();
            Object nval=Messages.getString("LazyContentProvider.noData"); //$NON-NLS-1$
            if(index<res.length){
                 nval=res[index];
            }
           ((TableViewer)tableviewer.getViewerWidget()).replace(nval,index);
        }else{
            JobPool pool=JobPool.getJobPool();
            if(pool.getJob(dataloader.getJobname())==null){
                pool.addJob(dataloader);

            }
            pool.activate(dataloader.getJobname(),Job.SHORT);
        }
      
    }
    public void jobFinished(BackgroundJob j)
    {
    	int size=0;
    	if( (j!=null) && (j.getData()!=null)){
    		size=((Object[])j.getData()).length;
    	}
        ((TableViewer)tableviewer.getViewerWidget()).getTable().setItemCount(size==0?1:size);
        tableviewer.notify(CommonViewer.Message.update);
        
    }
	public void startListening(){
		tableviewer.getConfigurer().controlFieldProvider.addChangeListener(this);
	}
	public void stopListening(){
		tableviewer.getConfigurer().controlFieldProvider.removeChangeListener(this);
	}

	public void applyFilter() {
		Query qbe=dataloader.getQuery();
		if(qbe!=null){
			ViewerConfigurer vc=tableviewer.getConfigurer();
			if(vc!=null){
				ControlFieldProvider cfp=vc.getControlFieldProvider();
				cfp.setQuery(qbe);
			}
		}
	}
	public void changed(String[] fields, String[] values) {
		dataloader.invalidate();
		if(tableviewer.getConfigurer().getControlFieldProvider().isEmpty()){
			tableviewer.notify(CommonViewer.Message.empty);
		}else{
			tableviewer.notify(CommonViewer.Message.notempty);
		}
		JobPool.getJobPool().activate(dataloader.getJobname(),Job.SHORT);
	}
	public void reorder(String field) {
		dataloader.setOrder(field);
		changed(null,null);
		
	}
	public void selected() {
		// nothing to do
	}
	public Object[] getElements(Object inputElement) {
		return (Object[])dataloader.getData();
	}
    
}