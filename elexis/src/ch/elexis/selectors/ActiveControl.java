package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * An Element consisting of a label and a control that is able to link itself
 * to the database and act on user input
 * @author Gerry
 *
 */
public class ActiveControl extends Composite {
	Label lbl;
	Control ctl;
	String displayName;
	String databaseName;
	String hashName;
	public enum DISPLAY{NO_LABEL,VERTICAL,HORIZONTAL};
	
	/**
	 * create a new field
	 * @param parent the parent Composite
	 * @param show ho to display the label
	 * @param displayName the name as displayed to the user
	 * @param databaseField the name of the underlying database field
	 * @param hashName the name of the hash element if databaseField ist a Hashtable-field
	 */
	public ActiveControl(Composite parent, DISPLAY show,String displayName, String databaseField, String hashName){
		super(parent,SWT.NONE);
	}
	
}
