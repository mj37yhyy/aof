package autonavi.online.framework.sharding.holder;

import java.util.HashMap;
import java.util.Map;

import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;

public class XmlBuilderHolder {

	public static ThreadLocal<Map<Class<?>, Map<String, SqlSource>>> classesHolder = new ThreadLocal<Map<Class<?>, Map<String, SqlSource>>>() {
		{
			set(new HashMap<Class<?>, Map<String, SqlSource>>());
		}
	};
}
