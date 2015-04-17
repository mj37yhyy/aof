package autonavi.online.framework.sharding.dao.ds.strategy;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 策略实现工厂，通过定义好的分片策略信息得到对应的策略实体并调用方法获取DSNAME
 * 
 * @author jia.miao
 * 
 */
public class LoadBalancingStrategyFactory {
	@SuppressWarnings("unused")
	private static Logger logger = LogManager
			.getLogger(LoadBalancingStrategyFactory.class);

	private static Map<String, String> strategyDefine = new HashMap<String, String>();// 策略实体定义

	public static LoadBalancingStrategy getStrategyDefineInstance(
			String strategyName) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (strategyName != null && strategyName.length() > 0) {
			String strategyClassName = strategyDefine.get(strategyName);
			if (strategyClassName != null && strategyClassName.length() > 0) {// 如果策略类名存在
				Class<?> clazz = LoadBalancingStrategyFactory.class
						.getClassLoader().loadClass(strategyClassName);// load策略类
				LoadBalancingStrategy loadBalancingStrategy = (LoadBalancingStrategy) clazz
						.newInstance();// 生成LoadBalancingStrategy实例
				return loadBalancingStrategy;
			} else {
				throw new RuntimeException("策略类名[" + strategyClassName
						+ "]不符合要求");
			}
		} else {
			throw new RuntimeException("策略名[" + strategyName + "]不符合要求");
		}
	}

	/**
	 * 批量添加策略定义
	 * 
	 * @param strategyDefinition
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void setStrategyDefinition(
			Map<String, String> strategyDefinition)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		for (String strategyName : strategyDefinition.keySet()) {
			String strategyClassName = strategyDefinition.get(strategyName);// 得到策略类名
			addStrategyDefinition(strategyName, strategyClassName);
		}

	}

	/**
	 * 单条添加策略定义
	 * 
	 * @param strategyName
	 * @param strategyClassName
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void addStrategyDefinition(String strategyName,
			String strategyClassName) {
		if (strategyName != null && strategyName.length() > 0) {
			if (strategyClassName != null && strategyClassName.length() > 0) {// 如果策略类名存在
				strategyDefine.put(strategyName, strategyClassName);
			} else {
				throw new RuntimeException("策略类名[" + strategyClassName
						+ "]不符合要求");
			}
		} else {
			throw new RuntimeException("策略名[" + strategyName + "]不符合要求");
		}
	}

}
