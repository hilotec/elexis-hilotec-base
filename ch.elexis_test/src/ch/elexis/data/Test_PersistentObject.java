package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;

import ch.elexis.Hub;
import ch.elexis.ResourceManager;
import ch.elexis.preferences.PreferenceInitializer;
import ch.rgw.tools.JdbcLink;

public class Test_PersistentObject extends AbstractPersistentObjectTest {
	
	@Test
	public void testFirstConnect() {
		ResourceManager rsc = ResourceManager.getInstance();
		String pluginPath = rsc.getResourceLocationByName("/createDB.script");
		int end = pluginPath.lastIndexOf('/');
		end = pluginPath.lastIndexOf('/', end - 1);
		pluginPath = pluginPath.substring(0, end);
		
		PowerMockito.mockStatic(Hub.class);
		PowerMockito.when(Hub.getBasePath()).thenReturn(pluginPath);
		PowerMockito.when(Hub.getCfgVariant()).thenReturn("default");
		
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		assertNotNull(link);
		link.connect("", "");
		boolean ret = PersistentObject.connect(link);
		assertFalse(ret);
		link.disconnect();
	}
	
	@Test
	public void testConnect() {
		ResourceManager rsc = ResourceManager.getInstance();
		String pluginPath = rsc.getResourceLocationByName("/createDB.script");
		int end = pluginPath.lastIndexOf('/');
		end = pluginPath.lastIndexOf('/', end - 1);
		pluginPath = pluginPath.substring(0, end);
		
		PowerMockito.mockStatic(PreferenceInitializer.class);
		PowerMockito.when(PreferenceInitializer.getDefaultDBPath()).thenReturn(pluginPath);
		
		JdbcLink link = initDB();
		boolean ret = PersistentObject.connect(link);
		assertTrue(ret);
		PersistentObject.disconnect();
		link.disconnect();
	}
	
	@Test
	public void testConnectFail() {
		ResourceManager rsc = ResourceManager.getInstance();
		PowerMockito.mockStatic(Hub.class);
		PowerMockito.when(Hub.getBasePath()).thenReturn(rsc.getResourceLocationByName("/createDB.script"));
		PowerMockito.when(Hub.getCfgVariant()).thenReturn("default");
		PowerMockito.mockStatic(PreferenceInitializer.class);
		PowerMockito.when(PreferenceInitializer.getDefaultDBPath()).thenReturn("");
		
		JdbcLink link = initDB();
		link.disconnect();
		// direct connect with disconnected JdbcLink
		try {
			PersistentObject.connect(link);
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
		
		// this connect methods opens its own JdbcLink by all means
		// it is looking for a demo db:
		// File demo = new File(base.getParentFile().getParent() + "/demoDB");
		
		// then for dom SWTBot related db:
		// String template = System.getProperty("SWTBot-DBTemplate");
		// File dbDir = new File(Hub.getTempDir(), "Elexis-SWTBot");
		
		// then from some user provided config
		// String connection = Hub.getCfgVariant();
		
		// then if provider is Medelexis the db wizard is opened else
		// look for db at default location
		// String d = PreferenceInitializer.getDefaultDBPath();
		
		// this is nice for runtime but makes testing really hard :)
		// we need to mock JdbcLink.createH2Link to stop creation of database
		PowerMockito.mockStatic(JdbcLink.class);
		PowerMockito.when(JdbcLink.createH2Link(Matchers.anyString())).thenReturn(new JdbcLink("", "", ""));
		// connect and simulate db creation failure with JdbcLink mock
		try {
			PersistentObject.connect(Hub.localCfg, null);
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	
	@Test
	public void testGet() {
		JdbcLink link = initDB();
		
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.get("TestGet");
		assertNotNull(ret);
		assertEquals("test", ret);
		link.disconnect();
	}
	
	@Test
	public void testGetFail() {
		JdbcLink link = initDB();
		
		PersistentObjectImpl impl = new PersistentObjectImpl();
		try {
			String ret = impl.get("");
			fail("Expected Exception not thrown!");
			
			assertNotNull(ret);
			assertEquals(PersistentObject.MAPPING_ERROR_MARKER + "**", ret);
		} catch (PersistenceException pe) {
			
		}
		
		// if we pass ID we should get to code that reaches into the db
		// we have no table specified so a JdbcLinkException is expected
		try {
			impl.get("ID");
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
		
		link.disconnect();
	}
	
	private class PersistentObjectImpl extends PersistentObject {

		public String getTestGet() {
			return "test";
		}
		
		@Override
		public String getLabel() {
			return null;
		}

		@Override
		protected String getTableName() {
			return null;
		}
		
	}
}
