/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: HistoryLoader.java 2760 2007-07-08 12:14:32Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.data.Konsultation;
import ch.elexis.text.Samdas;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;

/**
 * Texte früherer Konsultationen asynchron nachladen.
 * @author Gerry
 *
 */
public class HistoryLoader extends BackgroundJob {
	StringBuilder sb;
	List<Konsultation> lKons;
	KonsFilter filter;
	
	boolean multiline = false;
	
	public void setFilter(KonsFilter kf){
		filter=kf;
	}
	/*
	 * multine == true: show Konsultation text with newlines
	 */
	public HistoryLoader(StringBuilder sb,ArrayList<Konsultation> lKons){
		this(sb, lKons, false);
	}
	public HistoryLoader(StringBuilder sb,ArrayList<Konsultation> lKons, boolean multiline){
		super(Messages.getString("HistoryLoader.LoadKonsMessage")); //$NON-NLS-1$
		this.sb=sb;
		this.lKons=lKons;
		this.multiline = multiline;
		this.setPriority(Job.DECORATE);
		this.setUser(false);
	}
	@Override
	public IStatus execute(IProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("HistoryLoader.LoadKonsMessage"), lKons.size()+100); //$NON-NLS-1$
		monitor.subTask(Messages.getString("HistoryLoader.Sorting")); //$NON-NLS-1$
		Collections.sort(lKons, new Comparator<Konsultation>(){
			TimeTool t1=new TimeTool();
			TimeTool t2=new TimeTool();
			public int compare(Konsultation o1, Konsultation o2) {
				if((o1==null) || (o2==null)){
					return 0;
				}
				t1.set(o1.getDatum());
				t2.set(o2.getDatum());
				if(t1.isBefore(t2)){
					return 1;
				}
				if(t1.isAfter(t2)){
					return -1;
				}
				return 0;
			}});
		monitor.worked(50);
		Iterator it=lKons.iterator();
		sb.append("<form>"); //$NON-NLS-1$
		while(!monitor.isCanceled()){
			if(!it.hasNext()){
				sb.append("</form>"); //$NON-NLS-1$
				result=sb.toString();
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
			Konsultation k=(Konsultation)it.next();
			if(filter!=null){
				if(filter.pass(k)==false){
					continue;
				}
			}
			VersionedResource vr=k.getEintrag();
			String s=vr.getHead();
			if(s!=null){
				if(s.startsWith("<")){ //$NON-NLS-1$
					Samdas samdas=new Samdas(s);
					s=samdas.getRecordText();
				}
				s=s.replaceAll("<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
				s=s.replaceAll(">","&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
				s=s.replaceAll("&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
				if (multiline) {
					// TODO use system line separator
					// replace Windows line separator
					s = s.replaceAll("\r\n", "<br/>");
					// replace remaining "manual" line separators
					s = s.replaceAll("\n", "<br/>");
				}
				
			}else{
				s=""; //$NON-NLS-1$
			}
			String label=k.getLabel();
			//label+="<br/>"+k.getFall().getLabel();
			sb.append("<p><a href=\"") //$NON-NLS-1$
				.append(k.getId()).append("\">") //$NON-NLS-1$
				.append(label).append("</a><br/>") //$NON-NLS-1$
				.append("<span color=\"gruen\">") //$NON-NLS-1$
				.append(k.getFall().getLabel())
				.append("</span><br/>") //$NON-NLS-1$
				.append(s).append("</p>"); //$NON-NLS-1$
			monitor.worked(1);
			
		}
		sb.setLength(0);
		monitor.done();
		return Status.CANCEL_STATUS;
	}

	
	@Override
	public int getSize() {
		return lKons.size();
	}

}
