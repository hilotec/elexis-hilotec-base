/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: PersistentObject.java 3856 2008-05-02 11:58:31Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import com.mysql.jdbc.PacketTooBigException;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Xid.XIDException;
import ch.elexis.data.cache.SoftCache;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.preferences.PreferenceInitializer;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.util.DBUpdate;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.Compress.CompEx;
import ch.rgw.IO.Resource;
import ch.rgw.IO.Settings;
import ch.rgw.IO.SqlSettings;
import ch.rgw.net.NetTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.JdbcLink.Stm;


/**
 * Base class for all objects to be stored in the database. A PersistentObject has an unique ID, which is
 * assigned as the object is created. Every object is accessed "lazily" which means that "loading" an object
 * instantiates only a proxy with the ID of the requested object. Members are read only as needed.
 * The class provides static functions to log into the database, and provides methods for reading and writing
 * of fields for derived classes.
 * The get method uses a cache to reduce the number of costly database operations. Repeated read-requests
 * within a configurable life-time (defaults to 15 seconds) are satisfied from the cache.
 * PersistentObject can log every write-access in a trace-table, as desired.
 * get- and set- methods perform necessary coding/decoding of fields as needed. 
 * 
 * Basisklasse für alle Objekte, die in der Datenbank gespeichert werden sollen. Ein PersistentObject
 * hat eine eindeutige ID, welche beim Erstellen des Objekts automatisch vergeben wird. Grundsätzlich wird
 * jedes Objekt "lazy" geladen, indem jede Leseanforderung zunächst nur einen mit der ID des Objekts versehenen
 * Proxy instantiiert und jedes Member-Feld erst auf Anfrage nachlädt.
 * Die Klasse stellt statische Funktionen zur Kontaktaufnahme mit der Datenbank und member-Funktionen
 * zum Lesen und Schreiben von Feldern der Tochterobjekte zur Verfügung.
 * Die get-Methode verwendet einen zeitlich limitierten Cache. um die Zahl teurer Datenbankoperationen
 * zu minimieren: Wiederholte Lesezugriffe innerhalb einer einstellbaren lifetime (Standardmässig
 * 15 Sekunden) werden aus dem cache bedient.
 * PersistentObject kann auch alle Schreibvorgänge in einer speziellen Trace-Tabelle dokumentieren.
 * Die get- und set- Methoden kümmern sich selbst um codierung/decodierung der Felder, wenn nötig.
 * Aufeinanderfolgende und streng zusammengehörende Schreibvorgänge können auch in einer Transaktion
 * zusammengefasst werden, welche nur ganz oder gar nicht ausgeführt wird. (begin()). Es ist aber zu
 * beachten, das nicht alle Datenbanken Transaktionen unterstützen. MySQL beispielsweise nur, wenn es
 * mit InnoDB-Tabellen eingerichtet wurde (welche langsamer sind, als die standardmässig verwendeten
 * MyISAM-Tabellen).
 * @author gerry
 */
public abstract class PersistentObject{
	public static final int CACHE_DEFAULT_LIFETIME = 15;
	public static final int CACHE_MIN_LIFETIME = 5;

	// maximum character length of int fields in tables
	private static int MAX_INT_LENGTH = 10;
	
	protected static JdbcLink j=null;
	protected static Log log=Log.get("PersistentObject");
	//private static final HuffmanTree tree;
    private String id;
    private static Hashtable<String,String> mapping;
    private static SoftCache<String> cache;
    private static Job cacheCleaner;
    private static String username;
    private static String pcname;
    private static String tracetable;
    protected static int default_lifetime;
    private static boolean showDeleted=false;
    
	static{
		mapping=new Hashtable<String,String>();
		default_lifetime=Hub.localCfg.get(PreferenceConstants.ABL_CACHELIFETIME, CACHE_DEFAULT_LIFETIME);
		if(default_lifetime<CACHE_MIN_LIFETIME){
			default_lifetime=CACHE_MIN_LIFETIME;
			Hub.localCfg.set(PreferenceConstants.ABL_CACHELIFETIME, CACHE_MIN_LIFETIME);
		}
		cache=new SoftCache<String>(2000,0.7f);
        
		cacheCleaner=new Job("CacheCleaner"){
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				cache.purge();
				schedule(60000L);
				return Status.OK_STATUS;
			}
        	
        };
        cacheCleaner.setUser(false);
        cacheCleaner.setPriority(Job.DECORATE);
        //cacheCleaner.schedule(300000L);
        log.log("Cache setup: default_lifetime "+default_lifetime, Log.INFOS);
    }
	public static enum FieldType{
		TEXT,LIST,JOINT
	};
	  /**
	   * Connect to a database. 
	   * In the first place, the method checks if there is a demoDB in the Elexis base directory. If
	   * found, only this database will be used. If not, connection parameters are taken from the provided
	   * Settings. If there ist no database found, it will be created newly, using the createDB-Script.
	   * After successful connection, the global Settings (Hub.globalCfg) are linked to the database.
	   * @return true on success
	   * 
     * Verbindung mit der Datenbank herstellen. Die Verbindungsparameter werden
     * aus den übergebenen Settings entnommen. 
     * Falls am angegebenen Ort keine Datenbank gefunden wird, wird eine neue erstellt, falls
     * ein create-Script für diesen Datenbanktyp unter rsc gefunden wurde.
     * Wenn die Verbindung hergestell werden konnte, werden die global Settings mit dieser
     * Datenbank verbunden.
     * @return true für ok, false wenn keine Verbindung hergestellt werden konnte.
     */
    public static boolean connect(final Settings cfg){
    	File base=new File(Hub.getBasePath());
    	File demo=new File(base.getParentFile().getParent()+"/demoDB");
    	log.log("Verzeichnis Demo-Datenbank: "+demo.getAbsolutePath(), Log.DEBUGMSG);
    	if(System.getProperty("os.name").toLowerCase().startsWith("mac")){
    		demo=demo.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
    		demo=new File(demo,"demoDB");
    		log.log("Verzeichnis Demo-Datenbank mac-korrigiert: "+demo.getAbsolutePath(), Log.DEBUGMSG);
    	}
    	if(demo.exists() && demo.isDirectory()){
    		j=JdbcLink.createInProcHsqlDBLink(demo.getAbsolutePath()+"/db");
    		if(j.connect("sa", "")){
    			return connect(j);
    		}else{
    			MessageDialog.openError(Desk.theDisplay.getActiveShell(), "Fehler mit Demo-Datenbank", "Es wurde zar ein demoDB-Verzeichnis gefunden, aber dort ist keine verwendbare Datenbank");
    			return false;
    		}
    	}
    	
    	IPreferenceStore localstore = new SettingsPreferenceStore(cfg);
        String driver=localstore.getString(PreferenceConstants.DB_CLASS);
        String connectstring=localstore.getString(PreferenceConstants.DB_CONNECT);
        String user=localstore.getString(PreferenceConstants.DB_USERNAME);
        String pwd=localstore.getString(PreferenceConstants.DB_PWD);
        String typ=localstore.getString(PreferenceConstants.DB_TYP);
        /* experimentell
        driver="com.ibm.db2.jcc.DB2Driver";
        connectstring="jdbc:db2:elexis";
        user="testperson";
        pwd="blabla";
        typ="db2";
        */
        if(driver.equals("")){
        	String d=PreferenceInitializer.getDefaultDBPath();
            j=JdbcLink.createInProcHsqlDBLink(d);
            user="sa";
            pwd="";
            typ=j.DBFlavor;
        }else {
            j=new JdbcLink(driver,connectstring,typ);
        }
		if(j.connect(user,pwd)==true){
            log.log("Verbunden mit "+j.dbDriver()+", "+connectstring,Log.SYNCMARK);
            return connect(j);
        }
        return false;
	}
    public static boolean connect(final JdbcLink jd){
    	j=jd;
    	  Hub.globalCfg=new SqlSettings(j,"CONFIG");
          // Zugriffskontrolle initialisieren
          Hub.acl.load();
          String created=Hub.globalCfg.get("dbversion",null);
          
          if(created==null){
        	  created=Hub.globalCfg.get("created",null);
          }else{
        	  log.log("Database version "+created, Log.SYNCMARK);
          }
          if(created==null){
        	 log.log("No Version found. Creating new Database", Log.SYNCMARK);
          	java.io.InputStream is=null;
          	Stm stm=null;
              try{
            	  Resource rsc=new Resource("ch.elexis.data");
                  is=rsc.getInputStream("createDB.script");
                  stm=j.getStatement();
                  if(stm.execScript(is,true, true)==true){
                	  Log.setAlertLevel(Log.FATALS);
                  	Hub.globalCfg.undo();
                      Hub.globalCfg.set("created",new TimeTool().toString(TimeTool.FULL_GER));
                      Anwender.init();
                      Mandant.init();
                     	Hub.pin.initializeGrants();
                      Hub.pin.initializeGlobalPreferences();
                      Hub.globalCfg.flush();
                      Hub.localCfg.flush();
                      disconnect();
                      MessageDialog.openInformation(null,"Programmende","Es wurde eine neue Datenbank angelegt. Das Programm muss beendet werden. Bitte starten Sie danach neu.");
                      System.exit(1);
                  }else{
                      log.log("Kein create script für Datenbanktyp "+j.DBFlavor+" gefunden.",Log.ERRORS);
                      return false;
                  }
              }catch(Throwable ex){
                  ExHandler.handle(ex);
                  return false;
              }
              finally{
              	j.releaseStatement(stm);
              	try{
              		is.close();
              	}catch(Exception ex){
              		/* Janusode */
              	}
              }
          }
          VersionInfo vi=new VersionInfo(Hub.globalCfg.get("dbversion","0.0.0"));
          log.log("Verlangte Datenbankversion: "+Hub.DBVersion,Log.INFOS);
          log.log("Gefundene Datenbankversion: "+vi.version(),Log.INFOS);
          if(vi.isOlder(Hub.DBVersion)){
              log.log("ältere Version der Datenbank gefunden ",Log.WARNINGS);
              DBUpdate.doUpdate();
          }
          vi=new VersionInfo(Hub.globalCfg.get("ElexisVersion", "0.0.0"));
          log.log("Verlangte Elexis-Version: "+vi.version(),Log.INFOS);
          log.log("Vorhandene Elexis-Version: "+Hub.Version, Log.INFOS);
          VersionInfo v2=new VersionInfo(Hub.Version);
          if(vi.isNewerMinor(v2)){
        	  SWTHelper.showError("Verbindung nicht möglich: Version zu alt", "Die Datenbank ist für eine neuere Elexisversion. Bitte machen Sie ein Update.");
        	  log.log("Datenbank zu neu", Log.FATALS);
        	  System.exit(2);
          }
          // Wenn trace global eingeschaltet ist, gilt es für alle
          setTrace(Hub.globalCfg.get(PreferenceConstants.ABL_TRACE,null));
          // wenn trace global nicht eingeschaltet ist, kann es immer noch für diese 
          // Station eingeschaltet sein
          if(tracetable==null){
          	setTrace(Hub.localCfg.get(PreferenceConstants.ABL_TRACE,null));
          }
          return true;
    }
    /**
     * Return the Object containing the connection. This should only in very specific conditions be
     * neccessary, if one needs a direkt access to the database. It is strongly recommended to use this only 
     * very carefully, as callers must ensure for themselves that their code works with different
     * database engines equally.
     *  
     * Das Objekt, das die Connection enthält zurückliefern. Sollte nur in Ausnahmefällen nötig sein,
     * wenn doch mal ein direkter Zugriff auf die Datenbank erforderlich ist.
     * @return den JdbcLink, der die Verbindung zur Datenbank enthält
     */
	public static JdbcLink getConnection(){
		return j;
	}
    /** Die Zuordnung von Membervariablen zu Datenbankfeldern geschieht über statische mappings:
        * Jede abgeleitete Klassen muss ihre mappings in folgender Form deklarieren:
        * addMapping("Tabellenname","Variable=Feld"...); wobei:<ul>
        * <li>"Variable=Feld"    - Einfache Zuordnung, Variable wird zu Feld</li>
        * <li>"Variable=S:x:Feld" - Spezielle Abspeicherung<br>
        *               x=D - Datumsfeld, wird automatisch in Standardformat gebracht<br>
        *               x=C - Feld wird vor Abspeicherung komprimiert</li>
        * <li>"Variable=JOINT:FremdID:EigeneID:Tabelle[:type]" - n:m - Zuordnungen</li>
        * <li>"Variable=LIST:EigeneID:Tabelle:orderby[:type]"  - 1:n - Zuordnungen</li>
        * <li>"Variable=EXT:tabelle:feld"	- Das Feld ist in der genannten externen Tabelle  
        *</ul>
      */  
    static protected void addMapping(final String prefix, final String... map){
    	for(String s:map){
    		String[] def=s.trim().split("[ \t]*=[ \t]*");
    		if(def.length!=2){
    			mapping.put(prefix+def[0],def[0]);
    		}else{
    			mapping.put(prefix+def[0],def[1]);
    		}
    	}
    	mapping.put(prefix+"deleted", "deleted");
    }
    /**
     * Trace (protokollieren aller Schreibvorgänge) ein- und ausschalten. Die Trace-Tabelle muss folgende
     * Spalten haben: logtime (long), Workstation (VARCHAR), Username(Varchar), action (Text/Longvarchar)
     * @param Tablename Name der Trace-tabelle oder null: Trace aus.
     */
    public static void setTrace(String Tablename){
    	if((Tablename!=null) && (Tablename.equals("none") || Tablename.equals(""))){
    		Tablename=null;
    	}
        tracetable=Tablename;
        username=JdbcLink.wrap(System.getProperty("user.name"));
        pcname=JdbcLink.wrap(NetTool.hostname);
    }
    /**
     * Exklusiven Zugriff auf eine Ressource verlangen. Die Sperre kann für maximal
     * zwei Sekunden beansprucht werden, dann wird sie gelöst.
     * Dies ist eine sehr teure Methode, die eigentlich nur notwendig ist, weil es
     * keine standardisierte JDBC-Methode für Locks gibt...
     * Die Sperre ist kooperativ: Sie verhindert konkurrierende Zugriffe nicht wirklich,
     * sondern verlässt sich darauf, dass Zugreifende freiwillig zuerst die Sperre abfragen.
     * Sie bezieht sich auch nicht direkt auf eine bestimmte Tabelle, sondern immer nur auf eine
     * willkürliche frei wählbare Bezeichnung. Diese muss für jedes zu schützende Objekt standardisiert
     * werden.
     * @param name Name der gewünschten Sperre
     * @param wait wenn True, warten bis die sperre frei oder abgelaufen ist
     * @return null, wenn die Sperre belegt war, sonst eine id für unlock
     */
    public static synchronized String lock(final String name, final boolean wait){
    	Stm stm=j.getStatement();
    	String lockname="lock"+name;
    	String lockid=StringTool.unique("lock");
    	while(true){
	    	long timestamp=System.currentTimeMillis();
	    	// Gibt es das angeforderte Lock schon?
	    	String oldlock=stm.queryString("SELECT wert FROM CONFIG WHERE param="+JdbcLink.wrap(lockname));
	    	if(!StringTool.isNothing(oldlock)){
	    		// Ja, wie alt ist es?
	    		String[] def=oldlock.split("#");
	    		long locktime=Long.parseLong(def[1]);
	    		long age=timestamp-locktime;
	    		if(age>2000L){	// Älter als zwei Sekunden -> Löschen
	    			stm.exec("DELETE FROM CONFIG WHERE param="+JdbcLink.wrap(lockname));
	    		}else{
	    			if(wait==false){
	    				return null;
	    			}else{
	    				continue;
	    			}
	    		}
	    	}
	    	// Neues Lock erstellen
	    	String lockstring=lockid+"#"+Long.toString(System.currentTimeMillis());
	    	StringBuilder sb=new StringBuilder();
	    	sb.append("INSERT INTO CONFIG (param,wert) VALUES (")
	    		.append(JdbcLink.wrap(lockname)).append(",")
	    		.append("'").append(lockstring)
	    		.append("')");
	    	stm.exec(sb.toString());
	    	// Prüfen, ob wir es wirklich haben, oder ob doch jemand anders schneller war.
	    	String check=stm.queryString("SELECT wert FROM CONFIG WHERE param="+JdbcLink.wrap(lockname));
	    	if(check.equals(lockstring)){
	    		break;
	    	}
    	}
    	j.releaseStatement(stm);
    	return lockid;
    }
    /** 
     * Exklusivzugriff wieder aufgeben
     * @param name	Name des Locks
     * @param id	bei "lock" erhaltene LockID
     * @return true bei Erfolg
     */
    public static synchronized boolean unlock(final String name, final String id){
    	String lockname="lock"+name;
    	String lock=j.queryString("SELECT wert from CONFIG WHERE param="+JdbcLink.wrap(lockname));
    	if(StringTool.isNothing(lock)){
    		return false;
    	}
    	String[] res=lock.split("#");
    	if(res[0].equals(id)){
    		j.exec("DELETE FROM CONFIG WHERE param="+JdbcLink.wrap(lockname));
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Einschränkende Bedingungen für Suche nach diesem Objekt definieren
     * @return ein Constraint für eine Select-Abfrage
     */
    protected String getConstraint(){
    	return "";
    }
    
    /**
     * Bedingungen für dieses Objekt setzen
     */
    protected void setConstraint(){
        /* Standardimplementation ist leer */
    }
    
    /** Einen menschenlesbaren Identifikationsstring für dieses Objet liefern */ 
    abstract public String getLabel();
    
	/**
	 * Jede abgeleitete Klasse muss deklarieren, in welcher Tabelle sie gespeichert werden will.
	 * @return Der Name einer bereits existierenden Tabelle der Datenbank
	 */
	abstract protected String getTableName();
	
	/**
	 * Angeben, ob dieses Objekt gültig ist.
	 * @return true wenn die Daten gültig (nicht notwendigerweise korrekt) sind
	 */
	public boolean isValid(){
		if(state()<EXISTS){
			return false;
		}
		return true;
	}
	/**
	 * Die eindeutige Identifikation dieses Objektes/Datensatzes liefern. Diese ID wird jeweils automatisch
	 * beim Anlegen eines Objekts dieser oder einer abgeleiteten Klasse erstellt und bleibt dann unveränderlich.
	 * @return die ID.
	 */
	public String getId(){
		return id;
	}
	/**
	 * Die ID in einen datenbankgeeigneten Wrapper verpackt (je nach Datenbank; meist Hochkommata).
	 */
	public String getWrappedId(){
		return JdbcLink.wrap(id);
	}
	
	/** Der Konstruktor erstellt die ID */
	protected PersistentObject()
	{
		id=StringTool.unique("prso");
	}
	/** Konstruktor mit vorgegebener ID (zum Deserialisieren) 
     *  Wird nur von xx::load gebraucht.                    */
	protected PersistentObject(final String id){
		this.id=id;
	}
	/**
	 * Objekt in einen String serialisieren. Diese Standardimplementation macht eine "cheap copy":
	 * Es wird eine Textrepräsentation des Objektes erstellt,
	 * mit deren Hilfe das Objekt später wieder aus der Datenbank erstellt werden kann. Dies
	 * funktioniert nur innerhalb derselben Datenbank.
	 * @return der code-String, aus dem mit createFromCode wieder das Objekt erstellt werden 
	 * kann
	 */
	public String storeToString(){
		StringBuilder sb=new StringBuilder();
		sb.append(getClass().getName()).append("::").append(getId());
		return sb.toString();
	}
	public static final int INEXISTENT=0;
	public static final int INVALID_ID=1;
	public static final int DELETED=2;
	public static final int EXISTS=3;
	
	public int state(){
		if(StringTool.isNothing(getId())){
			return INVALID_ID;
		}
		String ch=j.queryString("SELECT ID FROM "+getTableName()+" WHERE "+"ID="+getWrappedId());
        if(ch==null){
        	return INEXISTENT;
        }
        String deleted=get("deleted");
        if(deleted==null){		// if we cant't find the column called 'deleted', the object exists anyway
        	return EXISTS;
        }
        if(showDeleted){
        	return EXISTS;
        }else{
        	return deleted.equals("1") ? DELETED : EXISTS;
        }
	}
	/**
     * Feststellen, ob ein PersistentObject bereits in der Datenbank existiert
     * @return true wenn es existiert, false wenn es nicht existiert oder gelöscht wurde
     */
	
    public boolean exists(){
    	return state()==EXISTS;
    }
    
    /**
     * Check whether the object exists in the database. This is the case for all objects
     * in the database for which state() returns neither INVALID_ID nor INEXISTENT.
     * @return true, if the object is available in the database, false otherwise
     */
    public boolean isAvailable() {
    	if (state() >= DELETED) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Return a xid (domain_id) for a specified domain
     * @param domain
     * @return an identifier that may be empty but will never be null
     */
    public String getXid(final String domain){
    	if(domain.equals(Xid.DOMAIN_ELEXIS)){
    		return getId();
    	}
    	Query<Xid> qbe=new Query<Xid>(Xid.class);
    	qbe.add("object", "=", getId());
    	qbe.add("domain", "=", domain);
    	List<Xid> res=qbe.execute();
    	if(res.size()>0){
    		return res.get(0).get("domain_id");
    	}
    	return "";
    }
    
    /**
     * return the "best" xid for a given object. This is the xid with the
     * highest quality. If no xid is given for this object, a newly created xid
     * of local quality will be returned
     */
    public Xid getXid(){
    	Query<Xid> qbe=new Query<Xid>(Xid.class);
    	qbe.add("object", "=", getId());
    	List<Xid> res=qbe.execute();
    	if(res.size()==0){
    		try{
    			return new Xid(this,Xid.DOMAIN_ELEXIS,getId());
    		}catch(XIDException xex){	// Should never happen, uh?
    			ExHandler.handle(xex);
    			return null;
    		}
    	}
    	int quality=0;
    	Xid ret=null;
    	for(Xid xid:res){
    		if(xid.getQuality()>quality){
    			quality=xid.getQuality();
    			ret=xid;
    		}
    	}
    	if(ret==null){
    		return res.get(0);
    	}
    	return ret;
    }
    
    /**
     * Assign a XID to this object.
     * @param domain the domain whose ID will be assigned
     * @param domain_id the id out of the given domain fot this object
     * @param updateIfExists if true update values if Xid with same domain and domain_id exists. Otherwise the method will
     * fail if a collision occurs.
     * @return true on success, false on failure
     */
    public boolean addXid(final String domain, final String domain_id, final boolean updateIfExists){
    	Xid oldXID=Xid.findXID(this, domain);
    	if(oldXID!=null){
    		if(updateIfExists){
    			oldXID.set("domain_id", domain_id);
    			return true;
    		}
    		return false;
    	}
    	
    	try {
			new Xid(this,domain,domain_id);
			return true;
		} catch (XIDException e) {
			ExHandler.handle(e);
			if(updateIfExists){
				Xid xid=Xid.findXID(domain, domain_id);
				if(xid!=null){
					xid.set("object",getId());
					return true;
				}
			}
			return false;
		}
    }
    
    /**
     * hilt die "höchstwertige" Etikette, falls mehrere existieren
     * @return
     */
    public Etikette getEtikette(){
    	List<Etikette> list=getEtiketten();
    	return list.size()>0 ? list.get(0) : null;
    }
    @SuppressWarnings("unchecked")
	public List<Etikette> getEtiketten(){
    	String ID=new StringBuilder().append("ETK").append(getId()).toString();
    	ArrayList<Etikette> ret=(ArrayList<Etikette>)cache.get(ID);
    	if(ret!=null){
    		return ret;
    	}
    	ret=new ArrayList<Etikette>();
    	StringBuilder sb=new StringBuilder();
    	Stm stm=j.getStatement();
		
    	sb.append("SELECT etikette FROM ")
    		.append(Etikette.LINKTABLE).append(" WHERE ")
    		.append("obj = '").append(getId()).append("'");
    	try{
    		ResultSet res=stm.query(sb.toString());

    		while(res!=null && res.next()){
    			Etikette et=Etikette.load(res.getString(1));
    			if(et!=null && et.exists()){
    				ret.add(Etikette.load(res.getString(1)));
    			}
    		}
    		res.close();
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		return null;
    	}
    	finally{
    		j.releaseStatement(stm);
    	}
    	Collections.sort(ret);
    	cache.put(ID, ret, getCacheTime());
    	return ret;
    }
    
    public void removeEtikette(Etikette et){
    	String ID=new StringBuilder().append("ETK").append(getId()).toString();
    	ArrayList<Etikette> ret=(ArrayList<Etikette>)cache.get(ID);
    	if(ret!=null){
    		ret.remove(et);
    	}
    	StringBuilder sb=new StringBuilder();
    	sb.append("DELETE FROM ").append(Etikette.LINKTABLE)
    		.append(" WHERE obj=").append(getWrappedId())
    		.append(" AND etikette=").append(et.getWrappedId());
    	j.exec(sb.toString());
    }
    
    public void addEtikette(Etikette et){
    	String ID=new StringBuilder().append("ETK").append(getId()).toString();
    	ArrayList<Etikette> ret=(ArrayList<Etikette>)cache.get(ID);
    	if(ret!=null){
    		ret.add(et);
    		Collections.sort(ret);
    	}
    	StringBuilder sb=new StringBuilder();
    	sb.append("INSERT INTO ").append(Etikette.LINKTABLE)
    		.append("(obj,etikette) VALUES (")
    		.append(getWrappedId()).append(",")
    		.append(et.getWrappedId()).append(");");
    	j.exec(sb.toString());
    }
    /**
     * Feststellen, ob ein PersistentObject als gelöscht markiert wurde 
     * @return true wenn es gelöscht ist
     */
    public boolean isDeleted(){
    	return get("deleted").equals("1");
    }
   
    /**
     * Darf dieses Objekt mit Drag&Drop verschoben werden
     * @return true wenn ja.
     */
    public boolean isDragOK(){
        return false;
    }
    /**
     * Aus einem Feldnamen das dazugehörige Datenbankfeld ermitteln
     * @param f Der Feldname
     * @return  Das Datenbankfeld oder **ERROR**, wenn kein mapping für das angegebene Feld existiert.
     */
	 public String map(final String f){
         if(f.equals("ID")){
             return f;
         }
		String prefix=getTableName();
		String res=mapping.get(prefix+f);
		if(res==null){
			log.log("Fehler bei der Felddefinition "+f,Log.ERRORS);
			return "**ERROR:"+f+"**";
		}
		return res;
	}
	
	 public FieldType getFieldType(final String f){
		 String mapped=map(f);
		 if(mapped.startsWith("LIST:")){
			 return FieldType.LIST;
		 }else if(mapped.startsWith("JOINT:")){
			 return FieldType.JOINT;
		 }else{
			 return FieldType.TEXT;
		 }
	 }
	
	/** 
	 * Ein Feld aus der Datenbank auslesen.
	 * Die Tabelle wird über getTableName() erfragt.
     * Das Feld wird beim ersten Aufruf in jedem Fall aus der Datenbank gelesen.
     * Dann werden weitere Lesezugriffe während der <i>lifetime</i> aus dem
     * cache bedient, um die Zahl der Datenbankzugriffe zu minimieren. Nach Ablauf
     * der lifetime erfolgt wieder ein Zugriff auf die Datenbank, wobei auch der cache wieder
     * erneuert wird.
	 * @param field Name des Felds
	 * @return Der Inhalt des Felds (kann auch null sein), oder **ERROR**, 
	 * wenn versucht werden sollte, ein nicht existierendes Feld auszulesen
	 */
	@SuppressWarnings("unchecked")
	public String get(final String field)
	{
        String key=getKey(field);
        Object ret=cache.get(key);
        if(ret instanceof String){
        	return (String) ret;
        }
        /*
    	if((cf!=null) && (cf.expired()==false) && !(cf.contents instanceof VersionedResource)){
    		return (String)cf.contents;
    	}*/
    	boolean decrypt=false;
		StringBuffer sql=new StringBuffer();
		String mapped=map(field);
		String table=getTableName();
		if(mapped.startsWith("EXT:")){
			int ix=mapped.indexOf(':',5);
			if(ix==-1){
				log.log("Fehlerhaftes Mapping bei "+field,Log.ERRORS);
				return "**ERROR: "+field+"**";
			}
			table=mapped.substring(4,ix);
			mapped=mapped.substring(ix+1);
		}else if(mapped.startsWith("S:")){
			mapped=mapped.substring(4);
			decrypt=true;
		}else if(mapped.startsWith("JOINT:")){
			String[] dwf=mapped.split(":");
			if(dwf.length>4){
				String objdef=dwf[4]+"::";
				StringBuilder sb=new StringBuilder();
				List<String[]> list=getList(field, new String[0]);
				PersistentObjectFactory fac=new PersistentObjectFactory();
				for(String[] s:list){
					PersistentObject po=fac.createFromString(objdef+s[0]);
					sb.append(po.getLabel()).append("\n");
				}
				return sb.toString();
			}
			
		}else if(mapped.startsWith("LIST:")){
			String[] dwf=mapped.split(":");
			if(dwf.length>4){
				String objdef=dwf[4]+"::";
				StringBuilder sb=new StringBuilder();
				List<String> list=getList(field, false);
				PersistentObjectFactory fac=new PersistentObjectFactory();
				for(String s:list){
					PersistentObject po=fac.createFromString(objdef+s);
					sb.append(po.getLabel()).append("\n");
				}
				return sb.toString();
			}
		}else if(mapped.startsWith("**")){	// If the field could not be mapped
			String exi=map("ExtInfo");		// Try to find it in ExtInfo
			if(!exi.startsWith("**")){
				Hashtable ht=getHashtable("ExtInfo");
				Object res=ht.get(field);
				if(res instanceof String){
					return (String)res;
				}
			}
			String method="get"+field;		// or try to find a "getter" Method for the field 
			try {
				Method mx=getClass().getMethod(method, new Class[0]);
				Object ro=mx.invoke(this, new Object[0]);
				if(ro==null){
					return "";
				}else if(ro instanceof String){
					return (String)ro;
				}else if (ro instanceof PersistentObject){
					return ((PersistentObject)ro).getLabel();
				}else{
					return "?invalid field? "+mapped;
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				log.log("Fehler in Felddefinition "+field, Log.ERRORS);
				return mapped;
			}
		}
		sql.append("SELECT ").append(mapped).append(" FROM ")
		    	.append(table).append(" WHERE ID='").append(id).append("'");
		Stm stm=j.getStatement();
        ResultSet rs=stm.query(sql.toString());
        String res=null;
        try{
            if((rs!=null) && (rs.next()==true)){
                if(decrypt){
                    res=decode(field,rs);
                }else{
                    res=rs.getString(mapped);
                }
                if(res==null){
                    res="";
                }
               	cache.put(key,res,getCacheTime());
            }
        }catch(Exception ex){
            ExHandler.handle(ex);
        }
        finally{
            j.releaseStatement(stm);
        }
        return res;
	}

		
    protected byte[] getBinary(final String field){
        StringBuilder sql=new StringBuilder();
        String mapped=/*map*/(field);
        String table=getTableName();
        sql.append("SELECT ").append(mapped).append(" FROM ")
            .append(table).append(" WHERE ID='").append(id).append("'");
       
       Stm stm=j.getStatement();
       ResultSet res=stm.query(sql.toString());
       try{
           if((res != null) && (res.next()==true)){
               return res.getBytes(mapped);
           }
       }catch(Exception ex){
           ExHandler.handle(ex);
       }finally{
           j.releaseStatement(stm);
       }
       return null;
    }
    
    protected VersionedResource getVersionedResource(final String field,final boolean flushCache){
        String key=getKey(field);
        if(flushCache==false){
        	Object o=cache.get(key);
        	if(o instanceof VersionedResource){
        		return (VersionedResource)o;
	        }
        }
        byte[] blob=getBinary(field);
        VersionedResource ret=VersionedResource.load(blob);
        cache.put(key,ret,getCacheTime());
        return ret;
    }
    
    /**
     * Eine Hashtable auslesen
     * @param field Feldname der Hashtable
     * @return eine Hashtable (ggf. leer)
     */
	@SuppressWarnings("unchecked")
	public Hashtable getHashtable(final String field){
        String key=getKey(field);
        Object o=cache.get(key);
        if(o instanceof Hashtable){
               return (Hashtable)o;
        }
        byte[] blob=getBinary(field);
        if(blob==null){
            return new Hashtable();
        }
        Hashtable<Object,Object> ret=StringTool.fold(blob);
        if(ret==null){
        	return new Hashtable();
        }
        cache.put(key,ret,getCacheTime());
        return ret;
 	}
    /** 
     * Bequemlichkeitsmethode zum lesen eines Integer.
     * @param field
     * @return einen Integer. 0 bei 0 oder unlesbar
     */
	public int getInt(final String field){
		return checkZero(get(field));
    }
	
	/**
	 * Eine 1:n Verknüpfung aus der Datenbank auslesen.
	 * @param field das Feld, wie in der mapping-Deklaration angegeben
	 * @param reverse wenn true wird rückwärts sortiert
	 * @return eine Liste mit den IDs (String!) der verknüpften Datensätze.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getList(final String field, final boolean reverse)
	{
		 StringBuffer sql=new StringBuffer();
		 String mapped=map(field);
		 if(mapped.startsWith("LIST:")){
				String[] m=mapped.split(":");
				if(m.length>2){
					//String order=null;
					
					sql.append("SELECT ID FROM ").append(m[2]).append(" WHERE ");
					if(showDeleted==false){
						sql.append("deleted=").append(JdbcLink.wrap("0")).append(" AND ");
					}
					sql.append(m[1]).append("=").append(getWrappedId());
					if(m.length>3){
						sql.append(" ORDER by ").append(m[3]);
						if(reverse){
							sql.append(" DESC");
						}
					}
					Stm stm=j.getStatement();
					List<String> ret=stm.queryList(sql.toString(),new String[]{"ID"});
					j.releaseStatement(stm);
					return ret;
				}
		 }else{
			log.log("Fehlerhaftes Mapping "+mapped,Log.ERRORS);
		 }
		 return null;
	}
    /**
     * Eine n:m - Verknüpfung auslesen
     * @param field Das Feld, für das ein entsprechendes mapping existiert
     * @param extra Extrafelder, die aus der joint-Tabelle ausgelesen werden sollen
     * @return eine Liste aus String-Arrays, welche jeweils die ID des gefundenen Objekts
     * und den Inhalt der Extra-Felder enthalten. Null bei Mapping-Fehler
     */
	public List<String[]> getList(final String field, String[] extra)
	{
		if(extra==null){
			extra=new String[0];
		}
		StringBuffer sql=new StringBuffer();
		String mapped=map(field);
		if(mapped.startsWith("JOINT:")){
			String[] abfr=mapped.split(":");
			sql.append("SELECT ").append(abfr[1]);
			for(String ex :extra){
				sql.append(",").append(ex);
			}
			sql.append(" FROM ")
				.append(abfr[3]).append(" WHERE ").append(abfr[2])
				.append("=").append(getWrappedId());
			
			Stm stm=j.getStatement();
			ResultSet rs=stm.query(sql.toString());
			j.releaseStatement(stm);
			LinkedList<String[]> list=new LinkedList<String[]>();
			try{
				while(rs.next()==true){
					String[] line=new String[extra.length+1];
					line[0]=rs.getString(abfr[1]);
					for(int i=1;i<extra.length+1;i++){
						line[i]=rs.getString(extra[i-1]);
					}
					list.add(line);
				}
				return list;

			}catch(Exception ex){
				ExHandler.handle(ex);
				log.log("Fehler beim Lesen der Liste ",Log.ERRORS);
				return null;
			}
		}else{
			log.log("Fehlerhaftes Mapping "+mapped,Log.ERRORS);
		}
		return null;

	}
	
	
	/**
	 * Ein Feld in die Datenbank übertragen. Gleichzeitig Cache-update 
	 * Die Tabelle wird über getTableName() erfragt.
	 * @param field Name des Feldes
	 * @param value Einzusetzender Wert (der vorherige Wert wird überschrieben)
	 * @return 0 bei Fehler
	 */
	public boolean set(final String field, String value)
	{
		if(value==null){
			value="";
		}
		String mapped=map(field);
		cache.put(getKey(field),value,getCacheTime());
        StringBuffer sql=new StringBuffer();
        String table=getTableName();
        
        
        if(mapped.startsWith("EXT:")){
        	int ix=mapped.indexOf(':',5);
			if(ix==-1){
				log.log("Fehlerhaftes Mapping bei "+field,Log.ERRORS);
				return false;
			}
			table=mapped.substring(4,ix);
			mapped=mapped.substring(ix+1);
			sql.append("UPDATE ").append(table).append(" SET ").append(mapped);	
        }else{
        	sql.append("UPDATE ").append(table).append(" SET ");
	        if(mapped.startsWith("S:")){
	            sql.append(mapped.substring(4));
	        }else{
	           sql.append(mapped);
	        }
        }
        sql.append("=? WHERE ID=").append(getWrappedId());
        String cmd=sql.toString();
        PreparedStatement pst=j.prepareStatement(cmd);
        
        encode(1,pst,field,value);
        if(tracetable!=null){
        	StringBuffer params = new StringBuffer();
        	params.append("[");
        	params.append(value);
        	params.append("]");
            doTrace(cmd + " " + params);
        }
        try{
            pst.executeUpdate();
            return true; 
        }catch(Exception ex){
            ExHandler.handle(ex);
            log.log("Fehler bei: "+cmd+"("+field+"="+value+")", Log.ERRORS);
            return false;
        }
        
	}
	/**
	 * Eine Hashtable speichern. Diese wird zunächst in ein byte[] geplättet, dann wird
	 * sicherheitshalber geprüft, ob sie wirklich aus diesem Array wiederhergestellt werden kann,
	 * und wenn ja, wird sie gespeichert.
	 * @param field
	 * @param hash
	 * @return 0 bei Fehler
	 */
    @SuppressWarnings("unchecked")
	public int setHashtable(final String field, final Hashtable hash){
        if(hash==null){
            return 0;
        }
        try{
        	byte[] bin=StringTool.flatten(hash);
        	/*
        	Hashtable res=StringTool.fold(bin);
        	if(res==null){
        		String ls=StringTool.flattenStrings(hash);
        		//byte[] bin2=StringTool.flatten(hash,StringTool.ZIP,null);
        		log.log("Hashtable: "+ls,Log.ERRORS);
        		throw new Exception("Hashtable nicht wiederherstellbar");
        	}
        	*/
        	cache.put(getKey(field),hash,getCacheTime());
        	return setBinary(field,bin);
        }catch(Throwable ex){
        	log.log("Fehler beim Speichern von "+field+" von "+getLabel(),Log.ERRORS);
        	MessageDialog.openError(Hub.getActiveShell(),"Interner Fehler","Konnte "+field+" von "+getLabel()+" nicht speichern!");
        	return 0;
        }
        
    }
    /**
     * Eine VersionedResource zurückschreiben. Um Datenverlust durch gleichzeitigen
     * Zugriff zu vermeiden, wird zunächst die aktuelle Version in der Datenbank
     * gelesen und mit der neuen Version überlagert.
     */
    protected int setVersionedResource(final String field, final String entry){
        String lockid=lock("VersionedResource",true);
        VersionedResource old=getVersionedResource(field,true);
        int ret=1;
        if(old.update(entry,Hub.actUser.getLabel())==true){
        	cache.put(getKey(field),old,getCacheTime());
        	ret= setBinary(field,old.serialize());
        }
        unlock("VersionedResource",lockid);
        return ret;
    }
	protected int setBinary(final String field, final byte[] value){
		StringBuilder sql=new StringBuilder(1000);
        sql.append("UPDATE ").append(getTableName()).append(" SET ")
        	.append(/*map*/(field)).append("=?")
        	.append(" WHERE ID=").append(getWrappedId());
        String cmd=sql.toString();
        if(tracetable!=null){
        	doTrace(cmd);
        }
        PreparedStatement stm=j.prepareStatement(cmd);
        try{
            stm.setBytes(1,value);
            stm.executeUpdate();
            return 1;
        }catch(PacketTooBigException pigex){
            ExHandler.handle(pigex);
            SWTHelper.showError("setBytes", "Schreibfehler", "Der Datensatz war zu gross zum Schreiben");
        }catch(Exception ex){
            log.log("Fehler beim Ausführen der Abfrage "+cmd,Log.ERRORS);
            SWTHelper.showError("setBytes", "Schreibfehler", "Es trat ein Fehler beim Schreiben auf.");
        }
        return 0;
	}
	
	/**
	 * Set a value of type int.
	 * @param field a table field of numeric type
	 * @param value the value to be set
	 * @return true on success, false else
	 */
	public boolean setInt(final String field, final int value) {
		String stringValue = new Integer(value).toString();
		if (stringValue.length() <= MAX_INT_LENGTH) {
			return set(field, stringValue);
		} else {
			return false;
		}
	}
	
	private void doTrace(final String sql)
    {
        StringBuffer tracer=new StringBuffer();
        tracer.append("INSERT INTO ").append(tracetable);
        tracer.append(" (logtime,Workstation,Username,action) VALUES (");
        tracer.append(System.currentTimeMillis()).append(",");
        tracer.append(pcname).append(",");
        tracer.append(username).append(",");
        tracer.append(JdbcLink.wrap(sql.replace('\'','/'))).append(")");
	    j.exec(tracer.toString());
    }
    /**
	 * Eine Element einer n:m Verknüpfung eintragen. Zur Tabellendefinition wird das mapping 
	 * verwendet.
	 * @param field Das n:m Feld, für das ein neuer Eintrag erstellt werden soll.
	 * @param oID ID des Zielobjekts, auf das der Eintrag zeigen soll
	 * @param extra Definition der zusätzlichen Felder der Joint-Tabelle. Jeder Eintrag in der Form Feldname=Wert
	 * @return 0 bei Fehler
	 */
	public int addToList(final String field, final String oID, final String... extra)
	{
		String mapped=map(field);
		if(mapped.startsWith("JOINT:")){
			String[] m=mapped.split(":");// m[1] FremdID, m[2] eigene ID, m[3] Name Joint
			if(m.length>3){
				StringBuffer head=new StringBuffer(100);
				StringBuffer tail=new StringBuffer(100);
				head.append("INSERT INTO ").append(m[3]).append("(ID,").append(m[2])
					.append(",").append(m[1]);
				tail.append(") VALUES (").append(JdbcLink.wrap(StringTool.unique("aij"))).append(",")
					.append(getWrappedId()).append(",").append(JdbcLink.wrap(oID));
				if(extra!=null){
					for(String s : extra){
						String[] def=s.split("=");
						if(def.length!=2){
							log.log("Fehlerhafter Aufruf addToList "+s,Log.ERRORS);
							return 0;
						}
						head.append(",").append(def[0]);
						tail.append(",").append(JdbcLink.wrap(def[1]));
					}
				}
				head.append(tail).append(")");
                if(tracetable!=null){
                    String sql=head.toString();
                    doTrace(sql);
                    return j.exec(sql);
                }
				return j.exec(head.toString());
			}
		}
		log.log("Fehlerhaftes Mapping: "+mapped,Log.ERRORS);
		return 0;
	}

	public void removeFromList(String field, String oID){
		String mapped=map(field);
		if(mapped.startsWith("JOINT:")){
			String[] m=mapped.split(":");// m[1] FremdID, m[2] eigene ID, m[3] Name Joint
			if(m.length>3){
				StringBuilder sql=new StringBuilder(200);
				sql.append("DELETE FROM ").append(m[3]).append(" WHERE ")
					.append(m[2]).append("=").append(getWrappedId())
					.append(" AND ").append(m[1]).append("=")
					.append(JdbcLink.wrap(oID));
                if(tracetable!=null){
                    String sq=sql.toString();
                    doTrace(sq);
                }
				j.exec(sql.toString());
				return;
			}
		}
		log.log("Fehlerhaftes Mapping: "+mapped,Log.ERRORS);
	}
	/*
	 * 
	public int addToList(String field, String oID, String... extra)
	{
		String mapped=map(field);
		if(mapped.startsWith("JOINT:")){
			String[] m=mapped.split(":");// m[1] FremdID, m[2] eigene ID, m[3] Name Joint
			if(m.length>3){
				StringBuffer head=new StringBuffer(100);
				StringBuffer tail=new StringBuffer(100);
				head.append("INSERT INTO ").append(m[3]).append("(ID,").append(m[2])
					.append(",").append(m[1]);
				tail.append(") VALUES (").append(JdbcLink.wrap(StringTool.unique("aij"))).append(",")
					.append(getWrappedId()).append(",").append(JdbcLink.wrap(oID));
				if(extra!=null){
					for(String s : extra){
						String[] def=s.split("=");
						if(def.length!=2){
							log.log("Fehlerhafter Aufruf addToList "+s,Log.ERRORS);
							return 0;
						}
						head.append(",").append(def[0]);
						tail.append(",").append(JdbcLink.wrap(def[1]));
					}
				}
				head.append(tail).append(")");
                if(tracetable!=null){
                    String sql=head.toString();
                    doTrace(sql);
                    return j.exec(sql);
                }
				return j.exec(head.toString());
				//j.exec("INSERT INTO ADRESS_IDENT_JOINT (IdentID,AdressID) VALUES ("+getWrappedId()+","+adr.getWrappedId()+")");
			}
		}
		log.log("Fehlerhaftes Mapping: "+mapped,Log.ERRORS);
		return 0;
	}
	*/
	
	/**
	 * Ein neues Objekt erstellen und in die Datenbank eintragen
	 * @param customID Wenn eine ID (muss eindeutig sein!) vorgegeben werden soll. Bei null wird eine generiert.
	 * @return true bei Erfolg
	 */
	protected boolean create(final String customID){
		//String pattern=this.getClass().getSimpleName();
		if(customID!=null){
			id=customID;
		}
		StringBuffer sql=new StringBuffer(300);
		sql.append("INSERT INTO ").append(getTableName())
		.append("(ID) VALUES (").append(getWrappedId()).append(")");
		if (j.exec(sql.toString())!=0){
            setConstraint();
            return true;
		}
		return false;
	}
	/**
	 * Ein Objekt und ggf. dessen XID's aus der Datenbank löschen
	 * @return true on success
	 */
	public boolean delete(){
		// we change this to ratehr set the deleted-flag than really delete
		if(set("deleted","1")){
			List<Xid> xids=new Query<Xid>(Xid.class,"object",getId()).execute();
			for(Xid xid:xids){
				xid.delete();
			}
			new DBLog(this,DBLog.TYP.DELETE);
			PersistentObject sel=GlobalEvents.getInstance().getSelectedObject(this.getClass());
			if((sel!=null) && sel.equals(this)){
				GlobalEvents.getInstance().clearSelection(this.getClass());
			}
			GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.delete);
			return true;
		}
		return false;
	}
	
	/**
	 * Alle Bezüge aus einer n:m-Verknüpfung zu diesem Objekt löschen
	 * @param field Feldname, der die Liste definiert
	 * @return
	 */
	public boolean deleteList(final String field){
		String mapped=map(field);
		if(!mapped.startsWith("JOINT:")){
			SWTHelper.alert("Interer Fehler", "Feld "+field+" ist keine n:m Verknüpfung");
			return false;
		}
		String[] m=mapped.split(":");// m[1] FremdID, m[2] eigene ID, m[3] Name Joint
		j.exec("DELETE FROM "+m[3]+" WHERE "+m[2]+"="+getWrappedId());
		return true;
	}
	/**
	 * We can undelete any object by simply clearing the deleted-flag
	 * and reanimate dependend XID's
	 * @return true on success
	 */
	public boolean undelete(){
		if(set("deleted","0")){
			boolean oldShowDeleted=showDeleted;
			showDeleted=true;
			List<Xid> xids=new Query<Xid>(Xid.class,"object",getId()).execute();
			for(Xid xid:xids){
				xid.undelete();
			}
			showDeleted=oldShowDeleted;
			new DBLog(this,DBLog.TYP.UNDELETE);
			GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.create);
			return true;
		}
		return false;
	}
	/**
	 * Eine zur konkreten Klasse des aufrufenden Objekts passende Query zurückliefern
	 * @return leere Query für die Klasse dieses Objekts.
	 */
	@SuppressWarnings("unchecked")
	public  Query getQuery(){
		return new Query(getClass());
	}
	
	/**
	 * Mehrere Felder auf einmal setzen (Effizienter als einzelnes set)
	 * @param fields die Feldnamen
	 * @param values die Werte
	 * @return false bei Fehler
	 */
	public boolean set(final String[] fields, final String... values){
		if((fields==null) || (values==null) || (fields.length!=values.length)){
			log.log("Falsche Felddefinition für set",Log.ERRORS);
			return false;
		}
		StringBuffer sql=new StringBuffer(200);
		sql.append("UPDATE ").append(getTableName()).append(" SET ");
		for(int i=0;i<fields.length;i++){
            String mapped=map(fields[i]);
            if(mapped.startsWith("S:")){
                sql.append(mapped.substring(4));
            }else{
                sql.append(mapped);
            }
			sql.append("=?,");
			cache.put(getKey(fields[i]),values[i],getCacheTime());
		}
		sql.delete(sql.length()-1,100000);
		sql.append(" WHERE ID=").append(getWrappedId());
        String cmd=sql.toString();
        PreparedStatement pst=j.prepareStatement(cmd);
        for(int i=0;i<fields.length;i++){
        	encode(i+1,pst,fields[i],values[i]);
        }
        if(tracetable!=null){
        	StringBuffer params = new StringBuffer();
        	params.append("[");
        	params.append(StringTool.join(values, ", "));
        	params.append("]");
            doTrace(cmd + " " + params);
        }
        try{
            pst.executeUpdate();
            return true;
        }catch(Exception ex){
            ExHandler.handle(ex);
            StringBuilder sb=new StringBuilder();
            sb.append("Fehler bei ").append(cmd).append("\nFelder:\n");
            for(int i=0;i<fields.length;i++){
            	sb.append(fields[i]).append("=").append(values[i]).append("\n");
            }
            log.log(sb.toString(), Log.ERRORS);
            return false;
        }
	}
	
	
	/**
	 * Mehrere Felder auf einmal auslesen
	 * @param fields die Felder
	 * @param values String Array für die gelesenen Werte
	 * @return true ok, values wurden gesetzt
	 */
	public boolean get(final String[] fields, final String[] values)
	{
		if( (fields==null) || (values==null) || (fields.length!=values.length)){
			log.log("Falscher Aufruf von get(String[],String[]",Log.ERRORS);
			return false;
		}
		StringBuffer sql=new StringBuffer(200);
		sql.append("SELECT ");
		boolean[] decode=new boolean[fields.length];
		for(int i=0;i<fields.length;i++){
			String key=getKey(fields[i]);
			Object ret=cache.get(key);
			if(ret instanceof String){
				values[i]=(String)ret;
			}else{
				String f1=map(fields[i]);
				if(f1.startsWith("S:")){
					sql.append(f1.substring(4));
					decode[i]=true;
				}else{
					sql.append(f1);
				}
				sql.append(",");
			}
		}
		if(sql.length()<8){
			return true;
		}
		sql.delete(sql.length()-1,1000);
		sql.append(" FROM ").append(getTableName()).append(" WHERE ID=")
		.append(getWrappedId());
		Stm stm=j.getStatement();
		ResultSet res=stm.query(sql.toString());
		try{
			if((res!=null) && res.next()){
				for(int i=0;i<values.length;i++){
					if(values[i]==null){
						if(decode[i]==true){
							values[i]=decode(fields[i],res);
						}else{
							values[i]=checkNull(res.getString(map(fields[i])));	
						}
						cache.put(getKey(fields[i]), values[i], getCacheTime());
					}
				}
				
			}
			return true;
		}catch(Exception ex){
			ExHandler.handle(ex);
			return false;
		}finally{
			j.releaseStatement(stm);
		}
		
	}
    private String decode(final String field, final ResultSet rs){
        
        try{
            String mapped=map(field);
            if(mapped.startsWith("S:")){
                char mode=mapped.charAt(2);
                switch(mode){
                case 'D':
                    String dat=rs.getString(mapped.substring(4));
                    if(dat==null){
                        return "";
                    }
                    TimeTool t=new TimeTool();
                    if(t.set(dat)==true){
                        return t.toString(TimeTool.DATE_GER);
                    }else{
                        return "";
                    }
                
                case 'C':
                        InputStream is=rs.getBinaryStream(mapped.substring(4));
                        if(is==null){
                            return "";
                        }
                        byte[] exp=CompEx.expand(is);
                        return new String(exp,StringTool.default_charset);
                    
                case 'V':
                	byte[] in=rs.getBytes(mapped.substring(4));
                	VersionedResource vr=VersionedResource.load(in);
                	return vr.getHead();
                }     
            }
        }catch(Exception ex){
            ExHandler.handle(ex);
            log.log("Fehler bei decode ",Log.ERRORS);
        }
        return null;
    }

    private String encode(final int num, final PreparedStatement pst, final String field, final String value) {
        String mapped=map(field);
         String ret=value;
        try{
            if(mapped.startsWith("S:")){
                String typ=mapped.substring(2,3);
                mapped=mapped.substring(4);
                byte[] enc;
                
                if(typ.startsWith("D")){    // datum
                    TimeTool t=new TimeTool();
                    if((!StringTool.isNothing(value)) && (t.set(value)==true)){
                    	ret=t.toString(TimeTool.DATE_COMPACT);
                        pst.setString(num,ret);
                    }else{
                        ret="";
                        pst.setString(num,"");
                    }
                    
                }else if(typ.startsWith("C")){  // string enocding
                	enc=CompEx.Compress(value,CompEx.ZIP);
                    pst.setBytes(num,enc);
                }else{
                    log.log("Unbekannter encode code "+typ,Log.ERRORS);
                }
            }else{
                pst.setString(num,value);
            }
        }catch(Throwable ex){
            ExHandler.handle(ex);
            log.log("Fehler beim String encoder: "+ex.getMessage(),Log.ERRORS);
        }
        return ret;
    }
    
    public static final int MATCH_EXACT=0;
    public static final int MATCH_LIKE=1;
    public static final int MATCH_REGEXP=2;
    /**
     * Testet ob zwei Objekte bezüglich definierbarer Felder übereinstimmend sind
     * @param other anderes Objekt
     * @param mode gleich, LIKE oder Regexp
     * @param fields die interessierenden Felder
     * @return true wenn this und other vom selben typ sind und alle interessierenden
     * Felder genäss mode übereinstimmen.
     */
    public boolean isMatching(final PersistentObject other,final int mode, final String... fields){
        if(getClass().equals(other.getClass())){
            String[] others=new String[fields.length];
            other.get(fields,others);
            return isMatching(fields,mode,others);
        }
        return false;
    }
    /**
     * testet, ob die angegebenen Felder den angegebenen Werten entsprechen.
     * @param fields die zu testenden Felde
     * @param mode Testmodus (MATCH_EXACT, MATCH_LIKE oder MATCH_REGEXP)
     * @param others die Vergleichswerte
     * @return true bei übereinsteimmung
     */
    public boolean isMatching(final String[] fields, final int mode, final String... others){
        String[] mine=new String[fields.length];
        get(fields,mine);

        for(int i=0;i<fields.length;i++){
        	if(mine[i]==null){
        		if(others[i]==null){
        			return true;
        		}
        		return false;
        	}
        	if(others[i]==null){
        		return false;
        	}
            switch(mode){
            case MATCH_EXACT:
                if(!mine[i].toLowerCase().equals(others[i].toLowerCase())){
                    return false;
                }
                break;
            case MATCH_LIKE:
                if(!mine[i].toLowerCase().startsWith(others[i].toLowerCase())){
                    return false;
                }
                break;
            case MATCH_REGEXP:
                if(!mine[i].matches(others[i])){
                    return false;
                }
            }
            
        }
        return true; 
    }
    /**
     * Eine Transaktion beginnen. schreiboperationen müssen auf das zurückgelieferte Transactions-Objekt erfolgen.
     * (Und können mit Schreiboperationen ausserhalb der Transaktion konkurrieren)
     * @return Ein Transaktionsobjekt, über das Schreiboperationen getätigt werden kann, und das am Ende mit commit()
     * oder rollback() ausgeführt resp. gestoppt werden kann.
     */
    public Transaction begin(){
        return new Transaction(this);
    }
    
    /**
     * Get a unique key for a value, suitable for identifying a key in a cache.
     * The current implementation uses the table name, the id of the PersistentObject
     * and the field name.
     * 
     * @param field the field to get a key for
     * @return a unique key
     */
    private String getKey(final String field){
        StringBuffer key=new StringBuffer();
        
        key.append(getTableName());
        key.append(".");
        key.append(getId());
        key.append("#");
        key.append(field);
        
        return key.toString();
    }
    
    /**
     * Verbindung zur Datenbank trennen
     *
     */
	public static void disconnect() {
		if(j!=null){
            if(j.DBFlavor.startsWith("hsqldb")){
                j.exec("SHUTDOWN COMPACT");
            }
            j.disconnect();
    		j=null;
    		log.log("Verbindung zur Datenbank getrennt.",Log.INFOS);
    		cache.stat();
		}
	}
	/*
	private class CacheField implements ICacheable{
	    Object contents;
        long expires;
        CacheField(Object value){
            contents=value;
            expires=System.currentTimeMillis()+lifetime;
        }
        boolean expired(){
        	long act=System.currentTimeMillis();
        	if(expires<act){
        		return true;
        	}
        	return false;
        }
        
    }
	 */

	@Override
	public boolean equals(final Object arg0) {
		if(arg0 instanceof PersistentObject){
			return getId().equals(((PersistentObject)arg0).getId());
		}
		return false;
	}
	
	public static String checkNull(final String in){
		return in==null ? "" : in;
	}
	 
	public static int checkZero(final String in){
		if(StringTool.isNothing(in)){
			return 0;
		}
		try {
			return Integer.parseInt(in.trim());
		} catch (NumberFormatException ex) {
			ExHandler.handle(ex);
			return 0;
		}
	}
	public static double checkZeroDouble(final String in){
		if(StringTool.isNothing(in)){
			return 0.0;
		}
		try {
			return Double.parseDouble(in.trim());
		} catch (NumberFormatException ex) {
			ExHandler.handle(ex);
			return 0.0;
		}
	}
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
	public static void clearCache(){
		synchronized(cache){
			cache.clear();
		}
		System.gc();
	}
	public static void resetCache(){
		synchronized(cache){
			cache.reset();
		}
	}
	/**
	 * Return time-to-live in cache for this object
	 * @return the time in seconds
	 */
	public int getCacheTime(){
		return default_lifetime;
	}
	public static void setDefaultCacheLifetime(int seconds){
    	default_lifetime=seconds;
    }
    public static int getDefaultCacheLifetime(){
    	return default_lifetime;
    }
	public static boolean isShowDeleted() {
		return showDeleted;
	}
	public static void setShowDeleted(final boolean showDeleted) {
		PersistentObject.showDeleted = showDeleted;
	}

	/**
	 * Utility function to create or modify a table consistently. Should be used by all
	 * plugins that contributa data types derived from PersistentObject
	 * @param name name of the table to create 
	 * @param jointDB create string
	 */
	protected static void createTable(final String name, final String jointDB){
		ByteArrayInputStream bais;
		try {
			bais = new ByteArrayInputStream(jointDB.getBytes("UTF-8"));
			if(j.execScript(bais,true,false)==false){
				SWTHelper.showError("Datenbank-Fehler","Konnte Tabelle "+name+" nicht erstellen");
			}
		} catch (UnsupportedEncodingException e) {
			// should really never happen
			e.printStackTrace();
		}
	}

	/**
	 * Utility function to remove a table and all objects defined therein consistentliy
	 * To kame sure dependen data are deleted as well, we call each object's delete
	 * operator individually before dropping the table
	 * @param name the name of the table
	 */
	@SuppressWarnings("unchecked")
	protected static void removeTable(final String name, final Class oclas){
		Query qbe=new Query(oclas);
		for(Object o:qbe.execute()){
			((PersistentObject)o).delete();
		}
		j.exec("DROP TABLE "+name);
	}
}
