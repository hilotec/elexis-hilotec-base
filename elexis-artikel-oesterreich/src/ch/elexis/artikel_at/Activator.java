package ch.elexis.artikel_at;

import java.util.List;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.artikel_at.views.VidalLabelProvider;
import ch.elexis.data.Query;

public class Activator extends AbstractUIPlugin {

	public Activator() {
	}

	/* This activators sole purpose is to initialize the HashMap caches for the VidalLabelProvider
	 * @author Marco Descher
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
//		VidalLabelProvider vlp = new VidalLabelProvider();
//		
//		Query<Medikament> qMedi = new Query<Medikament>(Medikament.class);
//		qMedi.clear();
//		qMedi.add("Name", "LIKE", "%");
//		qMedi.orderBy(false, "Name");
//		List<Medikament> list = qMedi.execute();
//		for (Medikament medikament : list) {
//			vlp.getColumnImage(medikament, 0);
//			vlp.getColumnText(medikament, 0);
//		}
//		super.start(context);
	}

}
