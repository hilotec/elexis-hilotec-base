package ch.elexis.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.IPlannable;
import ch.elexis.data.Termin;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class DayBar extends Composite {
	int height, width;
	double pixelPerDay;
	DayBar self=this;
	AgendaWeek container;	
	
	public DayBar(AgendaWeek parent){
		super(parent.cWeekDisplay,SWT.BORDER);
		container=parent;
		this.addControlListener(new ControlListener(){

			public void controlMoved(ControlEvent e) {
				// don't mind
			}

			public void controlResized(ControlEvent e) {
				Rectangle rec=getBounds();
				height=rec.height;
				width=rec.width;
				recalc();
			}});
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		//setBackground(Desk.theColorRegistry.get(Desk.COL_GREEN));
	}
	

	void recalc(){
		container.recalc();
		pixelPerDay=Math.max(width-10,5);
		for(Control ctrl:getChildren()){
			if(ctrl instanceof TerminFeld){
				((TerminFeld)ctrl).recalc();
			}
		}
		
	}
	public void set(TimeTool day, String bereich){
		List<IPlannable> dayList=Plannables.loadTermine(bereich, day);
		for(IPlannable ip:dayList){
			TerminFeld f=new TerminFeld(this,ip);
		}
	}
	
	class TerminFeld extends Composite{
		IPlannable t;
		DayBar mine;
		Label myLabel;
		TerminFeld(DayBar parent, IPlannable termin){
			super(parent,SWT.BORDER);
			setLayout(new FillLayout());
			t=termin;
			myLabel=new Label(this,SWT.WRAP);
			myLabel.setBackground(Plannables.getTypColor(t));
			myLabel.setForeground(Plannables.getStatusColor(t));
			myLabel.setText(t.getTitle());
			mine=parent;
			//setBounds(0, 0, 20, 20);
		}

		public void recalc() {
			int anzeigeDauer=0;
			int terminStart=t.getStartMinute();
			int terminDauer=t.getDurationInMinutes();
			int terminEnde=terminStart+terminDauer;
			if(terminStart<container.tagStart){
				terminStart=container.tagStart;
			}
			if(terminEnde>container.tagEnde){
				terminEnde=container.tagEnde;
			}
			anzeigeDauer=Math.max(0,terminEnde-terminStart);
			int h=(int)Math.round(anzeigeDauer*container.pixelPerMinute);
			int beg=(int)Math.round((terminStart-container.tagStart)*container.pixelPerMinute);
			setBounds(0, beg, (int)Math.round(pixelPerDay), h);
			layout();
		}
		
		
	}
}
