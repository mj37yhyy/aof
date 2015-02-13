package autonavi.online.framework.cc.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.cc.CcConfigUtils;
import autonavi.online.framework.cc.InitBaseConfig;
import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.cc.entity.CcDataSource;
import autonavi.online.framework.constant.Miscellaneous;
import autonavi.online.framework.metadata.CreateTable;
import autonavi.online.framework.metadata.DialectUtils;
import autonavi.online.framework.metadata.TableGenerator;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.metadata.entity.FormTable;
import autonavi.online.framework.metadata.xml.SessionFactoryEntity;
import autonavi.online.framework.monitor.ServerMonitor;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DaoHelper;
import autonavi.online.framework.sharding.dao.DynamicDataSource;
import autonavi.online.framework.sharding.dao.ds.LoadBalancingDataSource;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategy;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategyFactory;
import autonavi.online.framework.sharding.dao.exception.DataSourceKeyOutOfRangeException;
import autonavi.online.framework.sharding.dao.exception.ExistingDataSourceKeyException;
import autonavi.online.framework.sharding.entry.DaoSupportFactory;
import autonavi.online.framework.sharding.entry.aspect.DaoAspect;
import autonavi.online.framework.sharding.holder.DataSourceHolder;
import autonavi.online.framework.sharding.index.CreateIndexTables;
import autonavi.online.framework.sharding.index.SegmentTable;
import autonavi.online.framework.sharding.index.ShardInitEntity;
import autonavi.online.framework.sharding.index.ShardingIndex;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;
import autonavi.online.framework.support.hibernate.HibernateUtils;
import autonavi.online.framework.util.BeanUtils;
import autonavi.online.framework.util.ScanAllClassHandle;

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
			dm = shardInitEntity
					.getDynamicTransactionManager();
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
		//获取DaoSupport实例
		AbstractDaoSupport daoSupport=factory.getDaoSupport(ccBaseEntity, dm, shardInitEntity
				.getDataSourceRoute(), shardInitEntity
				.getDaoSupport());
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
		ccConfigUtilBeanDefinition.getPropertyValues().add(
				"daoSupportFactory", factory);
		ccConfigUtilBeanDefinition.getPropertyValues().add(
				"initBaseConfig", initBaseConfig);
		beanDefinitionRegistry.registerBeanDefinition("ccConfigUtil",
				ccConfigUtilBeanDefinition);
		return daoSupport;
		
//		 Map<Integer, Object> map=this.initDS(ccBaseEntity);
//		/**
//		 * 增加如果不设置数据源使用在线框架的情况
//		 */
//		if (map != null && map.size() > 0) {
//			/**
//			 * 生成路由DataSourceRoute的RootBeanDefinition
//			 */
//			RootBeanDefinition dataSourceRouteBeanDefinition = new RootBeanDefinition(
//					DefaultDataSourceRoute.class);
//			/**
//			 * 替换实现
//			 */
//			if (shardInitEntity.getDataSourceRoute() != null) {
//				dataSourceRouteBeanDefinition = new RootBeanDefinition(
//						shardInitEntity.getDataSourceRoute().getClass());
//			}
//			/**
//			 * 解析datasources部分，生成DynamicDataSource的RootBeanDefinition。
//			 */
//			RootBeanDefinition dynamicDataSourceBeanDefinition = initShardDS(
//					ccBaseEntity, map, dm);
//			/**
//			 * 解析index部分,生成shardingIndex
//			 */
//			RootBeanDefinition shardingIndexBeanDefinition = initShardIndex(
//					ccBaseEntity, beanDefinitionRegistry, map);
//
//			/**
//			 * 解析segment-tables部分，生成segment-table的List
//			 */
//			ManagedList<SegmentTable> segmentTables = this.initSegmentTables(
//					ccBaseEntity, beanDefinitionRegistry);
//
//			/**
//			 * 将多数据源注册到路由DataSourceRoute<br/>
//			 * 将shardingIndex注册进路由DataSourceRoute<br/>
//			 * 将segment-table的List注入供DataSourceRoute使用
//			 */
//			dataSourceRouteBeanDefinition.getPropertyValues()
//					.add("dynamicDataSource", dynamicDataSourceBeanDefinition)
//					.add("shardingIndex", shardingIndexBeanDefinition)
//					.add("segmentTables", segmentTables);
//
//			/**
//			 * 初始化SqlHelper，将路由DataSourceRoute注入
//			 */
//			RootBeanDefinition sqlHelperBeanDefinition = new RootBeanDefinition(
//					SqlHelper.class);
//			sqlHelperBeanDefinition.getPropertyValues().add("dataSourceRoute",
//					dataSourceRouteBeanDefinition);
//			/**
//			 * 初始化HbmHelper，将路由DataSourceRoute注入
//			 */
//			RootBeanDefinition hbmHelperBeanDefinition = new RootBeanDefinition(
//					HbmHelper.class);
//			hbmHelperBeanDefinition.getPropertyValues().add("dataSourceRoute",
//					dataSourceRouteBeanDefinition);
//
//			/**
//			 * 初始化daoSupport，将sqlHelper HbmHelper注入
//			 */
//			RootBeanDefinition daoSupportBeanDefinition = new RootBeanDefinition(
//					DefaultDaoSupport.class);
//			/**
//			 * 替换实现
//			 */
//			if (shardInitEntity.getDaoSupport() != null) {
//				daoSupportBeanDefinition = new RootBeanDefinition(
//						shardInitEntity.getDaoSupport().getClass());
//			}
//			daoSupportBeanDefinition.getPropertyValues().add("sqlHelper",
//					sqlHelperBeanDefinition);
//			daoSupportBeanDefinition.getPropertyValues().add("hbmHelper",
//					hbmHelperBeanDefinition);
//
//			/**
//			 * 将daoSupport注册进容器，供其它地方使用
//			 */
//			beanDefinitionRegistry.registerBeanDefinition("daoSupport",
//					daoSupportBeanDefinition);
//
//			/**
//			 * 生成DaoAspect的BeanDefinition
//			 */
//			RootBeanDefinition daoAspectBeanDefinition = new RootBeanDefinition(
//					DaoAspect.class);
//			/**
//			 * 将daoSupport注册进daoAspect
//			 */
//			RuntimeBeanReference daoSupportRef = new RuntimeBeanReference(
//					"daoSupport");
//			daoAspectBeanDefinition.getPropertyValues().add("daoSupport",
//					daoSupportRef);
//
//			/**
//			 * 将daoAspect注册进容器，供其它地方使用
//			 */
//			beanDefinitionRegistry.registerBeanDefinition("daoAspect",
//					daoAspectBeanDefinition);
//			/**
//			 * 工具DAOHelper
//			 */
//			RootBeanDefinition daoHelperBeanDefinition = new RootBeanDefinition(
//					DaoHelper.class);
//			daoHelperBeanDefinition.getPropertyValues().add("daoSupport",
//					daoSupportRef);
//			beanDefinitionRegistry.registerBeanDefinition("dAOHelper",
//					daoHelperBeanDefinition);
//
//			
//
//		} else {
//			log.warn("没有启动数据源配置,在线框架将不启动分布式事务和分库分表的相关功能");
//			// 无数据源情况 注册空的daoSupport和daoAspect
//			RootBeanDefinition daoSupportBeanDefinition = new RootBeanDefinition(
//					DefaultDaoSupport.class);
//			/**
//			 * 替换实现
//			 */
//			if (shardInitEntity.getDaoSupport() != null) {
//				daoSupportBeanDefinition = new RootBeanDefinition(
//						shardInitEntity.getDaoSupport().getClass());
//			}
//			beanDefinitionRegistry.registerBeanDefinition("daoSupport",
//					daoSupportBeanDefinition);
//			RootBeanDefinition daoAspectBeanDefinition = new RootBeanDefinition(
//					DaoAspect.class);
//			RuntimeBeanReference daoSupportRef = new RuntimeBeanReference(
//					"daoSupport");
//			daoAspectBeanDefinition.getPropertyValues().add("daoSupport",
//					daoSupportRef);
//			beanDefinitionRegistry.registerBeanDefinition("daoAspect",
//					daoAspectBeanDefinition);
//		}

	}

	/**
	 * 初始化数据源
	 * 
	 * @param ccBaseEntity
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private Map<Integer, Object> initDS(CcBaseEntity ccBaseEntity)
			throws Exception {
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		Map<String, DataSource> realMap = new HashMap<String, DataSource>();
		Map<Integer, CcDataSource> dataSources = ccBaseEntity.getDataSources();// 读取所有数据源
		Map<String, CcDataSource> realDataSources = ccBaseEntity
				.getRealDataSources();// 获取真实的数据源
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		// 针对属性中存在Properties、Map、List、Set和数组的情况，都要进行“Spring-managed”。不然无所作为对像插入BeanDefinition。
		// 需要注意的是，这里只进行了一级的处理，并没有进行递归。
		// 真实数据源初始化
		for (String key : realDataSources.keySet()) {
			CcDataSource ds = realDataSources.get(key);
			for (String prop : ds.getProps().keySet()) {
				Object obj = ds.getProps().get(prop);
				if (obj != null) {
					if (Properties.class.isAssignableFrom(obj.getClass())) {
						ManagedProperties props = new ManagedProperties();
						Properties p = (Properties) obj;
						for (Object o : p.keySet()) {
							props.put(o, p.get(o));
						}
						propertyMap.put(prop, props);
					} else if (Map.class.isAssignableFrom(obj.getClass())) {
						ManagedMap<Object, Object> mm = new ManagedMap<Object, Object>();
						Map<?, ?> m = (Map<?, ?>) obj;
						for (Object o : m.keySet()) {
							mm.put(o, m.get(o));
						}
						propertyMap.put(prop, mm);
					} else if (List.class.isAssignableFrom(obj.getClass())) {
						ManagedList<Object> ml = new ManagedList<Object>();
						List<?> l = (List<?>) obj;
						for (Object o : l) {
							ml.add(o);
						}
						propertyMap.put(prop, ml);
					} else if (Set.class.isAssignableFrom(obj.getClass())) {
						ManagedSet<Object> se = new ManagedSet<Object>();
						Set<?> s = (Set<?>) obj;
						for (Object o : s) {
							se.add(o);
						}
						propertyMap.put(prop, se);
					} else if (obj.getClass().isArray()) {
						Object[] ob = (Object[]) obj;
						ManagedArray ma = new ManagedArray(obj.getClass()
								.getCanonicalName().replace("[", "")
								.replace("]", ""), ob.length);
						for (Object o : ob) {
							ma.add(o);
						}
						propertyMap.put(prop, ma);
					} else {
						propertyMap.put(prop, obj);
					}
				}

			}
			realMap.put(key, (DataSource) BeanUtils.generateObject(
					ds.getBeanClass(), propertyMap));
		}
		// 代理AOF数据源初始化
		for (Integer key : dataSources.keySet()) {
			CcDataSource ds = dataSources.get(key);
			// 强制写成LoadBalancingDataSource防止包名变化无法找到类 后期JSON文件中不在存储这个属性
			ds.setBeanClass(LoadBalancingDataSource.class.getName());
			if (LoadBalancingDataSource.class.isAssignableFrom(this.getClass()
					.getClassLoader().loadClass(ds.getBeanClass()))) {
				String strategyName = (String) ds.getProps()
						.get("strategyName");
				if (strategyName == null || strategyName.length() == 0) {
					throw new RuntimeException("代理数据源分片策略名为空或者长度为零");
				}
				@SuppressWarnings("unchecked")
				List<String> realDss = (List<String>) ds.getProps().get(
						"realDss");
				if (realDss == null || realDss.size() == 0) {
					throw new RuntimeException("代理数据源中真实数据源配置为空");
				}
				LoadBalancingDataSource loadBalancingDataSource = new LoadBalancingDataSource();
				LoadBalancingStrategy strategy = LoadBalancingStrategyFactory
						.getStrategyDefineInstance(strategyName);
				if (strategy == null) {
					throw new RuntimeException("代理数据源提供的策略名[" + strategyName
							+ "]找不到对应的实现实例 ");
				}
				loadBalancingDataSource.setLoadBalancingStrategy(strategy);
				for (String dsName : realDss) {
					if (realMap.get(dsName.split("\\?")[0]) != null) {
						loadBalancingDataSource.addDataSource(dsName,
								realMap.get(dsName));
					} else {
						throw new RuntimeException("数据源["
								+ dsName.split("\\?")[0] + "] 在真实数据源中无法找到");
					}

				}
				map.put(key, loadBalancingDataSource);

			} else {
				throw new RuntimeException("代理数据源类型必须是["
						+ LoadBalancingDataSource.class.getName()
						+ "] 但是提供的类型为[" + ds.getBeanClass() + "]");
			}

		}
		return map;
	}

	/**
	 * 初始化分片数据源
	 * 
	 * @param ccBaseEntity
	 * @param map
	 * @return
	 * @throws ExistingDataSourceKeyException
	 * @throws DataSourceKeyOutOfRangeException
	 */
	@SuppressWarnings("unused")
	private RootBeanDefinition initShardDS(CcBaseEntity ccBaseEntity,
			Map<Integer, Object> map, DynamicTransactionManager dm)
			throws ExistingDataSourceKeyException,
			DataSourceKeyOutOfRangeException {
		ManagedMap<Integer, Object> targetDataSources = new ManagedMap<Integer, Object>();
		Map<Integer, CcDataSource> dataSources = ccBaseEntity.getDataSources();
		for (Integer child : dataSources.keySet()) {
			Integer key = Integer.valueOf(child);
			if (!Miscellaneous.isInDsKeyInterval(key)) {
				throw new DataSourceKeyOutOfRangeException("数据源Key超出取值范围，只能为"
						+ Miscellaneous.minDsKey + "到" + Miscellaneous.maxDsKey
						+ "的整数");
			}
			Object bean = map.get(key);
			targetDataSources.put(key, bean);
		}

		RootBeanDefinition dynamicDataSourceBeanDefinition = new RootBeanDefinition(
				DynamicDataSource.class);
		if (dm != null) {
			dynamicDataSourceBeanDefinition.getPropertyValues().add(
					"dynamicTransactionManager", dm);
		}
		dynamicDataSourceBeanDefinition.getPropertyValues()
				.add("targetDataSources", targetDataSources)
				.add("indexKey", ccBaseEntity.getShardIndex());
		return dynamicDataSourceBeanDefinition;
	}

	private DataSource getDataSource(Integer index, DataSource dataSource)
			throws SQLException {
		DataSource ds = null;
		Map<Integer, String> dsMap = DataSourceHolder.getDataSourceHolder();
		// 判断线程中数据源是否已经做过策略计算
		if (dsMap.get(index) != null) {
			String dsName = dsMap.get(index);
			ds = ((LoadBalancingDataSource) dataSource)
					.getRealDataSourceByDsName(dsName);
		} else {
			ds = ((LoadBalancingDataSource) dataSource)
					.getBalancedRealDataSource();
			dsMap.put(index, ((LoadBalancingDataSource) dataSource)
					.getDsNameByRealDataSource(ds));
		}
		return ds;
	}

	/**
	 * 初始化分片索引
	 * 
	 * @param ccBaseEntity
	 * @param beanDefinitionRegistry
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private RootBeanDefinition initShardIndex(CcBaseEntity ccBaseEntity,
			BeanDefinitionRegistry beanDefinitionRegistry,
			Map<Integer, Object> map) throws Exception {
		/**
		 * 得到cache配置
		 */
		String cache = "true";

		/**
		 * 得到索引datasource的配置
		 */
		DataSource indexDS = (DataSource) map.get(ccBaseEntity.getShardIndex());

		/*
		 * 建表-开始
		 */
		new CreateIndexTables(this.getDataSource(ccBaseEntity.getShardIndex(),
				indexDS), ccBaseEntity.getIndexTableMap()).init();// 创建用户索引表及分表配置表
		this.createOtherTables(
				this.getDataSource(ccBaseEntity.getShardIndex(), indexDS), map);// 其它用户的表
		/*
		 * 建表-结束
		 */

		/**
		 * 初始化Hibernate工具（注意，由于这个工具并不打算对开发人员开放。所以这个工具只能操作索引片）
		 */
		this.initHibernateUtils(this.getDataSource(
				ccBaseEntity.getShardIndex(), indexDS));

		/**
		 * 初始化用户监控类
		 */
		RootBeanDefinition serverMonitorBeanDefinition = new RootBeanDefinition(
				ServerMonitor.class);
		// 定义心跳
		serverMonitorBeanDefinition.setInitMethodName("heartbeat");
		// 定义数据源
		serverMonitorBeanDefinition.getPropertyValues().add("dataSource",
				indexDS);
		// 定义本地地址
		serverMonitorBeanDefinition.getPropertyValues().add("localhost",
				Miscellaneous.aof_messageListener_localhost);
		// 定义netInterfaceName
		serverMonitorBeanDefinition.getPropertyValues().add("netInterfaceName",
				Miscellaneous.aof_messageListener_netInterfaceName);
		beanDefinitionRegistry.registerBeanDefinition("serverMonitor",
				serverMonitorBeanDefinition);

		/**
		 * 创建并注册ShardingIndex
		 */
		RootBeanDefinition shardingIndexBeanDefinition = new RootBeanDefinition(
				ShardingIndex.class);
		shardingIndexBeanDefinition.getPropertyValues()
				.add("dataSource", indexDS).add("cache", cache)
				.add("index", ccBaseEntity.getShardIndex())
				.add("indexTableMap", ccBaseEntity.getIndexTableMap());

		return shardingIndexBeanDefinition;
	}

	/**
	 * 初始化hibernate工具<br/>
	 * <h1>注意，由于这个工具并不打算对开发人员开放。所以这个工具只能操作索引片</h1>
	 * 
	 * @param indexDS
	 */
	private void initHibernateUtils(final DataSource indexDS) {
		final HibernateUtils hibernateUtils = new HibernateUtils();
		hibernateUtils.setDataSource(indexDS);// 只针对索引片
		final List<String> xmls = new ArrayList<String>();
		ShardPipelineHolder.scanPipeline.add(new ScanAllClassHandle() {
			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				String className = metadataReader.getClassMetadata()
						.getClassName();
				if (className.startsWith("autonavi.online.framework.support")) {
					InputStream is = getClass().getResourceAsStream(
							"/" + className.replace(".", "/") + ".hbm.xml");
					if (is != null) {// 如果存在hbm.xml文件，说明是一个hibernate映射类
						String xml = IOUtils.toString(is);
						xmls.add(xml);
					}
					IOUtils.closeQuietly(is);// 关闭流
				}
			}
		});
		/**
		 * 将要扫描完处理的东西放入后置处理器
		 */
		ShardPipelineHolder.scanPipelineAfters.add(new ScanPipelineAfter() {
			@Override
			public void handle() throws Exception {
				// 将扫描到文件插入信息
				hibernateUtils.setXmls(xmls.toArray(new String[0]));
				// 插入方言
				hibernateUtils.setDialect(DialectUtils.getDialect4Hibernate(
						indexDS.getConnection(), true));
				hibernateUtils.init();
			}
		});
	}

	/**
	 * 初始化分表-将List转为ManagedList
	 * 
	 * @param ccBaseEntity
	 * @param beanDefinitionRegistry
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private ManagedList<SegmentTable> initSegmentTables(
			CcBaseEntity ccBaseEntity,
			BeanDefinitionRegistry beanDefinitionRegistry) throws Exception {
		ManagedList<SegmentTable> segmentTables = new ManagedList<SegmentTable>();
		for (SegmentTable _segmentTable : ccBaseEntity.getSegmentTables()) {
			segmentTables.add(_segmentTable);
		}
		return segmentTables;
	}

	/**
	 * 根据用户配置创建表
	 * 
	 * @param indexDS
	 * @param dsmap
	 * @throws Exception
	 */
	private void createOtherTables(final DataSource indexDS,
			final Map<Integer, Object> dsMap) throws Exception {
		// 加入扫描队列
		ShardPipelineHolder.scanPipeline.add(new ScanAllClassHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				Class<?> clazz = null;
				try {
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(metadataReader.getClassMetadata()
							.getClassName());
				} catch (NoClassDefFoundError e) {
					log.warn(e.getMessage() + "不存在，已跳过", e);
				}
				if (clazz != null && CreateTable.class.isAssignableFrom(clazz)
						&& !clazz.isInterface()) {// 如果类实现了CreateTable接口，说明要创建表
					CreateTable _createTable = (CreateTable) clazz
							.newInstance();// 得到实例
					SessionFactoryEntity sessionFactoryEntity = new SessionFactoryEntity();

					/**
					 * 索引片建表部分
					 */
					Connection indexConn = indexDS.getConnection();// 得到连接
					DatabaseMetaData indexShardDMD = indexConn.getMetaData();// 得到源数据
					FormTable indexShardFormTable = _createTable
							.createTable2IndexShard();// 得到表信息

					if (!this.isTableExist(indexShardDMD,
							indexShardFormTable.getTableName())) {
						if (indexShardFormTable != null) {
							sessionFactoryEntity.setDialect(DialectUtils
									.getDialect4Hibernate(indexShardDMD));// 得到方言
							new TableGenerator(sessionFactoryEntity,
									indexShardFormTable)
									.generatorTable(indexDS);// 创建表
						}
					}
					indexConn.close();

					/**
					 * 建指定分片表部分
					 */
					Map<Integer, FormTable> appointShardFormTables = _createTable
							.createTable2AppointShard();// 得到表信息
					if (appointShardFormTables != null) {
						for (Integer dskey : appointShardFormTables.keySet()) {
							DataSource _ds = (DataSource) dsMap.get(dskey);
							FormTable _formTable = (FormTable) appointShardFormTables
									.get(dskey);
							Connection _conn = _ds.getConnection();// 得到连接
							DatabaseMetaData _dmd = _conn.getMetaData();// 得到源数据

							if (!this.isTableExist(_dmd,
									_formTable.getTableName())) {
								sessionFactoryEntity.setDialect(DialectUtils
										.getDialect4Hibernate(_dmd));// 得到方言
								new TableGenerator(sessionFactoryEntity,
										_formTable).generatorTable(_ds);// 创建表
							}
							_conn.close();
						}
					}

					/**
					 * 建所有分片表部分
					 */
					FormTable allShardFormTables = _createTable
							.createTable2AllShard();// 得到表信息
					if (allShardFormTables != null) {
						for (Integer dskey : dsMap.keySet()) {
							DataSource _ds = (DataSource) dsMap.get(dskey);
							Connection _conn = _ds.getConnection();// 得到连接
							DatabaseMetaData _dmd = _conn.getMetaData();// 得到源数据

							if (!this.isTableExist(_dmd,
									allShardFormTables.getTableName())) {
								sessionFactoryEntity.setDialect(DialectUtils
										.getDialect4Hibernate(_dmd));// 得到方言
								new TableGenerator(sessionFactoryEntity,
										allShardFormTables).generatorTable(_ds);// 创建表
							}
							_conn.close();
						}
					}
				}
			}

			/**
			 * 表是否存在
			 * 
			 * @param databaseMetaData
			 * @param tableName
			 * @return
			 * @throws SQLException
			 */
			private boolean isTableExist(DatabaseMetaData databaseMetaData,
					String tableName) throws SQLException {
				ResultSet rsTables = databaseMetaData.getTables(null, null,
						tableName, null);// 得到表，用于判断表是否存在
				if (!rsTables.next()) {// 如果不存在
					return false;
				}
				return true;
			}
		});
	}

}
