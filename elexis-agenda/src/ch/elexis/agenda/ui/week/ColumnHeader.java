package ch.elexis.agenda.ui.week;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

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
		ihRes.setToolTipText("Tage für Anzeige auswählen");
		ihRes.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				new SelectDaysDlg().open(); 
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
	
	class SelectDaysDlg extends TitleAreaDialog{
		SelectDaysDlg(){
			super(ColumnHeader.this.getShell());
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=(Composite) super.createDialogArea(parent);
			ret.setLayout(new GridLayout());
			String[] days=view.getDisplayedDays();
			for (String s:TimeTool.Wochentage){
				Button b=new Button(ret,SWT.CHECK);
				b.setText(s);
				if(StringTool.getIndex(days, s)!=-1){
					b.setSelection(true);
				}
			}
			return ret;
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("Anzeige konfigurieren");
			setTitle("Anzuzeigende Wochentage");
			setMessage("Bitte geben Sie ein,welche Wochentage angezeigt werden sollen");
		}

		@Override
		protected void okPressed() {
			Composite dlg=(Composite)getDialogArea();
			String[] res=TimeTool.Wochentage;
			ArrayList<String> sel=new ArrayList<String>(res.length);
			for(Control c:dlg.getChildren()){
				if(c instanceof Button){
					if(((Button) c).getSelection()){
						sel.add(((Button)c).getText());
					}
				}
			}
			view.clear();
			Hub.localCfg.set(PreferenceConstants.AG_DAYSTOSHOW, StringTool.join(sel, ","));
			view.refresh();
			
			super.okPressed();
		}
		
	}
}
