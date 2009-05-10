package ch.elexis.agenda.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.actions.Activator;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.Termin;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;

public class TerminLabel extends Composite {
	private Label lbl;
	private Termin t;
	private int column;
	ProportionalSheet parent;
	Activator agenda=Activator.getDefault();
	
	TerminLabel(ProportionalSheet parent, Termin trm, int col){
		super(parent,SWT.BORDER);
		this.parent=parent;
		this.t=trm;
		this.column=col;
		setLayout(new GridLayout());
		lbl=new Label(this,SWT.WRAP);
		lbl.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				agenda.setActDate(t.getDay());
				new TerminDialog(t).open();
				refresh();
			}
		});
		lbl.addMouseMoveListener(new MouseMoveListener(){
			public void mouseMove(MouseEvent e) {
				agenda.setActDate(t.getDay());
				GlobalEvents.getInstance().fireSelectionEvent(t);
				System.out.println(t.dump());	
			}
			
		});
		lbl.setMenu(parent.getContextMenuManager().createContextMenu(lbl));
	}
	
	int getColumn(){
		return column;
	}
	
	Termin getTermin(){
		return t;
	}
	
	void refresh(){
		Color back=Plannables.getTypColor(t);
		lbl.setBackground(back);
		//l.setBackground(Desk.getColor(Desk.COL_GREY20));
		lbl.setForeground(SWTHelper.getContrast(back));

		//l.setForeground(Plannables.getStatusColor(t));
		StringBuilder sb = new StringBuilder();
		sb.append(t.getLabel()).append("\n").append(t.getGrund());
		lbl.setText(t.getTitle());
		lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lbl.setToolTipText(sb.toString());
		lbl.redraw();
	}
}
