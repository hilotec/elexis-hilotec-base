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
 *  $Id: KonsDetailView.java 2701 2007-07-04 17:12:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.ObjectListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.text.EnhancedTextField;
import ch.elexis.util.Extensions;
import ch.elexis.util.IKonsExtension;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;


/**
 * Behandlungseintrag, Diagnosen und Verrechnung
 * Dg und Verrechnung können wie Drag&Drop aus den entsprechenden Listen.Views
 * auf die Felder gezogen werden.
 * @author gerry
 *
 */
public class KonsDetailView extends ViewPart  implements SelectionListener, ActivationListener, ISaveablePart2, ObjectListener{
	public static final String ID="ch.elexis.Konsdetail";
	static Log log=Log.get("Detail");
	Hashtable<String, IKonsExtension>hXrefs;
	EnhancedTextField text;
	//DecimalFormat df=new DecimalFormat("0.00");
	Label lBeh,lVersion;
	Combo cbFall;
	private Konsultation actKons;
    FormToolkit tk;
    Form form,bottm;
    //private Hyperlink hDg,hVer;
    private DiagnosenDisplay dd;
    private VerrechnungsDisplay vd;
    private Action versionBackAction, purgeAction;
    Action versionFwdAction;
    int displayedVersion;
    Font emFont;
    
    
	@Override
	public void createPartControl(Composite p) {
		SashForm sash=new SashForm(p,SWT.VERTICAL);
		
        tk=Desk.theToolkit;
        form=tk.createForm(sash);
        form.getBody().setLayout(new GridLayout(1,true));
        form.setText("Keine Konsultation ausgewählt");
        lBeh=tk.createLabel(form.getBody(),"Keine Konsultation ausgewählt");
        emFont=new Font(Desk.theDisplay,"Helvetica",11,SWT.BOLD);
        lBeh.setFont(emFont);
		GridData gdBeh=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		lBeh.setLayoutData(gdBeh);
        //lFall=tk.createLabel(form.getBody(),"Kein Fall ausgewählt");
        cbFall=new Combo(form.getBody(),SWT.SINGLE);
        cbFall.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Fall[] faelle=(Fall[])cbFall.getData();
				int i=cbFall.getSelectionIndex();
				Fall nFall=faelle[i];
				Fall actFall=actKons.getFall();
				if(!nFall.getId().equals(actFall.getId())){
					MessageDialog msd=new MessageDialog(getViewSite().getShell(),"Fallzuordnung ändern",Desk.theImageRegistry.get(Desk.IMG_LOGO48),
							"Möchten Sie diese Behandlung vom Fall:\n'"+actFall.getLabel()+"' zum Fall:\n'"+nFall.getLabel()+"' transferieren?",
							MessageDialog.QUESTION,new String[]{"Ja","Nein"},0);
					if(msd.open()==0){
						actKons.setFall(nFall);
						setKons(actKons);
					}
				}
			}
        	
        });
        GridData gdFall=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		cbFall.setLayoutData(gdFall);
        
		lVersion=tk.createLabel(form.getBody(),"<aktuell>");
        GridData gdVer=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
        lVersion.setLayoutData(gdVer);
        
		text=new EnhancedTextField(form.getBody());
		hXrefs=new Hashtable<String,IKonsExtension>();
		@SuppressWarnings("unchecked")
    	List<IKonsExtension> xrefs=Extensions.getClasses("ch.elexis.KonsExtension", "KonsExtension");
    	for(IKonsExtension x:xrefs){
    		String provider=x.connect(text);
   			hXrefs.put(provider, x);
    	}
    	text.setXrefs(hXrefs);
		GridData gd=new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL|GridData.GRAB_VERTICAL|GridData.GRAB_HORIZONTAL);
		text.setLayoutData(gd);
	
		tk.adapt(text);
		SashForm bf=new SashForm(sash,SWT.HORIZONTAL);
        Composite botleft=tk.createComposite(bf);
        botleft.setLayout(new GridLayout(1,false));
        Composite botright=tk.createComposite(bf);
        botright.setLayout(new GridLayout(1,false));
        
        dd=new  DiagnosenDisplay(getSite().getPage(),botleft,SWT.NONE);
        dd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
        vd=new VerrechnungsDisplay(getSite().getPage(),botright,SWT.NONE);
        vd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
        
        sash.setWeights(new int[]{80,20});
        makeActions();
        ViewMenus menu=new ViewMenus(getViewSite());
        if(Hub.acl.request(AccessControlDefaults.AC_PURGE)){
        	menu.createMenu(versionFwdAction,versionBackAction,GlobalActions.neueKonsAction,GlobalActions.delKonsAction,GlobalActions.redateAction,purgeAction);
        }else{
        	menu.createMenu(versionFwdAction,versionBackAction,GlobalActions.neueKonsAction,GlobalActions.delKonsAction, GlobalActions.redateAction);
        }
        menu.createToolbar(GlobalActions.neueKonsAction);
        GlobalEvents.getInstance().addActivationListener(this,this);
        GlobalEvents.getInstance().addObjectListener(this);
        text.connectGlobalActions(getViewSite());
        adaptMenus();
        setKons(GlobalEvents.getSelectedKons());
	}
	
	
	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
    	GlobalEvents.getInstance().removeSelectionListener(this);
    	GlobalEvents.getInstance().removeActivationListener(this,this);
    	GlobalEvents.getInstance().removeObjectListener(this);
    	text.disconnectGlobalActions(getViewSite());
    	emFont.dispose();
        super.dispose();
    }

    
    /** Aktuellen patient setzen */
    private void setPatient(Patient pat){
    	if(pat==null){
    		pat=GlobalEvents.getSelectedPatient();
    	}
		if(pat!=null){
			form.setText(pat.getPersonalia());
			Fall[] faelle=pat.getFaelle();
			cbFall.removeAll();
			cbFall.setData(faelle);
			for(Fall f:faelle){
				cbFall.add(f.getLabel());
			}
		}
		//lFall.setText("");
		//lBeh.setText("");
	}
	@Override
	public void setFocus() {
		text.setFocus();
	}
	
    
    
	/**
	 * Aktuelle Konsultation setzen.
	 */
	private void setKons(Konsultation b){
		boolean inChange=false;
		if(!inChange){
			inChange=true;
	        if(b != null){
	        	Fall act=b.getFall();
	        	setPatient(act.getPatient());
	            setKonsText(b,b.getHeadVersion());
	            //form.setText(b.getFall().getPatient().getLabel());
	            //lFall.setText(b.getFall().getLabel());
	
	            Fall[] faelle=(Fall[])cbFall.getData();
	            for(int i=0;i<faelle.length;i++){
	            	if(faelle[i].getId().equals(act.getId())){
	            		cbFall.select(i);
	            		break;
	            	}
	            }
	            StringBuilder sb=new StringBuilder();
	            Mandant m=b.getMandant();
	            sb.append("Kons. vom ").append(b.getDatum()).append(" (")
	            	.append(m==null ? "nicht von Ihnen" : m.getLabel()).append(")");
	            lBeh.setText(sb.toString());
	            dd.setDiagnosen(b);
	            vd.setLeistungen(b);
	            text.setEnabled(true);
	            if((GlobalEvents.getSelectedKons()==null) || (!GlobalEvents.getSelectedKons().getId().equals(b.getId()))){
	            	inChange=true;
	            	GlobalEvents.getInstance().fireSelectionEvent(b);
	            }
	        }else{
	        	form.setText("Keine Konsultation ausgewählt");
	        	lBeh.setText("-");
	        	lVersion.setText("");
	        	//cbFall.removeAll();
	        	dd.clear();
	        	vd.clear();
	        	text.setText("");
	        	text.setEnabled(false);
	        }
	        actKons=b;
	        inChange=false;
		}
    }
    
    
    void setKonsText(Konsultation b, int version){
    	String ntext="";
        if((version>=0) && (version<=b.getHeadVersion())){
        	VersionedResource vr=b.getEintrag();
        	ResourceItem entry=vr.getVersion(version);
        	ntext=entry.data;
        	StringBuilder sb=new StringBuilder();
            sb.append("rev. ").append(version).append(" vom ")
                .append(new TimeTool(entry.timestamp).toString(TimeTool.FULL_GER))
                .append(" (").append(entry.remark).append(")");
            lVersion.setText(sb.toString());
        }else{
        	lVersion.setText("");
        }
        text.setText(ntext);
        text.setKons(b);
        displayedVersion=version;
        versionBackAction.setEnabled(version!=0);
        versionFwdAction.setEnabled(version!=b.getHeadVersion());
    }
   
    
    
	
	
	public void selectionEvent(PersistentObject first) {
		if(first instanceof Konsultation){
			setKons((Konsultation)first);
		}
		else if(first instanceof Patient){
			// letzte Konsultation waehlen, falls aktuelle Konsultation nicht zum Patienten gehoert
			if((actKons==null) ||
				 (!(actKons.getFall().getPatient().equals((Patient)first)))){ {
					setKons(((Patient)first).getLetzteKons(false));
				}
			}
		}else if(first instanceof Anwender){
			adaptMenus();
		}
	
	}
	
	private void makeActions(){
		
		purgeAction=new Action("Alte Eintragsversionen entfernen"){

			@Override
			public void run() {
				actKons.purgeEintrag();
				GlobalEvents.getInstance().fireSelectionEvent(actKons);
			}
			
		};
	    versionBackAction=new Action("Vorherige Version"){

            @Override
            public void run()
            {
            	if(MessageDialog.openConfirm(getViewSite().getShell(), "Konsultationstext ersetzen", "Wollen Sie wirklich den aktuellen Konsultationstext gegen eine frühere Version desselben Eintrags ersetzen?")){
                    setKonsText(actKons,displayedVersion-1);
                    text.setDirty(true);
            	}
            }
	        
        };
        versionFwdAction=new Action("nächste Version"){
            public void run()
            {
            	if(MessageDialog.openConfirm(getViewSite().getShell(), "Konsultationstext ersetzen", "Wollen Sie wirklich den aktuellen Konsultationstext gegen eine spätere Version desselben Eintrags ersetzen?")){
            		setKonsText(actKons,displayedVersion+1);
            		text.setDirty(true);
            	}
            }
        };
    }

	public void activation(boolean mode) {
		if((mode==false) &&  (text.isDirty())){
			if(actKons!=null){
				actKons.updateEintrag(text.getDocumentAsText(), false);
				log.log("saved.",Log.DEBUGMSG);
			}
			text.setDirty(false);
		}
		
	}

	public void visible(boolean mode) {
		if(mode==true){
			GlobalEvents.getInstance().addSelectionListener(this);
			adaptMenus();
			selectionEvent(GlobalEvents.getInstance().getSelectedObject(Patient.class));
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) { 
		if(template.equals(Konsultation.class) || template.equals(Patient.class) || template.equals(Fall.class)){
			setKons(null);
		}
	}
	
	public void adaptMenus(){
		vd.tVerr.getMenu().setEnabled(Hub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN));
		GlobalActions.delKonsAction.setEnabled(Hub.acl.request(AccessControlDefaults.KONS_DELETE));
		GlobalActions.neueKonsAction.setEnabled(Hub.acl.request(AccessControlDefaults.KONS_CREATE));
	}
	
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	public void objectChanged(PersistentObject o) {
		if( (o!=null) &&
			(actKons!=null) &&
			(o.getId().equals(actKons.getId()))){
				setKons((Konsultation)o);
		}
		
	}

	public void objectCreated(PersistentObject o) {
		if(o instanceof Fall){
			if(actKons != null){
				Fall fall=(Fall)o;
				if(fall.getPatient().getId().equals(actKons.getFall().getPatient().getId())){
					setPatient(fall.getPatient());
				}
			}
		}
		
	}

	public void objectDeleted(PersistentObject o) {
		if(o instanceof Konsultation){
			setKons(null);
		}
		
	}
}
