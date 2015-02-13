package autonavi.online.framework.sharding.entry;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import autonavi.online.framework.cc.config.ConfigMyidFile;
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
import autonavi.online.framework.sharding.dao.AbstractDataSourceRoute;
import autonavi.online.framework.sharding.dao.DefaultDaoSupport;
import autonavi.online.framework.sharding.dao.DefaultDataSourceRoute;
import autonavi.online.framework.sharding.dao.DynamicDataSource;
import autonavi.online.framework.sharding.dao.HbmHelper;
import autonavi.online.framework.sharding.dao.SqlHelper;
import autonavi.online.framework.sharding.dao.ds.LoadBalancingDataSource;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategy;
import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategyFactory;
import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;
import autonavi.online.framework.sharding.dao.exception.DataSourceKeyOutOfRangeException;
import autonavi.online.framework.sharding.dao.exception.StrategyNameIsEmpty;
import autonavi.online.framework.sharding.dao.exception.StrategyNameNotFound;
import autonavi.online.framework.sharding.holder.DataSourceHolder;
import autonavi.online.framework.sharding.index.CreateIndexTables;
import autonavi.online.framework.sharding.index.SegmentTable;
import autonavi.online.framework.sharding.index.ShardingIndex;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;
import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.UniqueIDHolder;
import autonavi.online.framework.support.hibernate.HibernateUtils;
import autonavi.online.framework.util.BeanUtils;
import autonavi.online.framework.util.classreading.ClassMetadata;
import autonavi.online.framework.util.classreading.ScanUtils;
import autonavi.online.framework.util.classreading.ScannerHandle;
import autonavi.online.framework.util.json.JsonBinder;

/**
 * DaoSupport工厂，用于生成DaoSupport实例
 * 
 * @author jia.miao
 * 
 */
public class DaoSupportFactory {

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @param dynamicTransactionManager
	 *            动态事务管理器
	 * @param userDataSourceRoute
	 *            AbstractDataSourceRoute子类
	 * @param userDaoSupport
	 *            AbstractDaoSupport子类
	 * @return
	 * @throws Exception
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity,
			DynamicTransactionManager dynamicTransactionManager,
			AbstractDataSourceRoute userDataSourceRoute,
			AbstractDaoSupport userDaoSupport) throws Exception {

		/**
		 * 主键生成器
		 */
		/*if (ccBaseEntity.getGenerateUniqueHandler() != null
				&& !ccBaseEntity.getGenerateUniqueHandler().isEmpty())
			UniqueIDHolder.setIdWorker((IdWorker) this.getClass()
					.getClassLoader()
					.loadClass(ccBaseEntity.getGenerateUniqueHandler())
					.newInstance());*/

		AbstractDataSourceRoute dataSourceRoute = new DefaultDataSourceRoute();// 数据源路由
		if (userDataSourceRoute != null)
			dataSourceRoute = userDataSourceRoute;

		SqlHelper sqlHelper = new SqlHelper();// jdbc sql助手
		HbmHelper hbmHelper = new HbmHelper();// hibernate助手
		AbstractDaoSupport daoSupport = new DefaultDaoSupport();// DaoSupport子类
		if (userDaoSupport != null)
			daoSupport = userDaoSupport;
		/**
		 * 读取本地属性
		 */
		this.readLocalPorpFile();

		/**
		 * 读取myid文件
		 */
		new ConfigMyidFile().readMyId();

		/**
		 * 初始化分片策略
		 */
		this.initStrategyInstance();

		/**
		 * 创建数据源
		 */
		Map<Integer, DataSource> dataSources = this
				.initDataSources(ccBaseEntity);

		/**
		 * 动态数据源
		 */
		dynamicDataSource
				.setDynamicTransactionManager(dynamicTransactionManager);// 事务管理器
		dynamicDataSource.setTargetDataSources(dataSources);// 数据源
		dynamicDataSource.setIndexKey(ccBaseEntity.getShardIndex());// 索引片ID

		/**
		 * 动态数据源插入路由
		 */
		dataSourceRoute.setDynamicDataSource(dynamicDataSource);

		/**
		 * 分片索引
		 */
		ShardingIndex shardingIndex = this.initShardingIndex(ccBaseEntity,
				dataSources);

		/**
		 * 分片索引插入路由
		 */
		dataSourceRoute.setShardingIndex(shardingIndex);

		/**
		 * 分表设置
		 */
		List<SegmentTable> segmentTables = this.initSegmentTables(ccBaseEntity);
		dataSourceRoute.setSegmentTables(segmentTables);

		/**
		 * 路由插入SQL助手
		 */
		sqlHelper.setDataSourceRoute(dataSourceRoute);

		/**
		 * SQL助手插入daoSupport
		 */
		daoSupport.setSqlHelper(sqlHelper);

		/**
		 * 路由插入HBM助手
		 */
		hbmHelper.setDataSourceRoute(dataSourceRoute);

		/**
		 * HBM助手插入daoSupport
		 */
		daoSupport.setHbmHelper(hbmHelper);

		/**
		 * 进行扫描并执行
		 */
		this.scanAndRunAfter();

		/**
		 * 返回daoSupport
		 */
		return daoSupport;
	}

	/**
	 * 创建数据源
	 * 
	 * @param ccBaseEntity
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, DataSource> initDataSources(CcBaseEntity ccBaseEntity)
			throws Exception {
		Map<Integer, DataSource> dataSources = new HashMap<Integer, DataSource>();
		Map<String, DataSource> realDataSources = new HashMap<String, DataSource>();// 真实数据源

		// 先得到真实数源
		for (String dsKey : ccBaseEntity.getRealDataSources().keySet()) {
			CcDataSource ccDataSource = ccBaseEntity.getRealDataSources().get(
					dsKey);
			// 通过工具得到真实数据源
			DataSource ds = (DataSource) BeanUtils.generateObject(
					ccDataSource.getBeanClass(), ccDataSource.getProps());
			// 放入Map中
			realDataSources.put(dsKey, ds);
		}
		if (realDataSources == null || realDataSources.size() == 0) {
			throw new RuntimeException("真实数据源配置为空");
		}

		// 负载数据源
		for (Integer proxyDsKey : ccBaseEntity.getDataSources().keySet()) {
			CcDataSource ccDataSource = ccBaseEntity.getDataSources().get(
					proxyDsKey);
			LoadBalancingDataSource lbDataSource = new LoadBalancingDataSource();
			String strategyName = (String) ccDataSource.getProps().get(
					"strategyName");
			if (strategyName == null || strategyName.length() == 0) {
				throw new RuntimeException("代理数据源分片策略名为空或者长度为零");
			}
			@SuppressWarnings("unchecked")
			List<String> realDss = (List<String>) ccDataSource.getProps().get(
					"realDss");
			if (realDss == null || realDss.size() == 0) {
				throw new RuntimeException("代理数据源中真实数据源配置为空");
			}
			LoadBalancingStrategy strategy = LoadBalancingStrategyFactory
					.getStrategyDefineInstance(strategyName);
			if (strategy == null) {
				throw new RuntimeException("代理数据源提供的策略名[" + strategyName
						+ "]找不到对应的实现实例 ");
			}
			lbDataSource.setLoadBalancingStrategy(strategy);// 负载算法
			// 插入真实数据源
			for (String dsName : realDss) {
				if (realDataSources.get(dsName.split("\\?")[0]) != null) {
					lbDataSource.addDataSource(dsName,
							realDataSources.get(dsName));
				} else {
					throw new RuntimeException("数据源[" + dsName.split("\\?")[0]
							+ "] 在真实数据源中无法找到");
				}

			}

			Integer key = proxyDsKey;
			if (!Miscellaneous.isInDsKeyInterval(key)) {
				throw new DataSourceKeyOutOfRangeException("数据源Key超出取值范围，只能为"
						+ Miscellaneous.minDsKey + "到" + Miscellaneous.maxDsKey
						+ "的整数");
			}
			dataSources.put(key, lbDataSource);
		}
		return dataSources;
	}

	/**
	 * 创建或更新分片索引表
	 * 
	 * @param ccBaseEntity
	 * @param dataSources
	 * @return
	 * @throws Exception
	 * @throws
	 */
	private ShardingIndex initShardingIndex(CcBaseEntity ccBaseEntity,
			Map<Integer, DataSource> dataSources) throws Exception {
		Integer shardIndex = ccBaseEntity.getShardIndex();// 索引片ID
		DataSource shardIndexndexDataSource = dataSources.get(ccBaseEntity
				.getShardIndex());// 索引片数据源

		Map<String, List<ColumnAttribute>> indexTables = ccBaseEntity
				.getIndexTableMap();
		ShardingIndex shardingIndex = new ShardingIndex();// 分片索引
		shardingIndex.setCache("true");
		shardingIndex.setIndex(shardIndex);// 索引片ID
		shardingIndex.setDataSource(shardIndexndexDataSource);// 索引片数据源
		shardingIndex.setIndexTableMap(indexTables);// 索引表信息
		/*
		 * 建表-开始
		 */
		new CreateIndexTables(this.getDataSource(shardIndex,
				shardIndexndexDataSource), indexTables).init();// 创建用户索引表及分表配置表
		this.createOtherTables(
				this.getDataSource(shardIndex, shardIndexndexDataSource),
				dataSources);// 其它用户的表
		/*
		 * 建表-结束
		 */

		/**
		 * 初始化Hibernate工具（注意，由于这个工具并不打算对开发人员开放。所以这个工具只能操作索引片）
		 */
		this.initHibernateUtils(this.getDataSource(shardIndex,
				shardIndexndexDataSource));

		/**
		 * 初始化用户监控类
		 */
		ServerMonitor serverMonitor = new ServerMonitor();
		serverMonitor.setDataSource(shardIndexndexDataSource);
		serverMonitor.setLocalhost(Miscellaneous.aof_messageListener_localhost);
		serverMonitor
				.setNetInterfaceName(Miscellaneous.aof_messageListener_netInterfaceName);
		serverMonitor.heartbeat();
		return shardingIndex;
	}

	/**
	 * 读取本地属性文件<br/>
	 * 这个方法主要是为那些需要不同配置的方法准备的，如比本机的地址
	 */
	private void readLocalPorpFile() {
		String json = null;
		InputStream is = getClass().getResourceAsStream("/aof.json");
		if (is != null) {
			try {
				json = IOUtils.toString(is);
			} catch (IOException e) {
				log.warn("读取本地属性文件错误，将使用默认配置");
			} finally {
				IOUtils.closeQuietly(is);
			}
			JsonBinder binder = JsonBinder.buildNonDefaultBinder();
			binder.setDateFormat("yyyy-MM-dd HH:mm:ss");
			binder.fromJson(json, Miscellaneous.class);// 将json文件中的属性写入类中
		}
	}

	/**
	 * 根据用户配置创建表
	 * 
	 * @param indexDS
	 * @param dsmap
	 * @throws Exception
	 */
	private void createOtherTables(final DataSource indexDS,
			final Map<Integer, DataSource> dsMap) throws Exception {
		// 加入扫描队列
		ShardPipelineHolder.scanPipeline.add(new ScannerHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(ClassMetadata classMetadata) throws Exception {
				Class<?> clazz = null;
				try {
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(classMetadata.getClassName());
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
							DataSource _ds = dsMap.get(dskey);
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
		ShardPipelineHolder.scanPipeline.add(new ScannerHandle() {
			@Override
			public void handle(ClassMetadata classMetadata) throws Exception {
				String className = classMetadata.getClassName();
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
	 * 获取当前线程中的数据源，因为同一线程要保持在同一数据源上进行操作
	 * 
	 * @param index
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
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
	 * 创建分片属性
	 * 
	 * @param ccBaseEntity
	 * @return
	 */
	private List<SegmentTable> initSegmentTables(CcBaseEntity ccBaseEntity) {

		return ccBaseEntity.getSegmentTables();
	}

	/**
	 * 初始化策略算法
	 * 
	 * @throws Exception
	 */
	private void initStrategyInstance() throws Exception {
		ScannerHandle handle = new ScannerHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(ClassMetadata classMetadata) throws Exception {
				Class<?> clazz = null;
				String className = "";
				try {
					className = classMetadata.getClassName();
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(className);
				} catch (NoClassDefFoundError e) {
					log.warn(e.getMessage() + "不存在，已跳过");
				}
				if (clazz != null
						&& LoadBalancingStrategy.class.isAssignableFrom(clazz)
						&& !clazz.isInterface() && !classMetadata.isAbstract()) {
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
		String loadBalancingStrategyClassName = LoadBalancingStrategy.class
				.getName();
		String loadBalancingStrategyPackage = loadBalancingStrategyClassName
				.substring(0, loadBalancingStrategyClassName.lastIndexOf("."));
		ScanUtils.scanner(loadBalancingStrategyPackage, handle);
	}

	/**
	 * 统一进行扫描并运行后置处理器
	 * 
	 * @throws Exception
	 */
	private void scanAndRunAfter() throws Exception {
		// 运行扫描，并依此调用管道里的接口
		ScanUtils.scanner("autonavi.online.framework", new ScannerHandle() {

			@Override
			public void handle(ClassMetadata classMetadata) throws Exception {
				for (ScannerHandle scannerHandle : ShardPipelineHolder.scanPipeline)
					scannerHandle.handle(classMetadata);
			}

		});
		// 运行后置处理器
		for (ScanPipelineAfter scanPipelineAfter : ShardPipelineHolder.scanPipelineAfters) {
			scanPipelineAfter.handle();
		}
	}

	/**
	 * 刷新动态数据源
	 * 
	 * @param newDataSources
	 */
	public void refreshDataSources(Map<Integer, DataSource> newDataSources) {
		dynamicDataSource.setTargetDataSources(newDataSources);
	}

	private Log log = LogFactory.getLog(this.getClass());
	private DynamicDataSource dynamicDataSource = new DynamicDataSource();// 动态数据源

	/**
	 * 扫描管道后置处理器
	 * 
	 * @author jia.miao
	 * 
	 */
	interface ScanPipelineAfter {
		public void handle() throws Exception;
	}

	/**
	 * 管道
	 * 
	 * @author jia.miao
	 * 
	 */
	static class ShardPipelineHolder {
		public static final List<ScannerHandle> scanPipeline = new ArrayList<ScannerHandle>();
		public static final List<ScanPipelineAfter> scanPipelineAfters = new ArrayList<ScanPipelineAfter>();

	}

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @throws Exception
	 * 
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity)
			throws Exception {
		return this.getDaoSupport(ccBaseEntity, null, null, null);
	}

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @param dynamicTransactionManager
	 *            动态事务管理器
	 * @throws Exception
	 * 
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity,
			DynamicTransactionManager dynamicTransactionManager)
			throws Exception {
		return this.getDaoSupport(ccBaseEntity, dynamicTransactionManager,
				null, null);
	}

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @param userDaoSupport
	 *            AbstractDaoSupport子类
	 * @throws Exception
	 * 
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity,
			AbstractDaoSupport userDaoSupport) throws Exception {
		return this.getDaoSupport(ccBaseEntity, null, null, userDaoSupport);
	}

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @param userDataSourceRoute
	 *            AbstractDataSourceRoute子类
	 * @throws Exception
	 * 
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity,
			AbstractDataSourceRoute userDataSourceRoute) throws Exception {
		return this
				.getDaoSupport(ccBaseEntity, null, userDataSourceRoute, null);
	}

	/**
	 * 根据配置生成DaoSupport对象
	 * 
	 * @param ccBaseEntity
	 *            对应json的实体
	 * @param userDataSourceRoute
	 *            AbstractDataSourceRoute子类
	 * @param userDaoSupport
	 *            AbstractDaoSupport子类
	 * @return
	 * @throws Exception
	 */
	public AbstractDaoSupport getDaoSupport(CcBaseEntity ccBaseEntity,
			AbstractDataSourceRoute userDataSourceRoute,
			AbstractDaoSupport userDaoSupport) throws Exception {
		return this.getDaoSupport(ccBaseEntity, null, userDataSourceRoute,
				userDaoSupport);
	}
}
