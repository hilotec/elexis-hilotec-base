// $Id: JdbcLink.java 3969 2008-05-27 12:31:42Z michael_imhof $

package ch.rgw.tools;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Vector;


/**
 * Weiterer Abstraktionslayer zum einfacheren Zugriff auf eine jdbc-fähige Datenbank
 */

public class JdbcLink {
  public static final String getVersion(){return "2.3.2";}
  public int lastErrorCode;
  public String lastErrorString;
  public int verMajor=0;
  public int verMinor=0;
  public String DBFlavor=null;
  String sDrv;
  String sConn;
  java.sql.Connection conn=null;
  private Vector statements;
  public int keepStatements=10;
  
  private static Log log;

  public static final int CONNECT_SUCCESS=0;
  public static final int CONNECT_CLASSNOTFOUND=1;
  public static final int CONNECT_FAILED=2;
  public static final int CONNECT_UNKNOWN_ERROR=10;
  public static final int TRANSACTION_COMMIT_FAILED=21;
  public static final int TRANSACTION_ROLLBACK_FAILED=22;
  public static final int TRANSACTION_COMMIT_NOT_SUPPORTED=23;
  public static final int CONNECTION_CANT_CREATE_STATEMENT=30;
  public static final int CONNECTION_CANT_PREPARE_STAMENT = 31;
  
  static
  { log=Log.get("jdbcLink");
  }
  private JdbcLink()
  {}
  /**
   * Bequemlichkeitsmethode, um einen JdcbLink auf eine MySQL-Datenbank zu erhalten
   */
  public static JdbcLink createMySqlLink(String host, String database)
  {	String driver="com.mysql.jdbc.Driver";	
  	String connect="jdbc:mysql://"+host+":3306/"+database;
  	return new JdbcLink(driver,connect,"mysql");
  }

  /**
   * Bequemlichkeitsmethode, um einen JdbcLink auf eine InProcess laufende
   * HSQL-Datenbank zu erhalten 
   * @param database ein Dateiname für die zu erzeugende bzw. zu verwendende Datenbank
   */
  public static JdbcLink createInProcHsqlDBLink(String database)
  {		String driver="org.hsqldb.jdbcDriver";
  		String connect="jdbc:hsqldb:"+database;
  		return new JdbcLink(driver,connect,"hsqldb");
  }
  /**
   * Bequemlichkeitsmethode, um einen JdbcLink auf eine als Server laufende
   * HSQL-Datenbank zu erhalten
   * @param host Server, auf dem die Datenbank läuft.
   */
  public static JdbcLink createHsqlDBLink(String host)
  {		String driver="org.hsqldb.jdbcDriver";
  		String connect="jdbc:hsqldb:hsql://"+host;
  		return new JdbcLink(driver,connect,"hsqldb");
  }
  /**
   * Bequemlichkeitsmethode für einen JdbcLink auf einen 4D-Server
   * @param host de Server, auf dem die 4D-Datenbnak läuft
   * @return
   */
  public static JdbcLink create4DLink(String host){
	  String driver="com.fourd.jdbc.DriverImpl";
	  String connect="jdbc:4d:"+host+":19813";
	  return new JdbcLink(driver,connect,"4d");
  }
  /** Bequemlichkeitsmethode für einen JdbcLink auf einen PostgreSQL-
   * Server
   * @param host
   * @return
   */
  public static JdbcLink createPostgreSQLLink(String host, String database){
      String driver="org.postgresql.Driver";
      String connect="jdbc:postgresql://"+host+":5432/"+database;
      return new JdbcLink(driver,connect,"postgresql");
  }
  
  public static JdbcLink createODBCLink(String dsn){
	  String driver="sun.jdbc.odbc.JdbcOdbcDriver";
	  String connect="jdbc:odbc:"+dsn;
	  return new JdbcLink(driver,connect,"ODBC");
  }
  /**
   * Erstelle einen neuen jdbcLink. Es wird kein Connect-Versuch gemacht, das heisst,
   * der Konstruktor wird nie scheitern.
   * @param driver Treiber-String (wie org.hsql.jdbc)
   * @param connect Connect-String (wie jdbc:odbc:data)
   */
  public JdbcLink(String driver,String connect,String flavor) {
    sDrv=driver;
    sConn=connect;
    DBFlavor=flavor.toLowerCase();
  }

  /**
   * Verbindung zur Datenbank herstellen
   * @param user Username, kann null sein
   * @param password Passwort, kann null sein
   * @return errcode
   */
  public boolean connect(String user, String password)
  {
     try{
      //Driver D=(Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
     
      Driver D=(Driver)Class.forName(sDrv).newInstance();
      verMajor=D.getMajorVersion();
      verMinor=D.getMinorVersion();
      
      //Class.forName("org.firebirdsql.jdbc.FBDriver");
      // "jdbc:mysql://<host>:<port>/<dbname>"
      // "jdbc:odbc:<dsn>
      conn=DriverManager.getConnection(sConn,user,password);
      statements=new Vector();
      lastErrorCode=CONNECT_SUCCESS;
      lastErrorString="Connect successful";
      log.log("Connect successful",Log.DEBUGMSG);
      return true;
    }
    catch(ClassNotFoundException ex) {
        lastErrorCode=CONNECT_CLASSNOTFOUND;
        lastErrorString=ex.getMessage();
    }
    catch(SQLException ex) {
       lastErrorCode=CONNECT_FAILED;
       lastErrorString=ex.getMessage();
    }
    catch(Throwable e)
    {	ExHandler.handle(e);
        lastErrorCode=CONNECT_UNKNOWN_ERROR;
        lastErrorString=e.getMessage();
    }
    log.log("Connect failed: "+lastErrorString,Log.ERRORS);
    return false;
  }
  public JdbcLink(Connection c){
	  conn=c;
	  statements=new Vector();
  }
  
/**
 * Utility-Funktion zum Einpacken von Strings in Hochkommata
 * und escapen illegaler Zeichen
 * @param s der String
 * @return Datenbankkonform eingepackte String
 */
  public static String wrap(String s)
  { 	
	  if(StringTool.isNothing(s)){
		  return "''";
	  }
      try {
		return wrap(s.getBytes("UTF-8"), "mysql");
	} catch (UnsupportedEncodingException e) {
		ExHandler.handle(e);
		return wrap(s.getBytes(), "mysql");
	}
  }
  
  public String wrapFlavored(String s){
	  if(StringTool.isNothing(s)){
		  return "''";
	  }
      try {
		return wrap(s.getBytes("UTF-8"), DBFlavor);
	} catch (UnsupportedEncodingException e) {
		ExHandler.handle(e);
		return wrap(s.getBytes(), DBFlavor);
	}
  }
  /**
   * Utility-Funktion zum Datenbankkonformen Verpacken von byte arrays
   * zwecks Einfügen in BLOB-Felder.
 * @param flavor TODO
 * @param b das rohe byte array
   * @return das verpackte array in Form eines String
   */
  public static String wrap(byte[] in, String flavor){
	
	  byte[] out=new byte[2*in.length+2];
	  int j=0;
	  out[j++]='\'';
	  for(int i=0;i<in.length;i++){
		  switch(in[i]){
          case  0: 
          case 34: 
         
          case '\'':
        	  if(flavor.startsWith("hsql")){
        		  out[j++]='\'';
        		  break;
        	  }
          case 92: boolean before = (i > 1 && in[i-1] == 92);
          		   boolean after = (i < in.length - 1 && in[i+1] == 92);
          		   if (!before && !after) {
          			   out[j++]='\\';
          		   }
          }
		  out[j++]=in[i];
	  }
	  out[j++]='\'';
	  try {
		return new String(out,0,j,"UTF-8");
	} catch (UnsupportedEncodingException e) {
		ExHandler.handle(e);
		return null;
	}
  }
  
  public Connection getConnection(){
	  return  conn;
  }
  public String getDriverName(){
	  return sDrv;
  }
  public String getConnectString(){
	  return sConn;
  }
  /**
   * Ent Statement aus dem pool beziehen. Jedes mit getStatement bezogene
   * Statement MUSS mit releaseStatement wieder zurückgegeben werden.
   * @return ein Stm (JdbcLink-spezifische Statement-Variante) 
   */
  public Stm getStatement(){
	  synchronized(statements){
		  if(statements.isEmpty()){
			  try{
				  return new Stm();
			  }catch(Throwable ex){
				  ExHandler.handle(ex);
				  lastErrorCode=CONNECTION_CANT_CREATE_STATEMENT;
				  lastErrorString=ex.getMessage();
				  log.log(lastErrorString,Log.ERRORS);
				  return null;
			  }
	
		  }else{
			  return (Stm)statements.remove(0);
		  }
	  }
  }
  
  /**
   * Ein Stm - Statement in den pool zurückgeben. Die Zahl der im pool
   * zu haltenden Statements wird mit keepStatements definiert.
   * @param s
   */
  @SuppressWarnings("unchecked")
public void releaseStatement(Stm s){
	  synchronized(statements){
		  if(s!=null){
			  if(statements.size()<keepStatements){
				  statements.add(s);
			  }else{
				  s.delete();
			  }
		  }
	  }
  }
  
  /**
   * Ein Prepared Statement anlegen
   * @param sql Abfrage für das statement (eizusetzende Parameter müssen 
   * als ? gesetzt sein
   * @return das vorkompilierte PreparedStatement
   */
  public PreparedStatement prepareStatement(String sql){
      try{
          return conn.prepareStatement(sql);
      }catch(Exception ex){
          ExHandler.handle(ex);
          lastErrorCode=CONNECTION_CANT_PREPARE_STAMENT;
          lastErrorString=ex.getMessage();
          log.log(lastErrorString,Log.ERRORS);
          return null;
      }
  }
  public static final int INTEGRAL=1;
  public static final int TEXT=2;
  public static final int BINARY=3;
  public static final int OTHER=4;
  
  public static int generalType(int t){
      switch(t) {
          case Types.BIGINT:
          case Types.BIT:
          case Types.BOOLEAN:
          case Types.INTEGER:
          case Types.SMALLINT:
          case Types.TINYINT:
              return INTEGRAL;
          
          case Types.VARCHAR:
          case Types.CHAR:
          case Types.LONGVARCHAR:
              return TEXT;
          
          case Types.BINARY:
          case Types.BLOB:
          case Types.CLOB:
          case Types.LONGVARBINARY:
          case Types.VARBINARY:
              return BINARY;
          
          default:
              return OTHER;
              
      }
  }
  
/**
 * Einen String-Value aus dem aktuellen Datensatz des ResultSets holen. Es
 * wird garantiert, dass immer etwas zurückgeliefert wird (" " für den leeren Sring)
 * @param r ResultSet
 * @param field Feldname
 * @return den String
 * @throws Exception Wenn das Feld nicht definiert ist.
 */
  public static String getString(ResultSet r, String field) throws Exception  {
	  String res = r.getString(field);
	  if (StringTool.isNothing(res)){
		  return " ";
	  }

	  return res;
  }


  /**
   * Verbindung zur Datenbank lösen
   *
   */
  public synchronized void disconnect()
  { try{
	  while((statements!=null) && (!statements.isEmpty())){
		  Stm stm=(Stm)statements.remove(0);
		  stm.delete();
	      stm=null;
	  }
	  if(conn!=null){
		  conn.close();
	  }
      statements=null;
      log.log("Disconnected",Log.INFOS);
    }
    catch(Exception e)
    { ExHandler.handle(e);
    }
  }
  /**
   * Anfrage, ob die Verbindung steht
   * @todo Muss noch besser implementiert werden
   * @return true wenn die Verbindung steht.
   */
  public boolean isAlive()
  { return (verMajor!=0);
  }
  public boolean setAutoCommit(boolean value){
	  try{
		  conn.setAutoCommit(value);
		  return true;
	  }
	  catch (Exception e) {
		  ExHandler.handle(e);
		  lastErrorCode=TRANSACTION_COMMIT_NOT_SUPPORTED;
		  lastErrorString=e.getMessage();
		  return false;
	}
  }
  public boolean commit(){
	  try {
		  conn.commit();
		  return true;
	} catch (Exception e) {
		ExHandler.handle(e);
		lastErrorCode=TRANSACTION_COMMIT_FAILED;
		lastErrorString=e.getMessage();
		return false;
	}

  }
  public boolean rollback(){
	  try {
		conn.rollback();
		return true;
	} catch (Exception e) {
		ExHandler.handle(e);
		lastErrorCode=TRANSACTION_ROLLBACK_FAILED;
		lastErrorString=e.getMessage();
		return false;
	}
  }
  
    /**
   * Unscharfes Suchen im ResultSet.
   * @param r das zu durchsuchende ResultSet
   * @param field Name des interessiernden Felds
   * @param m (vorher konfigurierter) fuzzyMatcher mit der Suchbedingung
   * @see ch.rgw.tools.FuzzyMatcher
   * @return true wenn gefunden; das ResultSet steht auf der ersten 
   * oder einzigen Fundstelle.
   */
  public static boolean nextMatch(ResultSet r, String field, FuzzyMatcher m)
  {   try{
        while(r.next())
        { if(m.match(r.getString(field)))
          { return true;
          }
        }
        return false;
      }
      catch(Exception ex)
      { ExHandler.handle(ex);
      }
      return false;
  }
  
    
  public String dbDriver()
  {
  		return sDrv;
  }
  
  /**
   * Einen String-Wert abfragen. Temporäres Statement erzeugen
   * @param sql SQL-String, der ein VARCHAR-oder Text-Feld liefern sollte
   * @return den gefundenen String oder null: nicht gefunden
   */
  public String queryString(String sql)
  { Stm stm=getStatement(); 
  	String res=stm.queryString(sql);
    releaseStatement(stm);
    return res;
  }
  public int queryInt(String sql){
	  Stm stm=getStatement();
	  int res=stm.queryInt(sql);
	  releaseStatement(stm);
	  return res;
  }
  public boolean execScript(InputStream i, boolean translate, boolean stopOnError){
	  Stm stm=getStatement();
	  boolean ret=stm.execScript(i,translate, stopOnError);
	  releaseStatement(stm);
	  return ret;
  }
  /** 
   * Wrapper für Stm#exec
   * @author gerry
   *
   */
  public int exec(String sql)
  {
	  Stm stm=getStatement();
	  int res=stm.exec(sql);
	  releaseStatement(stm);
	  return res;
  }
  public class Stm{
	  private Statement stm;
	  
	  Stm() throws Throwable
	  {
		  stm= conn.createStatement();
	  }
	  public void delete(){
		  try{
			  //stm.cancel();
			  stm.close();
		  }
		  catch(Exception ex){
			  ExHandler.handle(ex);
			  /* egal */
		  }
		  stm=null;
	  }
	  /**
	   * Eine execute auf die Datanbank ausführen
	   * @param SQLText Von der Datenbank verstandener SQL-String
	   * @return Zahl der affected rows.
	   */
	  public synchronized int exec(String SQLText)
	  { log.log("executing "+SQLText,Log.DEBUGMSG);
	    try{
	      return stm.executeUpdate(SQLText);
	      }
	    catch(Exception e)
	    { ExHandler.handle(e);
	      log.log("Fehler bei: "+SQLText,Log.ERRORS);
	      return 0;
	    }
	  }
	
	  /**
	   * Eine SQL-Anfrage an die Datenbank senden
	   * @param SQLText ein Query String in von der Datenbank verstandener Syntax
	   * @return ein ResultSet oder null bei Fehler
	   */
	  public synchronized ResultSet query(String SQLText)
	  { ResultSet res=null;
	    log.log("querying "+SQLText,Log.DEBUGMSG);
	    try{
	        res=stm.executeQuery(SQLText);
	        return res;
	    }
	    catch(Exception e)
	    { ExHandler.handle(e);
	      log.log("Fehler bei: "+SQLText,Log.ERRORS);
	      return null;
	    }

	  }

	  
	  
	  /**
	   * Eine Anzahl Werte als Vector zurückliefern
	   * @param sql SQL-String, der die Werte liefert
	   * @param fields interessierende Felder
	   * @return einen Vector aus Object[] Arrays mit den interessierenden
	   * Feldern aller gefundenen Datensätze
	   */
	  @SuppressWarnings("unchecked")
	public Vector queryList(String sql, String[] fields)
	  {	Vector rs=new Vector();
	  	log.log("executing "+sql,Log.DEBUGMSG);
	  	ResultSet res=null;
	  	try{	
	  		res=stm.executeQuery(sql);
	  		if(res!=null)
	  		{	while(res.next())
	  			{	Object[] o=new Object[fields.length];
	  				for(int i=0;i<fields.length;i++)
	  				{	o[i]=res.getObject(fields[i]);
	  				}
	  				if(fields.length==1)
	  				{	rs.add(o[0]);
	  				}
	  				else
	  				{	rs.add(o);
	  				}
	  			}
	  		}
	  	}
		catch(SQLException ex)
		{ ExHandler.handle(ex);
		}
	  	return rs;
	  }  
	  public String queryString(String sql)
	  {	  ResultSet res=null;
		  try{
		      res=stm.executeQuery(sql);
			    if(res.next())
			    {	String r=res.getString(1);
			    	if((r==null) || (r.equals("null")) || (r.equals(""))){
		                	return "";
			    	}
			    	return r;
			    }
			    return null;
		    }
		    catch(SQLException ex)
		    { ExHandler.handle(ex);
		    }
		    return null;
	  }
	  
	  /**
	   * Einen Integer-Wert abfragen.
	   * @param sql SQL-String, der ein Integer-Feld liefern sollte
	   * @return den ersten der Suchbedingung entsprechenden Integer-Wert
	   * oder -1: Wert nicht gefunden.
	   */
	  public int queryInt(String sql)
	  {     ResultSet res=null;
	      try{
	            res=stm.executeQuery(sql);
	              if(res.next())
	              { return res.getInt(1);
	              }
	          }
	          catch(SQLException ex)
	          { ExHandler.handle(ex);
	          }
	          return -1;
	  }
	  
      /**
       * Ein SQL-Script einlesen und ausführen.
       * alles nach # bis zum Zeilenende wird ignoriert
       * @param s der InputStream mit dem Script
     * @param translate true, wenn das Script zu den bekannten
       * Dialekten übersetzt werden soll 
     * @param stopOnError true: Abbruch des Scripts, wenn ein Fehler auftritt
       * @return false wenn ein Fehler passiert ist.
       */
	  public boolean execScript(InputStream s,boolean translate, boolean stopOnError)
	  {   String sql="<none>";
	  	  if(s==null){
	  		  return false;
	  	  }
          //autoc=conn.getAutoCommit();
          //setAutoCommit(false);
          while((sql=readStatement(s))!=null) {
              log.log(sql,Log.DEBUGMSG);
              if(translate){
            	  sql = translateFlavor(sql);
              }
              System.out.println(sql);
              try{
            	  stm.execute(sql);
              } catch(Exception ex){
            	  ExHandler.handle(ex);
            	  if(stopOnError==true){
            		  return false;
            	  }
              }
          }
          //commit();
          return true;
      	}       
	    
	  }  
	  public static String readStatement(InputStream is)
	  {
	      StringBuffer inp=new StringBuffer(1000);
	      String sql="<none>";
	      try {
	          int c;
              boolean comment=false;
	          while(((c=is.read())!=-1)) {
	              if(c==';') {
	                  break;
	              }
                  if(c=='#'){
                      comment=true;
                  }
                  if((c=='\r')||(c=='\n')){
                      comment=false;
                  }
                  if(comment==false){
                      inp.append((char)c);
                  }
	          }
              //sql=inp.toString().replace("#.+$","");
              //sql=sql.replace("--[^\\r]*","").trim();
	          sql=inp.toString().replaceAll("[\\n\\r\\t]"," ");
	          sql=sql.replaceAll(" {2,}"," ").trim();
	          if(sql.length()<4) {
	              return null;
	          }
	          return sql;
	      }catch(Exception ex) {
	          ExHandler.handle(ex);
	          return null;
	      }
	     
	      
	  }	  
  
  public boolean dumpTable(BufferedWriter w,String name) throws Exception
	{	Stm stm=getStatement();
		ResultSet res=stm.query("SELECT * from "+name);
	    ResultSetMetaData rm=res.getMetaData();
	    int cols=rm.getColumnCount();
	    String[] ColNames=new String[cols];
        int[] colTypes=new int[cols];
	    w.write("CREATE TABLE "+name+"(");
	    for(int i=0;i<cols;i++){
	        ColNames[i]=rm.getColumnName(i+1);
            colTypes[i]=rm.getColumnType(i+1);
            w.write(ColNames[i]+" "+colTypes[i]+",\n");
	    }
	    w.write(");");
      
	    while((res!=null) && (res.next()==true)){
	        w.write("INSERT INTO "+name+" (");
	        for(int i=0;i<cols;i++){
	            w.write(ColNames[i]);
	            if(i<cols-1){
	                w.write(",");
	            }
	        }
	        w.write(") VALUES (");
	        for(int i=0;i<cols;i++){
                Object o=res.getObject(ColNames[i]);
                switch(JdbcLink.generalType(colTypes[i])) {
                    case JdbcLink.INTEGRAL:
                        if(o==null) {
                            w.write("0");
                        }else {
                            w.write(Integer.toString(((Integer)o).intValue()));
                        }
                        break;
                    case JdbcLink.TEXT:
                        if(o==null) {
                            w.write(JdbcLink.wrap("null"));
                        }else {
                            w.write(JdbcLink.wrap((String)o));
                        }
                        break;
                        
                     default:
                        String t=o.getClass().getName();
                        log.log("Unknown type "+t,Log.ERRORS);
                        throw new Exception("Cant write "+t);
                        
                }
                if(i<cols-1){
	                w.write(",");
	            }
	        }
 	        w.write(");");
            w.newLine();
	    }
	    res.close();
	    releaseStatement(stm);
	    return true;
  }
 
  /** Einen SQL-String in die bekannten flavors übersetzen. Basisdialekt ist mysql */
public String translateFlavor(String sql) {
	//sql=sql.toLowerCase();
	// TODO: Konzept für case-sensitiveness klarer definieren
	  if(DBFlavor.equalsIgnoreCase("postgresql")){
	      sql=sql.replaceAll("BLOB","BYTEA");
	      sql=sql.replaceAll("DROP INDEX (.+?) ON .+?;", "DROP INDEX $1;");
	      sql=sql.replaceAll("MODIFY\\s+(\\w+)\\s+(.+)","ALTER COLUMN $1 TYPE $2");
	      sql=sql.replaceAll("SIGNED", "INT");
	  }else if(DBFlavor.startsWith("hsqldb")){
	      sql=sql.replaceAll("TEXT","LONGVARCHAR");
	      sql=sql.replaceAll("BLOB","LONGVARBINARY");
	      sql=sql.replaceAll("CREATE +TABLE","CREATE CACHED TABLE");
	      sql=sql.replaceAll("DROP INDEX (.+?) ON .+?;", "DROP INDEX $1;");
	      sql=sql.replaceAll("MODIFY (.+)", "ALTER COLUMN $1");
	  }else if(DBFlavor.equalsIgnoreCase("mysql")){
		  sql=sql.replaceAll("BLOB", "LONGBLOB");
		  sql=sql.replaceAll("TEXT", "LONGTEXT");
		  /* experimental - do not use */
	  }else if(DBFlavor.equalsIgnoreCase("db2")){	
		  sql=sql.replaceAll("VARCHAR\\s*\\([0-9]+\\)", "VARCHAR");
		  sql=sql.replaceAll("TEXT", "CLOB");
		  /*/experimental */
	  }
	return sql;
}
}