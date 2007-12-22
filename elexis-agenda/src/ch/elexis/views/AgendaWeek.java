package ch.elexis.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Termin;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class AgendaWeek extends BaseAgendaView implements ControlListener {
	DayBar[] days;
	Composite cLeft, cRight;
	
	@Override
	public void createPartControl(Composite parent) {
		days=new DayBar[TimeTool.Wochentage.length];
		Composite cWeek=new Composite(parent,SWT.BORDER);
		cWeek.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cWeek.setLayout(new GridLayout(days.length+2,true));
		TimeTool ttWeek=new TimeTool();
		ttWeek.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		Button bBack=new Button(cWeek,SWT.PUSH);
		for(int d=0;d<days.length;d++){
			new Label(cWeek,SWT.NONE).setText(ttWeek.toString(TimeTool.WEEKDAY)+", "+ttWeek.toString(TimeTool.DATE_GER));
			ttWeek.addHours(24);
		}
		Button bFwd=new Button(cWeek,SWT.PUSH);
		cLeft=new Composite(cWeek,SWT.NONE);
		for(int d=0;d<days.length;d++){
			days[d]=new DayBar(cWeek);
		}
		cRight=new Composite(cWeek,SWT.NONE);
		cWeek.addControlListener(this);
		setWeek(new TimeTool());
	}

	public void setWeek(TimeTool ttContained){
		ttContained.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		for(int i=0;i<days.length;i++){
			days[i].set(ttContained, actBereich);
			ttContained.addHours(24);
		}
	}
	@Override
	public void create(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTermin(Termin t) {
		// TODO Auto-generated method stub

	}

	public void reloadContents(Class clazz) {
		

	}

	public void controlMoved(ControlEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void controlResized(ControlEvent e) {
		int ts=Hub.userCfg.get("agenda/dayView/Start",7); //$NON-NLS-1$
		int te=Hub.userCfg.get("agenda/dayView/End",19); //$NON-NLS-1$
		int tagStart=ts*60; // 7 Uhr
		int tagEnde=te*60;
		TimeTool runner=new TimeTool();
		runner.set("07:00");
		
	}

}
