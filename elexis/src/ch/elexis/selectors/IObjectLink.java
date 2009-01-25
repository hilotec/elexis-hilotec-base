package ch.elexis.selectors;

import java.util.List;

import ch.elexis.data.PersistentObject;

/**
 * Link an input or display field to the database
 * @author Gerry
 *
 */
public interface IObjectLink<T extends PersistentObject> {

	public String getValueFromObject(T t, String fieldname);
	public void setValueToObject(T t, String fieldname);
	public List<T> getObjectsForValue(String fieldname,String value, boolean bMatchExact);
}
