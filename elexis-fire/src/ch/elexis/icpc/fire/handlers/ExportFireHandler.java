package ch.elexis.icpc.fire.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Sticker;
import ch.rgw.tools.TimeTool;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ExportFireHandler extends AbstractHandler {
	private static final String CFGPARAM="ICPC_FIRE_LAST_UPLOAD";
	private Sticker fireSticker;
	
	public ExportFireHandler() {
		String id=new Query<Sticker>(Sticker.class).findSingle("name", "=", "Fire (ICPC)");
		if(id==null){
			fireSticker=new Sticker("Fire (ICPC)",Desk.getColor(Desk.COL_BLUE),Desk.getColor(Desk.COL_GREY));
		}else{
			fireSticker=Sticker.load(id);
		}
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String lastupdate=Hub.globalCfg.get(CFGPARAM,null);
		if(lastupdate==null){
			lastupdate="20090101";
		}
		Query<Konsultation> qbe=new Query<Konsultation>(Konsultation.class);
		TimeTool ttFrom=new TimeTool(lastupdate);
		qbe.add("Datum", ">=", ttFrom.toString(TimeTool.DATE_COMPACT));
		List<Konsultation> konsen=qbe.execute();
		Hub.globalCfg.set(CFGPARAM, new TimeTool().toString(TimeTool.DATE_COMPACT));
		if(konsen.size()>0){
			Element eRoot=new Element("meldung");
			Document doc=new Document(eRoot);
			for(Konsultation k:konsen){
				Fall fall=k.getFall();
				if(fall==null){
					continue;
				}
				Patient pat=fall.getPatient();
				if(pat==null){
					continue;
				}
				if(!k.getStickers().contains(fireSticker)){
					k.addSticker(fireSticker);
					Element eKons=new Element("konsultation");
					eKons.addContent(createSub("konsdate",new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO)));
					eKons.addContent(createSub("patid",pat.getPatCode()));
					eKons.addContent(createSub("patyear", Integer.toString(new TimeTool(pat.getGeburtsdatum()).get(TimeTool.YEAR))));
					eKons.addContent(createSub("patgender", pat.getGeschlecht().equals(Person.MALE) ? "male" : "female"));
					eKons.addContent(createSub("arzt", k.getMandant().getId()));
					eRoot.addContent(eKons);
				}
			}
		}
		
		return null;
	}
	private Element createSub(String name, String contents){
		Element ret=new Element(name);
		ret.setText(contents);
		return ret;
	}
}
