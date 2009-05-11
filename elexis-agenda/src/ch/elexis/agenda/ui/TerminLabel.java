/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Sponsoring:
 * 	 mediX Notfallpaxis, diepraxen Stauffacher AG, Zürich
 * 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TerminLabel.java 5290 2009-05-11 17:37:52Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.actions.Activator;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.Termin;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;

public class TerminLabel extends Composite {
	private Label lbl;
	private Termin t;
	private int column;
	ProportionalSheet parent;
	Activator agenda = Activator.getDefault();

	TerminLabel(ProportionalSheet parent) {
		super(parent, SWT.BORDER);
		this.parent = parent;
		GridLayout gl = new GridLayout();
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		setLayout(gl);
		lbl = new Label(this, SWT.WRAP);

		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				agenda.setActDate(t.getDay());
				new TerminDialog(t).open();
				refresh();
			}

			@Override
			public void mouseUp(MouseEvent e) {
				agenda.dispatchTermin(t);
				super.mouseUp(e);
			}

		});
		new PersistentObjectDragSource2(this,
				new PersistentObjectDragSource2.Draggable() {

					public List<PersistentObject> getSelection() {
						System.out.println("Dragging");
						ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>(
								1);
						ret.add(GlobalEvents.getInstance().getSelectedObject(
								Termin.class));
						return ret;
					}
				});
		lbl.setMenu(parent.getContextMenuManager().createContextMenu(lbl));

	}

	void set(Termin t, int col){
		this.t=t;
		this.column=col;
	}
	TerminLabel(ProportionalSheet parent, Termin trm, int col) {
		this(parent);
		set(trm,col);
	}

	
	int getColumn() {
		return column;
	}

	Termin getTermin() {
		return t;
	}

	void refresh() {
		Color back = Plannables.getTypColor(t);
		lbl.setBackground(back);
		// l.setBackground(Desk.getColor(Desk.COL_GREY20));
		lbl.setForeground(SWTHelper.getContrast(back));

		// l.setForeground(Plannables.getStatusColor(t));
		StringBuilder sb = new StringBuilder();
		sb.append(t.getLabel()).append("\n").append(t.getGrund());
		lbl.setText(t.getTitle());
		lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lbl.setToolTipText(sb.toString());
		layout();
	}
}
