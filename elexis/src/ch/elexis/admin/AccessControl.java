/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: AccessControl.java 3862 2008-05-05 16:14:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.admin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.data.NamedBlob;
import ch.elexis.preferences.PreferenceConstants;
import ch.rgw.IO.InMemorySettings;
import ch.rgw.IO.Settings;
import ch.rgw.tools.StringTool;

/**
 * Diese Klasse realisiert das Zugriffskontroll- und Rechteverwaltungskonzept
 * von Elexis.
 * <ul>
 * <li> Es gibt Gruppen und Anwender.</li>
 * <li>Jeder Anwender gehört zu mindestens einer Gruppe.</li>
 * <li>Es existiert von Anfang an eine Gruppe "Alle" und ein Anwender "Jeder"</li>
 * <li>Jedes Recht kann einer oder mehreren Gruppen und/oder einer onder mehreren
 * Anwendern gewährt werden.</li>
 * <li> Ein Anwender erhält alle Rechte, die ihm entweder individuell gewährt wurden,
 * oder die einer der Gruppen gewährt wurden, zu denen er gehört.</li>
 * </ul>
 * Eine Ressource, die ein Zugriffsrecht realisieren will, muss diesem Recht einen 
 * individuellen Namen geben.
 * Zugriffsrechte können hierarchisch aufgebaut sein. Beispielsweise kann ein Recht foo/bar/baz
 * definiert sein. Wenn keine Regel für baz existiert, dann wird nach einer Regel für bar gesucht
 * und diese Angewandt. Wenn auch die nicht gefunden wird, wird nach einer Regel für foo gesucht. 
 * Wenn auch dies fehlschlägt, wird das Recht in jedem Fall verweigert.
 * Das Zugriffsrecht kann dann mit grant(gruppe,recht) oder grant(Anwender,recht) gewährt
 * resp. mit revoke(gruppe,Name) oder revoke(Anwender,name) entzogen werden.
 * Um herauszufinden, ob ein Anwender bw. einer seiner Gruppen das Recht hat, auf eine
 * ressource zuzugreifen, muss man request(anwedner,recht) fragen. Eine Abkürzung ist
 * request(recht). Dies fragt, ob der aktuell eingeloggte Anwender das betreffende Recht hat.
 *  
 * @author Gerry
 *
 */
public class AccessControl {
    
	public static final String ALL_GROUP=Messages.getString("AccessControl.GroupAll");
	public static final String USER_GROUP=Messages.getString("AccessControl.GroupUsers");
	public static final String ADMIN_GROUP=Messages.getString("AccessControl.GroupAdmin");
	public static final String GROUP_FOR_PREFERENCEPAGE="ch.elexis.preferences.acl";
	
	private static Hashtable<String,String> rights;
	private static Hashtable<String,List<String>> usergroups;
    
// TODO: Cleanup alte Gruppen/Anwender
    /**
     * Die Zugriffsrechte aus den globalen Settings laden.
     */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void load(){
		NamedBlob rset=NamedBlob.load("AccessControl"); //$NON-NLS-1$
		rights=rset.getHashtable();
		if(rights==null){
			rights=new Hashtable<String,String>();
			reset();
		}
		usergroups=new Hashtable<String,List<String>>();
	}
    /**
     * Zugriffsrechte zurücksichern. Alle Rechte, die seit dem letzten flush geändert wurden,
     * sind nur temporär bis zum nächsten flush()!
     */
	public void flush(){
		NamedBlob.load("AccessControl").put(rights); //$NON-NLS-1$
	}
    /**
     * Zugriffsrecht für den aktuell angemeldeten Anwender erfragen.
     * @param right Name des erfragten Rechts
     * @return true, wenn der Anwender (oder eine der Gruppen, zu denen
     * der Anwender gehört) das Recht hat.
     */
	public boolean request(String right){
		return(request(Hub.actUser,right));
	}
    
    /**
     * Zugriffsrecht für einen Anwender erfragen
     * @param user  Der Anwender
     * @param right Das Recht, das erfagt werden soll
     * @return true, wenn der Anwender (oder eine der Gruppen, zu der dieser
     * Anwender gehört) dieses Recht hat.
     * Immer true, wenn der Anwender zur Gruppe "Admin" gehört.
     * Immer false, wenn kein Anwender angemeldet ist
     */
	public boolean request(Anwender user, String right){
        if(StringTool.isNothing(right)){
        	return true;
        }
		if(rights==null){
			return false;
		}
		// Wenn alle dieses Recht haben-> ok
		if(rights.get(Messages.getString("AccessControl.GroupAll")+right)!=null){ //$NON-NLS-1$
			return true;
		}
		// Wenn gar kein user angegeben ist -> verweigern
		if(user==null){
			return false;
		}
		// Wenn das Recht für jeden User für sich besteht
		if(rights.get("Self"+right)!=null){ //$NON-NLS-1$
			if(Hub.actUser.getId().equals(user.getId())){
				return true;
			}
		}
		// Wenn das Recht für den genannten User individuell besteht
		if(rights.get(user.getId()+right)!=null){
			return true;
		}
		
		// Wenn das Recht für eine Gruppe, zu der der User gehört, besteht
		List<String> list=usergroups.get(user.getId()+"#groups#"); //$NON-NLS-1$
		// we cache the groups during runtime. If not yet in cache, load group membership from user data
		if(list==null){
			//Anwender act=Hub.actUser;
			list=new ArrayList<String>();
			Hashtable h=user.getHashtable("ExtInfo"); //$NON-NLS-1$
			if(h!=null){
				String grp=(String)h.get("Groups"); //$NON-NLS-1$
				if(grp!=null){
					String[] grps=grp.split(","); //$NON-NLS-1$
					for(String g:grps){
						list.add(g);
					}
					usergroups.put(user.getId()+"#groups#",list); //$NON-NLS-1$
				}
			}
		}
		// The list is never null here, but might be empty
		for(String g:list){
            if(ADMIN_GROUP.equals(g)){ //$NON-NLS-1$
            	// If the user is member of the admin groups, he has any right
                return true;
            }
			if(rights.get(g+right)!=null){
				return true;
			}
		}
		// Falls das gewünschte Recht nicht geregelt ist, eine Hierarchiestufe höher suchen
		int pos=right.lastIndexOf('/');
		if(pos!=-1){
			return request(user,right.substring(0, pos));
		}
		return false;
	}
	
    /**
     * Zugriffsrecht(e) erteilen
     * @param user Anwender, der diese Rechte erhalten soll
     * @param elements ein oder mehrere Rechte
     */
	public void grant (Anwender user, String... elements){
        for(String right:elements){
            rights.put(user.getId()+right,"1"); //$NON-NLS-1$
        }
	}
	
    /**
     * Zugriffsrechte entziehen
     * @param user Anwender, dem diese Rechte entzogen werden sollen
     * @param elements ein oder mehrere Rechte
     */
	public void revoke(Anwender user, String... elements){
        for(String right:elements){
            rights.remove(user.getId()+right);
        }
	}
	
    /**
     * Zugriffsrechte erteilen
     * @param group Gruppe, der diese Rechte erteilt werden sollen
     * @param elements ein oder mehrere Rechte
     */
	public void grant (String group, String... elements){
        for(String right:elements){
            rights.put(group+right,"1"); //$NON-NLS-1$
        }
	}
    /**
     * Zugriffsrechte entziehem
     * @param group Gruppe
     * @param elements ein oder mehrere Rechte
     */
	public void revoke(String group, String... elements){
        for(String right:elements){
            rights.remove(group+right);
        }
	}
	
	/**
	 * Zugriffsrecht für "self" erteilen
	 * 
	 */
	public void grantForSelf(String... elements){
		for(String r:elements){
			rights.put("Self"+r,"1"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void revokeFromSelf(String...strings ){
		for(String e:strings){
			rights.remove("Self"+e); //$NON-NLS-1$
		}
	}
    /**
     * Einen Anwender einer Gruppe zufügen
     * @param group Die Gruppe, der der Anwender angeschlossen werden soll
     * @param user der Anwender
     */
	public void addToGroup(String group, Anwender user){
		String g=remove(group,user);
		g=g+","+group; //$NON-NLS-1$
		user.setInfoElement("Groups",g); //$NON-NLS-1$
	}
    
    /**
     * Einen Anwender aus einer Gruppe entfernen
     * @param group Gruppe, aus der der Anwender austreten soll
     * @param user der Anwender
     */
	public void removeFromGroup(String group, Anwender user){
		String g=remove(group,user);
		user.setInfoElement("Groups",g); //$NON-NLS-1$
	}
	private String remove(String group, Anwender user){
		String g=(String)user.getInfoElement("Groups"); //$NON-NLS-1$
		if(g!=null){
			g=g.replaceAll(user.getId(),""); //$NON-NLS-1$
			g=g.replaceAll("\\s*,*$",""); //$NON-NLS-1$ //$NON-NLS-2$
			return g;
		}
		return ""; //$NON-NLS-1$
	}
	
	public List<String> getGroups(){
		ArrayList<String> ret=new ArrayList<String>();
		String grp=Hub.globalCfg.get(PreferenceConstants.ACC_GROUPS, ADMIN_GROUP);
		for(String s:grp.split(",")){
			ret.add(s);
		}
		return ret;
	}
	public List<String> groupsForGrant(String right){
		ArrayList<String> ret=new ArrayList<String>();
		Pattern p=Pattern.compile("([a-zA-Z0-9]+)"+right); //$NON-NLS-1$

		Enumeration e=rights.keys();
		while(e.hasMoreElements()){
			String k=(String)e.nextElement();
			Matcher m=p.matcher(k);
			if(m.matches()){
				String grp=m.group(1);
				Anwender an=Anwender.load(grp);
				if(an==null){
					ret.add(grp);
				}
			}
		}
		return ret;
	}
	public List<Anwender> usersForGrant(String right){
		ArrayList<Anwender> ret=new ArrayList<Anwender>();
		Pattern p=Pattern.compile("([a-zA-Z0-9]+)"+right); //$NON-NLS-1$

		Enumeration e=rights.keys();
		while(e.hasMoreElements()){
			String k=(String)e.nextElement();
			Matcher m=p.matcher(k);
			if(m.matches()){
				String grp=m.group(1);
				Anwender an=Anwender.load(grp);
				if((an!=null) && an.exists()){
					ret.add(an);
				}
			}
		}
		return ret;
	}
	public void deleteGrant(String grant){
		Pattern p=Pattern.compile("([a-zA-Z0-9]+)"+grant); //$NON-NLS-1$

		Enumeration e=rights.keys();
		while(e.hasMoreElements()){
			String k=(String)e.nextElement();
			Matcher m=p.matcher(k);
			if(m.matches()){
				rights.remove(k);
			}
		}
	}
	public Settings asSettings(){
		return new InMemorySettings(rights);
	}
    /** Alles auf Standard zurücksetzen und dbUID generieren */
	public void reset(){
	    rights.clear();
        grant(ALL_GROUP,AccessControlDefaults.getAlle()); //$NON-NLS-1$
        grant(USER_GROUP,AccessControlDefaults.getAnwender()); //$NON-NLS-1$
        //grant(ADMIN_GROUP,AccessControlDefaults.Admin); //$NON-NLS-1$
        rights.put("dbUID", StringTool.unique("db%id"));
        flush();
    }
	
	public String getDBUID(){
		String dbuid=rights.get("dbUID");
		if(dbuid==null){
			dbuid=StringTool.unique("db%id");
			rights.put("dbUID", dbuid);
	        flush();
		}
		return dbuid;
	}
}
