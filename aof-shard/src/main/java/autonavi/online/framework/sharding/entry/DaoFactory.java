package autonavi.online.framework.sharding.entry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DaoFactory {

	private static final Map<String, Object> daoMap = Collections
			.synchronizedMap(new HashMap<String, Object>());

	protected static void put(String id, Object obj) {
		daoMap.put(id, obj);
	}

	protected static void putAll(Map<String, Object> daoMap) {
		daoMap.putAll(daoMap);
	}

	public Object getDao(String id) {
		return daoMap.get(id);
	}

	@SuppressWarnings("unchecked")
	public <T> T getDao(Class<T> clazz) {
		for (Object proxyObj : daoMap.values()) {
			if (clazz.isAssignableFrom(proxyObj.getClass()))
				return (T) proxyObj;
		}
		return null;
	}
}
