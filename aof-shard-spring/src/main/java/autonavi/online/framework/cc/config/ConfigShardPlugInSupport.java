package autonavi.online.framework.cc.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.cc.CcBaseConfig;
import autonavi.online.framework.config.IShardSupportConfig;
import autonavi.online.framework.util.ScanAllClassHandle;

public class ConfigShardPlugInSupport {
	private Logger log = LogManager.getLogger(getClass());
	
	private final String AOF_SUPPORT_CONFIG_PACKAGE = "autonavi.online.framework.support.config";

	/**
	 * Support包自動加載
	 * 
	 * @param beanDefinitionRegistry
	 * @throws Exception
	 */
	public void initShardSupport(final BeanDefinitionRegistry beanDefinitionRegistry,final ConfigurableListableBeanFactory beanFactory)
			throws Exception {
		// 加入扫描队列
		CcBaseConfig.scanPipeline.add(new ScanAllClassHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				Class<?> clazz = null;
				try {
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(metadataReader.getClassMetadata()
							.getClassName());
				} catch (NoClassDefFoundError e) {
					log.warn(e.getMessage() + "不存在，已跳过");
				}
				if (clazz != null
						&& IShardSupportConfig.class.isAssignableFrom(clazz)
						&& metadataReader.getClassMetadata()
						.getClassName().startsWith(AOF_SUPPORT_CONFIG_PACKAGE)
						&& !clazz.isInterface()) {
					IShardSupportConfig supportConfig = (IShardSupportConfig) clazz
							.newInstance();
					supportConfig.processShardSupportConfig(beanDefinitionRegistry,beanFactory);
				}
			}
		});
	}
}
