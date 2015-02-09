package autonavi.online.framework.cc.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;

public class ConfigAspect {

	private final String AUTO_PROXY_CREATOR_BEAN_NAME = "org.springframework.aop.config.internalAutoProxyCreator";
	/**
	 * Stores the auto proxy creator classes in escalation order.
	 */
	private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 469498653481778014L;
		{
			/**
			 * Setup the escalation list.
			 */
			add(InfrastructureAdvisorAutoProxyCreator.class);
			add(AspectJAwareAdvisorAutoProxyCreator.class);
			add(AnnotationAwareAspectJAutoProxyCreator.class);
		}
	};

	/**
	 * 生成AnnotationAwareAspectJAutoProxyCreator的bean
	 * 
	 * @param registry
	 * @return
	 */
	public BeanDefinition registerOrEscalateApcAsRequired(
			BeanDefinitionRegistry registry) {
		Class<?> cls = AnnotationAwareAspectJAutoProxyCreator.class;
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition apcDefinition = registry
					.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
				int currentPriority = findPriorityForClass(apcDefinition
						.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				if (currentPriority < requiredPriority) {
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.getPropertyValues().add("order",
				Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME,
				beanDefinition);
		return beanDefinition;
	}

	private int findPriorityForClass(Class<?> clazz) {
		return APC_PRIORITY_LIST.indexOf(clazz);
	}

	private int findPriorityForClass(String className) {
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			Class<?> clazz = APC_PRIORITY_LIST.get(i);
			if (clazz.getName().equals(className)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Class name [" + className
				+ "] is not a known auto-proxy creator class");
	}
}
