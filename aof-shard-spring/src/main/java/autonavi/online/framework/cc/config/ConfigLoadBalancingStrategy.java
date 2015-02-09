package autonavi.online.framework.cc.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategy;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategyFactory;
import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;
import autonavi.online.framework.sharding.dao.exception.StrategyNameIsEmpty;
import autonavi.online.framework.sharding.dao.exception.StrategyNameNotFound;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

public class ConfigLoadBalancingStrategy {
	private Logger log = LogManager.getLogger(getClass());

	public void initStrategyInstance() throws Exception {
		ScanAllClassHandle handle = new ScanAllClassHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				Class<?> clazz = null;
				String className = "";
				try {
					className = metadataReader.getClassMetadata()
							.getClassName();
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(className);
				} catch (NoClassDefFoundError e) {
					log.warn(e.getMessage() + "不存在，已跳过");
				}
				if (clazz != null
						&& LoadBalancingStrategy.class.isAssignableFrom(clazz)
						&& !clazz.isInterface()
						&& !metadataReader.getClassMetadata().isAbstract()) {
					Strategy strategy = clazz.getAnnotation(Strategy.class);
					if (strategy == null)
						throw new StrategyNameNotFound("策略名称是必须的，请在类【"
								+ className + "】上加入@Strategy注解，并设置策略名称。");
					if (strategy.value() == null || strategy.value().equals(""))
						throw new StrategyNameIsEmpty("策略名称是必须的，请在类【"
								+ className + "】上加入@Strategy注解，并设置策略名称。");
					LoadBalancingStrategyFactory.addStrategyDefinition(
							strategy.value(), className);// 将策略名称和策略实例类名加入策略表
				}
			}
		};
		ScanAllClass scanAllClass = new ScanAllClass();
		String loadBalancingStrategyClassName = LoadBalancingStrategy.class
				.getName();
		String loadBalancingStrategyPackage = loadBalancingStrategyClassName
				.substring(0, loadBalancingStrategyClassName.lastIndexOf("."));
		scanAllClass.scanner(loadBalancingStrategyPackage, handle);
	}

}
