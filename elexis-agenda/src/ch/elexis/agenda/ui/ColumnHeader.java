package ch.elexis.agenda.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.Desk;
import ch.elexis.actions.Activator;

public class ColumnHeader extends Composite {
	AgendaParallel view;
	static final String IMG_PERSONS_NAME=Activator.PLUGIN_ID+"/personen";
	static final String IMG_PERSONS_PATH="icons/personen.png";
	
	ColumnHeader(Composite parent, AgendaParallel v){
		super(parent,SWT.NONE);
		view=v;
		if(Desk.getImage(IMG_PERSONS_NAME)==null){
			Desk.getImageRegistry().put(IMG_PERSONS_NAME, Activator.getImageDescriptor(IMG_PERSONS_PATH));
		}
	}
	
	void recalc(double widthPerColumn, int left_offset, int padding, int textSize){
		GridData gd=(GridData)getLayoutData();
		gd.heightHint=textSize+2;
		for(Control c:getChildren()){
			c.dispose();
		}
		ImageHyperlink ihRes=new ImageHyperlink(this,SWT.NONE);
		ihRes.setImage(Desk.getImage(IMG_PERSONS_NAME));
		Point bSize=ihRes.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		ihRes.setBounds(0, 0, bSize.x, bSize.y);
		ihRes.setToolTipText("Bereiche für Anzeige auswählen");
		String[] labels=view.getDisplayedResources();
		int count=labels.length;
		for(int i=0;i<count;i++){
			int lx=left_offset+(int) Math.round(i*(widthPerColumn+padding));
			Label l=new Label(this, SWT.NONE);
			l.setText(labels[i]);
			l.setBounds(lx, 0, l.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, textSize+2);
		}
	}
}
