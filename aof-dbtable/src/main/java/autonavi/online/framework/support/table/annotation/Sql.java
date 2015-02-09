package autonavi.online.framework.support.table.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Sql {
	/**
	 * 索引名
	 * @return
	 */
	String indexName();
	/**
	 * 索引列
	 * @return
	 */
	String indexColumn();
	/**
	 * 是否开启事务
	 * @return
	 */
	boolean openTx() default false;

}
