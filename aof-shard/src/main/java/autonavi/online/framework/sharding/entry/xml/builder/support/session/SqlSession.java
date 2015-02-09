package autonavi.online.framework.sharding.entry.xml.builder.support.session;

import java.util.HashMap;
import java.util.Map;

import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;

public class SqlSession {

	private Map<Class<?>, Map<String, SqlSource>> classes = new HashMap<Class<?>, Map<String, SqlSource>>();

	public Map<Class<?>, Map<String, SqlSource>> getClasses() {
		return classes;
	}

	public void addClass(Class<?> key, Map<String, SqlSource> value) {
		this.classes.put(key, value);
	}

	public void setClasses(Map<Class<?>, Map<String, SqlSource>> classes) {
		this.classes = classes;
	}

	public SqlSource getSqlSource(Class<?> interfaceClassName, String id) {
		return classes.get(interfaceClassName).get(id);
	}

}
