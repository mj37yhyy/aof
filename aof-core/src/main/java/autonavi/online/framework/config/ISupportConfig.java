package autonavi.online.framework.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface ISupportConfig {
	public void processSupportConfig(BeanDefinitionRegistry beanDefinitionRegistry,Object ...objects)throws Exception ;

}
