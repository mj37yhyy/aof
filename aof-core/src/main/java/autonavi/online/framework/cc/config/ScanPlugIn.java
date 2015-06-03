package autonavi.online.framework.cc.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.plugin.ConfigPlugInSupport;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

public class ScanPlugIn {
	private Logger log = LogManager.getLogger(getClass());

	private final String AOF_SUPPORT_CONFIG_PACKAGE = "autonavi.online.framework.support.config";

	/**
	 * 扫描并注册插件
	 * 
	 * @param beanDefinitionRegistry
	 * @throws Exception
	 */
	public void scanAndRegistryPlugIn(
			final BeanDefinitionRegistry beanDefinitionRegistry)
			throws Exception {
		ScanAllClass scanAllClass = new ScanAllClass();
		scanAllClass.scanner("autonavi.online.framework",
				new ScanAllClassHandle() {
					ClassLoader loader = Thread.currentThread()
							.getContextClassLoader();

					@Override
					public void handle(MetadataReader metadataReader)
							throws Exception {
						Class<?> clazz = null;
						try {
							// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
							clazz = loader.loadClass(metadataReader
									.getClassMetadata().getClassName());
						} catch (NoClassDefFoundError e) {
							log.warn(e.getMessage() + "不存在，已跳过");
						}
						if (clazz != null
								&& ConfigPlugInSupport.class
										.isAssignableFrom(clazz)
								&& metadataReader.getClassMetadata()
										.getClassName()
										.startsWith(AOF_SUPPORT_CONFIG_PACKAGE)
								&& !clazz.isInterface()) {
							ConfigPlugInSupport configPlugInSupport = (ConfigPlugInSupport) clazz
									.newInstance();
							configPlugInSupport
									.processSupportConfig(beanDefinitionRegistry);
						}
					}

				});
	}
}
