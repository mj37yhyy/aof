package autonavi.online.framework.sharding.entry.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标示ResultSetCallBack的形参
 * 
 * @author jia.miao
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ResultSetCallBackHandler {
	String value();
}
