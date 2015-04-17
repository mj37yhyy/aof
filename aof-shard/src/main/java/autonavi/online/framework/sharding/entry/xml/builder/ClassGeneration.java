package autonavi.online.framework.sharding.entry.xml.builder;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DaoEntity;
import autonavi.online.framework.sharding.dao.TableOperation;
import autonavi.online.framework.sharding.dao.exception.UsePrimitiveException;
import autonavi.online.framework.sharding.entry.entity.CollectionType;
import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.BoundSql;
import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;
import autonavi.online.framework.sharding.entry.xml.builder.support.session.SqlSession;
import autonavi.online.framework.util.bean.PropertyUtils;
import autonavi.online.framework.util.javassist.JavassistClassInfo;
import autonavi.online.framework.util.javassist.JavassistClassUtils;

public class ClassGeneration {

	/**
	 * 通过接口生成子类（通过接口同名的配置文件）
	 * 
	 * @param interfaceClass
	 * @return
	 * @throws Exception
	 */
	public Class<?> createImplClassFromInterface(Class<?> interfaceClass)
			throws Exception {
		InputStream is = null;
		try {
			if (interfaceClass != null) {
				String interfaceName = interfaceClass.getName();// 接口名
				String className = interfaceClass.getName() + "$JavassistImpl";// 接口实现类名
				is = interfaceClass.getResourceAsStream(interfaceClass
						.getSimpleName() + ".xml");
				Map<String, String> methodMap = null;
				if (is != null) {// 当确定是DAO的接口后再做校验
					this.isPrimitive(interfaceClass);// 不允许出现基本数据类型
					/**
					 * 解析xml文件得到方法MAP
					 */
					XmlBuilder ps = new XmlBuilder();
					methodMap = ps.createMethodsFromXml(interfaceClass, is);// 从接口所在的同目录得到同名的XML文件并进行解析
				}
				if (methodMap != null) {// 如果解析没有问题，得到了map
					/**
					 * 用javassist生成接口的子类
					 */
					JavassistClassInfo classInfo = new JavassistClassInfo();
					classInfo.setClazz(className);// 类名
					classInfo.setInterfaces(interfaceName);// 接口名
					classInfo.setImportPackages(Map.class.getName(),
							HashMap.class.getName(), DaoEntity.class.getName(),
							TableOperation.class.getName(),
							PropertyUtils.class.getName(),
							AbstractDaoSupport.class.getName(),
							SqlSession.class.getName(),
							SqlSource.class.getName(),
							BoundSql.class.getName(),
							CollectionType.class.getName());// 引入的类
					classInfo.setFields(
							"private AbstractDaoSupport daoSupport = null;",
							"private SqlSession sqlSession = null;");
					String[] methods = new String[methodMap.size() + 2];
					methods[0] = "public void setDaoSupport(AbstractDaoSupport daoSupport) {this.daoSupport = daoSupport;}";
					methods[1] = "public void setSqlSession(SqlSession sqlSession) {this.sqlSession = sqlSession;}";
					int index = 2;
					for (String key : methodMap.keySet()) {
						methods[index] = methodMap.get(key);// 得到解析过的方法字符串
						++index;
					}
					classInfo.setMethods(methods);// 将所有方法置入类生成器
					// 生成类
					JavassistClassUtils javassistClassUtils = new JavassistClassUtils();
					Class<?> implClass = javassistClassUtils.createNewClass(
							classInfo, null);
					return implClass;// 将生成的class返回
				}
			}
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}
		return null;
	}

	/**
	 * 判断参数和返回值是否有基本类型，如果有则报错。原因如下:<br/>
	 * 1、javassist不支持泛型。代码中存在Map.put(String,Object)，在不支持泛型的前提下
	 * ，Map.put(String,int)等插入基本数据类型的操作会抛出异常。<br/>
	 * 2、返回值中的基本数据类型会导致编译时出现一些不可预知的问题。
	 * 
	 * @param interfaceClass
	 * @return
	 * @throws UsePrimitiveException
	 */
	private void isPrimitive(Class<?> interfaceClass)
			throws UsePrimitiveException {
		for (Method method : interfaceClass.getMethods()) {
			for (Class<?> clazz : method.getParameterTypes()) {// 入参的校验
				if (clazz.isArray() && clazz.getName().indexOf("[L") == -1) {// 如果是数组
					throw new UsePrimitiveException("在接口"
							+ interfaceClass.getName() + "中的方法"
							+ method.getName() + "参数中发现基本数据类型数组"
							+ clazz.getName() + "，系统不支持基本数据类型数组，请改为对应的对象形式");
				} else if (clazz.isPrimitive()) {// 如果发现基本类型
					throw new UsePrimitiveException("在接口"
							+ interfaceClass.getName() + "中的方法"
							+ method.getName() + "参数中发现基本数据类型"
							+ clazz.getName() + "，系统不支持基本数据类型，请改为对应的对象形式");
				}
			}

			// 返回值的校验
			Class<?> returnType = method.getReturnType();
			if (returnType.isArray()
					&& returnType.getName().indexOf("[L") == -1) {// 如果是数组
				throw new UsePrimitiveException("在接口"
						+ interfaceClass.getName() + "中的方法" + method.getName()
						+ "的返回值是基本数据类型数组" + returnType.getName()
						+ "，系统不支持基本数据类型数组，请改为对应的对象形式");
			} else if (returnType.isPrimitive())
				throw new UsePrimitiveException("在接口"
						+ interfaceClass.getName() + "中的方法" + method.getName()
						+ "的返回值是基本数据类型" + returnType.getName()
						+ "，系统不支持基本数据类型，请改为对应的对象形式");

		}
	}
}
