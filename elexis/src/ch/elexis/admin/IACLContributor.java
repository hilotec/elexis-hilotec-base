/**
 * This interface is to be used with the ACLContribution expension point
 */
package ch.elexis.admin;

/**
 * An IACLContributer declares what AccessControl Verbs it will use. Such verbs will define, rights
 * a user has an will be editable via the Administrator UI (Settings tab "groups and rights"). The
 * names of such ACL^s might collide. In such case, the farmework will assign them on a first
 * come-first server-basis, thus, the second client will get a reject.
 * 
 * @author gerry
 * 
 */
public interface IACLContributor {
	
	/**
	 * return the ACLs to be used by this extension
	 * 
	 * @return an array of all ACL's to insert
	 */
	public String[] getACL();
	
	/**
	 * The framework will call this method if one ore more of the transmitted ACL's could not be
	 * integrated (illegal name or duplicate)
	 * 
	 * @param acl
	 *            array of all rejected acls (these have not been integrated)
	 * @return the plugin can return an array of corrected acls or null.
	 */
	public String[] reject(String[] acl);
	
}
