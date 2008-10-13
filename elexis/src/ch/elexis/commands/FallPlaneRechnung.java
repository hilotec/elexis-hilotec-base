package ch.elexis.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

import ch.elexis.Desk;

public class FallPlaneRechnung extends AbstractHandler {

	public Object execute(ExecutionEvent arg0) throws ExecutionException{
		InputDialog dlg=new InputDialog(Desk.getTopShell(),"Rechnungsstellung planen", 
			"Nach wievielen Tagen soll dieser Fall zur Rechnung vorgeschlagen werden?",
			"30",new IInputValidator(){

				public String isValid(String newText){
					if(newText.matches("[0-9]*")){
						return null;
					}
					return "Bitte eine positive Zahl eingeben";
				}});
		if(dlg.open()==Dialog.OK){
			return dlg.getValue();
		}
		return null;
	}
	
	
	
}
