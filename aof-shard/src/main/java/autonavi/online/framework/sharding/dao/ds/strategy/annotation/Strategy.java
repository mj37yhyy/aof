package autonavi.online.framework.sharding.dao.ds.strategy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标注策略的名称
 * 
 * @author jia.miao
 * 
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Strategy {
	String value();// 策略的名称

	String description() default "";// 策略的描述
}
