package autonavi.online.framework.sharding.entry.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作者：姬昂 2014年2月21日 说明：标识Sql参数的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SqlParameter {
	String value();
}