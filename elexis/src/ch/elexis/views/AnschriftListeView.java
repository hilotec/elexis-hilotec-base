// $Id: AnschriftListeView.java 1987 2007-03-02 11:54:58Z rgw_ch $
/*
 * Created on 11.09.2005
 */
package ch.elexis.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.JobPool;
import ch.elexis.data.Anschrift;
import ch.elexis.util.*;
@Deprecated
public class AnschriftListeView extends ViewPart {
    public static final String ID="ch.elexis.AnschriftListeView";
    private CommonViewer cv;
    private ViewerConfigurer vc;
    private AbstractDataLoaderJob loader;
    String[] fields={"Strasse","Plz","Ort"};
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
    	((LazyContentProvider)vc.getContentProvider()).stopListening();
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent)
    {
    	parent.setLayout(new FillLayout());
    	 cv=new CommonViewer();
         loader=(AbstractDataLoaderJob) JobPool.getJobPool().getJob("Anschriften");
         vc=new ViewerConfigurer(
         		//new ViewerConfigurer.DefaultContentProvider(cv, Anschrift.class),
         		new LazyContentProvider(cv,loader, null),
         		new DefaultLabelProvider(),
         		new DefaultControlFieldProvider(cv, fields),
         		new ViewerConfigurer.DefaultButtonProvider(cv,Anschrift.class),
         		new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null)
         );
         cv.create(vc,parent,SWT.NONE,getViewSite());
         //cv.getViewerWidget().addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
         ((LazyContentProvider)vc.getContentProvider()).startListening();
    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }

}
