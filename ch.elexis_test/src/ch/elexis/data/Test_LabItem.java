package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_LabItem extends AbstractPersistentObjectTest {

	private JdbcLink link;
	private Organisation org;
	
	private static final String REF_ITEM_KUERZEL = "kuerzel"; 
	private static final String REF_ITEM_NAME = "testname"; 
	private static final String REF_ITEM_UNIT = "mg/dl";
	private static final String REF_ITEM_REFM = "0-1";
	private static final String REF_ITEM_REFW = "0-2";
	private static final String REF_ITEM_GROUP = "gruppe";	
	
	
	@Before
	public void setUp() {
		link = initDB();
		// create a instance of an PersistentObject ex. Organisation to test the query
		org = new Organisation("orgname", "orgzusatz1");
		new LabItem(REF_ITEM_KUERZEL, REF_ITEM_NAME, org,
				REF_ITEM_REFM, REF_ITEM_REFW, REF_ITEM_UNIT,
				LabItem.typ.NUMERIC, REF_ITEM_GROUP, "0");
	}
	
	@After
	public void tearDown() {
		link.disconnect();
	}
	
	@Test
	public void testGetLabItems() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
	}
	
	@Test
	public void testGetLabItemsSelective() {
		// create a second lab item to select
		LabItem item = new LabItem("kuerzel1", "testname1", org,
				"0-1", "0-2", "mg/dl",
				LabItem.typ.NUMERIC, "gruppe", "0");
		
		List<LabItem> items = LabItem.getLabItems(org.getId(), "kuerzel1", "0-1", "0-2", "mg/dl");
		assertEquals(1, items.size());
		assertEquals(item.getId(), items.get(0).getId());
		
		items = LabItem.getLabItems(org.getId(), null, "0-1", "0-2", "mg/dl");
		assertEquals(2, items.size());
		
		items = LabItem.getLabItems(org.getId(), REF_ITEM_KUERZEL, null, null, null);
		assertEquals(1, items.size());
	}
	
	@Test
	public void testGetEinheit() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_UNIT, loc.getEinheit());
	}
	
	@Test
	public void testSetEinheit() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setEinheit("l");
		assertEquals("l", loc.getEinheit());
	}
	
	@Test
	public void testGetGroup() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_GROUP, loc.getGroup());
	}
	
	@Test
	public void testGetRefM() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_REFM, loc.getRefM());
	}

	@Test
	public void testSetRefM() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setRefM("1-2");
		assertEquals("1-2", loc.getRefM());
	}

	@Test
	public void testGetRefW() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_REFW, loc.getRefW());
	}

	@Test
	public void testSetRefW() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setRefW("1-2");
		assertEquals("1-2", loc.getRefW());
	}
	
	@Test
	public void testGetLabor() {
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(org, loc.getLabor());
	}
}
