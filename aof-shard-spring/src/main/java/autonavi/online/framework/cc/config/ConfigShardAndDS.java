package autonavi.online.framework.cc.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import autonavi.online.framework.cc.CcConfigUtils;
import autonavi.online.framework.cc.InitBaseConfig;
import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DaoHelper;
import autonavi.online.framework.sharding.dao.exception.DataSourceKeyOutOfRangeException;
import autonavi.online.framework.sharding.dao.exception.ExistingDataSourceKeyException;
import autonavi.online.framework.sharding.entry.DaoSupportFactory;
import autonavi.online.framework.sharding.entry.aspect.DaoAspect;
import autonavi.online.framework.sharding.index.ShardInitEntity;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;

public class ConfigShardAndDS {
	private Logger log = LogManager.getLogger(getClass());

	/**
	 * 初始化分片
	 * 
	 * @param ccBaseEntity
	 * @param beanDefinitionRegistry
	 * @param map
	 * @throws ExistingDataSourceKeyException
	 * @throws DataSourceKeyOutOfRangeException
	 * @throws Exception
	 */
	public AbstractDaoSupport initShard(InitBaseConfig initBaseConfig,
			BeanDefinitionRegistry beanDefinitionRegistry,
			ShardInitEntity shardInitEntity)
			throws ExistingDataSourceKeyException,
			DataSourceKeyOutOfRangeException, Exception {
		CcBaseEntity ccBaseEntity = null;
		DynamicTransactionManager dm = null;
		if (shardInitEntity.getDynamicTransactionManager() != null) {
			dm = shardInitEntity.getDynamicTransactionManager();
		}

		try {
			CcBaseEntity entity = initBaseConfig.getBeseConfig();
			Map<String, List<ColumnAttribute>> m = new HashMap<String, List<ColumnAttribute>>();
			for (String key : entity.getIndexTableMap().keySet()) {
				m.put(key.toUpperCase(), entity.getIndexTableMap().get(key));
			}
			entity.setIndexTableMap(m);
			ccBaseEntity = entity;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		DaoSupportFactory factory = new DaoSupportFactory();
		// 获取DaoSupport实例
		AbstractDaoSupport daoSupport = factory.getDaoSupport(ccBaseEntity, dm,
				shardInitEntity.getDataSourceRoute(),
				shardInitEntity.getDaoSupport());
		/**
		 * 生成DaoAspect的BeanDefinition
		 */
		RootBeanDefinition daoAspectBeanDefinition = new RootBeanDefinition(
				DaoAspect.class);
		/**
		 * 将daoSupport注册进daoAspect
		 */

		daoAspectBeanDefinition.getPropertyValues().add("daoSupport",
				daoSupport);
		/**
		 * 将daoAspect注册进容器，供其它地方使用
		 */
		beanDefinitionRegistry.registerBeanDefinition("daoAspect",
				daoAspectBeanDefinition);
		/**
		 * 工具DAOHelper
		 */
		RootBeanDefinition daoHelperBeanDefinition = new RootBeanDefinition(
				DaoHelper.class);
		daoHelperBeanDefinition.getPropertyValues().add("daoSupport",
				daoSupport);
		beanDefinitionRegistry.registerBeanDefinition("daoHelper",
				daoHelperBeanDefinition);
		/**
		 * CcConfigUtil注册 保证DynamiticDataSource单例
		 */
		RootBeanDefinition ccConfigUtilBeanDefinition = new RootBeanDefinition(
				CcConfigUtils.class);
		ccConfigUtilBeanDefinition.getPropertyValues().add("daoSupportFactory",
				factory);
		ccConfigUtilBeanDefinition.getPropertyValues().add("initBaseConfig",
				initBaseConfig);
		beanDefinitionRegistry.registerBeanDefinition("ccConfigUtil",
				ccConfigUtilBeanDefinition);
		return daoSupport;
	}

}
