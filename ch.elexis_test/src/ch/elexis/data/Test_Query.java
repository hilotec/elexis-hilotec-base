package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_Query extends AbstractPersistentObjectTest {

	private JdbcLink link;
	
	@Before
	public void setUp() {
		link = initDB();
		// create a instance of an PersistentObject ex. Organisation to test the query
		Organisation org = new Organisation("orgname", "orgzusatz1");
	}
	
	@After
	public void tearDown() {
		link.disconnect();
	}
	
	@Test
	public void testConstructor() {
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
		

		query = new Query<Organisation>(Organisation.class, Organisation.FLD_NAME1, "orgname");
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
	}
	
	@Test
	public void testConstructorFail() {
		try {
			Query<PersistentObjectImpl> query = new Query<PersistentObjectImpl>(PersistentObjectImpl.class);
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
		
		try {
			Query<PersistentObjectImpl> query = new Query<PersistentObjectImpl>(PersistentObjectImpl.class, "", "");
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}

	@Test
	public void testExecute() {
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.add(Organisation.FLD_NAME1, "=", "orgname");
		List<Organisation> result = query.execute();
		assertEquals(1, result.size());
	}
	
	@Test
	public void testExecutePreparedStatement() {
		PreparedStatement ps = link.prepareStatement("SELECT * FROM " + Organisation.TABLENAME);
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		ArrayList<String> result = query.execute(ps, new String[0]);
		assertEquals(3, result.size());
	}
	
	private class PersistentObjectImpl extends PersistentObject {

		public String getTestGet() {
			return "test";
		}
		
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected String getTableName() {
			return null;
		}
		
	}
}
