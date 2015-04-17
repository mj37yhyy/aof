package autonavi.online.framework.sharding.entry.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import autonavi.online.framework.sharding.index.ShardingHandleSupport;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Shard {
	/**
	 * 索引名
	 * 
	 * @return
	 */
	String indexName();

	/**
	 * 索引列
	 * 
	 * @return
	 */
	String indexColumn();

	/**
	 * 分片算法实现
	 * 
	 * @return
	 */
	Class<?> handle() default ShardingHandleSupport.class;

}
