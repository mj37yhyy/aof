package autonavi.online.framework.cache.ehcache;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esotericsoftware.minlog.Log;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class EhcacheUtils {
	public static String CLOSE_TIMEOUT = "CLOSE_TIMEOUT";
	public static String OPEN_TIMEOUT = "OPEN_TIMEOUT";
	private static CacheManager manager = null;
	private static Map<String, Long> cacheCount = new ConcurrentHashMap<String, Long>();
	private Cache cache = null;

	private Logger log = LogManager.getLogger(this.getClass());

	public Cache getCache() {
		return cache;
	}

	public EhcacheUtils(String cacheName) {
		if (manager == null) {
			URL url = getClass().getResource("ehcache.xml");
			this.setManager(CacheManager.create(url));
		}

		if (this.getManager().getCache(cacheName) == null)
			this.getManager().addCache(cacheName);
		cache = this.getManager().getCache(cacheName);
	}

	public synchronized Long openOrCloseCacheTimeOut(String oper) {
		CacheConfiguration config = this.getCache().getCacheConfiguration();
		Long count = 0L;
		if (cacheCount.get(this.getCache().getName()) != null) {
			count = cacheCount.get(this.getCache().getName());
		}
		if (oper.equals(EhcacheUtils.OPEN_TIMEOUT)) {
			log.info("将分区信息缓存设置为自动超时");
			if (count - 1 == 0) {
				config.setTimeToIdleSeconds(60);
				config.setTimeToLiveSeconds(60);
			}
			cacheCount.put(this.getCache().getName(), count - 1);
			return count - 1;
		} else if (oper.equals(EhcacheUtils.CLOSE_TIMEOUT)) {
			log.info("将分区信息全部加在DAO缓存");
			if (count == 0L) {
				config.setTimeToIdleSeconds(0);
				config.setTimeToLiveSeconds(0);
			}
			cacheCount.put(this.getCache().getName(), count + 1);
			return count + 1;
		} else {
			return count;
		}

	}

	private void setManager(CacheManager manager) {
		EhcacheUtils.manager = manager;
	}

	public static CacheManager getManager() {
		return manager;
	}

	public void set(Serializable key, Serializable value) {
		Element element = new Element(key, value);
		cache.put(element);
	}

	public Object get(Serializable key) {
		Element el = cache.get(key);
		return el == null ? null : el.getObjectValue();
	}

	public static void clean(String cacheName) {
		if (manager.cacheExists(cacheName)) {
			manager.getCache(cacheName).removeAll();
		}
	}

	public static void close() {
		manager.shutdown();
	}
}
