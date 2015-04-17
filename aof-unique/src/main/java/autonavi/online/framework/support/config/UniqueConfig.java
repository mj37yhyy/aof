package autonavi.online.framework.support.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import autonavi.online.framework.cc.config.ConfigMyidFile;
import autonavi.online.framework.plugin.ConfigPlugInSupport;

public class UniqueConfig implements ConfigPlugInSupport {

	@Override
	public void processSupportConfig(
			BeanDefinitionRegistry beanDefinitionRegistry, Object... objects)
			throws Exception {
		new ConfigMyidFile().readMyId();
	}

}
