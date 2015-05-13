package autonavi.online.framework.support.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.monitor.handler.MonitorHandler;
import autonavi.online.framework.plugin.ConfigPlugInSupport;
import autonavi.online.framework.support.zookeeper.MonitorZooKeeper;
import autonavi.online.framework.support.zookeeper.ZooKeeperProp;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

public class MonitorZooKeeperConfig implements ConfigPlugInSupport {

	@Override
	public void processSupportConfig(
			final BeanDefinitionRegistry beanDefinitionRegistry,Object ...objects) throws Exception {
		RootBeanDefinition monitorZooKeeperBeanDefinition = new RootBeanDefinition(MonitorZooKeeper.class);
		String[] beanName=beanDefinitionRegistry.getBeanDefinitionNames();
		for(String _beanName:beanName){
			BeanDefinition bean=beanDefinitionRegistry.getBeanDefinition(_beanName);
		    final ClassLoader loader=Thread.currentThread().getContextClassLoader();
		    Class<?> clazz=loader.loadClass(bean.getBeanClassName());
		    if(ZooKeeperProp.class.isAssignableFrom(clazz)){
		    	RuntimeBeanReference ref=new RuntimeBeanReference(_beanName);
		    	//扫秒获取handler
		    	final List<MonitorHandler> monitorHandler=new ArrayList<MonitorHandler>();
		    	ScanAllClassHandle handle = new ScanAllClassHandle(){

					@Override
					public void handle(MetadataReader metadataReader)
							throws Exception {
						String[] interfaces=metadataReader.getClassMetadata().getInterfaceNames();
						boolean addFlag=false;
						for(String i:interfaces){
							if(i.equals(MonitorHandler.class.getName())){
								addFlag=true;
								break;
							}
						}
						if(addFlag){
							monitorHandler.add((MonitorHandler)loader.loadClass(metadataReader.getClassMetadata().getClassName()).newInstance());
						}
					}
		    		
		    	};
		    	ScanAllClass scanAllClass = new ScanAllClass();
				scanAllClass.scanner("autonavi.online.framework", handle);
		    	monitorZooKeeperBeanDefinition.getPropertyValues().add("zooKeeperProp", ref)
		    	.add("monitorHandler", monitorHandler);
		    	break;
		    }
		    
		}
		beanDefinitionRegistry.registerBeanDefinition(MonitorZooKeeper.class.getName(), monitorZooKeeperBeanDefinition);
		

	}

}
