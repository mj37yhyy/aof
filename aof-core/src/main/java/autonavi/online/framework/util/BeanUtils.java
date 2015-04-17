package autonavi.online.framework.util;

import java.util.Map;


public class BeanUtils {
	/**
	 * 实类生产工具
	 * 
	 * @param className
	 * @param map
	 * @return
	 */
	public static Object generateObject(String className, Map<String, Object> map)
			throws Exception {
		Class<?> clazz = BeanUtils.class.getClassLoader().loadClass(className);
		Object obj = clazz.newInstance();
		org.apache.commons.beanutils.BeanUtils.populate(obj, map);
		return obj;
	}

}
