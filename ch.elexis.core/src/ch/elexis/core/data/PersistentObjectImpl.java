/*****************************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     G. Weirich - initial implementation
 *
 * $Id$
 ****************************************************************************************/

package ch.elexis.core.data;

import ch.elexis.core.ElexisStorageException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class PersistentObjectImpl implements IPersistentObject {
	Map<String, Object> props = new HashMap<String, Object>();
	Map<String, List<IChangeListener>> listeners = new HashMap<String, List<IChangeListener>>();
	
	private void fireChanged(String field, Object oldVal, Object newVal){
		List<IChangeListener> fields = listeners.get(field);
		if (fields != null) {
			for (IChangeListener icl : fields) {
				icl.valueChanged(this, field, oldVal, newVal);
			}
		}
	}
	
	@Override
	public boolean isValid(){
		return false;
	}
	
	@Override
	public String getId(){
		return get(IPersistentObject.FLD_ID);
	}
	
	@Override
	public String storeToString(){
		return getClass().getName() + "::" + getId();
	}
	
	@Override
	public int state(){
		return IPersistentObject.STATE_INVALID_ID;
	}
	
	@Override
	public boolean exists(){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean isAvailable(){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public IXid getXid(String domain){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public IXid getXid(){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public List<IXid> getXids(){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean addXid(String domain, String domain_id, boolean updateIfExists){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public ISticker getSticker(){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public List<ISticker> getStickers(){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public void removeSticker(ISticker et){
	// To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public void addSticker(ISticker et){
	// To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean isDeleted(){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean isDragOK(){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public String get(String field){
		return (String) props.get(field);
	}
	
	@Override
	public Map<?, ?> getMap(String field){
		return null; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public int getInt(String field){
		return Integer.parseInt(get(field));
	}
	
	@Override
	public boolean set(String field, String value){
		Object oldVal = get(field);
		props.put(field, value);
		fireChanged(field, oldVal, value);
		return true;
	}
	
	@Override
	public void setMap(String field, Map<Object, Object> map) throws ElexisStorageException{
	// To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean setInt(String field, int value){
		return set(field, Integer.toString(value));
	}
	
	@Override
	public void removeFromList(String field){
	// To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public void removeFromList(String field, String oID){
	// To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean deleteList(String field){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean set(String[] fields, String[] values){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean get(String[] fields, String[] values){
		for (int i = 0; i < fields.length; i++) {
			values[i] = get(fields[i]);
		}
		return true;
	}
	
	@Override
	public boolean isMatching(IPersistentObject other, int mode, String[] fields){
		String[] values = new String[fields.length];
		if (get(fields, values)) {
			return isMatching(fields, mode, values);
		}
		return false;
	}
	
	@Override
	public boolean isMatching(String[] fields, int mode, String[] others){
		for (int i = 0; i < fields.length; i++) {
			if (!get(fields[i]).equals(others[i])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isMatching(Map<String, String> fields, int mode, boolean bSkipInexisting){
		
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public boolean isMatching(List<Query.Term> terms){
		return false; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public long getLastUpdate(){
		return 0; // To change body of implemented methods use File | Settings | File Templates.
	}
	
	@Override
	public void addChangeListener(IChangeListener listener, String fieldToObserve){
		List<IChangeListener> fields = listeners.get(fieldToObserve);
		if (fields == null) {
			fields = new ArrayList<IChangeListener>();
			listeners.put(fieldToObserve, fields);
		}
		if (!fields.contains(listener)) {
			fields.add(listener);
		}
	}
	
	@Override
	public void removeChangeListener(IChangeListener listener, String fieldObserved){
		List<IChangeListener> fields = listeners.get(fieldObserved);
		if (fields != null) {
			fields.remove(listener);
		}
	}
}
