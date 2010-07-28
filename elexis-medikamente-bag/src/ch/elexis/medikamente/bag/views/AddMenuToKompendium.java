package ch.elexis.medikamente.bag.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import ch.elexis.Desk;
import ch.elexis.views.KompendiumView;

public class AddMenuToKompendium extends ExtensionContributionFactory {

	public AddMenuToKompendium() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		Action action=new Action("Pull"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_IMPORT));
				setToolTipText("Text zu Medikament übernehmen");
			}

			@Override
			public void run() {
				String text=KompendiumView.getText();
				System.out.println(text);
			}
			
			
		};
		additions.addContributionItem(new ActionContributionItem(action), null);

	}

}
