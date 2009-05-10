package ch.elexis.agenda.ui;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.actions.AgendaActions;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.util.SWTHelper;

public class ProportionalSheet extends Composite {
	static final int LEFT_OFFSET_DEFAULT=20;
	static final int PADDING_DEFAULT=5;
	
	int left_offset, padding;
	private AgendaParallel view;
	private MenuManager contextMenuManager;
	
	public ProportionalSheet(Composite parent, AgendaParallel v) {
		super(parent, SWT.NONE);
		view = v;
		addControlListener(new ControlAdapter(){

			@Override
			public void controlResized(ControlEvent e) {
				recalc();
			}});
		//setBackground(Desk.getColor(Desk.COL_GREEN));
		contextMenuManager = new MenuManager();
		contextMenuManager.add(AgendaActions.terminStatusAction);
		contextMenuManager.add(view.terminKuerzenAction);
		contextMenuManager.add(view.terminVerlaengernAction);
		contextMenuManager.add(view.terminAendernAction);
		contextMenuManager.add(AgendaActions.delTerminAction);
		left_offset=LEFT_OFFSET_DEFAULT;
		padding=PADDING_DEFAULT;
	}


	MenuManager getContextMenuManager(){
		return contextMenuManager;
	}
	void addAppointments(List<IPlannable> tt, int column) {
		for (IPlannable ip : tt) {
			Termin t = (Termin) ip;
			new TerminLabel(this,t,column);
		}
	}
/*
	@Override
	public Point computeSize(int hint, int hint2){
		Point parentSize=getParent().getSize();
		int w=parentSize.x;
		int h=(int)Math.round(AgendaParallel.getPixelPerMinute()*60*24);
		return new Point(w,h);
		
	}
	*/
/*
	@Override
	public Point computeSize(int hint, int hint2, boolean changed){
		//return super.computeSize(hint, hint2, changed);
		
	}
*/
	void recalc() {
		double ppm = AgendaParallel.getPixelPerMinute();
		int height = (int) Math.round(ppm * 60 * 24);
		ScrolledComposite sc=(ScrolledComposite)getParent();
		Point mySize=getSize();
		
		if (mySize.x > 0.0) {
			if(mySize.y!=height){
				setSize(mySize.x, height);
				sc.setMinSize(getSize());
				//sc.layout();
			}
			String[] resources=view.getDisplayedResources();
			int count=resources.length;
			Point textSize=SWTHelper.getStringBounds(this, "88:88");
			left_offset=textSize.x+2;
			double width = mySize.x-2*left_offset;
			double widthPerColumn = width / count;
			Composite header=view.getHeader();
			for(int i=0;i<count;i++){
				int lx=left_offset+(int) Math.round(i*(widthPerColumn+padding));
				
			}
			for (Control c : getChildren()) {
				TerminLabel l = (TerminLabel) c;
				int lx = left_offset+(int) Math.round(l.getColumn()*(widthPerColumn+padding));
				Termin t = l.getTermin();
				int ly = (int) Math.round(t.getBeginn() * ppm);
				int lw = (int) Math.round(widthPerColumn);
				int lh = (int) Math.round(t.getDauer() * ppm);
				l.setBounds(lx, ly, lw, lh);
				l.refresh();
			}
			sc.layout();
		}
	}
}
