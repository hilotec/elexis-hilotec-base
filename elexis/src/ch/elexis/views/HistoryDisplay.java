/*******************************************************************************
 * Copyright (c) 2006, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: HistoryDisplay.java 2205 2007-04-13 08:28:15Z danlutz $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import ch.elexis.Desk;
import ch.elexis.actions.*;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

/**
 * Anzeige der vergangenen Konsultationen.
 * Es sollen einerseits "sofort" die letzten 3 oder 4 Kons angezeigt werden,
 * andererseits aber je nach Anforderung auch frühere nachgeladen werden.
 * Dies ist noch nicht korrekt implemetiert - aktuell werden immer alle Kons. 
 * geladen.
 * @author Gerry
 *
 */
public class HistoryDisplay extends ScrolledComposite implements BackgroundJobListener{
	FormText text;
	ArrayList<Konsultation> lKons;
	StringBuilder sb;
	HistoryLoader loader;
	HistoryDisplay self=this;
	
	boolean multiline = false;
	
	public HistoryDisplay(Composite parent, final IViewSite site){
		this(parent, site, false);
	}
	public HistoryDisplay(Composite parent, final IViewSite site, boolean multiline){
		super(parent,SWT.V_SCROLL|SWT.BORDER);
		this.multiline = multiline;
		lKons=new ArrayList<Konsultation>(20);
		text=Desk.theToolkit.createFormText(this,false);
		text.setWhitespaceNormalized(true);
		text.setColor("blau",Desk.theColorRegistry.get("blau"));
		text.setColor("gruen",	Desk.theColorRegistry.get("hellgrau"));
		setContent(text);
		text.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				String id=(String)e.getHref();
				Konsultation k=Konsultation.load(id);
				GlobalEvents.getInstance().fireSelectionEvent(k);
			}
			
		});
		text.setText("Kein Patient ausgewählt",false,false);
		sb=new StringBuilder(1000);
		addControlListener(new ControlAdapter(){
			@Override
			public void controlResized(ControlEvent e) {
				text.setSize(text.computeSize(self.getSize().x-15,SWT.DEFAULT));
			}
			
		});
	}
	
	public void setFilter(KonsFilter f){
		stop();
		loader.setFilter(f);
	}
	public void start(){
		start(null);
	}
	public void start(KonsFilter f){
		stop();
		sb.setLength(0);
		loader=new HistoryLoader(sb,lKons,multiline);
		loader.setFilter(f);
		loader.addListener(this);
		loader.schedule();
	}
	public void stop(){
		if(loader!=null){
			loader.removeListener(this);
			loader.cancel();
			loader=null;
		}
	}
	public void load(Fall fall, boolean clear){
		if(clear){
			lKons.clear();
		}
		if(fall!=null){
			Konsultation[] kons=fall.getBehandlungen(true);
			for(Konsultation k:kons){
				lKons.add(k);
			}
		}
	}
	public void load(Patient pat){
		lKons.clear();
		Fall[] faelle=pat.getFaelle();
		for(Fall f:faelle){
			load(f,false);
		}
	}

	public void jobFinished(BackgroundJob j) {
		Desk.theDisplay.asyncExec(new Runnable(){
			public void run() {
				String s=(String)loader.getData();
				//System.out.println(s);
				
				// check if widget is valid
				if (!isDisposed()) {
					text.setText(s,true,true);
					text.setSize(text.computeSize(self.getSize().x-10,SWT.DEFAULT));
				}
			}});
	}
}
