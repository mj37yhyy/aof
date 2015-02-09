package autonavi.online.framework.sharding.entry.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import autonavi.online.framework.sharding.dao.ResultSetCallback;
import autonavi.online.framework.sharding.entry.entity.CollectionType;

/**
 * 作者：姬昂 2014年3月14日 说明：
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Select {
	@SuppressWarnings("rawtypes")
	Class callbackClass() default ResultSetCallback.class;

	/**
	 * 分页
	 * 
	 * @return
	 */
	Paging paging() default @Paging;

	/**
	 * 返回的集合类型
	 * 
	 * @return
	 */
	CollectionType collectionType() default CollectionType.mapList;

	/**
	 * 字段类型，默认为Object<br/>
	 * 该字段只对bean, beanList, column, columnList四个集合类型有效，其它的array, arrayList, map,
	 * mapList四个类型使用数据库元数据
	 * 
	 * @return
	 */
	Class<?> resultType() default Object.class;

	/**
	 * 是否查询总行数
	 * 
	 * @return
	 */
	boolean queryCount() default false;

	/**
	 * 是否缓存
	 * 
	 * @return
	 */
	Cache cache() default @Cache;

	/**
	 * 分页注解
	 * 
	 * @author jia.miao
	 * 
	 */
	public @interface Paging {
		String skip() default "";

		String size() default "";
	}

	/**
	 * 缓存注解
	 * 
	 * @author jia.miao
	 * 
	 */
	public @interface Cache {
		int timeOut() default 1000;
	}

}