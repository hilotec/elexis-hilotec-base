package ch.elexis.selectors;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.util.SWTHelper;

/**
 * An Element consisting of a label and a control that is able to link itself
 * to the database and act on user input
 * @author Gerry
 *
 */
public abstract class ActiveControl extends Composite {
	protected Label lbl;
	protected Control ctl;
	private String displayName;
	private int displayBits;
	private LinkedList<ActiveControlListener> listeners;
	
	/** Constant to hide the label (Default: éabel is visible) */
	public static final int HIDE_LABEL=			0x0001; 
	/** Display label and control lined up horizontally (default: vertically) */
	public static final int DISPLAY_HORIZONTAL=	0x0002;
	/** Label reacts on mouse clicks (and informs listeners) */
	public static final int LABEL_IS_HYPERLINK=	0x0004;

	/** Displayed label of the field */
	public static final String PROP_DISPLAYNAME="displayName";
	/** Internal name od the field in the PersistentObject */
	public static final String PROP_FIELDNAME="fieldName";
	/** Name in the Hashtable if fieldName denotes a hash field */
	public static final String PROP_HASHNAME="hashName";
	/** Message to display if the field contents is invalid */
	public static final String POP_ERRMSG="invalidContents";
	
	/**
	 * create a new field
	 * @param parent the parent Composite
	 * @param properties TODO
	 * @param show ho to display the label
	 */
	public ActiveControl(Composite parent, int displayBits, String displayName){
		super(parent,SWT.NONE);
		this.displayBits=displayBits;
		this.displayName=displayName;
		if((displayBits&(DISPLAY_HORIZONTAL|HIDE_LABEL))==DISPLAY_HORIZONTAL){
			setLayout(new GridLayout(2,false));
		}else{
			setLayout(new GridLayout(1,false));
		}
		if((displayBits&HIDE_LABEL)==0){
			lbl=new Label(this,SWT.NONE);
			lbl.setText(displayName);
		}
		lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	public void addListener(ActiveControlListener listen){
		if(listeners==null){
			listeners=new LinkedList<ActiveControlListener>();
		}
		listeners.add(listen);
	}
	
	public void removeSelectorListener(ActiveControlListener listen){
		if(listeners!=null){
			listeners.remove(listen);
		}
	}
	
	public void fireChangedEvent(){
		if(listeners!=null){
			for(ActiveControlListener sl:listeners){
				sl.contentsChanged(this);
			}
		}
	}
	public abstract void setText(String text);
	public abstract String getText();
	public abstract boolean isValid();
	public abstract void clear();

	public String getLabel(){
		return getLbl().getText();
		
	}
	protected void setControl(Control control){
		ctl=control;
		ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ctl.addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e){
				if(isValid()){
					
				}
			}
		});
	}
	
	public Label getLbl(){
		return lbl;
	}

	public void setLabel(Label lbl){
		this.lbl = lbl;
	}

	public Control getCtl(){
		return ctl;
	}

	public void setCtl(Control ctl){
		this.ctl = ctl;
	}

	public String getDisplayName(){
		return displayName;
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}

	
	public void setEnabled(boolean bEnable){
		if(ctl!=null){
			ctl.setEnabled(bEnable);
		}
	}
	
	
	
}
