package autonavi.online.framework.cc.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import autonavi.online.framework.sharding.dao.DaoContextHolder;
/**
 * 初始化核心的上下文持有者
 * @author yaming.xu
 *
 */
public class ConfigContextHolder {
	public void initContextHolder(BeanDefinitionRegistry beanDefinitionRegistry) {
		RootBeanDefinition contextHolderBeanDefinition = new RootBeanDefinition(
				DaoContextHolder.class);
		beanDefinitionRegistry.registerBeanDefinition("daoContextHolder",
				contextHolderBeanDefinition);

	}

}
