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
 * $Id: PatListFilterBox.java 4047 2008-06-18 13:38:22Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Etikette;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Script;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;

/**
 * This will be displayed on top of the PatientListeView. It allows to drop Objects (Artikel, 
 * IVerrechnet, IDiagnose etc.) as filter conditions.
 * The PatListFilterBox will also be added as an IFilter to the StructuredViewer that displays
 * the patients thus allowing to filter the list according to the conditions.
 * 
 * The Objects that can act as filter conditions must e declared as IPatFilter. Later, we'll define
 * an extension point for Plugins to connect their classes.
 * @author Gerry
 *
 */
public class PatListFilterBox extends ListDisplay<PersistentObject> implements IFilter{
    PersistentObjectDropTarget dropTarget;
    private static final String ETIKETTE="Etikette...";
    private static final String FELD="Feld...";
    private static final String LEEREN=" Leeren";
    private static final String NB_PREFIX="PLF_FLD:";
    private ArrayList<IPatFilter> filters=new ArrayList<IPatFilter>();
    private IPatFilter defaultFilter=new PatFilterImpl();
    private boolean parseError=false;
    
	PatListFilterBox(Composite parent){
		super(parent,SWT.NONE,null);
		setDLDListener(new LDListener(){

			public String getLabel(Object o) {
				if(o instanceof NamedBlob){
					return "Feld: "+((NamedBlob)o).getString();
				}else if(o instanceof PersistentObject){
					return o.getClass().getSimpleName()+":"+((PersistentObject)o).getLabel();
				}else{
					return o.toString();
				}
			}

			public void hyperlinkActivated(String l) {
				if(l.equals(LEEREN)){
					clear();
				}else if(l.equals(ETIKETTE)){
					new EtikettenAuswahl().open();
				}else if(l.equals(FELD)){
					new FeldauswahlDlg().open();
				}
			}});
		addHyperlinks(FELD,ETIKETTE,LEEREN);
		dropTarget=new PersistentObjectDropTarget("Statfilter",this,new DropReceiver());

	}
	private  class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(final PersistentObject o, final DropTargetEvent ev) {
			PatListFilterBox.this.add(o);
		}

		public boolean accept(final PersistentObject o) {
			if(o instanceof Script){
				if(Hub.acl.request(AccessControlDefaults.SCRIPT_EXECUTE)==false){
					return false;
				}
			}
			return true;
		}
	}
	
	public void reset(){
		parseError=false;
	}
	public boolean aboutToStart(){
		for(PersistentObject cond:getAll()){
			if(cond instanceof Script){
				if(!defaultFilter.aboutToStart(cond)){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean finished(){
		for(PersistentObject cond:getAll()){
			if(cond instanceof Script){
				if(!defaultFilter.finished(cond)){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * We select the Patient with an AND operation running over all filter conditions
	 * If no filter was registered for a type, we use our defaultFilter
	 * @throws Exception 
	 */
	public boolean select(Object toTest) {
		if(parseError){
			return false;
		}
		if(toTest instanceof Patient){
			Patient p=(Patient)toTest;

			for(PersistentObject cond:getAll()){
				boolean handled=false;
				for(IPatFilter filter:filters){
					int result=filter.accept(p, cond);
					if(result==IPatFilter.REJECT){
						return false;
					}
					if(result==IPatFilter.ACCEPT){
						handled=true;
					}else if(result==IPatFilter.FILTER_FAULT){
						parseError=true;
					}
					
				}
				if(!handled){
					int result=defaultFilter.accept(p, cond);
					if(result==IPatFilter.REJECT){
						return false;
					}
					if(result==IPatFilter.FILTER_FAULT){
						remove(cond);
						parseError=true;
					}
				}

			}
			return true; // Only if all conditions accept or don't handle
		}
		return false;
	}

	public void addPatFilter(IPatFilter filter){
		filters.add(filter);
	}
	public void removeFilter(IPatFilter filter){
		filters.remove(filter);
	}
	
	public interface IPatFilter{
		/** The Patient is not selected with the filter object */
		public static final int REJECT=-1;
		/** The Patient is selected with the filter objct */
		public static final int ACCEPT=1;
		/** We do not handle this type of filter object */
		public static final int DONT_HANDLE=0;
		/** We encountered an error while trying to filter */
		public static final int FILTER_FAULT=-2;
		
		/**
		 * Will the Patient be accepted for the Filter depending on the Object? 
		 * @param p The Patient to consider
		 * @param o The Object to check
		 * @return one of REJECT, ACCEPT, DONT_HANDLE
		 * @throws Exception 
		 */
		public int accept(Patient p, PersistentObject o);
		
		public boolean aboutToStart(PersistentObject o);
		public boolean finished(PersistentObject o);
	}
	
	class EtikettenAuswahl extends Dialog{
		List lEtiketten;
		Etikette[] etiketten;
		Etikette[] result;
		public EtikettenAuswahl() {
			super(PatListFilterBox.this.getShell());
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("Etikette/n für Filter auswählen");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=(Composite) super.createDialogArea(parent);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			lEtiketten=new List(ret,SWT.MULTI);
			Query<Etikette> qbe=new Query<Etikette>(Etikette.class);
			etiketten=qbe.execute().toArray(new Etikette[0]);
			String[] etexts=new String[etiketten.length];
			for(int i=0;i<etiketten.length;i++){
				etexts[i]=etiketten[i].getLabel();
			}
			lEtiketten.setItems(etexts);
			return ret;
		}

		@Override
		protected void okPressed() {
			int[] indices=lEtiketten.getSelectionIndices();
			result=new Etikette[indices.length];
			for(int i=0;i<indices.length;i++){
				add(etiketten[indices[i]]);
			}
			super.okPressed();
		}
	}
	
	class FeldauswahlDlg extends Dialog{
		Text tFeld, tValue;
		Combo cbOp;
		public NamedBlob value;
		public FeldauswahlDlg() {
			super(PatListFilterBox.this.getShell());
		}
		@Override
		public void create() {
			super.create();
			getShell().setText("Filterbedingung eingeben");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=(Composite) super.createDialogArea(parent);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout(3,false));
			new Label(ret,SWT.NONE).setText("Feld");
			new Label(ret,SWT.NONE).setText(" ");
			new Label(ret,SWT.NONE).setText("Wert");
			tFeld=new Text(ret,SWT.BORDER);
			tFeld.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cbOp=new Combo(ret,SWT.SINGLE|SWT.READ_ONLY);
			cbOp.setItems(new String[]{"=","LIKE","Regexp"});
			cbOp.select(0);
			tValue=new Text(ret,SWT.BORDER);
			tValue.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			return ret;
		}

		@Override
		protected void okPressed() {
			String fld=tFeld.getText();
			if(fld.length()>0){
				value=NamedBlob.load(NB_PREFIX+fld);
				value.putString(fld+"::"+cbOp.getText()+"::"+tValue.getText());
				PatListFilterBox.this.add(value);
			}
			super.okPressed();
		}
	}
}
