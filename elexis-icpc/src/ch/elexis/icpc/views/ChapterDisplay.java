/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: ChapterDisplay.java 4356 2008-09-02 16:20:10Z rgw_ch $
 *******************************************************************************/
package ch.elexis.icpc.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.data.Query;
import ch.elexis.icpc.IcpcCode;
import ch.elexis.preferences.UserSettings2;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.WidgetFactory;
import ch.elexis.util.ViewerConfigurer.ContentProviderAdapter;
import ch.rgw.tools.StringTool;


public class ChapterDisplay extends Composite {
	private static final String UC2_HEADING="ICPCChapter/"; 
	FormToolkit tk=Desk.getToolkit();
	ScrolledForm form;
	String chapter; 
	ExpandableComposite[] ec;
	
	
	public ChapterDisplay(Composite parent, final String chapter){
		super(parent, SWT.NONE);
		this.chapter=chapter;
		setLayout(new FillLayout());
		form=tk.createScrolledForm(this);
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		form.setText(chapter);
		ec=new ExpandableComposite[IcpcCode.components.length];
		
		for(int i=0;i<ec.length;i++){
			String c=IcpcCode.components[i];
			ec[i]=WidgetFactory.createExpandableComposite(tk, form, c);
            UserSettings2.setExpandedState(ec[i], UC2_HEADING+c);
            Composite inlay=new Composite(ec[i],SWT.NONE);
            inlay.setLayout(new FillLayout());
			CommonViewer cv=new CommonViewer();
			ViewerConfigurer vc=new ViewerConfigurer(new ComponentContentProvider(c), new DefaultLabelProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TABLE,SWT.SINGLE,cv));
			ec[i].setData(cv);
			cv.create(vc, inlay, SWT.NONE, this);
            ec[i].addExpansionListener(new ExpansionAdapter(){
                @Override
                public void expansionStateChanging(final ExpansionEvent e)
                {
                	ExpandableComposite src=(ExpandableComposite)e.getSource();
                	
                    if(e.getState()==true){
                    	CommonViewer cv=(CommonViewer)src.getData();
                    	cv.notify(CommonViewer.Message.update);
                    }
                    UserSettings2.saveExpandedState(UC2_HEADING+src.getText(), e.getState());
                }
                
            });
            ec[i].setClient(inlay);

		}
	}
	
	

	class ComponentContentProvider extends ContentProviderAdapter{
		private String component;
		
		public ComponentContentProvider( String component){
			this.component=component;
		}
		public Object[] getElements(Object inputElement){
			Query<IcpcCode> qbe=new Query<IcpcCode>(IcpcCode.class);
			qbe.add("ID", "Like", chapter.substring(0, 1)+"%");
			qbe.add("component", StringTool.equals, component.substring(0, 1));
			List<IcpcCode> codes=qbe.execute();
			return codes.toArray();
		}
		
	}



	public void setComponent(String mode){
		for(int i=0;i<ec.length;i++){
			ec[i].setEnabled(true);
		}
		if("RFE".equals(mode)){
			// all components enabled
		}else if("DG".equals(mode)){
			// only 1 and 7 enabled
			for(int i=1;i<6;i++){
				ec[i].setEnabled(false);
			}
		}else if("PROC".equals(mode)){
			// 2,3,5,6 enabled
			ec[0].setEnabled(false);
			ec[3].setEnabled(false);
			ec[6].setEnabled(false);
		}
	}
}
