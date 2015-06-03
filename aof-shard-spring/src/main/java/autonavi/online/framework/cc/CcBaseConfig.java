package autonavi.online.framework.cc;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.cc.config.ConfigAspect;
import autonavi.online.framework.cc.config.ConfigShardPlugInSupport;
import autonavi.online.framework.cc.config.ScanPipelineAfter;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

/**
 * 
 * @author Xuyaming-iMac
 * 
 */
public class CcBaseConfig implements BeanDefinitionRegistryPostProcessor {
	private AbstractDaoSupport daoSupport;
	private BeanDefinitionRegistry registry;

	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {
       this.registry=registry;
		try {
			log.info("开始加载分库分表配置");
			/**
			 * 启动AspectJ的支持
			 */
			new ConfigAspect().registerOrEscalateApcAsRequired(registry);
			
			log.info("启动分库分表核心");
			/**
			 * 启动shard组件
			 */
			daoSupport=ShardingConfig.ShardingConfigInit(registry, initBaseConfig);
			
			
			/**
			 * 启动ccConfig清理器
			 */
			this.initClean(registry);
			log.info("加载分库分表配置完毕");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("分库分表配置加载错误", e);
			System.exit(0);
		}
	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			log.info("注册核心组件");
			/**
			 * 注册DaoSupport
			 */
			beanFactory.registerSingleton("daoSupport", daoSupport);
			/**
			 * 扫描shard-support组件
			 */
			new ConfigShardPlugInSupport().initShardSupport(registry,beanFactory);
			/**
			 * 统一进行扫描并运行后置处理器
			 */
			log.info("扫描分库分表插件");
			this.scanAndRunAfter();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("分库分表配置加载错误", e);
			System.exit(0);
		}

	}

	/**
	 * 统一进行扫描并运行后置处理器
	 * 
	 * @throws Exception
	 */
	private void scanAndRunAfter() throws Exception {
		// 运行扫描，并依此调用管道里的接口
		ScanAllClass scanAllClass = new ScanAllClass();
		scanAllClass.scanner("autonavi.online.framework", new ScanAllClassHandle() {

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				for (ScanAllClassHandle _scanAllClassHandle : scanPipeline)
					_scanAllClassHandle.handle(metadataReader);
			}

		});
		// 运行后置处理器
		for (ScanPipelineAfter scanPipelineAfter : scanPipelineAfters) {
			scanPipelineAfter.handle();
		}
	}

	/**
	 * 一些清理工作
	 * 
	 * @param registry
	 */
	private void initClean(BeanDefinitionRegistry registry) {
		RootBeanDefinition ccConfigCleanBeanDefinition = new RootBeanDefinition(
				CcBaseConfigClean.class);
		registry.registerBeanDefinition("ccBaseConfigClean",
				ccConfigCleanBeanDefinition);
		
	}


	

	private Logger log = LogManager.getLogger(getClass());

	public static final List<ScanAllClassHandle> scanPipeline = new ArrayList<ScanAllClassHandle>();
	public static final List<ScanPipelineAfter> scanPipelineAfters = new ArrayList<ScanPipelineAfter>();

	private InitBaseConfig initBaseConfig;


	public void setInitBaseConfig(InitBaseConfig initBaseConfig) {
		this.initBaseConfig = initBaseConfig;
	}
	
	


}
