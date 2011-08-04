/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id$
 *******************************************************************************/

package ch.elexis.core.data;

import java.util.List;
import java.util.Map;

import ch.elexis.core.ElexisStorageException;
import ch.elexis.core.data.Query.Term;

/**
 * An IPersistentObject is an abstract representation of an Object with a number of features:
 * <ul>
 * <li>it persists itself</li>
 * <li>it has an unlimited number of randomly named properties, that can be accessed with
 * set(String,String) and get(String) methods</li>
 * <li>it has a globally unique identifier</li>
 * </ul>
 * 
 * @author gerry
 * 
 */
public interface IPersistentObject extends ISelectable {
	/** predefined field name for the GUID */
	public static final String FLD_ID = "id";
	/** predefined property to handle a fields that is a compressed HashMap */
	public static final String FLD_EXTINFO = "ExtInfo";
	/** predefined property to hande a field that marks the Object as deleted */
	public static final String FLD_DELETED = "deleted";
	/**
	 * predefined property that holds an automatically updated field containing the last update of
	 * this object as long value (milliseconds as in Date())
	 */
	public static final String FLD_LASTUPDATE = "lastupdate";
	/**
	 * predefined property that holds the date of creation of this object in the form YYYYMMDD
	 */
	public static final String FLD_DATE = "Datum";
	
	/**
	 * return a human readable identifier (not necessarily unique) for this Object
	 */
	abstract public String getLabel();
	
	/**
	 * Tell whether this Object is valid (measured by its own implementation dependent means)
	 * 
	 * @return true if this Object is valid (which is not the same as "correct")
	 */
	public abstract boolean isValid();
	
	/**
	 * Return an identifier for this object that is guaranteed to be globally unique
	 * 
	 * @return the ID.
	 */
	public abstract String getId();
	
	/**
	 * Objekt in einen String serialisieren. Diese Standardimplementation macht eine "cheap copy":
	 * Es wird eine Textrepräsentation des Objektes erstellt, mit deren Hilfe das Objekt später
	 * wieder aus der Datenbank erstellt werden kann. Dies funktioniert nur innerhalb derselben
	 * Datenbank.
	 * 
	 * @return der code-String, aus dem mit createFromCode wieder das Objekt erstellt werden kann
	 */
	public abstract String storeToString();
	
	/** An object with this ID does not exist */
	public static final int STATE_INEXISTENT = 0;
	/** This id is not valid */
	public static final int STATE_INVALID_ID = 1;
	/** An object with this ID exists but is marked deleted */
	public static final int DELETED = 2;
	/** This is an existing object */
	public static final int STATE_EXISTING = 3;
	
	/**
	 * Check the state of an object with this ID Note: This method accesses the database and
	 * therefore is much more costly than the simple instantaniation of a PersistentObject
	 * 
	 * @return a value between INEXISTENT and EXISTING
	 */
	
	public abstract int state();
	
	/**
	 * Feststellen, ob ein PersistentObject bereits in der Datenbank existiert
	 * 
	 * @return true wenn es existiert, false wenn es nicht existiert oder gelöscht wurde
	 */
	
	public abstract boolean exists();
	
	/**
	 * Check whether the object exists in the database. This is the case for all objects in the
	 * database for which state() returns neither INVALID_ID nor INEXISTENT. Note: objects marked as
	 * deleted will also return true!
	 * 
	 * @return true, if the object is available in the database, false otherwise
	 */
	public abstract boolean isAvailable();
	
	/**
	 * Return an IXid (domain_id) for a specified domain
	 * 
	 * @param domain
	 * @return an identifier that might be null
	 */
	public abstract IXid getXid(final String domain);
	
	/**
	 * return the "best" AbstractXid for a given object. This is the AbstractXid with the highest
	 * quality. If no AbstractXid is given for this object, a newly created AbstractXid of local
	 * quality will be returned
	 */
	public IXid getXid();
	
	/**
	 * retrieve all AbstractXids of this object
	 * 
	 * @return a List that might be empty but is never null
	 */
	public abstract List<IXid> getXids();
	
	/**
	 * Assign a AbstractXid to this object.
	 * 
	 * @param domain
	 *            the domain whose ID will be assigned
	 * @param domain_id
	 *            the id out of the given domain fot this object
	 * @param updateIfExists
	 *            if true update values if AbstractXid with same domain and domain_id exists.
	 *            Otherwise the method will fail if a collision occurs.
	 * @return true on success, false on failure
	 */
	public abstract boolean addXid(final String domain, final String domain_id,
		final boolean updateIfExists);
	
	/**
	 * holt den "höchstwertigen" Sticker, falls mehrere existieren
	 * 
	 * @return
	 */
	public ISticker getSticker();
	
	/**
	 * Return all Stickers attributed to this object
	 * 
	 * @return A possibly empty list of Stickers
	 */
	public abstract List<ISticker> getStickers();
	
	/**
	 * Remove aAbstractStickerfrom this object
	 * 
	 * @param et
	 *            theAbstractStickerto remove
	 */
	public abstract void removeSticker(ISticker et);
	
	/**
	 * Add aAbstractStickerto this object
	 * 
	 * @param et
	 *            theAbstractStickerto add
	 */
	public abstract void addSticker(ISticker et);
	
	/**
	 * Feststellen, ob ein PersistentObject als gelöscht markiert wurde
	 * 
	 * @return true wenn es gelöscht ist
	 */
	public abstract boolean isDeleted();
	
	/**
	 * Darf dieses Objekt mit Drag&Drop verschoben werden?
	 * 
	 * @return true wenn ja.
	 */
	public abstract boolean isDragOK();
	
	public abstract String get(final String field);
	
	/**
	 * Read a property that contains a Map
	 * 
	 * @param field
	 *            Name of the map
	 * @return a map that might be empty but is never null
	 */
	@SuppressWarnings("rawtypes")
	public abstract Map<?, ?> getMap(final String field);
	
	/**
	 * Bequemlichkeitsmethode zum lesen eines Integer.
	 * 
	 * @param field
	 * @return einen Integer. 0 bei 0 oder unlesbar
	 */
	public abstract int getInt(final String field);
	
	/**
	 * Eine 1:n Verknüpfung aus der Datenbank auslesen.
	 * 
	 * @param field
	 *            das Feld, wie in der mapping-Deklaration angegeben
	 * @param reverse
	 *            wenn true wird rückwärts sortiert
	 * @return eine Liste mit den IDs (String!) der verknüpften Datensätze oder null, wenn das Feld
	 *         keine 1:n-Verknüofung ist
	 */
	// public List<String> getList(String field,
	// boolean reverse);
	
	/**
	 * Eine n:m - Verknüpfung auslesen
	 * 
	 * @param field
	 *            Das Feld, für das ein entsprechendes mapping existiert
	 * @param extra
	 *            Extrafelder, die aus der joint-Tabelle ausgelesen werden sollen
	 * @return eine Liste aus String-Arrays, welche jeweils die ID des gefundenen Objekts und den
	 *         Inhalt der Extra-Felder enthalten. Null bei Mapping-Fehler
	 */
	// public abstract List<String[]> getList(final String field, String[] extra);
	
	/**
	 * Ein Feld in die Datenbank übertragen. Gleichzeitig Cache-update Die Tabelle wird über
	 * getTableName() erfragt.
	 * 
	 * @param field
	 *            Name des Feldes
	 * @param value
	 *            Einzusetzender Wert (der vorherige Wert wird überschrieben)
	 * @return true bei Erfolg
	 */
	public abstract boolean set(final String field, String value);
	
	/**
	 * store a map.
	 * 
	 * @param field
	 * @param hash
	 * @return 0 bei Fehler
	 */
	@SuppressWarnings("rawtypes")
	public abstract void setMap(final String field, final Map<Object, Object> map)
		throws ElexisStorageException;
	
	/**
	 * Set a value of type int.
	 * 
	 * @param field
	 *            a table field of numeric type
	 * @param value
	 *            the value to be set
	 * @return true on success, false else
	 */
	public abstract boolean setInt(final String field, final int value);
	
	/**
	 * Eine Element einer n:m Verknüpfung eintragen. Zur Tabellendefinition wird das mapping
	 * verwendet.
	 * 
	 * @param field
	 *            Das n:m Feld, für das ein neuer Eintrag erstellt werden soll.
	 * @param oID
	 *            ID des Zielobjekts, auf das der Eintrag zeigen soll
	 * @param extra
	 *            Definition der zusätzlichen Felder der Joint-Tabelle. Jeder Eintrag in der Form
	 *            Feldname=Wert
	 * @return 0 bei Fehler
	 */
	// public abstract int addToList(final String field, final String oID,
	// final String[] extra);
	
	/**
	 * Remove all relations to this object from link
	 * 
	 * @param field
	 */
	public abstract void removeFromList(String field);
	
	/**
	 * Remove a relation to this object from link
	 * 
	 * @param field
	 * @param oID
	 */
	public abstract void removeFromList(String field, String oID);
	
	/**
	 * Alle Bezüge aus einer n:m-Verknüpfung zu diesem Objekt löschen
	 * 
	 * @param field
	 *            Feldname, der die Liste definiert
	 * @return
	 */
	public abstract boolean deleteList(final String field);
	
	/**
	 * Mehrere Felder auf einmal setzen (Effizienter als einzelnes set)
	 * 
	 * @param fields
	 *            die Feldnamen
	 * @param values
	 *            die Werte
	 * @return false bei Fehler
	 */
	public abstract boolean set(final String[] fields, final String[] values);
	
	/**
	 * Mehrere Felder auf einmal auslesen
	 * 
	 * @param fields
	 *            die Felder
	 * @param values
	 *            String Array für die gelesenen Werte
	 * @return true ok, values wurden gesetzt
	 */
	public abstract boolean get(final String[] fields, final String[] values);
	
	/** Strings must match exactly (but ignore case) */
	public static final int MATCH_EXACT = 0;
	/** String must start with test (ignoring case) */
	public static final int MATCH_START = 1;
	/** String must match as regular expression */
	public static final int MATCH_REGEXP = 2;
	/** String must contain test (ignoring case) */
	public static final int MATCH_CONTAINS = 3;
	/**
	 * Try to find match method.
	 * <ul>
	 * <li>If test starts with % or * use MATCH_CONTAINS</li>
	 * <li>If test is enclosed in / use MATCH_REGEXP</li>
	 * </ul>
	 * 
	 */
	public static final int MATCH_AUTO = 4;
	
	/**
	 * Testet ob zwei Objekte bezüglich definierbarer Felder übereinstimmend sind
	 * 
	 * 
	 * @param other
	 *            anderes Objekt
	 * @param mode
	 *            gleich, LIKE oder Regexp
	 * @param fields
	 *            die interessierenden Felder
	 * @return true wenn this und other vom selben typ sind und alle interessierenden Felder genäss
	 *         mode übereinstimmen.
	 */
	public boolean isMatching(final IPersistentObject other, final int mode, final String[] fields);
	
	/**
	 * testet, ob die angegebenen Felder den angegebenen Werten entsprechen.
	 * 
	 * @param fields
	 *            die zu testenden Felde
	 * @param mode
	 *            Testmodus (MATCH_EXACT, MATCH_LIKE oder MATCH_REGEXP)
	 * @param others
	 *            die Vergleichswerte
	 * @return true bei übereinsteimmung
	 */
	public boolean isMatching(final String[] fields, final int mode, final String[] others);
	
	/**
	 * Testet ob dieses Objekt den angegebenen Feldern entspricht.
	 * 
	 * @param fields
	 *            HashMap mit name,wert paaren für die Felder
	 * @param mode
	 *            Testmodus (MATCH_EXACT, MATCH_BEGIN, MATCH_REGEXP, MATCH_CONTAIN oder MATCH_AUTO)
	 * @param bSkipInexisting
	 *            don't return false if a fieldname is not found but skip this field instead
	 * @return true wenn dieses Objekt die entsprechenden Felder hat
	 */
	public boolean isMatching(final Map<String, String> fields, final int mode,
		final boolean bSkipInexisting);
	
	public boolean isMatching(final List<Term> terms);
	
	/**
	 * return the time of the last update of this object
	 * 
	 * @return the time (as given in System.currentTimeMillis()) of the last write operation on this
	 *         object or 0 if there was no valid lastupdate time
	 */
	public abstract long getLastUpdate();
	
	public void addChangeListener(IChangeListener listener, String fieldToObserve);
	
	public void removeChangeListener(IChangeListener listener, String fieldObserved);
	
}