package autonavi.online.framework.util.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.beans.BeanCopier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.sharding.dao.constant.ReservedWord;

public class PropertyUtils {
	private static Logger log = LogManager.getLogger(PropertyUtils.class);

	/**
	 * 获取实体中的内容，只允许是Map,List和JavaBean
	 * 
	 * @param root
	 * @param expression
	 * @return
	 * @throws Exception
	 */
	public static Object getValue(Object root, String expression)
			throws Exception {
		String[] eArray = expression.split("\\.");
		Object result = root;
		int i = 0;
		while (i < eArray.length) {
			if (result instanceof Map) {// map
				result = ((Map<?, ?>) result).get(eArray[i]);
			} else if (result instanceof List || result.getClass().isArray()) {// list
				int index = -1;
				String indexString = expression.substring(
						expression.indexOf("[") + 1, expression.indexOf("]"));
				if (indexString != null && !indexString.isEmpty()) {
					index = Integer.valueOf(indexString);
				}
				if (index > -1) {
					if (result instanceof List) {// list
						result = ((List<?>) result).get(index);
					} else if (result.getClass().isArray()) {
						result = ((Object[]) result)[index];
					}
				}

			} else {// bean
				String name = eArray[i], getterMethodName = null, setterMethodName = null;
				Method getterMethod = null;
				try {
					getterMethod = new PropertyDescriptor(name,
							result.getClass()).getReadMethod();
				} catch (IntrospectionException e) {
					// 按照javabean规范的一条特殊规定：如果名字的第二个字母为大写，则第一个字母就不用变成大写；如果第一个字母本身就是大写则不变。
					// 比如eName的getter方法为geteName()；而URL的getter方法为getURL()
					// 注：该条规范在javabean
					// 1.01规范中没有找到，但eclipse自动生成的javabean方法却遵循此原则，故在这里做兼容处理
					// 附第三条规范：对于boolean类型,其getter方法前缀要变为is。比如:boolean
					// ok的getter方法为isOk;对于第一个字母小写，第二个字母大写的情况，同样遵循上述方法，即：boolean
					// mTime的getter方法为ismTime。
					if (name.length() > 1 && name.charAt(1) >= 'A'
							&& name.charAt(1) <= 'Z') {
						getterMethodName = "is" + name;
						setterMethodName = "set" + name;
						try {
							getterMethod = new PropertyDescriptor(name,
									result.getClass(), getterMethodName,
									setterMethodName).getReadMethod();
						} catch (IntrospectionException e1) {
							try {
								getterMethodName = "get" + name;
								getterMethod = new PropertyDescriptor(name,
										result.getClass(), getterMethodName,
										setterMethodName).getReadMethod();
							} catch (IntrospectionException e2) {
								throw e2;
							}
						}
					} else {
						throw e;
					}
				}
				result = getterMethod.invoke(result);
			}
			if (result == null) {
				if (log.isWarnEnabled())
					log.warn("入参可能为空，请检查入参 expression="
							+ expression.replaceAll("\\[[0-9]\\]",
									ReservedWord.index));
			}
			i++;
		}
		return result;
	}

	/**
	 * 复制属性
	 * 
	 * @param srcObj
	 * @param destObj
	 */
	public static void copy(Object srcObj, Object destObj) {
		String key = genKey(srcObj.getClass(), destObj.getClass());
		BeanCopier copier = null;
		if (!BEAN_COPIERS.containsKey(key)) {
			copier = BeanCopier.create(srcObj.getClass(), destObj.getClass(),
					false);
			BEAN_COPIERS.put(key, copier);
		} else {
			copier = BEAN_COPIERS.get(key);
		}
		copier.copy(srcObj, destObj, null);
	}

	private static String genKey(Class<?> srcClazz, Class<?> destClazz) {
		return srcClazz.getName() + destClazz.getName();
	}

	static final Map<String, BeanCopier> BEAN_COPIERS = new HashMap<String, BeanCopier>();
}
