package ch.elexis.labortarif.ch2009;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ch.elexis.labortarif2009.data.Test_Importer.class,
	ch.elexis.labortarif2009.data.Test_Labor2009Tarif.class
})
public class AllPluginTests {

}