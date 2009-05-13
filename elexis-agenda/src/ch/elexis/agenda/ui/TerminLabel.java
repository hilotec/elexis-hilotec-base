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
 *  $Id: TerminLabel.java 5294 2009-05-13 15:25:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
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
	private Composite state;
	ProportionalSheet parent;
	Activator agenda = Activator.getDefault();

	TerminLabel(ProportionalSheet parent) {
		super(parent, SWT.BORDER);
		this.parent = parent;
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		setLayout(gl);
		lbl = new Label(this, SWT.WRAP);
		state = new Composite(this, SWT.NONE);
		state.setLayoutData(new GridData());
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				agenda.setActDate(t.getDay());
				agenda.setActResource(t.getBereich());
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

	void set(Termin t, int col) {
		this.t = t;
		this.column = col;
	}

	TerminLabel(ProportionalSheet parent, Termin trm, int col) {
		this(parent);
		set(trm, col);
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
		
		int lx = parent.left_offset
				+ (int) Math.round(getColumn()
						* (parent.widthPerColumn + parent.padding));
		int ly = (int) Math.round(t.getBeginn() * parent.ppm);
		int lw = (int) Math.round(parent.widthPerColumn);
		int lh = (int) Math.round(t.getDauer() * parent.ppm);
		setBounds(lx, ly, lw, lh);
		GridData gd=(GridData)state.getLayoutData();
		Point s=lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		gd.minimumWidth=10;
		gd.widthHint=10;
		gd.heightHint=lh;
		state.setBackground(Plannables.getStatusColor(t));
		layout();
	}
}
