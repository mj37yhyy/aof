package autonavi.online.framework.sharding.entry.xml.builder.support;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import autonavi.online.framework.sharding.dao.ResultSetCallback;
import autonavi.online.framework.sharding.dao.exception.MustContainAnnotationException;
import autonavi.online.framework.sharding.entry.aspect.annotation.Author;
import autonavi.online.framework.sharding.entry.aspect.annotation.ResultSetCallBackHandler;
import autonavi.online.framework.sharding.entry.aspect.annotation.SqlParameter;
import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;
import autonavi.online.framework.sharding.entry.xml.builder.support.xmltags.XMLScriptBuilder;
import autonavi.online.framework.sharding.holder.XmlBuilderHolder;
import autonavi.online.framework.sharding.index.ShardingHandleSupport;
import freemarker.template.Template;

public abstract class AbstractCodeGeneration {

	/**
	 * 读取XML上的属性，生成paramMap
	 * 
	 * @param interfaceClass
	 * @param node
	 * @return
	 * @throws Exception
	 */
	protected Map<String, Object> getParamMap(Class<?> interfaceClass, Node node)
			throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();

		// 得到相关属性
		String sqlid = this.parseSql(interfaceClass, node);// 编译sql，得到sql id

		Element el = (Element) node;
		paramMap.put("sqlid", sqlid);
		String id = el.getAttribute(idAttribute);
		paramMap.put("id", el.getAttribute(idAttribute));
		paramMap.put("indexName", el.getAttribute(indexNameAttribute));
		paramMap.put("indexColumn", el.getAttribute(indexColumnAttribute));
		if (el.getAttribute(dataSourceKeyAttribute) != null) {
			paramMap.put("dataSourceKey",
					el.getAttribute(dataSourceKeyAttribute));
		} else {
			paramMap.put("dataSourceKey", "");
		}
		if (el.getAttribute(dataSourceKeyNameAttribute) != null) {
			paramMap.put("dataSourceKeyName",
					el.getAttribute(dataSourceKeyNameAttribute));
		} else {
			paramMap.put("dataSourceKeyName", "");
		}
		if (el.getAttribute(parameterTypeAttribute) != null) {
			paramMap.put("parameterType",
					el.getAttribute(parameterTypeAttribute));
		} else {
			paramMap.put("parameterType", "");
		}
		if (el.getAttribute(collectionTypeAttribute) != null) {
			paramMap.put("collectionType",
					el.getAttribute(collectionTypeAttribute));
		} else {
			paramMap.put("collectionType", "");
		}
		if (el.getAttribute(resultTypeAttribute) != null) {
			paramMap.put("resultType", el.getAttribute(resultTypeAttribute));
		} else {
			paramMap.put("resultType", "");
		}
		if (el.getAttribute(skipAttribute) != null) {
			paramMap.put("skip", el.getAttribute(skipAttribute));
		} else {
			paramMap.put("skip", "");
		}
		if (el.getAttribute(sizeAttribute) != null) {
			paramMap.put("size", el.getAttribute(sizeAttribute));
		} else {
			paramMap.put("size", "");
		}
		if (el.getAttribute(isQueryCountAttribute) != null) {
			paramMap.put("queryCount",
					Boolean.valueOf(el.getAttribute(isQueryCountAttribute))
							.booleanValue());
		} else {
			paramMap.put("queryCount", false);
		}
		String shardingHandleValue = el.getAttribute(shardingHandleAttribute);
		paramMap.put(
				"shardingHandle",
				(shardingHandleValue == null || shardingHandleValue.isEmpty()) ? ShardingHandleSupport.class
						.getName() : shardingHandleValue);

		for (Method targetMethod : interfaceClass.getMethods()) {
			if (targetMethod.getName().equals(id)) {// 如果是当前方法
				// 作者
				Author author = targetMethod.getAnnotation(Author.class);
				if (author != null) {
					paramMap.put("author", author.value());
				} else {
					paramMap.put("author", "");
				}

				String returnType = targetMethod.getReturnType().getName();
				returnType = this.supportArray(returnType);
				paramMap.put("returnType", returnType);// 返回类型

				// 得到参数名
				String[] paramNames = new String[targetMethod
						.getParameterTypes().length];

				for (int i = 0; i < targetMethod.getParameterTypes().length; i++) {
					Annotation[] parameterAnnotation = targetMethod
							.getParameterAnnotations()[i];
					if (parameterAnnotation.length == 0) {// 没有注解代表普通参数，跳过
						throw new MustContainAnnotationException(
								targetMethod
										+ "的入参必须包含@SqlParameter或@ResultSetCallBackHandler注解");
					}
					for (Annotation annotation : parameterAnnotation) {
						if (annotation instanceof ResultSetCallBackHandler) {
							ResultSetCallBackHandler resultSetCallBackHandler = (ResultSetCallBackHandler) annotation;
							if (ResultSetCallback.class
									.isAssignableFrom(targetMethod
											.getParameterTypes()[i])) {
								// 设置ResultSetCallback实现 注意判断是否已经设置过
								if (paramMap.containsKey("resultCallBack")
										&& !paramMap.get("resultCallBack")
												.equals("")) {
									throw new RuntimeException(
											"只能传入一个Callback实现类");
								}
								paramMap.put("resultCallBack",
										resultSetCallBackHandler.value());
								break;
							}
						} else if (annotation instanceof SqlParameter) {// 得到SqlParameter注解
							SqlParameter sqlParameter = (SqlParameter) annotation;
							paramNames[i] = sqlParameter.value();
							break;
						}
					}
				}
				/*
				 * for (int i = 0; i <
				 * targetMethod.getParameterAnnotations().length; i++) {
				 * Annotation[] parameterAnnotation = targetMethod
				 * .getParameterAnnotations()[i]; if (parameterAnnotation.length
				 * == 0) { // paramNames[i] = "arg" + i; // continue; throw new
				 * MustContainAnnotationException(targetMethod +
				 * "的入参必须包含@SqlParameter注解"); } for (Annotation annotation :
				 * parameterAnnotation) { if (annotation instanceof
				 * SqlParameter) {// 得到SqlParameter注解 SqlParameter sqlParameter
				 * = (SqlParameter) annotation; paramNames[i] =
				 * sqlParameter.value(); break; } } }
				 */
				paramMap.put("paramNames", paramNames);// 入参名数组

				// 生成入参字符串
				String parameterStrings = "";
				Class<?>[] parameterTypes = targetMethod.getParameterTypes();// 参数类型
				for (int i = 0; i < parameterTypes.length; i++) {
					Class pClass = parameterTypes[i];
					parameterStrings += this.supportArray(pClass.getName())
							+ " " + paramNames[i] + ",";
				}

				if (parameterStrings.length() > 1)
					parameterStrings = parameterStrings.substring(0,
							parameterStrings.length() - 1);// 去掉最后一个逗号
				paramMap.put("parameterStrings", parameterStrings);// 入参字符串
			}
		}
		if (!paramMap.containsKey("resultCallBack")) {
			paramMap.put("resultCallBack", "");
		}
		return paramMap;
	}

	/**
	 * 转化数组字符串<br/>
	 * 
	 * 特别的，对于Class.getName(),如果此类对象表示一个数组类，则名字的内部形式为：表示该数组嵌套深度的一个或多个 '['
	 * 字符加元素类型名。元素类型名的编码如下： <br/>
	 * 
	 * <table width="200" border="1">
	 * <tr>
	 * <th>元素类型</th>
	 * <th>编码</th>
	 * </tr>
	 * <tr>
	 * <td>boolean</td>
	 * <td align="center">Z</td>
	 * </tr>
	 * <tr>
	 * <td>byte</td>
	 * <td align="center">B</td>
	 * </tr>
	 * <tr>
	 * <td>char</td>
	 * <td align="center">C</td>
	 * </tr>
	 * <tr>
	 * <td>类或接口</td>
	 * <td align="center">L<em>classname;</em></td>
	 * </tr>
	 * <tr>
	 * <td>double</td>
	 * <td align="center">D</td>
	 * </tr>
	 * <tr>
	 * <td>float</td>
	 * <td align="center">F</td>
	 * </tr>
	 * <tr>
	 * <td>int</td>
	 * <td align="center">I</td>
	 * </tr>
	 * <tr>
	 * <td>long</td>
	 * <td align="center">J</td>
	 * </tr>
	 * <tr>
	 * <td>short</td>
	 * <td align="center">S</td>
	 * </tr>
	 * </table>
	 * 
	 * 类或接口名 classname 是上面指定类的二进制名称。 <br/>
	 * 
	 * @param className
	 * @return
	 */
	private String supportArray(String className) {
		if (className.indexOf("[") > -1) {
			int counter = 0;
			for (char c : className.toCharArray()) {
				if (c == '[')
					++counter;
			}
			if (className.indexOf("[L") > -1) {
				// 如果是对像，最后存在";"需要删除
				className = className
						.substring(counter + 1,
								className.lastIndexOf(";") == className
										.length() - 1 ? className.length() - 1
										: className.length());
			} else if (className.indexOf("[Z") > -1) {// 如果是基本数据类型boolean
				className = "boolean";
			} else if (className.indexOf("[B") > -1) {// 如果是基本数据类型byte
				className = "byte";
			} else if (className.indexOf("[C") > -1) {// 如果是基本数据类型char
				className = "char";
			} else if (className.indexOf("[D") > -1) {// 如果是基本数据类型double
				className = "double";
			} else if (className.indexOf("[F") > -1) {// 如果是基本数据类型float
				className = "float";
			} else if (className.indexOf("[I") > -1) {// 如果是基本数据类型int
				className = "int";
			} else if (className.indexOf("[J") > -1) {// 如果是基本数据类型long
				className = "long";
			} else if (className.indexOf("[S") > -1) {// 如果是基本数据类型short
				className = "short";
			}
			// 加上括号
			for (int i = 0; i < counter; i++) {
				className += "[]";
			}
		}
		return className;
	}

	/**
	 * 将xml中的SQL部分替换成java代码
	 * 
	 * @param interfaceClass
	 * @param node
	 * @return
	 * @throws Exception
	 * @throws DocumentException
	 */
	protected String parseSql(Class<?> interfaceClass, Node node)
			throws Exception {
		final String id = ((Element) node).getAttribute("id");
		XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(node);
		final SqlSource sqlSource = xmlScriptBuilder.parseScriptNode();// 解析xml生成SqlSource

		// 将SqlSource保存于本地线程的MAP中
		Map<Class<?>, Map<String, SqlSource>> classes = XmlBuilderHolder.classesHolder
				.get();
		if (classes.containsKey(interfaceClass)) {
			classes.get(interfaceClass).put(id, sqlSource);
		} else {
			classes.put(interfaceClass, new HashMap<String, SqlSource>() {
				{
					put(id, sqlSource);
				}
			});
		}
		XmlBuilderHolder.classesHolder.set(classes);

		return id;
	}

	protected String doGenerator(Map<String, Object> paramMap) throws Exception {
		Writer out = new StringWriter();
		try {
			String className = this.getClass().getName();
			// 通过模板生成代码
			Template tl = getTemplateConfig(
					"/"
							+ className
									.substring(0, className.lastIndexOf("."))
									.replace(".", "/") + "/template")
					.getTemplate("template.codeGeneration.ftl");
			tl.process(paramMap, out);
			return out.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			out.close();
		}
	}

	/**
	 * 获取freemarker的cfg
	 * 
	 * @param resource
	 * @return Configuration
	 */
	protected freemarker.template.Configuration getTemplateConfig(
			String resource) {

		freemarker.template.Configuration cfg = new freemarker.template.Configuration();
		cfg.setDefaultEncoding("UTF-8");
		cfg.setClassForTemplateLoading(this.getClass(), resource);
		return cfg;
	}

	// Attributes
	protected final String idAttribute = "id";
	protected final String indexNameAttribute = "indexName";
	protected final String indexColumnAttribute = "indexColumn";
	protected final String dataSourceKeyAttribute = "dataSourceKey";
	protected final String dataSourceKeyNameAttribute = "dataSourceKeyName";
	protected final String parameterTypeAttribute = "parameterType";
	// protected final String resultCallBackClassAttribute =
	// "resultCallBackClass";
	protected final String collectionTypeAttribute = "collectionType";
	protected final String resultTypeAttribute = "resultType";
	protected final String skipAttribute = "skip";
	protected final String sizeAttribute = "size";
	protected final String isQueryCountAttribute = "isQueryCount";
	protected final String shardingHandleAttribute = "shardingHandle";
}
