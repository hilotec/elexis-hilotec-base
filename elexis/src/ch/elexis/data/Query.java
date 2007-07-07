/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - case insenitive add()
 *    
 * $Id: Query.java 2736 2007-07-07 14:07:40Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import org.eclipse.jface.viewers.IFilter;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;


/**
 * Query manages all database queries of PersistentObjects and derived classes
 * 
 * Die Query-Klasse erledigt alle Datenbankabfragen auf PersistentObjects und 
 * davon abgeleitete Klassen.
 * @author Gerry
 */

public class Query<T>{
	// private Query(){/* leer */}
	private StringBuffer sql;
	private static Log log=Log.get("Query");
	//private boolean restrictions;
	private PersistentObject template;
    private Method load;
    private String link=" WHERE ";
    private String lastQuery="";
    private LinkedList<IFilter> postQueryFilters=new LinkedList<IFilter>();
  
/**
 * Der einzige öffentliche Konstruktor 
 * @param cl Die Klasse, auf die die Abfrage angewendet werden soll (z.B. Patient.class)
 */
    public Query(Class<? extends PersistentObject> cl){
		try{
			template=Hub.poFactory.createTemplate(cl);
           //template=cl.newInstance();
            load=cl.getMethod("load",new Class[]{String.class});
            clear();
            
		}
		catch(Throwable ex){
		    log.log("Konnte Methode load auf "+cl.getName()+" nicht auflösen",Log.ERRORS);
            ExHandler.handle(ex);
        }

		
	}
    /** 
     * Abfrage löschen, beispielsweise um dasselbe Query-Objekt für eine neue
     * Abfrage zu verwenden.
     */
	public void clear()
	{
		sql=new StringBuffer(200);
		String table=template.getTableName();
		sql.append("SELECT ID FROM ").append(table);
		String cns=template.getConstraint();
		if(cns.equals("")){
			if(PersistentObject.isShowDeleted()){
				link=" WHERE ";
			}else{
				sql.append(" WHERE deleted=").append(JdbcLink.wrap("0"));
				link=" AND ";
			}
		}else{
			sql.append(" WHERE ").append(cns);
			if(!PersistentObject.isShowDeleted()){
				sql.append(" AND deleted=").append(JdbcLink.wrap("0"));
			}
			link=" AND ";
		}
	}
	
	private void append(String... s){
		sql.append(link);
		for(String a:s){
			sql.append(" ").append(a);
		}
		if(link.equals(" WHERE ") || link.equals("")){
			link=" AND ";
		}
	}
    /**
     * Folgende Ausdrücke bis endGroup gruppieren
     */
    public void startGroup(){
    	append("(");
    	link="";
    }
    /**
     * Gruppierung ende
     */
    public void endGroup(){
        sql.append(")");
    }
    /** Bedingung einsetzen, die immer erfüllt ist */
    public void insertTrue(){
        append("1=1");
    }
    /** Bedingung einsetzen, die nie erfüllt ist */
    public void insertFalse(){
        append("1=0");
    }
	/**
	 * AND-Verknüpfung anfügen.
	 */
	public void and(){
		if(link.equals(" OR ")){
			link=" AND ";
		}
	}
	/**
	 * OR-Verknüpfung anfügen 
	 */
	public void or(){
		link=" OR ";
	}
	/**
	 * Bedingung zufügen. Mehrere Bedingungen können hinzugefügt werden, indem jeweils zwischen
	 * zwei add() Aufrufen and() oder or() aufgerufen wird.
	 * Die Abfrage wird noch nicht ausgeführt, sondern erst beim abschliessenden execute()
	 * @param feld	Das Feld, für das die Bedingung gilt
	 * @param operator Vergleich (z.B. "=", "LIKE", ">", "<")
	 * @param wert Der Wert, der gesucht wird. Für Wildcard suche kann der Wert % enthalten,
	 * der Operator muss dann aber "LIKE" sein
	 * @param toLower bei true werden die Parameter mit der SQL-Funktion "lower()" in
+     * Kleinschreibung umgewandelt, so dass die Gross-/Kleinschreibung egal ist.
	 * @return false bei Fehler in der Syntax oder nichtexistenten Feldern
	 */
	public boolean add(String feld,String operator, String wert,boolean toLower){
		String mapped;
		mapped=template.map(feld);
		// treat date parameter separately
		// TODO This works only for european-style dates (dd.mm.yyyy)
		if(mapped.startsWith("S:D:")){
			mapped=mapped.substring(4);
			// if a date should be matched partially
			if(operator.equalsIgnoreCase("LIKE") && !wert.matches("[0-9]{8,8}")){
				StringBuilder sb=null;
				wert=wert.replaceAll("%", "");
				final String filler="%%%%%%%%";
				// are we looking for the year?
				if(wert.matches("[0-9]{3,}")){
					sb=new StringBuilder(wert);
					sb.append(filler);
					wert=sb.substring(0,8);
				}else{
					// replace single digits as in 1.2.1932 with double digits as in 01.02.1932
				    wert=wert.replaceAll("[^0-9]([0-9])\\.","0$1.");
				    // remove dots
					sb=new StringBuilder(wert.replaceAll("\\.",""));
					// String must consist of 8 or more digits (ddmmYYYY)
					sb.append(filler);
					// convert to YYYYmmdd format
					wert=sb.substring(4,8)+sb.substring(2,4)+sb.substring(0,2);
				}
			}else{
				TimeTool tm=new TimeTool();
				if(tm.set(wert)==true){
					wert=tm.toString(TimeTool.DATE_COMPACT);
				}
			}
		}else if(mapped.matches(".*:.*")){
			log.log("Ungültiges Feld "+feld,Log.ERRORS);
			return false;
		}

		if(wert==null){
			append(mapped,"is",operator,"null");
		}else{
			wert=PersistentObject.getConnection().wrapFlavored(wert);
		    //wert = JdbcLink.wrap(wert);
		    if (toLower) {
		    	mapped = "lower(" + mapped + ")";
		        wert = "lower(" + wert + ")";
		    }
		    append(mapped, operator, wert);
		}
		
		return true;
	}
	
	public boolean add(String feld,String operator, String wert){
		return add(feld,operator,wert, false);
	}
	/** Unverändertes Token in den SQL-String einfügen */
	public void addToken(String token){
		append(token);
	}
	/**
	 * Bequemlichkeitsmethode für eine Abfrage, die nur einen einzigen Treffer
	 * liefern soll. Die Syntax ist wie bei der add() Methode, aber die Abfrage wird gleich ausgeführt 
	 * @param f Feld
	 * @param op Vergleichsoperator (s. auch unter add())
	 * @param v Wert (@see Query#add() )
	 * @return Die ID des gefundenen Objekts oder null, wenn nicht gefunden
	 */
	public String findSingle(String f,String op, String v){
		clear();
		sql.append(link).append(template.map(f)).append(op).append(JdbcLink.wrap(v));
		String ret=PersistentObject.j.queryString(sql.toString());
		return ret;
	}
	
	/**
	 * Bequemlichkeitsmethode, um gleich mehrere Felder auf einmal anzugeben, welche mit
	 * AND verknüpft werden. Dies ist dasselbe, wie mehrere Aufrufe nacheinander von add()
	 * und and(), aber die Abfrage wird gleich ausgeführt und die Resultate werden nach den
	 * übergebenen Feldern sortiert, in der Reihenfolge, in der sie übergeben wurden.
	 * @param fields Die Felder, die in die abfrage eingesetzt werden sollen 
	 * @param values die Werte, nach denen gesucht werden soll. Wenn values für ein Feld leer
	 * ist (null oder ""), dann wird dieses Feld aus der Abfrage weggelassen
	 * @param exact false, wenn die Abfrage mit LIKE erfolgen soll, sonst mit =
	 * @return eine Liste mit den gefundenen Objekten
	 */
    public List<T> queryFields(String[] fields,String[] values, boolean exact){
    	clear();
        String op="=";
        if(exact==false){
            op=" LIKE ";
        }
        and();
        for(int i=0;i<fields.length;i++){
            if(StringTool.isNothing(values[i])){
                continue;
            }
            add(fields[i],op,values[i]);
        }
        return execute();
    }
    
    public PreparedStatement getPreparedStatement(PreparedStatement previous){
    	try{
	    	if(previous!=null){
	    		previous.close();
	    	}
	    	PreparedStatement ps=PersistentObject.j.prepareStatement(sql.toString());
	    	return ps;
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		log.log("Fehler beim PreparedStatement ",Log.ERRORS);
    		return null;
    	}
    }
    
    public ArrayList<String> execute(PreparedStatement ps, String[] values){
    	
    	try{
	    	for(int i=0;i<values.length;i++){
	    		ps.setString(i+1,values[i]);
	    	}
	    	if(ps.execute()==true){
	    		ArrayList<String> ret=new ArrayList<String>();
	    		ResultSet res=ps.getResultSet();
	    		while(res.next()){
	    			ret.add(res.getString(1));
	    		}
	    		return ret;
	    	}
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		log.log("Fehler beim Ausführen von "+sql.toString(),Log.ERRORS);
    	}
    	return null;
    }
    /**
     * Sortierung angeben. Dies muss als letzter Befehl nach einer Reihe von add() Sequenzen
     * erfolgen.
     * @param reverse true bei umgekehrter Sortierung
     * @param n1 Beliebig viele Strings, die in absteigender Priorität die Felder angeben,
     * nach denen sortiert werden soll.
     */
	public void orderBy(boolean reverse, String... n1){
		sql.append(" ORDER BY ");
		for(String s:n1){
			String mapped=template.map(s);
			if(mapped.matches("[A-Z]{2,}:.+")){
				log.log("Ungültiges Feld "+s,Log.ERRORS);
				return;
			}
			if(mapped.startsWith("S:D:")){
				mapped=mapped.substring(4);
			}
			sql.append(mapped);
			if(reverse==true){
				sql.append(" DESC");
			}
			sql.append(",");
		}
		sql.delete(sql.length()-1,10000);
	}
	
	/**
	 * Die zusammengestellte Abfrage ausführen
	 * Dies kann aufgerufen werden, nachdem alle nötigen add(), AND(), or()
	 * und orderBy() Operationen eingegeben wurden und liefert das Ergebnis dieser Abfrage.
	 * execute() kann mit derselben Abfrage beliebig oft aufgerufen werden (und kann unzterschiedliche
	 * Resultate liefern, wenn von anderer Stelle zwischenzeitlich eine Änderung der Datenbank erfolgte)
	 * @return eine Liste aus Objekten, die das Resultat der Abfrage sind.
	 */
	public List<T> execute(){
		lastQuery=sql.toString();
		//log.log("Executing query: "+lastQuery,Log.DEBUGMSG);
		LinkedList<T> ret=new LinkedList<T>();
        return (List<T>)queryExpression(lastQuery,ret);
	}
    
	public Collection<T> execute(Collection<T> collection){
		lastQuery=sql.toString();
		return queryExpression(lastQuery,collection);
	}
	
	/**
	 * Eine komplexe selbst zusammengestellte Abfrage ausführen. Die Methoden von Query erlauben
	 * eine einfache Zusammenstellung einer SQL-Abfrage, Für spezielle Fälle will man aber vielleicht die
	 * SQL-Abfrage doch selber direkt angeben. Dies kann hier erfolgen. 
	 * @param expr ein für die verwendete Datenbank akzeptabler SQL-String. Es soll nach Möglichkeit nur
	 * Standard-SQL verwendet werden, um sich nicht von einer bestimmten Datenbank abhängig zu machen.
	 * Die Abfrage muss nur nach dem Feld ID fragen; das Objekt wird von query selbst hergestellt.
	 * @return Eine Liste der Objekte, die als Antwort auf die Anfrage geliefert wurden.
	 */
    @SuppressWarnings("unchecked")
	public Collection<T> queryExpression(String expr, Collection<T> ret){
        //LinkedList<T> ret=new LinkedList<T>();
        
        Stm stm=null;
        try{
            stm=PersistentObject.j.getStatement();
            /*
            if(Hub.acl.request("Query"+template.getClass().getSimpleName())==false){
                log.log("Nicht genügend Rechte zum Lesen von "+template.getClass().getSimpleName(),Log.ERRORS);
                return null;
            }*/
            ResultSet res=stm.query(expr);
            while(res.next()==true){
                String id=res.getString("ID");
                T o=(T)load.invoke(null,new Object[]{id});
                boolean bAdd=true;
                for(IFilter fi:postQueryFilters){
                	if(fi.select(o)==false){
                		bAdd=false;
                		break;
                	}
                }
                if(bAdd==true){
                	ret.add(o);
                }

            }
            return ret;
            
        }catch(Exception ex){
            log.log("Fehler bei Datenbankabfrage ",Log.ERRORS);
            return null;
        }finally{
            PersistentObject.j.releaseStatement(stm);
        }
    }
    /*
    public PersistentObject createFromID(String id){
    	try{
    		return(PersistentObject)load.invoke(null,new Object[]{id});
    	}catch(Exception ex){
    		ExHandler.handle(ex);
    		log.log("Konnte Objekt nicht erzeugen",Log.ERRORS);
    		return null;
    	}
    }
    */
    /**
     * Die Grösse des zu erwartenden Resultats abfragen. Dieses Resultat stimmt nur ungefähr, da
     * es bis zur tatsächlichen Abfrage noch Änderungen geben kann, und da allfällige
     * postQueryFilter das Resultat verkleinern könnten.
     * @return die ungefähre Zahl der erwarteten Objekte.
     */
	public int size() {
		try{
			Stm stm=PersistentObject.j.getStatement();
			String res=stm.queryString("SELECT COUNT(*) FROM "+template.getTableName());
			PersistentObject.j.releaseStatement(stm);
			return Integer.parseInt(res);
		}catch(Exception ex){
			ExHandler.handle(ex);
			return 10000;
		}
	}
    public String getLastQuery(){
        return lastQuery;
    }
    public String getActualQuery(){
    	return sql.toString();
    }
 
    /**
     * PostQueryFilters sind Filter-Objeckte, die <i>nach</i> der Datenbankanfrage auf
     * das zurückgelieferte Resultat angewendet werden. Diese sind weniger effizient,
     * als Filter, die bereits im Query-String enthalten sind, aber sie erlauben
     * Datenbankunabhängig feinere Filterungen.
     * Sie sind auch die einzige Möglichkeit, auf komprimierte oder codierte Felder
     * zu filtern. 
     * @param f ein Filter
     */
    public void addPostQueryFilter(IFilter f){
    	postQueryFilters.add(f);
    }
    public void removePostQueryFilter(IFilter f){
    	postQueryFilters.remove(f);
    }
}