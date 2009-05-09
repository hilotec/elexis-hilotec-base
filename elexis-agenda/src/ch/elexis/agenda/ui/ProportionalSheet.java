package ch.elexis.agenda.ui;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.actions.AgendaActions;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.util.Plannables;

public class ProportionalSheet extends Composite {
	private BaseView view;
	private int count;
	
	public ProportionalSheet(Composite parent, BaseView v){
		super(parent,SWT.NONE);
		view=v;
	}
	
	void setRangeCount(int c){
		count=c;
	}
	
	void addAppointments(List<IPlannable> tt, int column){
		MenuManager manager=new MenuManager();
		manager.add(AgendaActions.terminStatusAction);
		manager.add(view.terminKuerzenAction);
		manager.add(view.terminVerlaengernAction);
		manager.add(view.terminAendernAction);
		manager.add(AgendaActions.delTerminAction);
		for(IPlannable ip:tt){
			Termin t=(Termin)ip;
			Label l=new Label(this,SWT.WRAP);
			l.setData("termin",t);
			l.setData("column",column);
			l.setMenu(manager.createContextMenu(l));
			l.setBackground(Plannables.getTypColor(t));
			l.setForeground(Plannables.getStatusColor(t));
			l.setText(t.getTitle());
			StringBuilder sb=new StringBuilder();
			sb.append(t.getLabel()).append("\n")
				.append(t.getGrund());

		}
	}
	
	void recalc(){
		double ppm=AgendaParallel.getPixelPerMinute();
		int height=(int)Math.round(ppm*60*24);
		double width=getParent().getSize().x;
		double widthPerColumn=width/count;
		for(Control c:getChildren()){
			Label l=(Label)c;
			int lx=(int)Math.round((Integer)l.getData("column")*widthPerColumn);
			Termin t=(Termin)l.getData("termin");
			int ly=(int)Math.round(t.getBeginn()*ppm);
			int lw=(int)Math.round(widthPerColumn);
			int lh=(int)Math.round(t.getDauer()*ppm);
			l.setBounds(lx, ly, lw, lh);
		}
	}
}
