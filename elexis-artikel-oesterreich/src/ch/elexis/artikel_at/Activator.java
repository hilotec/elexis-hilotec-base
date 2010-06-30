package ch.elexis.artikel_at;

import java.util.List;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.Hub;
import ch.elexis.artikel_at.data.Artikel_AT_Cache;
import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.artikel_at.views.VidalLabelProvider;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;

public class Activator extends AbstractUIPlugin {

	public Activator() {
	}

	/* This activators sole purpose is to initialize the HashMap caches for the VidalLabelProvider
	 * @author Marco Descher
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		if(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_CACHEUPDATE_TIME, "invalid").equalsIgnoreCase("invalid")) {
			Artikel_AT_Cache.updateCache();
		}
		
	}

}
