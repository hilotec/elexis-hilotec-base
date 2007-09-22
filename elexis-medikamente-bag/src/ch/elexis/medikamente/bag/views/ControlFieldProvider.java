/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ControlFieldProvider.java 3185 2007-09-22 18:24:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.medikamente.bag.data.BAGMediFactory;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.Messages;
import ch.elexis.util.SWTHelper;

public class ControlFieldProvider extends DefaultControlFieldProvider {
	Text tMedi, tSubst;
	Button bGenerics, bGroup;
	FormToolkit tk=Desk.theToolkit;
	boolean bGenericsOnly;
	
	public ControlFieldProvider(final CommonViewer viewer) {
		super(viewer, new String[]{"Medikament","Substanz","Notiz"});
	}

	@Override
	public Composite createControl(final Composite parent) {
		Form form=tk.createForm(parent);
        form.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        Composite ret=form.getBody();
	   //Composite ret=new Composite(parent,style);
        ret.setLayout(new GridLayout(2,false));
        Button bReload=new Button(ret,SWT.PUSH);
        bReload.setImage(Desk.theImageRegistry.get(Desk.IMG_REFRESH));
        bReload.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if(isEmpty()){
					fireChangedEvent();
				}else{
					clearValues();
				}
			}
        	
        });
        /*Hyperlink hClr=tk.createHyperlink(ret,"x",SWT.NONE); //$NON-NLS-1$
        hClr.addHyperlinkListener(new HyperlinkAdapter(){
            @Override
            public void linkActivated(final HyperlinkEvent e)
            {	clearValues();
            }
            
        });
        */
        Composite inner=new Composite(ret,SWT.NONE);
        GridLayout lRet=new GridLayout(fields.length,true);
        inner.setLayout(lRet);
        inner.setLayoutData(SWTHelper.getFillGridData(1,true,2,true));
        
        for(String l:fields){
            Hyperlink hl=tk.createHyperlink(inner,l,SWT.NONE);
            hl.addHyperlinkListener(new HyperlinkAdapter(){

                @Override
                public void linkActivated(final HyperlinkEvent e)
                {
                    Hyperlink h=(Hyperlink)e.getSource();
                    fireSortEvent(h.getText());
                }
                
            });
        }
        
        selectors=new Text[fields.length];
        for(int i=0;i<selectors.length;i++){
            selectors[i]=tk.createText(inner,"",SWT.BORDER); //$NON-NLS-1$
            selectors[i].addModifyListener(ml);
            selectors[i].addSelectionListener(sl);
            selectors[i].setToolTipText(Messages.getString("DefaultControlFieldProvider.enterFilter")); //$NON-NLS-1$
            selectors[i].setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        }
        bGenerics=new Button(ret,SWT.TOGGLE);
        bGenerics.setImage(BAGMediFactory.loadImageDescriptor("icons/ggruen.png").createImage());
        bGenerics.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				bGenericsOnly=bGenerics.getSelection();
				fireChangedEvent();
			}
        	
        });
        return ret;
	}

		
}
