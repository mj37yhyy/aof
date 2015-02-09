package autonavi.online.framework.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
/**
 * 需要shard启动后才能启动的插件的代理类
 * @author yaming.xu
 *
 */
public interface IShardSupportConfig {
	public void processShardSupportConfig(BeanDefinitionRegistry beanDefinitionRegistry,ConfigurableListableBeanFactory beanFactory,Object ...objects)throws Exception ;

}
