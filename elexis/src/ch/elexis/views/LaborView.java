/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: LaborView.java 2444 2007-05-28 18:50:44Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.*;
import ch.elexis.dialogs.DateSelectorDialog;
import ch.elexis.dialogs.DisplayTextDialog;
import ch.elexis.util.*;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Anzeige von Laboritems und Anzeige udn Eingabemöglichkeit von Laborwerten.
 * @author gerry
 *
 * Der Algorithmus geht so: Zuerst werden alle Laboritems eingesammelt und gemäss ihren Gruppen
 * und Prioritäten sortiert (nur beim create)
 * Beim Einlesen eines neuen Patienten werden zunächst alle Daten gesammelt, an denen für diesen Patienten 
 * Laborwerte vorliegen. Diese werden nach Alter sortiert und mit den jeweiligen Laborwerten zusammengefasst.
 * Jeweils NUMCOLUMNS Daten werden auf einer Anzeigeseite angezeigt. Der Anwender kann auf den Seiten blättern, aber
 * es werden alle Laborwerte des aktuellen Patienten im Speicher gehalten.
 */
public class LaborView extends ViewPart implements SelectionListener, ActivationListener, BackingStoreListener, ISaveablePart2 {
	public static final String ID="ch.elexis.Labor";
	private static Log log=Log.get("LaborView");
	
	final static int NUMCOLUMNS=7;			// Pro Seite angezeigte Laborspalten 
	final static int COL_OFFSET=2;			// Für Information benötigte Spalten
	final static Color COL_PATHOLOGIC=Desk.theColorRegistry.get(Desk.COL_RED);
	final static Color COL_REMARK=Desk.theColorRegistry.get(Desk.COL_BLUE);
	final static Color COL_BACKGND=Desk.theColorRegistry.get(Desk.COL_WHITE);
	int	actPage;							// Aktuell angezeigte Seite
	int firstColumn, lastColumn;			// Erste und letzte Datumspalte der aktuellen Seite
	Patient actPatient;						// Aktuell ausgewählter Patient

	/* Tabelle */
	Table table;
	TableColumn[] columns;
	TableItem[] rows;
	TableCursor cursor;
	ControlEditor editor;
	
	private Hashtable<String,List<LabItem>> hGroups;	//Gruppen von Laboritems
	private Hashtable<String,Integer> hLabItems;	    //Mapping von Laboritems auf Tabellenzeilen
	List<String> lGroupNames;							//Alphabetische Gruppenliste
	//List<LabResult> lResults;
	String[] sDaten;									// Sortierte Liste aller Daten von Laborresultaten
	Hashtable<String,Integer> hDaten;					// Mapping von Datum auf Tabellenspalten
	
	private Action fwdAction, backAction, printAction, importAction, xmlAction, newAction, setStateAction;
	private ViewMenus menu;
	private FormToolkit tk=Desk.theToolkit;
	private Form form;
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		form=tk.createForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		
		table=new Table(body,SWT.FULL_SELECTION|SWT.LEFT|SWT.V_SCROLL|SWT.H_SCROLL);
		table.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		table.addListener(SWT.PaintItem, new Listener(){

			private void paintCell(String text, Event event, Color foregnd, Color backgnd){
				Point size = event.gc.textExtent(text);
				int offset1= Math.max(0,(event.width-size.x)/2);
				int offset2 = Math.max(0, (event.height - size.y) / 2);
				GC gc=event.gc;
				gc.setForeground(backgnd);
				gc.fillRectangle(event.x, event.y, event.width, event.height);
				gc.setForeground(foregnd);
				event.gc.drawText(text, event.x+offset1, event.y + offset2, true);
			}
			public void handleEvent(Event event) {
		          TableItem item = (TableItem) event.item;
		          String text = item.getText(event.index);
		          LabItem it=(LabItem)item.getData("Item");
		          if(it!=null){
		        	  LabResult[] lrs=(LabResult[])item.getData("Values");
		        	  if(lrs!=null){
		        		  int screenIdx=event.index-COL_OFFSET;
		        		  if(screenIdx>=0 && screenIdx<lrs.length){
		        			  LabResult lr=lrs[screenIdx];
							  if(lr!=null){
							  if( lr.isFlag(LabResult.PATHOLOGIC)){
								  paintCell(text,event,COL_PATHOLOGIC,COL_BACKGND);
								}
							  if(lr.getComment().length()>0){
								  paintCell(text,event,COL_REMARK,COL_BACKGND);
							  }
							 }
		        		  }
		        	  }
		          }
			}
			
		});
		
		cursor=new TableCursor(table,SWT.NONE);
		editor=new ControlEditor(cursor);
		editor.grabHorizontal=true;
		editor.grabVertical=true;
		
		/*	Tastatursteuerung für die Tabelle: Druck auf Eingabetaste lässt die Zelle editieren, sofern 
		 *  sie auf einem editierbaren Feld ist. Wenn sie nicht auf einem editierbaren Feld ist, wird
		 *  der stattdessen Cursor eine Zeile nach unten bewegt.
		 *  Druck auf irgendeine Zahl- oder Buchstabentaste lässt die Zelle editieren, wenn sie editierbar ist.
		 *  Editierbar ist eine Zelle dann, wenn sie sich a) in einer Spalte mit einem Datum im Kopf befindet, 
		 *  und b) sich in einer Zeile mit einem LaborItem am Anfang befindet.
		 */
		cursor.addSelectionListener(new SelectionAdapter() {
			// Tabellenauswahl soll dem Cursor folgen
			public void widgetSelected(SelectionEvent e) {
				table.setSelection(new TableItem[] {cursor.getRow()});
			}
			// Eingabetaste
			public void widgetDefaultSelected(SelectionEvent e){

				TableItem row = cursor.getRow();
				LabItem li=(LabItem)row.getData("Item");
				if(li==null){
					cursorDown();
					return;
				}
				int column = cursor.getColumn();
				if(columns[column].getText().matches("[0-9\\.]+")){
					doEdit(row.getText(column));
				}
				
			}
		});
		// Sonstige Taste
		cursor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				TableItem row = cursor.getRow();
				e.doit=false;
				if(row.getData("Item")==null){
					return;
				}
				if(e.character>0x30){
					StringBuilder sb=new StringBuilder();
					sb.append(e.character);
					int column = cursor.getColumn();
					if(columns[column].getText().matches("[0-9\\.]+")){
						doEdit(sb.toString());
					}
				}
			}
		});
		cursor.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				LabResult lr=actResult();
				LabItem li=lr.getItem();
				if(li.getTyp().equals(LabItem.typ.TEXT) || (lr.getComment().length()>0)){
					new DisplayTextDialog(getViewSite().getShell(),"Textbefund",li.getName(),lr.getComment()).open();
				}
				super.mouseDoubleClick(e);
			}
			
		});
		
 
		makeActions();
		menu=new ViewMenus(getViewSite());
		menu.createMenu(newAction,backAction,fwdAction,printAction,importAction,xmlAction);
		menu.createToolbar(newAction,backAction,fwdAction,printAction);
		final MenuManager mgr=new MenuManager("path");
		Menu menu=mgr.createContextMenu(cursor);
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				LabResult lr=getSelectedResult();
				if(lr!=null){
					mgr.add(setStateAction);
					setStateAction.setChecked(lr.isFlag(LabResult.PATHOLOGIC));
				}
				
			}});
		cursor.setMenu(menu);
		//menu.createControlContextMenu(cursor, setStateAction);
		rebuild();
		GlobalEvents.getInstance().addActivationListener(this,this);
		GlobalEvents.getInstance().addBackingStoreListener(this);
	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this,this);
		GlobalEvents.getInstance().removeBackingStoreListener(this);
		super.dispose();
	}
	
	public void rebuild(){
		actPage=0;
		hDaten=new Hashtable<String,Integer>();
		hGroups=new Hashtable<String,List<LabItem>>(50,0.7f);
		lGroupNames=new ArrayList<String>(20);
		showBusy(true);
		createColumns();
		loadItems();
		createRows();
		actPatient=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
		loadValues();
		showBusy(false);
	}
	LabResult actResult(){
		TableItem it=cursor.getRow();
		int idx=cursor.getColumn();
		LabItem lit=(LabItem)it.getData("Item");
		if(lit!=null){
			LabResult[] lrs=(LabResult[])it.getData("Values");
			if(lrs==null){
				lrs=new LabResult[NUMCOLUMNS];
				it.setData("Values",lrs);
			}
			return lrs[idx-COL_OFFSET];
		}
		return null;
	}
	/* Tabellenzelle editieren. CR oder Pfeil unten verlässt die Zelle mit Speichern und
	 * geht zur nächst unteren Zelle. Esc verlässt die Zelle ohne speichern
	 */
	private void doEdit(String inp){
		final Text text = new Text(cursor, SWT.NONE);
		text.setText(inp);
		text.setSelection(inp.length());
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((e.character == SWT.CR) || (e.keyCode == SWT.ARROW_DOWN)){
					TableItem it = cursor.getRow();
					int idx = cursor.getColumn();				// Spalte der Anzeige
					int idx_values=firstColumn+idx-COL_OFFSET;	// Spalte bezogen auf alle Daten
					LabItem lit=(LabItem)it.getData("Item");
					if(lit!=null){
						LabResult[] lrs=(LabResult[])it.getData("Values");
						if(lrs==null){
							lrs=new LabResult[NUMCOLUMNS];
							it.setData("Values",lrs);
						}
						LabResult lr=lrs[idx-COL_OFFSET];
						if(lr==null){
							lr=new LabResult(actPatient,new TimeTool(sDaten[idx_values]),lit,text.getText(),"");
							lrs[idx-COL_OFFSET]=lr;
						}else{
							lr.setResult(text.getText());
						}
					}
					it.setText(idx,text.getText());
					text.dispose();
					cursorDown();
					table.setFocus();
				}
				// close the text editor when the user hits "ESC"
				if (e.character == SWT.ESC) {
					text.dispose();
					table.setFocus();
				}
			}
		});
		editor.setEditor(text);
		text.setFocus();
	}
	private void cursorDown(){
		int row=table.getSelectionIndex();
		if(row>=rows.length){
			return;
		}
		cursor.setSelection(row+1,cursor.getColumn());
		table.setSelection(row+1);
		LabItem it=(LabItem)cursor.getRow().getData("Item");
		if(it==null){
			cursorDown();
		}
	}
	@Override
	public void setFocus() {
		// TODO Automatisch erstellter Methoden-Stub

	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
			actPatient=(Patient)obj;
			loadValues();
		}else if(obj instanceof Konsultation){
			Patient p=((Konsultation)obj).getFall().getPatient();
			if((actPatient==null) || (!p.equals(actPatient))){
				actPatient=p;
				loadValues();
			}
		}

	}
	
	/*
	 * Daten eines neuen Patienten einlesen
	 */
	private void loadValues(){
		hDaten.clear();
		sDaten=null;
	    if(actPatient!=null){
	    	form.setText(actPatient.getLabel());
	    	// Zuerst sehen, für wieviele Daten Laborwerte vorliegen, und diese Daten auf Index mappen
	    	// Hier müssen wir ausnahmsweise direkt auf den JdbcLink zugreifen
			Stm stm=PersistentObject.getConnection().getStatement();
			ResultSet rs=stm.query("SELECT DISTINCT Datum FROM LABORWERTE WHERE PatientID="+actPatient.getWrappedId()+" ORDER BY Datum");
			LinkedList<String> lDaten=new LinkedList<String>();
			try{
				int col=0;
				while((rs!=null) && (rs.next()==true)){
					String dat=rs.getString(1);
					lDaten.add(dat);
					hDaten.put(dat,col++);
				}
				sDaten=lDaten.toArray(new String[0]);
				loadPage(getLastPage());	
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
			// Referenzwerte je nach Geschlecht eintragen
			boolean s=(actPatient.getGeschlecht().equals("m"));
			for(int i=0;i<rows.length;i++){
				TableItem ti=rows[i];
				LabItem li=(LabItem)ti.getData("Item");
				if(li!=null){
					if(s==true){
						ti.setText(1,li.getRefM());
					}else{
						ti.setText(1,li.getRefW());
					}
				}
			}
		}else{
			form.setText("Kein Patient ausgewählt");
		}
	}

	private int getLastPage(){
		return sDaten.length/NUMCOLUMNS;
	}
	
	/*
	 * Eine Seite mit Laborwerten (=NUMCOLUMNS Spalten) einlesen
	 */
	private void loadPage(int p){
		// Zuerst prüfen, ob die angeforderte Seite gültig ist
		if(p<0){
			return;
		}
		actPage=p;
		
		// Dann alte Einträge löschen
		String[] line=new String[NUMCOLUMNS+COL_OFFSET];
		for(int i=COL_OFFSET;i<NUMCOLUMNS+COL_OFFSET;i++){
			line[i]="";
			columns[i].setText("");
		}
		// Zeilentitel und Wertelisten vorbelegen
		for(int i=0;i<rows.length;i++){
			line[0]=((String)rows[i].getData("Text"));
			rows[i].setText(line);
			rows[i].setData("Values",new LabResult[NUMCOLUMNS]);
		}
		firstColumn= (p==0) ? 0 : (p*(NUMCOLUMNS-1));
		lastColumn=firstColumn+NUMCOLUMNS-1;
		
		// Keine Anzeigbaren Daten vorhanden?
		if((sDaten.length==0) || (sDaten.length<firstColumn)){
			loadPage(p-1);
			return;
		}
		
	
		// Query für alle Laborwerte zwischen erstem und letztem Datum der aktuellen Seite
		String sBegin=sDaten[firstColumn];
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add("PatientID","=",actPatient.getId());
		qbe.add("Datum",">=",sBegin);
		
		//int numvalid=NUMCOLUMNS;						// Wieviele Spalten können tatsächlich angezeigt werden?		
		if(lastColumn<sDaten.length){
			qbe.add("Datum","<=",sDaten[lastColumn]);
		}else{
			lastColumn=sDaten.length-1;
		}
		int numvalid=lastColumn-firstColumn+1;
		List<LabResult> list=qbe.execute();

		
		// Spaltenköpfe beschriften
		TimeTool dats=new TimeTool();
		for(int i=0;i<numvalid;i++){
			String dat=sDaten[i+firstColumn];
			dats.set(dat);
			columns[COL_OFFSET+i].setText(dats.toString(TimeTool.DATE_GER));
		}
		
		// Laborresultate eintragen mithilfe der Mappings in hDaten und hLabItems
		TimeTool tt=new TimeTool();
		for(LabResult lr:list){
			LabItem lit=lr.getItem();
			if(lit==null){
				log.log("Fehlerhaftes LabResult "+lr.getId(),Log.WARNINGS);
				continue;
			}
			tt.set(lr.getDate());
			int col_values=hDaten.get(tt.toString(TimeTool.DATE_COMPACT));	// Absolute Spalte
			int col_display=col_values-firstColumn+COL_OFFSET;				// Spalte relativ zur Seite
			Integer row=hLabItems.get(lit.getId());							// Zeile für die Anzeige
			if(row==null){
				continue;
			}
			rows[row].setText(col_display,lr.getResult());					// Spalte für die Anzeige
			LabResult[] lrs=(LabResult[])rows[row].getData("Values");		// LabResult anfügen
			if(lrs==null){
				lrs=new LabResult[NUMCOLUMNS];
				rows[row].setData("Values",lrs);
			}
			lrs[col_values-firstColumn]=lr;
		}
	}
	/*
	 * Zeilen erstellen und Mappings zwischen LabItem und Zeilennummer erstellen.
	 * Jeder Gruppentitel in blauer Farbe, darunter die Gruppe, dann eine Leerzeile.
	 */
	private void createRows(){
		table.removeAll();
		hLabItems=new Hashtable<String,Integer>(50,0.75f);
		ArrayList<TableItem> lTI=new ArrayList<TableItem>(50);
		int line=0;
		for(String g:lGroupNames){
			List<LabItem> groupItems=hGroups.get(g);
			if(groupItems==null){
				log.log("Fehler bei Laborgruppe "+g,Log.ERRORS);
				continue;
			}
			TableItem ti=new TableItem(table,SWT.NONE);
			ti.setForeground(Desk.theDisplay.getSystemColor(SWT.COLOR_BLUE));
			String[] gn=g.split(" +");
			if(gn.length>1){
				ti.setText(0,gn[1]);
				ti.setData("Text",gn[1]);
			}else{
				ti.setText("? "+g+" ?");
				ti.setData("Text","? "+g+" ?");
			}
			
			lTI.add(ti);
			line+=1;
			for(LabItem it:groupItems){
				TableItem ti2=new TableItem(table,SWT.NONE);
				ti2.setData("Item",it);
				ti2.setText(it.getShortLabel());
				ti2.setData("Text",it.getShortLabel());
				lTI.add(ti2);
				hLabItems.put(it.getId(),line++);
			}
			TableItem tiSpace=new TableItem(table,SWT.NONE);
			tiSpace.setText(" ");
			tiSpace.setData("Text"," ");
			lTI.add(tiSpace);
			line+=1;
		}
		rows=lTI.toArray(new TableItem[0]);
	}
	/** 
	 * Liste der Laboritems, Gruppiert nach groups und Sequenznummer aufbauen
	 *
	 */
	@SuppressWarnings("unchecked")
	private void loadItems(){
		Query qbe=new Query(LabItem.class);
		List lItems=qbe.execute();

		for(LabItem it:(List<LabItem>)lItems){
			String group=it.getGroup();	
			List<LabItem> lGroupItems=hGroups.get(group);		// Existiert die Gruppe schon?
			if(lGroupItems==null){
				lGroupItems=new ArrayList<LabItem>();			// Wenn nein, neu erstellen
				hGroups.put(group,lGroupItems);					// und in die Groups-Hashtable einfügen
				int i=0;
				for(i=0;i<lGroupNames.size();i++){				// Dann sortiert in die Gruppenliste eintragen.
					if(group.compareTo(lGroupNames.get(i))<0){
						break;
					}
				}
				lGroupNames.add(i,group);
			}
			lGroupItems.add(it);								// Schliesslich den Item einfügen
			Collections.sort(lGroupItems);						// und die Itemliste neu sortieren
		}

	}
	
	public LabResult getSelectedResult(){
		TableItem item = cursor.getRow();
        LabItem it=(LabItem)item.getData("Item");
        if(it!=null){
      	  LabResult[] lrs=(LabResult[])item.getData("Values");
      	  if(lrs!=null){
      		  LabResult lr=lrs[cursor.getColumn()-COL_OFFSET];
      		  if(lr!=null){
      			  return lr;
      		  }
      	  }
        }
        return null;
	}
	
	private void createColumns(){
		if(columns!=null){
			for(int i=0;i<columns.length;i++){
				columns[i].dispose();
			}
			columns=null;
		}
		columns=new TableColumn[NUMCOLUMNS+COL_OFFSET];
		for(int i=0;i<NUMCOLUMNS+COL_OFFSET;i++){
			columns[i]=new TableColumn(table,SWT.LEFT);
			columns[i].setWidth(75);
			columns[i].addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					TimeTool dOld=new TimeTool();
					if(dOld.set(((TableColumn)e.getSource()).getText())==true){
						DateSelectorDialog dsl=new DateSelectorDialog(getViewSite().getShell());
						if(dsl.open()==Dialog.OK){
							TimeTool dat=dsl.getSelectedDate();
							String nDat=dat.toString(TimeTool.DATE_COMPACT);
							Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
							qbe.add("Datum","=",dOld.toString(TimeTool.DATE_COMPACT));
							qbe.add("PatientID","=",actPatient.getId());
							for(LabResult lr:qbe.execute()){
								lr.set("Datum",nDat);
							}
							loadValues();
							loadPage(actPage);
						}
					}
				}
				
			});
			
		}
		columns[0].setWidth(200);
		columns[1].setWidth(70);
		columns[0].setText("Parameter");
		columns[1].setText("Referenz");
	}

	private void makeActions(){
		fwdAction=new Action("Nächste Seite"){
			public void run(){
				loadPage(actPage+1);
			}
		};
		backAction=new Action("vorherige Seite"){
			public void run(){
				if(actPage>0){
					loadPage(actPage-1);
				}
			}
		};
		printAction=new Action("Drucken..."){
			public void run(){
				try{
				    LaborblattView lb=(LaborblattView)getViewSite().getPage().showView(LaborblattView.ID);
					Patient pat=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
					String[] headers=new String[columns.length];
					for(int i=0;i<headers.length;i++){
						headers[i]=columns[i].getText();
					}
				    lb.createLaborblatt(pat,headers,rows);
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}	
		};
		importAction=new Action("Import..."){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_IMPORT));
				setToolTipText("Laborwerte von externen Labors oder Apparaten importieren");
			}
			public void run(){
				Importer imp=new Importer(getViewSite().getShell(),"ch.elexis.LaborDatenImport");
        		imp.create();
        		imp.setMessage("Datenquellen auswählen");
        		imp.getShell().setText("Labor-Importer");
        		imp.setTitle("Import von externen Laborbefunden");
        		imp.open();
			}
		};
		xmlAction=new Action("XML export..."){
			public void run() {
				Document doc=makeXML();
				if(doc!=null){
					FileDialog fsel=new FileDialog(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
					String fname=fsel.open();
					if(fname!=null){
						try{
							FileOutputStream fout=new FileOutputStream(fname);
							OutputStreamWriter cout=new OutputStreamWriter(fout,"UTF-8");
							XMLOutputter xout=new XMLOutputter(Format.getPrettyFormat());
							xout.output(doc,cout);
							cout.close(); fout.close();
						}catch(Exception ex){
							ExHandler.handle(ex);
							SWTHelper.alert("Fehler","Konnte Datei "+fname+" nicht schreiben");
	
						}
					}
				}
			}
		};
		newAction=new Action("Neues Datum..."){
			public void run(){
				DateSelectorDialog dsd=new DateSelectorDialog(getViewSite().getShell());
				dsd.create();
				Point m=Desk.theDisplay.getCursorLocation();
				dsd.getShell().setLocation(m.x,m.y);
				if(dsd.open()==Dialog.OK){
					String date=dsd.getSelectedDate().toString(TimeTool.DATE_COMPACT);
					String[] nDates=new String[sDaten.length+1];
					System.arraycopy(sDaten,0,nDates,0,sDaten.length);
					nDates[sDaten.length]=date;
					hDaten.put(date,sDaten.length);
					sDaten=nDates;
					loadPage(getLastPage());
				}
		
			}
		};
		setStateAction=new Action("pathologisch",Action.AS_CHECK_BOX){
			public void run(){
				LabResult lr=getSelectedResult();
				lr.setFlag(LabResult.PATHOLOGIC, isChecked());
				loadPage(getLastPage());
			}
		};
		
		newAction.setImageDescriptor(Hub.getImageDescriptor("rsc/add.gif"));
		fwdAction.setImageDescriptor(Hub.getImageDescriptor("rsc/arrow_next.gif"));
		backAction.setImageDescriptor(Hub.getImageDescriptor("rsc/arrow_prev.gif"));
		printAction.setImageDescriptor(Desk.theImageRegistry.getDescriptor("print"));
		xmlAction.setImageDescriptor(Hub.getImageDescriptor("rsc/xml.gif"));
	}
	public Document makeXML(){
		Document doc=null;
		try{
			doc=new Document();
			Element r=new Element("Laborblatt");
			r.setAttribute("Erstellt",new TimeTool().toString(TimeTool.FULL_GER));
			Patient actpat=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
			if(actpat!=null){
				r.setAttribute("Patient",actpat.getLabel());
			}
			doc.setRootElement(r);
		
			Element Daten=new Element("Daten");
			for(String d:sDaten){
				Element dat=new Element("Datum");
				dat.setAttribute("Tag",d);
				Daten.addContent(dat);
			}
			r.addContent(Daten);
			for(String g:lGroupNames){
				Element eGroup=new Element("Gruppe");
				eGroup.setAttribute("Name",g);
				List<LabItem> items=hGroups.get(g);
				if(items==null){
					log.log("Ungültige Gruppe "+g,Log.WARNINGS);
					continue;
				}
				if(items.size()==0){
					continue;
				}
				for(LabItem it:items){
					Element eItem=new Element("Parameter");
					eItem.setAttribute("Name",it.getName());
					eItem.setAttribute("Kürzel",it.getKuerzel());
					eItem.setAttribute("Einheit",it.getEinheit());
					boolean hasContent=false;
					for(String t:sDaten){
						Element eResult=new Element("Resultat");
						eResult.setAttribute("Datum",t);
						eItem.addContent(eResult);
						List<LabResult> results=new LinkedList<LabResult>(); //hValues.get(t);
						for(LabResult lr:results){
							if(lr.getItem().equals(it)){
								eResult.addContent(lr.getResult());
								hasContent=true;
					
							}
						}
					}
					if(hasContent==true){
						Element ref=new Element("Referenz");
						ref.setAttribute("m",it.get("RefMann"));
						ref.setAttribute("f",it.get("RefFrauOrTx"));
						eItem.addContent(ref);
						eGroup.addContent(eItem);								
					}
				}
				if(eGroup.getContentSize()!=0){
					r.addContent(eGroup);
				}
			}
			
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
	return doc;
	}

	public void visible(boolean mode) {
		if(mode==true){
			GlobalEvents.getInstance().addSelectionListener(this);
			Patient act=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
			if((act!=null) && ((actPatient==null) ||(!act.getId().equals(actPatient.getId())))){
				actPatient=act;
				loadValues();
			}
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);	
		}
		
	}
	public void activation(boolean mode){
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
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

	public void reloadContents(Class clazz) {
		if(clazz.equals(LabItem.class)){
			Desk.theDisplay.asyncExec(new Runnable(){
				public void run() {
					rebuild();		
				}
			});
		}
	}
	
}
