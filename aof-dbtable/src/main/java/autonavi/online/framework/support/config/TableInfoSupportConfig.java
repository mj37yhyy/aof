package autonavi.online.framework.support.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import autonavi.online.framework.config.ISupportConfig;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.support.table.aspect.TableInfoAspect;

public class TableInfoSupportConfig implements ISupportConfig {
	private final String ASPECT="AOF_TABLEINFO_ASPECT";
	@Override
	public void processSupportConfig(
			BeanDefinitionRegistry beanDefinitionRegistry,Object...objects)throws Exception {
		
		RootBeanDefinition tableInfoAspectBeanDefinition = new RootBeanDefinition(
				TableInfoAspect.class);
		String[] beanName=beanDefinitionRegistry.getBeanDefinitionNames();
		for(String _beanName:beanName){
			BeanDefinition bean=beanDefinitionRegistry.getBeanDefinition(_beanName);
		    ClassLoader loader=Thread.currentThread().getContextClassLoader();
		    Class<?> clazz=loader.loadClass(bean.getBeanClassName());
		    if(AbstractDaoSupport.class.isAssignableFrom(clazz)){
		    	RuntimeBeanReference ref=new RuntimeBeanReference(_beanName);
		    	tableInfoAspectBeanDefinition.getPropertyValues().add("daoSupport", ref);
		    	break;
		    }
		    
		}
		beanDefinitionRegistry.registerBeanDefinition(ASPECT, tableInfoAspectBeanDefinition);
		
		
	}

}
