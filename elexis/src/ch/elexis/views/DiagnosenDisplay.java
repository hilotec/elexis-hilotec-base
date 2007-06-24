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
 *  $Id: DiagnosenDisplay.java 2528 2007-06-18 10:52:49Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.Log;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.views.codesystems.DiagnosenView;
import ch.rgw.tools.ExHandler;

public class DiagnosenDisplay extends Composite {
	Table tDg;
	private Hyperlink hDg;
	private Log log=Log.get("DiagnosenDisplay");
	private PersistentObjectDropTarget dropTarget;
	
	public DiagnosenDisplay(final IWorkbenchPage page, Composite parent, int style){
		super(parent,style);
		setLayout(new GridLayout());
		hDg=Desk.theToolkit.createHyperlink(this,"Behandlungsdiagnosen",SWT.NONE);
        hDg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
        hDg.addHyperlinkListener(new HyperlinkAdapter(){
			@Override
			public void linkActivated(HyperlinkEvent e) {
				try{
					page.showView(DiagnosenView.ID);
					GlobalEvents.getInstance().setCodeSelectorTarget(dropTarget);
				}catch(Exception ex){
					ExHandler.handle(ex);
					log.log("Fehler beim Starten des Diagnosecodes "+ex.getMessage(),Log.ERRORS );
				}
			}
        });
        tDg=Desk.theToolkit.createTable(this,SWT.SINGLE|SWT.WRAP);
        tDg.setLayoutData(new GridData(GridData.FILL_BOTH));
        tDg.setMenu(createDgMenu());
      

        dropTarget=new PersistentObjectDropTarget("Diagnosen", tDg,new DropReceiver());

	}
	public void clear(){
		tDg.removeAll();
	}
	private final class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(PersistentObject o, DropTargetEvent ev) {
			 Konsultation actKons=GlobalEvents.getSelectedKons();
			 if(o instanceof IDiagnose){
				 actKons.addDiagnose((IDiagnose)o);
		         setDiagnosen(actKons);
		     }
		}

		public boolean accept(PersistentObject o) {
			if(o instanceof IVerrechenbar){
				return true;
			}
			if(o instanceof IDiagnose){
				return true;
			}
			return false;
		}
	}
	void setDiagnosen(Konsultation b){
        List<IDiagnose> dgl=b.getDiagnosen();
        tDg.removeAll();
        for(IDiagnose dg:dgl){
            TableItem ti=new TableItem(tDg,SWT.WRAP);
            ti.setText(dg.getLabel());
            ti.setData(dg);
        }
        //tDg.setEnabled(b.getStatus()==RnStatus.NICHT_VON_HEUTE);
        
    }
	private Menu createDgMenu(){
        Menu ret=new Menu(tDg);
        MenuItem delDg=new MenuItem(ret,SWT.NONE);
        delDg.setText("Diagnose entfernen");
        delDg.addSelectionListener(new delDgListener());
        return ret;
    }
	class delDgListener extends SelectionAdapter{
        public void widgetSelected(SelectionEvent e) {
        	int sel=tDg.getSelectionIndex();
        	TableItem ti=tDg.getItem(sel);
            GlobalEvents.getSelectedKons().removeDiagnose((IDiagnose)ti.getData());
            tDg.remove(sel);
            // setBehandlung(actBehandlung);
        }
    }
}
