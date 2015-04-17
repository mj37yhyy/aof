package autonavi.online.framework.sharding.entry.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定数据源的ID
 * 
 * @author jia.miao
 * 
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface SingleDataSource {
	int value() default -1;// 直接指定

	String keyName() default "";// 也可以通过参数名进行指定
}
