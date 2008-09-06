package ch.elexis.data.cache;

import java.util.logging.Logger;

import ch.elexis.util.Log;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class EhBasedCache<T> implements IPersistentObjectCache<T> {
	CacheManager mgr;
	Cache defaultCache, eternalCache;
	Log log=Log.get("EhCache");
	
	public EhBasedCache(String filename){
		if(filename==null){
			mgr=new CacheManager();
			defaultCache=new Cache("podefault",
                100000,							// Elements in memory
                MemoryStoreEvictionPolicy.LRU,
                false,							// boolean overflowToDisk,
                "java.io.tmpdir",				// String diskStorePath,
                false,							// boolean eternal,
                600,							// long timeToLiveSeconds,
                180,							// long timeToIdleSeconds,
                false,							//boolean diskPersistent,
                600,							// long diskExpiryThreadIntervalSeconds,
                null,							// RegisteredEventListeners registeredEventListeners,
                null							//BootstrapCacheLoader bootstrapCacheLoader)
           );
			mgr.addCache(defaultCache);
		}else{
			mgr=new CacheManager(filename);
			defaultCache=mgr.getCache("default");
		}
		
		
	}
	public void clear(){
		defaultCache.removeAll();
	}
	
	public Object get(Object key){
		Element el=defaultCache.get(key);
		return el==null ? el : el.getValue();
	}
	
	public void purge(){
		defaultCache.evictExpiredElements();
	
	}
	
	public void put(Object key, Object object, int timeToCacheInSeconds){
		Element el=new Element(key,object,false,timeToCacheInSeconds,timeToCacheInSeconds*4);
		defaultCache.put(el);
	}
	
	public void remove(Object key){
		defaultCache.remove(key);
	
	}
	
	public void reset(){
		clear();
	}
	
	public void stat(){
		Statistics stat=defaultCache.getStatistics();
		long hits=stat.getCacheHits();
		long misses=stat.getCacheMisses();
		long removed=stat.getEvictionCount();
		long expired=0;
		long total=hits+misses+removed+expired;
		long inserts=stat.getObjectCount();
		if(total!=0){
			StringBuilder sb=new StringBuilder();
			sb.append("--------- cache statistics ------\n")
				.append("Total read:\t").append(total).append("\n")
				.append("cache hits:\t").append(hits).append(" (").append(hits*100/total).append("%)\n")
				.append("object expired:\t").append(expired).append(" (").append(expired*100/total).append("%)\n")
				.append("cache missed:\t").append(misses).append(" (").append(misses*100/total).append("%)\n")
				.append("object removed:\t").append(removed).append(" (").append(removed*100/total).append("%)\n")
				.append("Object inserts:\t").append(inserts).append("\n");
			log.log(sb.toString(), Log.INFOS);
		}
	
	}
	
}
