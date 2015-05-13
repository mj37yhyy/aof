package autonavi.online.framework.cc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.cc.entity.CcDataSource;
import autonavi.online.framework.sharding.dao.ds.LoadBalancingDataSource;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategy;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategyFactory;
import autonavi.online.framework.sharding.entry.DaoSupportFactory;
import autonavi.online.framework.util.BeanUtils;

public class CcConfigUtils {
	private InitBaseConfig initBaseConfig;
	// private CcConfigUtils ccConfigUtils;
	private DaoSupportFactory  daoSupportFactory ;

    
	
	public void setDaoSupportFactory(DaoSupportFactory daoSupportFactory) {
		this.daoSupportFactory = daoSupportFactory;
	}

	private Logger log = LogManager.getLogger(getClass());

	private CcConfigUtils() {
		super();
	}

	public void setInitBaseConfig(InitBaseConfig initBaseConfig) {
		this.initBaseConfig = initBaseConfig;
	}

	// public static CcConfigUtils getInstance() {
	// if (ccConfigUtils == null) {
	// ccConfigUtils = new CcConfigUtils();
	// }
	// return ccConfigUtils;
	// }

	// /**
	// * 实类生产工具
	// *
	// * @param className
	// * @param map
	// * @return
	// */
	// public Object generateObject(String className, Map<String, Object> map)
	// throws Exception {
	// Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
	// Object obj = clazz.newInstance();
	// BeanUtils.populate(obj, map);
	// return obj;
	// }

	/**
	 * 刷新配置
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception {
		CcBaseEntity ccBaseEntity = this.initBaseConfig.getBeseConfig();// 再次得到核心配置信息
		// 更新数据源-开始
		Map<Integer, DataSource> map = new HashMap<Integer, DataSource>();
		Map<String, DataSource> realMap = new HashMap<String, DataSource>();
		Map<Integer, CcDataSource> dataSources = ccBaseEntity.getDataSources();// ds为AOFDS
		Map<String, CcDataSource> realDataSources = ccBaseEntity
				.getRealDataSources();// 全部真实数据源
		// 构建所有数据源
		for (String key : realDataSources.keySet()) {
			CcDataSource ds = realDataSources.get(key);
			if (DataSource.class.isAssignableFrom(this.getClass()
					.getClassLoader().loadClass(ds.getBeanClass()))) {
				realMap.put(key, (DataSource) BeanUtils.generateObject(
						ds.getBeanClass(), ds.getProps()));
			} else {
				log.warn("Refreah realDataSource must load DataSource!Your Class is["
						+ ds.getBeanClass() + "]");
				return;
			}
		}
		// 更新AOF数据源
		for (Integer key : dataSources.keySet()) {
			CcDataSource ds = dataSources.get(key);
			ds.setBeanClass(LoadBalancingDataSource.class.getName());
			if (LoadBalancingDataSource.class.isAssignableFrom(this.getClass()
					.getClassLoader().loadClass(ds.getBeanClass()))) {
				String strategyName = (String) ds.getProps()
						.get("strategyName");
				if (strategyName == null || strategyName.length() == 0) {
					log.warn("strategyName is null or length is 0");
					return;
				}
				@SuppressWarnings("unchecked")
				List<String> realDss = (List<String>) ds.getProps().get(
						"realDss");
				if (realDss == null || realDss.size() == 0) {
					log.warn("realDss is null or size is 0");
					return;
				}
				LoadBalancingDataSource loadBalancingDataSource = new LoadBalancingDataSource();
				LoadBalancingStrategy strategy;
				try {
					strategy = LoadBalancingStrategyFactory
							.getStrategyDefineInstance(strategyName);
				} catch (Exception e) {
					log.warn("StrategyName[" + strategyName
							+ "] is not found in classPath ");
					return;
				}
				loadBalancingDataSource.setLoadBalancingStrategy(strategy);
				for (String dsName : realDss) {
					if (realMap.get(dsName.split("\\?")[0]) != null) {
						loadBalancingDataSource.addDataSource(dsName,
								realMap.get(dsName));
					} else {
						log.warn("DataSource[" + dsName.split("\\?")[0]
								+ "] not found in realDateSource");
						return;
					}

				}
				map.put(key, loadBalancingDataSource);

			} else {
				log.warn("Refreah dataSource must load "
						+ LoadBalancingDataSource.class.getName()
						+ "!Your Class is[" + ds.getBeanClass() + "]");
				return;
			}
		}
		// AbstractDynamicDataSource<PlatformTransactionManager> dyd = new
		// DynamicDataSource();
		daoSupportFactory.refreshDataSources(map);
		// dyd.setTargetDataSources(map);
		// 更新数据源-结束
	}

}
