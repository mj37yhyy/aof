package autonavi.online.framework.plugin;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface ConfigPlugInSupport {
	public void processSupportConfig(
			BeanDefinitionRegistry beanDefinitionRegistry, Object... objects)
			throws Exception;
}
