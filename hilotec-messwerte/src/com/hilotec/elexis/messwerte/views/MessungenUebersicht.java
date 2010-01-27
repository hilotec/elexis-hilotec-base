/*******************************************************************************
 * Copyright (c) 2009-2010, A. Kaufmann and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - copied from befunde-Plugin and adapted to new data structure 
 *    G. Weirich - adapted to Eventhandling API Change in 2.1
 *    
 * $Id: MessungenUebersicht.java 5970 2010-01-27 16:43:04Z rgw_ch $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.views;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListener;
import ch.elexis.data.Patient;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;

import com.hilotec.elexis.messwerte.data.Messung;
import com.hilotec.elexis.messwerte.data.MessungKonfiguration;
import com.hilotec.elexis.messwerte.data.MessungTyp;
import com.hilotec.elexis.messwerte.data.Messwert;
import com.hilotec.elexis.messwerte.data.typen.IMesswertTyp;

/**
 * View fuer Uebersicht ueber alle Messungen des aktuellen Patienten
 * 
 * @author Antoine Kaufmann
 * 
 */
public class MessungenUebersicht extends ViewPart implements
		ElexisEventListener {
	private MessungKonfiguration config;
	private ScrolledForm form;
	private ArrayList<MessungstypSeite> seiten;
	private CTabFolder tabsfolder;

	private Action neuAktion;
	private Action editAktion;
	private Action loeschenAktion;

	public MessungenUebersicht() {
		config = MessungKonfiguration.getInstance();
		seiten = new ArrayList<MessungstypSeite>();
	}

	/**
	 * Ein einzelner Tab fuer einen bestimmten Messungstyp. In diesem wird dann
	 * eine Tabelle mit allen Messungen des aktuell ausgewaehlten Patienten
	 * angezeigt.
	 * 
	 * @author Antoine Kaufmann
	 * 
	 */
	class MessungstypSeite extends Composite {
		private MessungTyp typ;
		private Table table;
		private TableColumn cols[];
		private Patient patient;

		/**
		 * Einzelnes Tab fuer einen bestimmten Typ Messungen mit Tabelle der
		 * Messwerte.
		 * 
		 * @param dt
		 *            Typ der Messungen
		 */
		public MessungstypSeite(Composite parent, MessungTyp dt) {
			super(parent, SWT.NONE);
			typ = dt;

			parent.setLayout(new FillLayout());
			setLayout(new GridLayout());

			table = new Table(this, SWT.FULL_SELECTION | SWT.V_SCROLL);
			table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			cols = new TableColumn[typ.getMesswertTypen().size() + 1];

			// Spalten anlegen
			int i = 0;
			cols[i] = new TableColumn(table, SWT.NONE);
			cols[i].setText("Datum");
			cols[i].setWidth(80);
			i++;
			for (IMesswertTyp dft : typ.getMesswertTypen()) {
				cols[i] = new TableColumn(table, SWT.NONE);
				if (dft.getUnit().equals("")) {
					cols[i].setText(dft.getTitle());
				} else {
					cols[i]
							.setText(dft.getTitle() + " [" + dft.getUnit()
									+ "]");
				}
				cols[i].setWidth(80);
				i++;
			}

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(final MouseEvent e) {
					editAktion.run();
				}
			});

			ViewMenus menu = new ViewMenus(getViewSite());
			menu.createControlContextMenu(table, editAktion, loeschenAktion,
					neuAktion);
		}

		/**
		 * Seite neu zeichnen (damit veraenterte Daten aktualisiert werden)
		 */
		public void aktualisieren() {
			table.removeAll();

			if (patient == null) {
				return;
			}

			for (Messung messung : Messung.getPatientMessungen(patient, typ)) {
				TableItem ti = new TableItem(table, SWT.NONE);
				ti.setData(messung);

				int i = 0;
				ti.setText(i++, messung.getDatum());
				for (Messwert mwrt : messung.getMesswerte()) {
					ti.setText(i++, mwrt.getDarstellungswert());
				}
			}
		}

		/**
		 * Aktuell angezeigten Patienten festlegen. Dabei wird die Ansicht neu
		 * aufgebaut, da die Daten meist aendern.
		 */
		public void setCurPatient(Patient p) {
			patient = p;
			aktualisieren();
		}

		/**
		 * @return Messungstyp der von dieser Seite angezeigt wird
		 */
		public MessungTyp getTyp() {
			return typ;
		}
	}

	/**
	 * Aktionen fuer Menuleiste und Kontextmenu initialisieren
	 */
	private void erstelleAktionen() {
		neuAktion = new Action("Neue Messung") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
				setToolTipText("Eine neue Messung hinzufügen");
			}

			public void run() {
				Patient p = ElexisEventDispatcher.getSelectedPatient();
				if (p == null) {
					return;
				}

				CTabItem tab = tabsfolder.getSelection();
				MessungstypSeite mts = (MessungstypSeite) tab.getControl();
				Messung messung = new Messung(p, mts.getTyp());
				MessungBearbeiten dialog = new MessungBearbeiten(getSite()
						.getShell(), messung);
				if (dialog.open() != Dialog.OK) {
					messung.delete();
				}
				aktualisieren();
			}
		};

		editAktion = new Action("Messung editieren") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText("Die gewählte Messung editieren");
			}

			public void run() {
				CTabItem ci = tabsfolder.getSelection();
				if (ci == null) {
					return;
				}

				MessungstypSeite seite = (MessungstypSeite) ci.getControl();
				TableItem[] tableitems = seite.table.getSelection();
				if (tableitems.length == 1) {
					Messung messung = (Messung) tableitems[0].getData();
					MessungBearbeiten dialog = new MessungBearbeiten(getSite()
							.getShell(), messung);
					if (dialog.open() == Dialog.OK) {
						aktualisieren();
					}
				}
			}
		};

		loeschenAktion = new Action("Messung löschen") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText("Die gewählte Messung löschen");
			}

			public void run() {
				CTabItem ci = tabsfolder.getSelection();
				if (ci == null) {
					return;
				}

				MessungstypSeite seite = (MessungstypSeite) ci.getControl();
				TableItem[] tableitems = seite.table.getSelection();
				if ((tableitems.length > 0)
						&& SWTHelper
								.askYesNo("Messung(en) löschen",
										"Wollen Sie diese Messung(en) wirklich unwiderruflich löschen?")) {
					for (TableItem ti : tableitems) {
						Messung messung = (Messung) ti.getData();
						messung.delete();
					}
					aktualisieren();
				}
			}
		};
	}

	/**
	 * Menuleiste generieren
	 */
	private ViewMenus erstelleMenu(IViewSite site) {
		ViewMenus menu = new ViewMenus(site);
		erstelleAktionen();
		menu.createToolbar(neuAktion, editAktion, loeschenAktion);
		return menu;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		form = Desk.getToolkit().createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body = form.getBody();
		body.setLayout(new FillLayout());
		tabsfolder = new CTabFolder(body, SWT.NONE);
		tabsfolder.setLayout(new FillLayout());

		erstelleMenu(getViewSite());

		for (MessungTyp t : config.getTypes()) {
			CTabItem cti = new CTabItem(tabsfolder, SWT.NONE);
			cti.setText(t.getTitle());
			MessungstypSeite mts = new MessungstypSeite(tabsfolder, t);
			seiten.add(mts);
			cti.setControl(mts);
		}
		tabsfolder.setSelection(0);

		setCurPatient(ElexisEventDispatcher.getSelectedPatient());
		ElexisEventDispatcher.getInstance().addListeners(this);
	}

	@Override
	public void dispose() {
		ElexisEventDispatcher.getInstance().removeListeners(this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * Aktuell ausgewaehlten Patient festlegen
	 * 
	 * @param patient
	 *            Ausgewaehlter Patient oder null falls keiner ausgewaehlt ist.
	 */
	private void setCurPatient(Patient patient) {
		if (patient == null) {
			form.setText("Kein Patient ausgewaehlt");
		} else {
			form.setText(patient.getLabel());
		}

		// Tabs benachrichtigen
		for (MessungstypSeite mts : seiten) {
			mts.setCurPatient(patient);
		}
	}

	/**
	 * Alle Seiten aktualisieren
	 */
	private void aktualisieren() {
		for (MessungstypSeite mts : seiten) {
			mts.aktualisieren();
		}
	}

	/**
	 * Dieser Event-Handler ist dafuer zustaendig, uns ueber den aktuell
	 * ausgewaehlten Patienten auf dem Laufenden zu halten, damit die Ansicht
	 * aktualisiert wird.
	 */

	public void catchElexisEvent(final ElexisEvent ev) {
		Desk.asyncExec(new Runnable() {

			public void run() {
				if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
					setCurPatient((Patient) ev.getObject());
				} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
					setCurPatient(null);

				}
			}
		});
	}

	private final ElexisEvent eetmpl = new ElexisEvent(null, Patient.class,
			ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED);

	public ElexisEvent getElexisEventFilter() {
		return eetmpl;
	}
}
