package ch.elexis.agenda.ui.week;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.Desk;
import ch.elexis.actions.Activator;

public class ColumnHeader extends Composite {
	AgendaWeek view;
	static final String IMG_PERSONS_NAME=Activator.PLUGIN_ID+"/personen";
	static final String IMG_PERSONS_PATH="icons/personen.png";
	ImageHyperlink ihRes;
	
	public ColumnHeader(Composite parent, AgendaWeek aw){
		super(parent,SWT.NONE);
		view=aw;
		if(Desk.getImage(IMG_PERSONS_NAME)==null){
			Desk.getImageRegistry().put(IMG_PERSONS_NAME, Activator.getImageDescriptor(IMG_PERSONS_PATH));
		}
		ihRes=new ImageHyperlink(this,SWT.NONE);
		ihRes.setImage(Desk.getImage(IMG_PERSONS_NAME));
		ihRes.setToolTipText("Bereiche für Anzeige auswählen");
		ihRes.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				//new SelectResourceDlg().open(); 
			}
			
		});
	}
	
	void recalc(double widthPerColumn, int left_offset, int padding, int textSize){
		GridData gd=(GridData)getLayoutData();
		gd.heightHint=textSize+2;
		for(Control c:getChildren()){
			if(c instanceof Label){
				c.dispose();
			}
		}
		Point bSize=ihRes.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		ihRes.setBounds(0, 0, bSize.x, bSize.y);
		String[] labels=view.getDisplayedDays();
		int count=labels.length;
		for(int i=0;i<count;i++){
			int lx=left_offset+(int) Math.round(i*(widthPerColumn+padding));
			Label l=new Label(this, SWT.NONE);
			l.setText(labels[i]);
			int outer=(int)Math.round(widthPerColumn);
			int inner=l.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			int off=(outer-inner)/2;
			lx+=off;
			l.setBounds(lx, 0, inner, textSize+2);
		}
	}
}
