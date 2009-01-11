package ch.elexis.selectors;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SelectorField extends Composite {
	Label lbl;
	Text text;
	int len;
	
	private LinkedList<SelectorListener> listeners=new LinkedList<SelectorListener>();
	
	public SelectorField(Composite parent, String label){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		lbl=new Label(this,SWT.NONE);
		lbl.setText(label);
		text=new Text(this,SWT.BORDER);
		lbl.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseUp(MouseEvent e){
				// TODO Auto-generated method stub
				super.mouseUp(e);
			}
			
		});
		text.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e){
				String fld=text.getText();
				int l2=fld.length();
				if((l2>2) || (len>2)){
					for(SelectorListener sl:listeners){
						sl.selectionChanged(SelectorField.this);
					}
				}
				len=l2;
				
			}});
		len=0;
	}
	
	public void addSelectorListener(SelectorListener listen){
		listeners.add(listen);
	}
	
	public void removeSelectorListener(SelectorListener listen){
		listeners.remove(listen);
	}
}
