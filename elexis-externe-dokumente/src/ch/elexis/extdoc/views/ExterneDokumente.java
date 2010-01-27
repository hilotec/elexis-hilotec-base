/*******************************************************************************
 * Copyright (c) 2006-2010, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    G. Weirich - small changes to follow API changes
 *    
 *  $Id: ExterneDokumente.java 5970 2010-01-27 16:43:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.extdoc.views;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.BackgroundJob;
import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEventDispatcher;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.extdoc.dialogs.FileEditDialog;
import ch.elexis.extdoc.dialogs.VerifierDialog;
import ch.elexis.extdoc.preferences.PreferenceConstants;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;


/**
 * Diese Ansicht zeigt externe Dokumente an. Die Dokumente
 * liegen in einem Verzeichnis im Dateisystem. Dieses Verzeichnis
 * kann in den Einstellungen angegeben werden.
 * Falls ein Patient ausgewaehlt ist, wird nach einem bestimmten
 * Schema nach diesem Patienten gefiltert.
 */

// TODO datum

public class ExterneDokumente extends ViewPart implements IActivationListener {
	//private static final String NONE = "Keine Dokumente";

	// Erwartete Anzahl Dokumente falls noch nicht bekannt
	private static final int DEFAULT_SIZE = 1;
	
	private Button path1CheckBox;
	private Button path2CheckBox;
	private Button path3CheckBox;
	
	private String[] paths = {null, null, null};
	
	/*
	private Combo pathCombo;
	*/
	private TableViewer viewer;
	private Action doubleClickAction;
	private Action openAction;
	private Action editAction;
	private Action renameAction;
	private Action deleteAction;
	private Action verifyAction;
	
	private Patient actPatient;
	/*
	private String actPath = null;
	*/
	
	// work-around to get the job
	// TODO cleaner design
	BackgroundJob globalJob;
	
	// letzte bekannte Anzahl Dokumente (fuer getSize())
	int lastSize = DEFAULT_SIZE;
	
	private Log log = Log.get("Externe Dokumente");
	
	private ElexisEventListenerImpl eeli_pat=new ElexisEventListenerImpl(Patient.class,ElexisEvent.EVENT_SELECTED){
		@Override
		public void runInUi(ElexisEvent ev){
			actPatient = (Patient) ev.getObject();
			refresh();
		}
	};
	class DataLoader extends BackgroundJob {
		public DataLoader(String jobName) {
			super(jobName);
		}
		
		/**
		 * Filter fuer die folgende Festlegung:
		 * 
		 *  - Die ersten 6 Zeichen des Nachnamens. Falls kuerzer, mit Leerzeichen aufgefuellt
		 *  - Der Vorname (nur der erste, falls es mehrere gibt)
		 *  - Bezeichnung, durch ein Leerzeichen getrennt. 
		 */
		class MyFilenameFilter implements FilenameFilter {
			private Pattern pattern;
			
			MyFilenameFilter(String lastname, String firstname) {
				// only use first part of firstname
				firstname = firstToken(firstname);
				
				// remove dashes, underscores and spaces
				lastname = cleanName(lastname);
				firstname = cleanName(firstname);
				
				String shortLastname;
				
				if (lastname.length() >= 6) {
					// Nachname ist lang genug
					shortLastname = lastname.substring(0, 6);
				} else {
					// Nachname ist zu kurz, mit Leerzeichen auffuellen
					StringBuilder sb = new StringBuilder();
					sb.append(lastname);
					while (sb.length() < 6) {
						sb.append(" ");
					}
					shortLastname = sb.toString();
				}
				
				String regex = "^" + shortLastname + firstname + ".*$";
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			}
			
			public boolean accept(File dir, String name) {
				Matcher matcher = pattern.matcher(name);
				return matcher.matches();
			}
			
			private String cleanName(String name) {
				String cleanName = name.replaceAll("[-_\\p{Space}]+", "");
				return cleanName;
			}
			
			private String firstToken(String text) {
				String firstToken = text.replaceFirst("[-_\\p{Space}].*", "");
				return firstToken;
			}
		}

	    public IStatus execute(IProgressMonitor monitor) {
	    	if (actPatient != null) {
		    	List<File> list = new ArrayList<File>();
	    		
		    	list.addAll(loadFiles(paths[0]));
		    	list.addAll(loadFiles(paths[1]));
		    	list.addAll(loadFiles(paths[2]));
	    		
	    		if (list.size() > 0) {
	    			result = list;
	    		} else {
	    			result = "Keine Dateien gefunden";
	    		}
			} else {
				result = "Kein Patient ausgewählt";
			}
	    	
	    	return Status.OK_STATUS;
	    }
	    
	    /**
	     * Load files
	     * @param path the path from where to load files; may be null
	     * @return a list of files (maybe empty)
	     */
	    private List<File> loadFiles(String path) {
	    	List<File> list = new ArrayList<File>();
	    	
			if (!StringTool.isNothing(path)) {
				File mainDirectory = new File(path);
				if (mainDirectory.isDirectory()) {
					MyFilenameFilter filter = new MyFilenameFilter(actPatient.getName(), actPatient.getVorname());
					File[] files = mainDirectory.listFiles(filter);
					for (File file : files) {
						list.add(file);
					}
				}
			}
			
			return list;
	    }

	    public int getSize() {
	    	return lastSize;
	    }
	}

	class ViewContentProvider implements IStructuredContentProvider, BackgroundJobListener {
		BackgroundJob job;
		
		public ViewContentProvider() {
			job = new DataLoader("Externe Dokumente");
			globalJob = job;
	    	if(JobPool.getJobPool().getJob(job.getJobname())==null){
	    		JobPool.getJobPool().addJob(job);
	    	}
	    	job.addListener(this);

		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
	    	job.removeListener(this);
		}
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object parent) {
	        Object result = job.getData();
	        if(result == null){
	        	JobPool.getJobPool().activate(job.getJobname(),Job.LONG);
	            return new String[]{"Lade..."};
	        } else {
	        	if (result instanceof List) {
	        		return ((List) result).toArray();
	        	} else if (result instanceof String) {
	        		return new Object[] {result};
	        	} else {
	        		return null;
	        	}
	        }
		}
		
	    public void jobFinished(BackgroundJob j)
	    {
	        //int size=((Object[])j.getData()).length;
	        viewer.refresh(true);
	        
	    }
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final int DATE_COLUMN = 0;
		private static final int NAME_COLUMN = 1;
		
		public String getColumnText(Object obj, int index) {
			switch (index) {
			case DATE_COLUMN:
				return getDate(obj);
			case NAME_COLUMN:
				return getText(obj);
			}
			return "";
		}
		
		public String getText(Object obj) {
			if (obj instanceof File) {
				File file = (File) obj;
				return file.getName();
			} else if (obj instanceof String) {
				return obj.toString();
			} else {
				return "";
			}
		}

		public String getDate(Object obj) {
			if (obj instanceof File) {
				File file = (File) obj;
				long modified = file.lastModified();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(modified);
				TimeTool tl = new TimeTool(cal.getTimeInMillis());
				String modifiedTime = tl.toString(TimeTool.DATE_ISO) + " " + tl.toString(TimeTool.TIME_SMALL);
				return modifiedTime;
			} else {
				return "";
			}
		}

		public Image getColumnImage(Object obj, int index) {
			switch (index) {
			case NAME_COLUMN:
				return getImage(obj);
			}
			return null;
		}
		
		public Image getImage(Object obj) {
			if (!(obj instanceof File)) {
				return null;
			}
			
			File file = (File) obj;
			if (file.isDirectory()) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
		}
	}
	
	class TimestampComparator extends ViewerComparator {
	    public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null) {
				return 1;
			}
			if (e2 == null) {
				return -1;
			}

			File file1 = (File) e1;
			File file2 = (File) e2;
			
			long modified1 = file1.lastModified();
			long modified2 = file2.lastModified();
			
			if (modified1 < modified2) {
				return -1;
			} else if (modified1 > modified2) {
				return 1;
			} else {
				return 0;
			}

	    }
	}

	/**
	 * The constructor.
	 */
	public ExterneDokumente() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
		
		Composite topArea = new Composite(parent, SWT.NONE);
		topArea.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		topArea.setLayout(new GridLayout());

		Composite bottomArea = new Composite(parent, SWT.NONE);
		bottomArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottomArea.setLayout(new GridLayout());
		
		// path list
		
		String item1 = Hub.localCfg.get(PreferenceConstants.BASIS_PFAD, "");
		String item2 = Hub.localCfg.get(PreferenceConstants.BASIS_PFAD2, "");
		String item3 = Hub.localCfg.get(PreferenceConstants.BASIS_PFAD3, "");

		// check boxes
		
		Composite pathArea = new Composite(topArea, SWT.NONE);
		pathArea.setLayout(new GridLayout(3, false));
		
		SelectionAdapter checkBoxListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		};
		
		path1CheckBox = new Button(pathArea, SWT.CHECK);
		path1CheckBox.setText(item1);
		path1CheckBox.setSelection(true);
		paths[0] = item1;
		path1CheckBox.addSelectionListener(checkBoxListener);
		
		
		path2CheckBox = new Button(pathArea, SWT.CHECK);
		path2CheckBox.setText(item2);
		path2CheckBox.addSelectionListener(checkBoxListener);

		path3CheckBox = new Button(pathArea, SWT.CHECK);
		path3CheckBox.setText(item3);
		path3CheckBox.addSelectionListener(checkBoxListener);

		// combo box

		/*
		pathCombo = new Combo(topArea, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		pathCombo.add(item1);
		pathCombo.add(item2);
		actPath = item1;
		pathCombo.setText(actPath);
		pathCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				actPath = pathCombo.getText();
				refresh();
			}
		});
		*/

		// table
		
		viewer = new TableViewer(bottomArea, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		
		Table table = viewer.getTable();
		
		
		table.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        TableColumn tc;

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Datum");
        tc.setWidth(120);
        tc.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                        // TODO sort by Datum
                }
        });

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(400);
        tc.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                        // TODO sort by Name
                }
        });
        
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setComparator(new TimestampComparator());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// Welcher Patient ist im aktuellen WorkbenchWindow selektiert?
		actPatient=(Patient)ElexisEventDispatcher.getSelected(Patient.class);
		GlobalEventDispatcher.addActivationListener(this,this);
		
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ch.elexis.extdoc.views.ExterneDokumente.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openAction);
		manager.add(renameAction);
		manager.add(editAction);
		manager.add(verifyAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openAction);
		manager.add(renameAction);
		manager.add(editAction);
		manager.add(deleteAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openAction);
		manager.add(editAction);
	}

	private void makeActions() {
		openAction = new Action() {
			public void run() {
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection != null) {
					Object element = selection.getFirstElement();
					if (element instanceof File) {
						File file = (File) element;
						String path = file.getAbsolutePath();
						
						Program.launch(path);
					}
				}
			}
		};
		openAction.setText("Öffnen");
		openAction.setToolTipText("Datei öffnen");
		
		doubleClickAction = new Action() {
			public void run() {
				openAction.run();
			}
		};
		
		editAction = new Action() {
			public void run() {
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection != null) {
					Object element = selection.getFirstElement();
					if (element instanceof File) {
						openFileEditorDialog((File) element);
					}
				}
			}
		};
		editAction.setText("Eigenschaften");
		editAction.setToolTipText("Datei umbenennen oder Zeit der letzten Änderung setzen");
		editAction.setActionDefinitionId(GlobalActions.PROPERTIES_COMMAND);
		GlobalActions.registerActionHandler(this, editAction);
		
		deleteAction = new Action() {
			public void run() {
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection != null) {
					Object element = selection.getFirstElement();
					if (element instanceof File) {
						File file = (File) element;
						
						if (SWTHelper.askYesNo("Dokument löschen", "Soll das Dokument " + file.getName() + " wirklich gelöscht werden?"
								+ " (Achtung: Diese Aktion kann nicht rückgängig gemacht werden!)")) {
							
							log.log("Datei Löschen: " + file.getAbsolutePath(), Log.INFOS);
							file.delete();
							refresh();
						}
					}
				}
			}
		};
		deleteAction.setText("Löschen");
		deleteAction.setToolTipText("Datei löschen");
		deleteAction.setActionDefinitionId(GlobalActions.DELETE_COMMAND);
		GlobalActions.registerActionHandler(this, deleteAction);
		
		renameAction = new Action() {
			public void run() {
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection != null) {
					Object element = selection.getFirstElement();
					if (element instanceof File) {
						openFileEditorDialog((File) element);
					}
				}
			}
		};
		renameAction.setText("Datei umbenennen");
		renameAction.setToolTipText("Datei umbenennen");
		renameAction.setActionDefinitionId(GlobalActions.RENAME_COMMAND);
		GlobalActions.registerActionHandler(this, renameAction);

		verifyAction = new Action() {
			public void run() {
				new VerifierDialog(getViewSite().getShell(), actPatient).open();
				
				// files may have been renamed
				refresh();
			}
		};
		verifyAction.setText("Dateien überprüfen");
		verifyAction.setToolTipText("Überprüfen, ob alle Dateien einem Patienten zugeordnet werden können");
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void refresh() {
		paths[0] = null;
		paths[1] = null;
		paths[2] = null;
		
		if (path1CheckBox.getSelection()) {
			paths[0] = path1CheckBox.getText();
		}
		if (path2CheckBox.getSelection()) {
			paths[1] = path2CheckBox.getText();
		}
		if (path3CheckBox.getSelection()) {
			paths[2] = path3CheckBox.getText();
		}
		
		globalJob.invalidate();
		viewer.refresh(true);
	}

	/*
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Externe Dokumente",
			message);
	}
	*/
	
	private void openFileEditorDialog(File file) {
		FileEditDialog fed = new FileEditDialog(getViewSite().getShell(), file); 
		fed.open();
		refresh();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * Wichtig! Alle Listeners, die eine View einhängt, müssen in dispose() wieder ausgehängt werden.
	 * Sonst kommt es zu Exceptions, wenn der Anwender eine View schliesst und später ein Objekt
	 * selektiert.
	 */
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this,this);
	}
	
	// Die Methode des SelectionListeners
	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
		}
	}

	
	// Die  beiden Methoden des ActivationListeners
	/**
	 * Die View wird aktiviert (z.B angeklickt oder mit Tab)
	 */
	public void activation(boolean mode) {
		/* Interessiert uns nicht */
	}

	/**
	 * Die View wird sichtbar (mode=true). Immer dann hängen wir unseren SelectionListener ein. 
	 * (Benutzeraktionen interessieren uns ja nur dann, wenn wir etwas damit machen 
	 * müssen, also sichtbar sind. Im unsichtbaren Zustand würde das Abfangen von 
	 * SelectionEvents nur unnötig Ressourcen verbrauchen.
	 * Aber weil es ja sein könnte, dass der Anwender, während wir im Hintergrund waren, 
	 * etliche Aktionen durchgefürt hat, über die wir jetzt nicht informiert sind, 
	 * "simulieren" wir beim Sichtbar-Werden gleich einen selectionEvent, um uns zu infomieren,
	 * welcher Patient jetzt gerade selektiert ist.
	 * 
	 * Oder die View wird unsichtbar (mode=false). Dann hängen wir unseren SelectionListener aus
	 * und faulenzen ein wenig.
	 */
	public void visible(boolean mode) {
		if(mode==true){
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
			eeli_pat.catchElexisEvent(new ElexisEvent(ElexisEventDispatcher.getSelectedPatient(),Patient.class,ElexisEvent.EVENT_SELECTED));
		}else{
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}
	}
}
