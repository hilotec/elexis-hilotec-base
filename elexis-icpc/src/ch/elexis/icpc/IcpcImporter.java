/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: IcpcImporter.java 1723 2007-02-02 21:17:08Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.PersistentObject;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;


public class IcpcImporter extends ImporterPage {
	ImporterPage.DBBasedImporter dbi;
	JdbcLink j,pj;
	
	
	public IcpcImporter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		dbi=new ImporterPage.DBBasedImporter(parent,this);
		dbi.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		return dbi;
	}

	public boolean connect(){
        String type = results[0];
        if (type != null) {
            String server = results[1];
            String db = results[2];
            String user = results[3];
            String password = results[4];
            
            if (type.equals("MySQL")) {
                j = JdbcLink.createMySqlLink(server, db);
                return j.connect(user, password);
            } else if (type.equals("PostgreSQL")) {
                j = JdbcLink.createPostgreSQLLink(server, db);
                return j.connect(user, password);
            } else if (type.equals("ODBC")) {
                j = JdbcLink.createODBCLink(db);
                return j.connect(user, password);
            }
        }
        
        return false;
	}
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Importiere ICPC-2", 727);
		monitor.subTask("Verbinde");
		if(!connect()){
			monitor.done();
			return new Status(Status.ERROR,"Icpc",1,"Konnte keine Verbindung herstellen",null);
		}
		pj=PersistentObject.getConnection();
		Stm stmSrc=j.getStatement();
		monitor.subTask("Lösche alte Daten");
		pj.exec("DROP INDEX "+IcpcCode.TABLENAME+"_IDX1 ON "+IcpcCode.TABLENAME);
		pj.exec("DROP TABLE "+IcpcCode.TABLENAME);
		IcpcCode.initialize();
		monitor.worked(1);
		monitor.subTask("Lese Daten ein");
		PreparedStatement ps=pj.prepareStatement("INSERT INTO "+IcpcCode.TABLENAME+" (" +
				"ID,component,txt,short,icd10,criteria,inclusion,exclusion,consider,note)"+
				"VALUES (?,?,?,?,?,?,?,?,?,?);");
		monitor.worked(1);
		try{
			ResultSet res=stmSrc.query("SELECT * FROM ICPC2");
			while(res.next()){
				ps.setString(1, res.getString(1));
				ps.setString(2, res.getString(2));
				ps.setString(3, res.getString(3));
				ps.setString(4, res.getString(4));
				ps.setString(5, res.getString(5));
				ps.setString(6, res.getString(6));
				ps.setString(7, res.getString(7));
				ps.setString(8, res.getString(8));
				ps.setString(9, res.getString(9));
				ps.setString(10, res.getString(10));
				ps.execute();
				if(monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
				monitor.worked(1);
			}
			monitor.done();
		}catch(Exception ex){
			ExHandler.handle(ex);
			return new Status(Status.ERROR,"ICPC",3,ex.getMessage(),null);
		}finally{
			j.releaseStatement(stmSrc);
		}
		return Status.OK_STATUS;
	}

	@Override
	public String getDescription() {
		return "International Classification of Primary Care";
	}

	@Override
	public String getTitle() {
		return "ICPC-2";
	}

}
