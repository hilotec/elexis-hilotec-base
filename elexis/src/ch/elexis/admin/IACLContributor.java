/**
 * This interface is to be used with the ACLContribution expension point
 */
package ch.elexis.admin;

public interface IACLContributor {
	
	/**
	 * return the ACLs to be used by this extension 
	 * @return an array of all ACL's to insert
	 */
	public String[] getACL();
	
	/**
	 * The framework will call this method if one ore more of the 
	 * transmitted ACL's could not be integrated (illegal name or duplicate)
	 * @param acl array of all rejected acls (these have not been integrated)
	 * @return the plugin can return an array of corrected acls or null.
	 */
	public String[] reject(String[] acl);

}
