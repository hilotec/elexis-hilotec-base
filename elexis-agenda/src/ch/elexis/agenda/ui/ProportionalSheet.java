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
 *  $Id: ProportionalSheet.java 5293 2009-05-13 13:37:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

import ch.elexis.actions.Activator;
import ch.elexis.actions.AgendaActions;
import ch.elexis.agenda.data.Termin;
import ch.elexis.data.Query;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ProportionalSheet extends Composite {
	static final int LEFT_OFFSET_DEFAULT = 20;
	static final int PADDING_DEFAULT = 5;

	int left_offset, padding;
	private AgendaParallel view;
	private MenuManager contextMenuManager;
	private List<TerminLabel> tlabels;
	private double ppm;
	private int sheetHeight;
	private String[] resources;
	private int textWidth;
	private double sheetWidth;
	private double widthPerColumn;
	
	
	public ProportionalSheet(Composite parent, AgendaParallel v) {
		super(parent, SWT.NO_BACKGROUND);
		view = v;
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				layout();
				recalc();
			}
		});
		addPaintListener(new TimePainter());
		addMouseListener(new MouseAdapter(){

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String resource="";
				for(int i=0;i<resources.length;i++){
					double lower=left_offset+i*(widthPerColumn+padding);
					double upper=lower+widthPerColumn;
					if(isBetween(e.x, lower, upper)){
						resource=resources[i];
						break;
					}
				}
				int minute=(int)Math.round(e.y/ppm);
				TimeTool tt=new TimeTool(Activator.getDefault().getActDate());
				int hour=minute/60;
				minute=minute-(60*hour);
				int raster=15;
				minute = ((minute + (raster >> 1)) / raster) * raster;
				tt.set(TimeTool.AM_PM,TimeTool.AM);
				tt.set(TimeTool.HOUR, hour);
				tt.set(TimeTool.MINUTE,minute);
				if(resource.length()>0){
					Activator.getDefault().setActResource(resource);
				}

				TerminDialog dlg=new TerminDialog(null);
				dlg.create();
				dlg.setTime(tt);
				if(dlg.open()==Dialog.OK){
					
					refresh();
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				System.out.println("mousdown");
				super.mouseDown(e);
			}
			
			
			
		});
		// setBackground(Desk.getColor(Desk.COL_GREEN));
		contextMenuManager = new MenuManager();
		contextMenuManager.add(AgendaActions.terminStatusAction);
		contextMenuManager.add(view.terminKuerzenAction);
		contextMenuManager.add(view.terminVerlaengernAction);
		contextMenuManager.add(view.terminAendernAction);
		contextMenuManager.add(AgendaActions.delTerminAction);
		left_offset = LEFT_OFFSET_DEFAULT;
		padding = PADDING_DEFAULT;
		
		
	}

	private boolean isBetween(int x,double lower,double upper){
		int y=(int)Math.round(lower);
		int z=(int)Math.round(upper);
		if((x>=y) && (x<=z)){
			return true;
		}
		return false;
	}
	MenuManager getContextMenuManager() {
		return contextMenuManager;
	}

	public void clear(){
		while(tlabels!=null && tlabels.size()>0){
			tlabels.remove(0).dispose();
		}
		recalc();
		
	}
	synchronized void refresh() {
		String[] resnames=view.getDisplayedResources();
		Query<Termin> qbe = new Query(Termin.class);
		String day = Activator.getDefault().getActDate().toString(TimeTool.DATE_COMPACT);
		qbe.add("Tag", "=", day);
		qbe.startGroup();
		
		for(String n:resnames){
			qbe.add("BeiWem", "=", n);
			qbe.or();
		}
		qbe.endGroup();
		List<Termin> apps=qbe.execute();
		Collections.sort(apps);
		if(tlabels==null){
			tlabels=new LinkedList<TerminLabel>();
		}
		int s=apps.size();
		while(s<tlabels.size()){
			tlabels.remove(0).dispose();
		}
		while(s>tlabels.size()){
			tlabels.add(new TerminLabel(this));
		}
		Iterator<Termin> ipi=apps.iterator();
		Iterator<TerminLabel> iptl=tlabels.iterator();
		while(ipi.hasNext()){
			TerminLabel tl=iptl.next();
			Termin t=ipi.next();
			String m=t.getBereich();
			int idx=StringTool.getIndex(resnames, m);
			if(idx==-1){
				ipi.remove();
				iptl.remove();
			}else{
				tl.set(t, idx);
			}
		}
		recalc();
	}

	
	
	void recalc() {
		ppm = AgendaParallel.getPixelPerMinute();
		sheetHeight = (int) Math.round(ppm * 60 * 24);
		ScrolledComposite sc = (ScrolledComposite) getParent();
		Point mySize = getSize();

		if (mySize.x > 0.0) {
			if (mySize.y != sheetHeight) {
				setSize(mySize.x, sheetHeight);
				sc.setMinSize(getSize());
				// sc.layout();
			}
			ScrollBar bar=sc.getVerticalBar();
			int barWidth=14;
			if(bar!=null){
				barWidth=bar.getSize().x;
			}
			resources = view.getDisplayedResources();
			int count = resources.length;
			Point textSize = SWTHelper.getStringBounds(this, "88:88");
			textWidth=textSize.x;
			left_offset = textWidth + 2;
			sheetWidth = mySize.x - 2 * left_offset - barWidth;
			widthPerColumn = sheetWidth / count;
			ColumnHeader header = view.getHeader();
			header.recalc(widthPerColumn, left_offset, padding, textSize.y);

			for (TerminLabel l:tlabels) {
				l.refresh();
				int lx = left_offset
						+ (int) Math.round(l.getColumn()
								* (widthPerColumn + padding));
				Termin t = l.getTermin();
				int ly = (int) Math.round(t.getBeginn() * ppm);
				int lw = (int) Math.round(widthPerColumn);
				int lh = (int) Math.round(t.getDauer() * ppm);
				l.setBounds(lx, ly, lw, lh);
				
			}
			sc.layout();
		}
	}

	class TimePainter implements PaintListener {

		public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			gc.fillRectangle(e.x, e.y, e.width, e.height);
			int y = 0;
			TimeTool runner = new TimeTool();
			runner.set("00:00");
			TimeTool limit = new TimeTool("23:59");
			Point textSize = gc.textExtent("88:88");
			int textwidth=textSize.x;
			
			int quarter = (int) Math.round(15.0 * AgendaParallel.getPixelPerMinute());
			int w=ProportionalSheet.this.getSize().x-5;
			int left=0;
			int right=w-textwidth;
			while (runner.isBefore(limit)) {
				gc.drawLine(left, y, w, y);		// volle Linie
				String time=runner.toString(TimeTool.TIME_SMALL);
				gc.drawText(time, 0, y + 1);
				gc.drawText(time, right,y+1);
				y += quarter;
				gc.drawLine(textwidth-3, y, textwidth, y);
				gc.drawLine(right, y, right+3, y);
				y += quarter;
				gc.drawLine(textwidth-6, y, textwidth, y);
				gc.drawLine(right, y, right+6, y);
				y += quarter;
				gc.drawLine(textwidth-3, y, textwidth, y);
				gc.drawLine(right, y, right+3, y);
				y += quarter;
				runner.addHours(1);
			}
		}

	}
}
