package ch.elexis.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.util.ResultAdapter;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.Tree;
import ch.elexis.views.rechnung.Messages;
import ch.rgw.tools.Result;

public class ErstelleRnnCommand extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent eev) throws ExecutionException{
		Map params=eev.getParameters();
		Tree tSelection=(Tree)params.get("selection");
		for (Tree tPat = tSelection.getFirstChild(); tPat != null; tPat =
			tPat.getNextSibling()) {
			int rejected = 0;
			for (Tree tFall = tPat.getFirstChild(); tFall != null; tFall =
				tFall.getNextSibling()) {
				Fall fall = (Fall) tFall.contents;
				if (Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true)) {
					if (!fall.isValid()) {
						rejected++;
						continue;
					}
				}
				Collection<Tree> lt = tFall.getChildren();
				ArrayList<Konsultation> lb = new ArrayList<Konsultation>(lt.size() + 1);
				for (Tree t : lt) {
					lb.add((Konsultation) t.contents);
				}
				Result<Rechnung> res = Rechnung.build(lb);
				if (!res.isOK()) {
					ErrorDialog.openError(HandlerUtil.getActiveShell(eev), Messages
						.getString("KonsZumVerrechnenView.errorInInvoice"), //$NON-NLS-1$
						Messages.getString("KonsZumVerrechnenView.invoiceForCase", //$NON-NLS-1$
							new Object[] {
								fall.getLabel()
							}), ResultAdapter.getResultAsStatus(res)); 
				} else {
					tPat.remove(tFall);
				}
			}
			if (rejected != 0) {
				SWTHelper
					.showError(
						"Fehlerhafte Falldefinitionen",
						Integer.toString(rejected)
							+ " Rechnungen wurden nicht erstellt, weil die Fälle nicht alle notwendigen Angaben enthalten. "
							+ "Bitte kontrollieren Sie die Fall-Details");
			} else {
				tSelection.remove(tPat);
			}
		}
		return null;
	}
	
}
