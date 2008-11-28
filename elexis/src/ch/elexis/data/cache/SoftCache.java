/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: SoftCache.java 4700 2008-11-28 16:01:51Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data.cache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.elexis.Hub;
import ch.elexis.util.Log;

/**
 * A Cache with soft references and optional expiring items The cache keeps count on numbes of items
 * that are added, removed or expired and can display its statistic
 * 
 * @author Gerry
 */
@SuppressWarnings("unchecked")
public class SoftCache<K> implements IPersistentObjectCache<K> {
	private static boolean enabled = true;
	private int num = 2000;
	private float load = 0.7f;
	protected Map<K, CacheEntry> cache;
	protected long hits, misses, removed, inserts, expired;
	protected Log log = Log.get("SoftCache");
	
	public SoftCache(){
		// must be thread-safe
		cache = Collections.synchronizedMap(new HashMap<K, CacheEntry>());
		// cache=new HashMap<K,CacheEntry>();
	}
	
	public SoftCache(final int num, final float load){
		cache = new HashMap<K, CacheEntry>(num, load);
		this.num = num;
		this.load = load;
	}
	
	public SoftCache(final int num){
		cache = new HashMap<K, CacheEntry>(num);
		this.num = num;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#put(K, java.lang.Object, int)
	 */
	public void put(final K key, final Object object, final int timeToCacheInSeconds){
		if (enabled) {
			cache.put(key, new CacheEntry(object, timeToCacheInSeconds));
			inserts++;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#get(K)
	 */
	public Object get(final K key){
		if (!enabled) {
			return null;
		}
		CacheEntry ref = cache.get(key);
		if (ref == null) {
			misses++;
			return null;
		}
		Object ret = ref.get();
		if (ret == null) {
			remove(key);
			return null;
		} else {
			hits++;
			return ret;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#remove(K)
	 */
	public void remove(final K key){
		cache.remove(key);
		removed++;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#clear()
	 */
	public void clear(){
		purge();
		cache.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#stat()
	 */
	public void stat(){
		long total = hits + misses + removed + expired;
		if (total != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("--------- cache statistics ------\n").append("Total read:\t").append(total)
				.append("\n").append("cache hits:\t").append(hits).append(" (").append(
					hits * 100 / total).append("%)\n").append("object expired:\t").append(expired)
				.append(" (").append(expired * 100 / total).append("%)\n")
				.append("cache missed:\t").append(misses).append(" (").append(misses * 100 / total)
				.append("%)\n").append("object removed:\t").append(removed).append(" (").append(
					removed * 100 / total).append("%)\n").append("Object inserts:\t").append(
					inserts).append("\n");
			log.log(sb.toString(), Log.INFOS);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#purge()
	 */
	public void purge(){
		Iterator<K> it = cache.keySet().iterator();
		long freeBefore = Runtime.getRuntime().freeMemory();
		while (it.hasNext()) {
			K k = it.next();
			CacheEntry ce = cache.get(k);
			ce.expires = 0;
			ce.get();
			it.remove();
		}
		
		if (Hub.DEBUGMODE) {
			long freeAfter = Runtime.getRuntime().freeMemory();
			StringBuilder sb = new StringBuilder();
			sb.append("Cache purge: Free memore before: ").append(freeBefore).append(
				", free memory after: ").append(freeAfter).append("\n");
			Hub.log.log(sb.toString(), Log.INFOS);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#reset()
	 */
	public synchronized void reset(){
		purge();
		cache = new HashMap<K, CacheEntry>(num, load);
	}
	
	public class CacheEntry extends SoftReference {
		long expires;
		
		public CacheEntry(final Object obj, final int timeInSeconds){
			super(obj);
			expires = System.currentTimeMillis() + timeInSeconds * 1000;
		}
		
		@Override
		public Object get(){
			Object ret = super.get();
			if (System.currentTimeMillis() > expires) {
				expired++;
				super.clear();
				ret = null;
			}
			return ret;
		}
	}
	
}
