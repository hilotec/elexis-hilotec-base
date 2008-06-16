/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ControlFieldProvider.java 4044 2008-06-16 19:38:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
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
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.BAGMediFactory;
import ch.elexis.text.ElexisText;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.Messages;
import ch.elexis.util.SWTHelper;

public class ControlFieldProvider extends DefaultControlFieldProvider {
	Text tMedi, tSubst;
	Button bGenerics, bGroup;
	FormToolkit tk=Desk.getToolkit();
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
        bReload.setImage(Desk.getImage(Desk.IMG_REFRESH));
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
        
        selectors=new ElexisText[fields.length];
        for(int i=0;i<selectors.length;i++){
            selectors[i]=new ElexisText(inner, SWT.BORDER);
            selectors[i].addModifyListener(ml);
            selectors[i].addSelectionListener(sl);
            selectors[i].setToolTipText(Messages.getString("DefaultControlFieldProvider.enterFilter")); //$NON-NLS-1$
            selectors[i].setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        }
        bGenerics=new Button(ret,SWT.TOGGLE);
        String img="icons"+File.separator+"ggruen.png";
        ImageDescriptor image=BAGMediFactory.loadImageDescriptor(img);
        if(image!=null){
        	bGenerics.setImage(image.createImage());
        }
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
