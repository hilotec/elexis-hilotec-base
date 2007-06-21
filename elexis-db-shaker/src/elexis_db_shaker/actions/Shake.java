package elexis_db_shaker.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class Shake implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public Shake() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		if(SWTHelper.askYesNo("Wirklich Datenbank anonymisieren", "Achtung! Diese Aktion macht die Datenbank unwiderruflich unbrauchbar! Wirklich anonymisieren?")){
			Query<Kontakt> qbe=new Query<Kontakt>(Kontakt.class);
			List<Kontakt> list=qbe.execute();
			for(Kontakt k:list){
				k.set("Bezeichnung1", getWord());
				k.set("Bezeichnung2", getWord());
				k.set("Anschrift", "");
				k.set("Telefon1", getPhone());
				k.set("Telefon2", getPhone());
				k.set("E-Mail", "");
				k.set("NatelNr", "");
				k.set("Fax", "");
			}	
		}
	}

	private String getPhone(){
		StringBuilder ret=new StringBuilder();
		ret.append("555-");
		for(int i=0;i<7;i++){
			ret.append((char)Math.round(Math.random()*('9'-'0')+'0'));
		}
		return ret.toString();
	}
	private String getWord(){
		int l=(int)Math.round(Math.random()*5+5);
		StringBuilder ret=new StringBuilder();
		ret.append(Character.toUpperCase(getLetter()));
		for(int i=0;i<l;i++){
			ret.append(getLetter());
		}
		return ret.toString();
	}
	
	private char getLetter(){
		return (char)Math.round(Math.random()*('z'-'a')+'a');
	}
	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}