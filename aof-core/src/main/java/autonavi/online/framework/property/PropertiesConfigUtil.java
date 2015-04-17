package autonavi.online.framework.property;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * 属性加载通用对象<br/>
 * 抽象类-不可实例化<br/>
 * 若数据库字段有变，请继承此类比实现方法getPropertiesConfigFormDB，并用继承的方法来调用 例如
 * :YourPropertiesConfigUtil.getPropertiesConfigInstance();<br/>
 * 此方法实现机制为：<br/>
 * 通过一个固定接口PropertiesConfig 每次在系统启动第一次调用getPropertiesConfigInstance()的时候，<br/>
 * 获取配置数据并用字节码技术生成一个接口PropertiesConfig的实现，并清除已经存在的实现，<br/>
 * 这个实现的后缀为时间戳，并直接放入ClassLoader
 * 以后再调用getPropertiesConfigInstance()时候，直接获取这个生成的实类使用(Class.forName);<br/>
 * 当出现配置数据更新时后，请调用refreshPropertiesConfigInstance()方法，此方法将清除掉已经生成的.class的字节码文件，<br/>
 * 并生成新的带时间戳后缀的.class文件并放入ClassLoader中。<br/>
 * 已经删除的.class文件将等待java GC自动处理<br/>
 * 
 * @author yaming.xu
 * 
 */

public class PropertiesConfigUtil {
	private static final String PROPERTIES_CONFIG_INTERFACE = ".PropertiesConfig";
	private static final String PROPERTIES_CONFIG_IMPL = ".PropertiesConfigImpl";
	private static String PROPERTIES_CONFIG_IMPL_SUFFIX = "";

	private static PropertiesData propertiesData = null;

	/**
	 * 获取或者初始化配置对象
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public final static PropertiesConfig getPropertiesConfigInstance() throws Exception {
		if (PROPERTIES_CONFIG_IMPL_SUFFIX.equals(""))
			PROPERTIES_CONFIG_IMPL_SUFFIX = new Date().getTime() + "";
		try {
			return (PropertiesConfig) Class.forName(
					PropertiesConfigUtil.class.getPackage().getName()
							+ PROPERTIES_CONFIG_IMPL
							+ PROPERTIES_CONFIG_IMPL_SUFFIX).newInstance();
		} catch (ClassNotFoundException e) {
			refresh();
			return (PropertiesConfig) Class.forName(
					PropertiesConfigUtil.class.getPackage().getName()
							+ PROPERTIES_CONFIG_IMPL
							+ PROPERTIES_CONFIG_IMPL_SUFFIX).newInstance();
		}
	}

	/**
	 * 更新配置数据后调用
	 * 
	 * @param type
	 * @return 
	 * @throws Exception
	 */
	public final static synchronized Map<String, String> refresh() throws Exception {
		cleanClazz();
		String implSuffix = new Date().getTime() + "";
		Map<String, String> map = propertiesData
				.getProperties();
		generateClazz(map, implSuffix);
		PROPERTIES_CONFIG_IMPL_SUFFIX = implSuffix;
		return map;
	}

	/**
	 * 更新配置数据后调用
	 * 
	 * @param type
	 * @return 
	 * @throws Exception
	 */
	public final static synchronized Map<String, String> refresh(PropertiesData propertiesData)
			throws Exception {
		PropertiesConfigUtil.propertiesData = propertiesData;
//		cleanClazz();
//		String implSuffix = new Date().getTime() + "";
//		Map<String, String> map = propertiesData.getProperties();
//		generateClazz(map, implSuffix);
//		PROPERTIES_CONFIG_IMPL_SUFFIX = implSuffix;
//		return map;
		return refresh();
	}

	/**
	 * 清除已经生成的class文件
	 */
	private final static void cleanClazz() {
		if(PropertiesConfigUtil.class.getResource("/")!=null){
			String path = PropertiesConfigUtil.class.getResource("/").toString();
			path = path.replaceAll("file:", "")
					+ (PropertiesConfigUtil.class.getPackage().getName())
							.replaceAll("\\.", "/");
			File f = new File(path);
			if (f.exists()) {
				File[] fs = f.listFiles();
				if (fs != null) {
					for (File file : fs) {
						if (file.isFile()
								&& file.getName().indexOf(
										PROPERTIES_CONFIG_IMPL.substring(1)) != -1) {
							file.delete();
						}
					}
				}
			}
		}
		
	}

	/**
	 * 生成class文件
	 * 
	 * @param map
	 * @param implSuffix
	 * @return
	 * @throws Exception
	 */
	private final static Class<?> generateClazz(Map<String, String> map,
			String implSuffix) throws Exception {
		if(PropertiesConfigUtil.class.getResource("/")!=null){
			StringBuffer buffer = new StringBuffer();
			ClassPool pool = ClassPool.getDefault();
			String path = PropertiesConfigUtil.class.getResource("/").toString();
			pool.insertClassPath(path.replaceAll("file:", ""));
			pool.insertClassPath(new ClassClassPath(PropertiesConfigUtil.class));
			Class<?> clazz = null;
			int count = 0;
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String v = it.next();
				if (count == 0) {
					buffer.append("if(name!=null&&name.equals(\"" + v
							+ "\"))return \"" + map.get(v).replaceAll("\"", "\\\\\"") + "\";");
				} else {
					buffer.append("else if(name!=null&&name.equals(\"" + v
							+ "\"))return \"" + map.get(v).replaceAll("\"", "\\\\\"") + "\";");
				}
				count++;

			}
			if (count > 0) {
				buffer.append("else return null;");
			} else {
				buffer.append(" return null;");
			}

			CtClass cc = pool.makeClass(PropertiesConfigUtil.class.getPackage()
					.getName() + PROPERTIES_CONFIG_IMPL + implSuffix);

			cc.addInterface(pool.get(PropertiesConfigUtil.class.getPackage()
					.getName() + PROPERTIES_CONFIG_INTERFACE));
			CtMethod cm = CtMethod.make("public Object getProperty(String name){"
					+ buffer.toString() + "}", cc);
			cc.addMethod(cm);
			clazz = cc.toClass();
			cc.writeFile(PropertiesConfigUtil.class.getResource("/").toString()
					.replaceAll("file:", ""));
			return clazz;
		}
		return null;
		
	}

}
