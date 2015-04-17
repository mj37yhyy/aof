package autonavi.online.framework.cc;

import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.support.PropertiesLoaderSupport;

import autonavi.online.framework.cc.config.ConfigBiz;
import autonavi.online.framework.cc.config.ScanPlugIn;
import autonavi.online.framework.property.PropertiesData;

/**
 * 
 * @author Xuyaming-iMac
 * 
 */
public class CcBizConfig implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {

		try {
			log.info("初始化框架启动器");
			log.info("开始扫描新框架插件");

			/**
			 * 扫描并注册插件
			 */
			new ScanPlugIn().scanAndRegistryPlugIn(registry);

			/**
			 * 启动ccConfig清理器
			 */
			this.initClean(registry);
			log.info("框架启动器初始化完毕");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("框架启动器启动异常", e);
			System.exit(0);
		}
	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		/**
		 * 处理Biz部分<br/>
		 * 当SPRING加载的时候 如果在builder.addPropertyValue("properties",
		 * managedProperties); 加载或者配置文件配置 会自动加载PropertyPlaceholderConfigurer
		 * 但是如果没做
		 * 进入到BeanDefinitionRegistryPostProcessor或者BeanFactoryPostProcessor后
		 * 因为Spring只会自动扫描BeanDefinitionRegistryPostProcessor或者BeanFactoryPostProcessor加载
		 * 而PropertyPlaceholderConfigurer也是个BeanFactoryPostProcessor
		 * 因此不会在这阶段注入后再自动处理 但是可以用手工的方式加载 来自Spring的源码调用方法
		 * 
		 */
		try {
			log.info("开始获取框架的自定义配置属性");
			// 处理已有属性
			ManagedProperties managedProperties = new ManagedProperties();
			String[] name = beanFactory
					.getBeanNamesForType(PropertiesLoaderSupport.class);
			for (String _name : name) {
				Object obj = beanFactory.getBean(_name);
				Method mergeProperties = PropertiesLoaderSupport.class
						.getDeclaredMethod("mergeProperties");
				mergeProperties.setAccessible(true);
				Properties props = (Properties) mergeProperties.invoke(obj);
				for (Object o : props.keySet()) {
					managedProperties.put(o, props.get(o));
				}
			}

			// 合并
			PropertyPlaceholderConfigurer p = new PropertyPlaceholderConfigurer();
			p.setProperties(new ConfigBiz().initBiz(propertiesData,
					managedProperties));
			p.postProcessBeanFactory(beanFactory);// 手工调用PropertyPlaceholderConfigurer的postProcessBeanFactory方法，替换EL表达式里的值
			log.info("获取框架的自定义配置属性完毕");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("获取框架的自定义配置属性错误", e);
			System.exit(0);
		}

	}

	/**
	 * 统一进行扫描并运行后置处理器
	 * 
	 * @throws Exception
	 */
//	private void scanAndRunAfter() throws Exception {
//		// 运行扫描，并依此调用管道里的接口
//		ScanAllClass scanAllClass = new ScanAllClass();
//		scanAllClass.scanner("autonavi.online.framework", new ScanAllClassHandle() {
//
//			@Override
//			public void handle(MetadataReader metadataReader) throws Exception {
//				for (ScanAllClassHandle _scanAllClassHandle : scanPipeline)
//					_scanAllClassHandle.handle(metadataReader);
//			}
//
//		});
//		// 运行后置处理器
//		for (ScanPipelineAfter scanPipelineAfter : scanPipelineAfters) {
//			scanPipelineAfter.handle();
//		}
//	}

	/**
	 * 一些清理工作
	 * 
	 * @param registry
	 */
	private void initClean(BeanDefinitionRegistry registry) {
		RootBeanDefinition ccConfigCleanBeanDefinition = new RootBeanDefinition(
				CcBizConfigClean.class);
		registry.registerBeanDefinition("ccBizConfigClean",
				ccConfigCleanBeanDefinition);
		
	}

	/**
	 * 得到biz的配置实体
	 * 
	 * @param propertiesData
	 */
	public void setPropertiesData(PropertiesData propertiesData) {
		this.propertiesData = propertiesData;
	}

	

	private Logger log = LogManager.getLogger(getClass());

	//public static final List<ScanAllClassHandle> scanPipeline = new ArrayList<ScanAllClassHandle>();
	//public static final List<ScanPipelineAfter> scanPipelineAfters = new ArrayList<ScanPipelineAfter>();

	private PropertiesData propertiesData;
	
	
	


}
