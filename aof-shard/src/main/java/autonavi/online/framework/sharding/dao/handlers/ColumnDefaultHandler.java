package autonavi.online.framework.sharding.dao.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class ColumnDefaultHandler<T> {
	protected static final Map<String, Map<Class<?>, Method>> resultSetGetterMethods = new HashMap<String, Map<Class<?>, Method>>() {
		private static final long serialVersionUID = -1668494496368917439L;
		{
			if (size() == 0) {
				Method[] methods = ResultSet.class.getMethods();
				for (Method method : methods) {
					if (method.getParameterTypes().length == 1
							&& method.getReturnType() != null
							&& method.getName().startsWith("get")) {
						Map<Class<?>, Method> map = new HashMap<Class<?>, Method>();
						if(method.getName().equals("getInt")){
							map.put(method.getParameterTypes()[0], method);
							put("getInteger", map);
						}
						if (!containsKey(method.getName())) {
							map.put(method.getParameterTypes()[0], method);
							put(method.getName(), map);
						} else {
							map = get(method.getName());
							map.put(method.getParameterTypes()[0], method);
						}
					}
				}
			}

		}
	};
	/**
	 * The column number to retrieve.
	 */
	protected int columnIndex;

	/**
	 * The column name to retrieve. Either columnName or columnIndex will be
	 * used but never both.
	 */
	protected String columnName=null;

	protected Class<T> resultType;
	
	protected T defaultHandle(ResultSet rs) throws SQLException{
		if(rs.next()){
			return defaultHandleWithOutNext(rs);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	protected  T defaultHandleWithOutNext(ResultSet rs) throws SQLException{
		try {
			String resultTypeSimpleName = this.resultType.getSimpleName();
			Map<Class<?>, Method> map = resultSetGetterMethods.get("get"
					+ resultTypeSimpleName.substring(0, 1).toUpperCase()
					+ resultTypeSimpleName.substring(1));
			if (map != null) {
				Method method = null;
				if (this.columnName == null) {
					method = map.get(int.class);
					if (method != null) {
						return (T) method.invoke(rs, this.columnIndex);
					}
				} else {
					method = map.get(String.class);
					if (method != null) {
						return (T) method.invoke(rs, this.columnName);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
