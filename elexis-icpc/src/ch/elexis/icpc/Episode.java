/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Episode.java 2888 2007-07-24 14:50:07Z danlutz $
 *******************************************************************************/
package ch.elexis.icpc;

import java.io.ByteArrayInputStream;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionInfo;

public class Episode extends PersistentObject {
    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

	private static final String VERSION="0.3.0";
	private final static String TABLENAME="CH_ELEXIS_ICPC_EPISODES";
		
    private static final String INACTIVE_VALUE = "0";
    private static final String ACTIVE_VALUE = "1";

    private final static String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25),"+
		"deleted 		CHAR(1) default '0',"+
		"PatientID		VARCHAR(25),"+
		"Title			VARCHAR(80),"+
	    "StartDate      VARCHAR(20),"+  // date of first occurrence; may be simply a year or any text 
	    "Number         VARCHAR(10),"+  // number for individual, possibly hierarchical, organization
	    "Status         CHAR(1) DEFAULT '1'"+  // status, '1' == active, '0' == inactive
		");"+
		
		"CREATE INDEX "+TABLENAME+"1 ON "+TABLENAME+" (PatientID);"+
		
		"INSERT INTO "+TABLENAME+" (ID,Title) VALUES ('1',"+JdbcLink.wrap(VERSION)+");";
	
	static{
		addMapping(TABLENAME, "PatientID","Title", "StartDate", "Number", "Status");
		Episode version=load("1");
		if(!version.exists()){
			try{
				ByteArrayInputStream bais=new ByteArrayInputStream(createDB.getBytes("UTF-8"));
				j.execScript(bais,true, false);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}else{
			VersionInfo vi=new VersionInfo(version.get("Title"));
			if(vi.isOlder(VERSION)){
				if(vi.isOlder("0.2.0")){
					PersistentObject.j.exec("ALTER TABLE "+TABLENAME+" ADD deleted CHAR(1) default '0';");
					version.set("Title", VERSION);
				}

				if (vi.isOlder("0.3.0")) {
					String sql;
					
					// add column StartDate
					sql = "ALTER TABLE " + TABLENAME + " ADD StartDate VARCHAR(20);";
					j.exec(j.translateFlavor(sql));
					
					// add column Number
					sql = "ALTER TABLE " + TABLENAME + " ADD Number VARCHAR(10);";
					j.exec(j.translateFlavor(sql));
					
					// add column Status
					sql = "ALTER TABLE " + TABLENAME + " ADD Status CHAR(1) DEFAULT '1';";
					j.exec(j.translateFlavor(sql));

					version.set("Title", VERSION);
				}
			}
		}
	}
	
	public Episode(Patient pat, String title){
		create(null);
		set(new String[]{"PatientID","Title"},pat.getId(),title);
	}
	@Override
	public String getLabel() {
		String title = get("Title");
		String startDate = get("StartDate");
		String number = get("Number");
		int status = getStatus();
		
		StringBuffer sb = new StringBuffer();
		
		if (!StringTool.isNothing(startDate)) {
			sb.append(startDate);
			sb.append(": ");
		}
		
		sb.append(title);
		
		if (!StringTool.isNothing(number)) {
			sb.append(" (" + number + ")");
		}
		
		if (status == INACTIVE) {
			sb.append(" [" + getStatusText() + "]");
		}
		
		return sb.toString();
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	
	public static Episode load(String id){
		return new Episode(id);
	}
	
	protected Episode(String id){
		super(id);
	}
	protected Episode(){}
	
	@Override
	public boolean isDragOK() {
		return true;
	}

	/**
	 * Get the status of an episode
	 * @return Episode.ACTIVE or Episode.INACTIVE
	 */
    public int getStatus() {
        String statusText = get("Status");
        if (statusText.equals(ACTIVE_VALUE)) {
            return ACTIVE;
        } else {
            return INACTIVE;
        }
    }

    /**
     * Get the status localized text
     * @return the status as localized text
     */
    public String getStatusText() {
        int status = getStatus();
        if (status == ACTIVE) {
            return Messages.Active;
        } else {
            return Messages.Inactive;
        }
    }

    /**
     * Set the status of an episode
     * @param status Episode.ACTIVE or Episode.INACTIVE
     */
    public void setStatus(int status) {
    	switch (status) {
    	case ACTIVE:
    		set("Status", ACTIVE_VALUE);
    		break;
    	case INACTIVE:
    		set("Status", INACTIVE_VALUE);
    		break;
    	default:
    		set("Status", ACTIVE_VALUE);
    		break;
    	}
    }

}
