package ch.elexis.agenda.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.actions.AgendaActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.acl.ACLContributor;
import ch.elexis.agenda.data.ICalTransfer;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.TagesgrenzenDialog;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.dialogs.TerminListeDruckenDialog;
import ch.elexis.dialogs.TermineDruckenDialog;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

/**
 * Abstract base class for an agenda window.
 * 
 * @author Gerry
 * 
 */
public abstract class BaseView extends ViewPart implements BackingStoreListener, HeartListener,
		ActivationListener {
	
	IAction newTerminAction, blockAction,terminKuerzenAction,terminVerlaengernAction,terminAendernAction;
	IAction dayLimitsAction, newViewAction, printAction, exportAction, importAction;
	IAction printPatientAction;
	MenuManager menu=new MenuManager();
	Activator agenda=Activator.getDefault();
		
	@Override
	public void createPartControl(Composite parent){
		makeActions();
		create(parent);
		refresh();
	}
	
	abstract protected void create(Composite parent);
	
	abstract protected void refresh();
	
	abstract protected IPlannable getSelection();
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	protected void updateActions(){
		dayLimitsAction.setEnabled(Hub.acl.request(ACLContributor.CHANGE_DAYSETTINGS));
		boolean canChangeAppointments=Hub.acl.request(ACLContributor.CHANGE_APPOINTMENTS);
		newTerminAction.setEnabled(canChangeAppointments);
		terminKuerzenAction.setEnabled(canChangeAppointments);
		terminVerlaengernAction.setEnabled(canChangeAppointments);
		terminAendernAction.setEnabled(canChangeAppointments);
		AgendaActions.updateActions();
		refresh();
	}
	public void reloadContents(Class<? extends PersistentObject> clazz){
		if (clazz.equals(Termin.class)) {
			Desk.getDisplay().asyncExec(new Runnable() {
				public void run(){
					refresh();
					
				}
			});
		} else if (clazz.equals(Anwender.class)) {
			updateActions();
			/*
			if (tv != null) {
				if (!tv.getControl().isDisposed()) {
					tv.getControl().setFont(
						Desk.getFont(ch.elexis.preferences.PreferenceConstants.USR_DEFAULTFONT));
				}
			}
			*/
			agenda.setActResource(Hub.userCfg.get(PreferenceConstants.AG_BEREICH, agenda.getActResource()));
			setPartName("Agenda "+agenda.getActResource()); //$NON-NLS-1$
		}
		
	}
	
	public void heartbeat(){
		refresh();
	}
	
	public void activation(boolean mode){
	// TODO Auto-generated method stub
	
	}
	
	public void visible(boolean mode){
	// TODO Auto-generated method stub
	
	}
	
	protected void makeActions(){
		dayLimitsAction=new Action("Tagesgrenzen"){
			@Override
			public void run(){
				new TagesgrenzenDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						agenda.getActDate().toString(TimeTool.DATE_COMPACT),agenda.getActResource())
						.open();
				refresh();
			}
		};

		blockAction=new Action(Messages.TagesView_lockPeriod){ 
			@Override
			public void run(){
				IPlannable p=getSelection();
				if(p!=null){
					if(p instanceof Termin.Free){
						new Termin(agenda.getActResource(),agenda.getActDate().toString(TimeTool.DATE_COMPACT),p.getStartMinute(),
								p.getDurationInMinutes()+p.getStartMinute(),Termin.typReserviert(),Termin.statusLeer());
						GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
					}
				}

			}
		};
		terminAendernAction=new Action(Messages.TagesView_changeTermin){ 
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText(Messages.TagesView_changeThisTermin); 
			}
			@Override
			public void run(){
				TerminDialog dlg=new TerminDialog(
						(Termin)GlobalEvents.getInstance().getSelectedObject(Termin.class));
				dlg.open();
				refresh();
				
			}
		};
		terminKuerzenAction=new Action(Messages.TagesView_shortenTermin){ 
			@Override
			public void run(){
				Termin t=(Termin) GlobalEvents.getInstance().getSelectedObject(Termin.class);
				if(t!=null) {
					t.setDurationInMinutes(t.getDurationInMinutes()>>1);
					GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
				}
			}
		};
		terminVerlaengernAction=new Action(Messages.TagesView_enlargeTermin){ 
			@Override
			public void run(){
				Termin t=(Termin) GlobalEvents.getInstance().getSelectedObject(Termin.class);
				if(t!=null) {
					agenda.setActDate(t.getDay());
					Termin n=Plannables.getFollowingTermin(agenda.getActResource(), agenda.getActDate(), t);
					if(n!=null){
						t.setEndTime(n.getStartTime());
						//t.setDurationInMinutes(t.getDurationInMinutes()+15);
						GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
					}
				}
			}
		};
		newTerminAction=new Action(Messages.TagesView_newTermin){ 
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
				setToolTipText(Messages.TagesView_createNewTermin); 
			}
			@Override
			public void run(){
				new TerminDialog(null).open();
				refresh();
			}
		};
		printAction=new Action("Tagesliste drucken"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Termine des gewählten Tages ausdrucken");
			}
			@Override
			public void run(){
				IPlannable[] liste=Plannables.loadDay(agenda.getActResource(), agenda.getActDate());
				new TerminListeDruckenDialog(getViewSite().getShell(),liste).open();
				refresh();
			}
		};
		printPatientAction=new Action("Patienten-Termine drucken"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Zukünftige Termine des ausgewählten Patienten drucken");
			}
			@Override
			public void run(){
				Patient patient = GlobalEvents.getSelectedPatient();
				if (patient != null) {
					Query<Termin> qbe = new Query<Termin>(Termin.class);
					qbe.add("Wer", "=", patient.getId());
					qbe.add("deleted", "<>", "1");
					qbe.add("Tag", ">=", new TimeTool().toString(TimeTool.DATE_COMPACT));
					qbe.orderBy(false, "Tag", "Beginn");
					java.util.List<Termin> list=qbe.execute();
					if (list != null) {
						boolean directPrint = Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT,
								PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT);

						TermineDruckenDialog dlg = new TermineDruckenDialog(getViewSite().getShell(), list.toArray(new Termin[0]));
						if (directPrint) {
							dlg.setBlockOnOpen(false);
							dlg.open();
							if (dlg.doPrint()) {
								dlg.close();
							} else {
								SWTHelper.alert("Fehler beim Drucken",
										"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
							}
						} else {
							dlg.setBlockOnOpen(true);
							dlg.open();
						}
					}
				}
			}
		};
		exportAction=new Action("Agenda exportieren"){
			{
				setToolTipText("Termine eines Bereichs exportieren");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_GOFURTHER));
			}
			@Override
			public void run(){
				ICalTransfer ict=new ICalTransfer();
				ict.doExport(agenda.getActDate(), agenda.getActDate(), agenda.getActResource());
			}
		};
		
		importAction=new Action("Termine importieren"){
			{
				setToolTipText("Termine aus einer iCal-Datei importieren");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_IMPORT));
			}
			@Override
			public void run(){
				ICalTransfer ict=new ICalTransfer();
				ict.doImport(agenda.getActResource());
			}
		};
		final IAction bereichMenu=new Action(Messages.TagesView_bereich,Action.AS_DROP_DOWN_MENU){ 
			Menu mine;
			{
				setToolTipText(Messages.TagesView_selectBereich); 
				setMenuCreator(new IMenuCreator(){

					public void dispose() {
						mine.dispose();
					}

					public Menu getMenu(Control parent) {
						mine=new Menu(parent);
						fillMenu();
						return mine;
					}

					public Menu getMenu(Menu parent) {
						mine=new Menu(parent);
						fillMenu();
						return mine;
					}});
			}
			private void fillMenu(){
				String[] sMandanten=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_praxis).split(","); 
				for(String m:sMandanten){
					MenuItem it=new MenuItem(mine,SWT.NONE);
					it.setText(m);
					it.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem mi=(MenuItem)e.getSource();
							agenda.setActResource(mi.getText());
							refresh();
						}
						
					});
				}
			}
			
		};
		
		IMenuManager mgr=getViewSite().getActionBars().getMenuManager();
		mgr.add(bereichMenu);
		mgr.add(dayLimitsAction);
		//mgr.add(newViewAction);
		mgr.add(exportAction);
		mgr.add(importAction);
		mgr.add(printAction);
		mgr.add(printPatientAction);
	}


	
}
