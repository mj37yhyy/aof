package autonavi.online.framework.support.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import autonavi.online.framework.plugin.ShardConfigPlugInSupport;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.support.init.ShardIndexInit;

public class ShardIndexInitConfig implements ShardConfigPlugInSupport {

	@Override
	public void processShardSupportConfig(
			BeanDefinitionRegistry beanDefinitionRegistry,ConfigurableListableBeanFactory beanFactory,Object...objects) throws Exception {
		RootBeanDefinition shardIndexInitBeanDefinition = new RootBeanDefinition(
				ShardIndexInit.class);
		if(beanFactory.getBean(AbstractDaoSupport.class)!=null){
	    	shardIndexInitBeanDefinition.getPropertyValues().add("daoSupport", beanFactory.getBean(AbstractDaoSupport.class));
	    }
		beanDefinitionRegistry.registerBeanDefinition(ShardIndexInit.class.getName(), shardIndexInitBeanDefinition);

	}

}
