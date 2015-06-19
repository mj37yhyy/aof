package autonavi.online.framework.sharding.index;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cache.ehcache.EhcacheUtils;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.dao.DynamicDataSource;
import autonavi.online.framework.sharding.dao.SqlPrint;
import autonavi.online.framework.sharding.dao.exception.DBTableNotFoundException;
import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.UniqueIDHolder;
import autonavi.online.framework.util.StopWatchLogger;

public class ShardingIndex {
	/**
	 * 获取分区表信息
	 * 
	 * @param tableName
	 * @return
	 */
	public List<ColumnAttribute> getIndexTable(String tableName) {
		if (this.indexTableMap != null) {
			return this.indexTableMap.get(tableName.toUpperCase());
		}
		return null;
	}

	/**
	 * 保持Ehcache缓存不释放，同时加载全部索引信息到索引缓存 暂时不支持分表信息的不释放处理
	 */
	public void maintainShardingIndexEntity(Connection conn) throws Exception {
		for (String tableName : indexTableMap.keySet()) {
			List<Map<String, Object>> list = null;
			List<ColumnAttribute> columnAttributeList = this
					.getIndexTable(tableName);
			if (null == columnAttributeList) {
				throw new RuntimeException("索引表【" + tableName + "】不存在");
			}
			// hold住分库的索引表
			if (this.shardingIndexCacheUtils != null) {
				if (this.shardingIndexCacheUtils
						.openOrCloseCacheTimeOut(EhcacheUtils.CLOSE_TIMEOUT) == 1) {
					// Connection conn = this.getConnection();
					try {
						StringBuffer selectSQL = new StringBuffer();
						selectSQL.append("select * from ");
						selectSQL.append(tableName.toUpperCase());
						list = new QueryRunner().query(conn,
								selectSQL.toString(), new MapListHandler());
						if (log.isInfoEnabled())
							log.info("执行SQL " + selectSQL.toString());
						for (Map<String, Object> _map : list) {
							ShardingIndexEntity shardingIndexEntity = new ShardingIndexEntity();
							shardingIndexEntity.setId((Long) _map.get("id"));
							shardingIndexEntity.setMigration_lock((Long) _map
									.get("migration_lock"));
							shardingIndexEntity.setOld_shard_id((Long) _map
									.get("old_shard_id"));
							shardingIndexEntity.setShard_id(((Long) _map
									.get("shard_id")).intValue());
							String cacheKey = tableName;
							for (int i = 0; i < columnAttributeList.size(); i++) {
								ColumnAttribute columnAttribute = columnAttributeList
										.get(i);
								Object _icv = _map.get(columnAttribute
										.getColumnName());
								cacheKey += "_"
										+ columnAttribute.getColumnName() + "_"
										+ _icv.toString();
							}
							this.shardingIndexCacheUtils.set(cacheKey,
									shardingIndexEntity);
						}
					} finally {
						if (conn != null && !conn.isClosed()) {
							conn.close();
							conn = null;
						}
					}
				}
			}
		}
	}

	/**
	 * 启动缓存自动释放
	 * 
	 * @throws Exception
	 */
	public void releaseShardingIndexEntity() throws Exception {
		// 需要多次循环降次数-1下去 为0时候才最终关闭
		for (@SuppressWarnings("unused")
		String tableName : indexTableMap.keySet()) {
			if (this.shardingIndexCacheUtils != null) {
				this.shardingIndexCacheUtils
						.openOrCloseCacheTimeOut(EhcacheUtils.OPEN_TIMEOUT);
			}
		}

	}

	/**
	 * 直接读取缓存获取
	 * 
	 * @param tableName
	 * @param indexColumn
	 * @param indexColumnValue
	 * @param singleDataSourceKey
	 * @param handle
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public ShardingIndexEntity getShardingIndexEntityFromCache(String tableName,
			Object[] indexColumnValue) throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("getShardingIndexEntity");
		ShardingIndexEntity shardingIndexEntity = null;
		List<ColumnAttribute> columnAttributeList = this
				.getIndexTable(tableName);// 列信息
		if (null == columnAttributeList) {
			throw new RuntimeException("索引表【" + tableName + "】不存在");
		}
		if (columnAttributeList.size() != indexColumnValue.length) {
			throw new RuntimeException("索引表【" + tableName
					+ "】所配置的列数与实际参数不符，请检查");
		}
		// 从缓存中取分片信息
		String cacheKey = tableName;
		for (int i = 0; i < columnAttributeList.size(); i++) {
			Object _icv = indexColumnValue[i];
			if (_icv == null) {
				throw new RuntimeException("入参中分片字段的值为空,请检查");
			}
			ColumnAttribute columnAttribute = columnAttributeList.get(i);
			cacheKey += "_" + columnAttribute.getColumnName() + "_"
					+ _icv.toString();
		}
		if (this.shardingIndexCacheUtils != null)
			shardingIndexEntity = (ShardingIndexEntity) this.shardingIndexCacheUtils
					.get(cacheKey);
		return shardingIndexEntity;
	}

	/**
	 * 通过索引键获取数据所在的数据源的Key供切换
	 * 
	 * @param tableName
	 *            表名
	 * @param indexColumnValue
	 *            索引列的值
	 * @param singleDataSourceKey
	 *            指定数据源时便不再计算数据源
	 * @param columns
	 *            索引键
	 * @return 数据源的Key
	 * @throws Exception
	 */
	public ShardingIndexEntity getShardingIndexEntityFromDb(String tableName,
			Object[] indexColumn, Object[] indexColumnValue,
			int singleDataSourceKey, ShardingHandle handle, String sql,
			Connection conn) throws Exception {
		ShardingIndexEntity shardingIndexEntity = null;
		try {
			StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
			swlogger.start("getShardingIndexEntity");

			List<ColumnAttribute> columnAttributeList = this
					.getIndexTable(tableName);// 列信息
			if (null == columnAttributeList) {
				throw new RuntimeException("索引表【" + tableName + "】不存在");
			}
			if (columnAttributeList.size() != indexColumnValue.length) {
				throw new RuntimeException("索引表【" + tableName
						+ "】所配置的列数与实际参数不符，请检查");
			}
			// 从缓存中取分片信息
			String cacheKey = tableName;
			for (int i = 0; i < columnAttributeList.size(); i++) {
				Object _icv = indexColumnValue[i];
				if (_icv == null) {
					throw new RuntimeException("入参中分片字段的值为空,请检查");
				}
				ColumnAttribute columnAttribute = columnAttributeList.get(i);
				cacheKey += "_" + columnAttribute.getColumnName() + "_"
						+ _icv.toString();
			}

			// 查询当前表的字段，看是否与用户传入的字段匹配
			shardingIndexEntity = this.selectFromOldTable(conn, tableName,
					indexColumnValue, this.getIndexTable(tableName), cacheKey,
					singleDataSourceKey);
			if (shardingIndexEntity == null) {// 如果不存在，插入一条
				shardingIndexEntity = this
						.insertIntoNewTable(conn, tableName, indexColumnValue,
								this.getIndexTable(tableName), cacheKey,
								singleDataSourceKey, handle, indexColumn, sql);
			}
			swlogger.stop();
			swlogger.writeLog();
			if (shardingIndexEntity.getShard_id() != singleDataSourceKey
					&& singleDataSourceKey > 0) {
				throw new RuntimeException("指定的单一数据源和索引记录的数据源ID不相符");
			}

		} catch (Exception e) {// 如果发生异常，尝试从缓存中取得
			e.printStackTrace();
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			// if (this.shardingIndexCacheUtils != null)
			// shardingIndexEntity = (ShardingIndexEntity)
			// this.shardingIndexCacheUtils
			// .get(cacheKey);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
			}
		}
		return shardingIndexEntity;

	}

	/**
	 * 查询已存的表中的分片ID
	 * 
	 * @param conn
	 * @param tableName
	 * @param indexColumnValue
	 * @param columns
	 * @param singleDataSourceKey
	 * @return
	 * @throws Exception
	 */
	private ShardingIndexEntity selectFromOldTable(Connection conn,
			String tableName, Object[] indexColumnValue,
			List<ColumnAttribute> columns, String cacheKey,
			int singleDataSourceKey) throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("selectFromOldTable");
		ShardingIndexEntity shardingIndexEntity = null;
		String sqlParameters = "";
		if (columns == null)
			throw new DBTableNotFoundException("索引表不存在于配置中，请查询xml！");
		// 查看缓存中是否存在，如果存在，则直接返回，如果不存在，加入并返回
		// Object _key = this.LRUCache.get(cacheKey);
		if (this.shardingIndexCacheUtils != null)
			shardingIndexEntity = (ShardingIndexEntity) this.shardingIndexCacheUtils
					.get(cacheKey);
		if (shardingIndexEntity != null) {
			return shardingIndexEntity;
		}
		try {
			StringBuffer selectSQL = new StringBuffer();
			selectSQL.append("select * from ");
			selectSQL.append(tableName.toUpperCase());
			selectSQL.append(" where 1=1 ");
			for (ColumnAttribute column : columns) {// 将用户传入的列信息插入
				selectSQL.append(" and ");
				selectSQL.append(column.getColumnName());
				selectSQL.append("=?");
			}
			// 执行查询
			shardingIndexEntity = new QueryRunner().query(conn, selectSQL
					.toString(), new BeanHandler<ShardingIndexEntity>(
					ShardingIndexEntity.class), indexColumnValue);
			// 如果指定了数据源，直接使用而无视数据库中的
			// if (singleDataSourceKey > 0)
			// shardingIndexEntity.setShard_id(singleDataSourceKey);
			// 拼装打印信息
			for (int i = 0; i < indexColumnValue.length; i++) {
				sqlParameters += indexColumnValue[i] + "("
						+ indexColumnValue[i].getClass().getName() + "), ";
			}
			// 打印SQL和参数
			SqlPrint.sqlAndParamConsuming(this.getClass(),
					selectSQL.toString(), sqlParameters);
			if (this.shardingIndexCacheUtils != null)
				this.shardingIndexCacheUtils.set(cacheKey, shardingIndexEntity);// 保存在缓存
		} finally {
			swlogger.stop();
			swlogger.writeLog();
		}
		return shardingIndexEntity;
	}

	/**
	 * 将数据插入表中
	 * 
	 * @param conn
	 * @param indexColumnValue
	 * @param sql
	 * @param columns
	 * @param singleDataSourceKey
	 * @return
	 * @throws Exception
	 */
	private ShardingIndexEntity insertIntoNewTable(Connection conn,
			String tableName, Object[] indexColumnValue,
			List<ColumnAttribute> columns, String cacheKey,
			int singleDataSourceKey, ShardingHandle handle,
			Object[] indexColumn, String sql) throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("insertIntoNewTable");
		Set<Integer> set = new HashSet<Integer>();
		for (Integer i : DynamicDataSource.getKeySet()) {
			set.add(i);
		}
		Object[] values = new Object[indexColumnValue.length];
		for (int i = 0; i < indexColumnValue.length; i++) {
			if (Date.class.isAssignableFrom(indexColumnValue[i].getClass())) {
				// date
				values[i] = new Date(((Date) indexColumnValue[i]).getTime());
			} else if (Timestamp.class.isAssignableFrom(indexColumnValue[i]
					.getClass())) {
				// 时间戳
				values[i] = new Timestamp(
						((Date) indexColumnValue[i]).getTime());
			} else {
				values[i] = indexColumnValue[i];
			}
		}
		ShardingEntity entity = new ShardingEntity();
		entity.setDsKeys(set);
		entity.setIndexColumn(indexColumn.clone());
		entity.setIndexColumnValue(values);
		entity.setSql(sql);
		int dsKey = singleDataSourceKey > 0 ? singleDataSourceKey : handle
				.handleDataSource(entity);// 如果指定了数据源，则直接使用，如果没有，得到一个顺序数据源KEY
		ShardingIndexEntity shardingIndexEntity = null;
		String sqlParameters = "";
		try {
			conn.setAutoCommit(false);
			StringBuffer insertSQL = new StringBuffer();// 插入SQL
			StringBuffer valueSQL = new StringBuffer();
			insertSQL.append("insert into ");
			insertSQL.append(tableName.toUpperCase() + " (ID, ");
			long id = idWorker.nextId(dsKey);// 计算ID
			valueSQL.append(id + ",");

			for (int i = 0; i < columns.size(); i++) {// 将用户传入的列信息插入
				ColumnAttribute column = columns.get(i);
				insertSQL.append(column.getColumnName() + ",");
				valueSQL.append("?,");
			}
			insertSQL.append("SHARD_ID,");
			valueSQL.append("'" + dsKey + "',");
			insertSQL.append("MIGRATION_LOCK,");
			valueSQL.append("0,");
			insertSQL.append("OLD_SHARD_ID");
			valueSQL.append("0");
			insertSQL.append(") values(");
			insertSQL.append(valueSQL);
			insertSQL.append(")");

			// 更新
			int count = new QueryRunner().update(conn, insertSQL.toString(),
					indexColumnValue);
			// 拼装打印信息
			for (int i = 0; i < indexColumnValue.length; i++) {
				sqlParameters += indexColumnValue[i] + "("
						+ indexColumnValue[i].getClass().getName() + "), ";
			}
			// 打印SQL和参数
			SqlPrint.sqlAndParamConsuming(this.getClass(),
					insertSQL.toString(), sqlParameters);
			if (count > 0) {// 更新成功，更新缓存
				shardingIndexEntity = new ShardingIndexEntity();
				shardingIndexEntity.setId(id);
				shardingIndexEntity.setShard_id(dsKey);
				if (this.shardingIndexCacheUtils != null)
					this.shardingIndexCacheUtils.set(cacheKey,
							shardingIndexEntity);// 保存在缓存
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			throw e;
		} finally {
			swlogger.stop();
			swlogger.writeLog();
		}
		return shardingIndexEntity;
	}

	/**
	 * 通过索引表的ID和分表名得到分表序列，如果没有就创建一个，缓存于缓存中
	 * 
	 * @param indexTableID
	 * @param segmentTableName
	 * @param dsKey
	 * @return
	 * @throws Exception
	 */
	public int getSegmentTableIndexVlaue(long indexTableID,
			String segmentTableName, int count, int dsKey,
			ShardingHandle handle, Object[] indexColumn,
			Object[] indexColumnValue, Connection conn) throws Exception {
		Integer indexValue = 0;
		String cacheKey = indexTableID + "_" + segmentTableName;
		if (segmentTableNameCacheUtils != null) {// 先从缓存里取
			indexValue = (Integer) segmentTableNameCacheUtils.get(cacheKey);
		}
		// Connection conn = null;
		if (indexValue == null) {// 如果缓存里不存在
			// conn = this.getConnection();
			QueryRunner queryRunner = new QueryRunner();
			// 先进行查询
			String sql = "select INDEX_VALUE from "
					+ CreateIndexTables.SEGMENT_TABLE_CONFIG
					+ " where INDEX_TABLE_FK=? and TABLE_NAME=?";
			indexValue = queryRunner.query(conn, sql,
					new ScalarHandler<Integer>("INDEX_VALUE"), indexTableID,
					segmentTableName);
			if (indexValue == null) {// 如果在数据库在没有查询到，则新增一条
				long id = idWorker.nextId(dsKey);// 计算ID
				ShardingEntity entity = new ShardingEntity();
				Object[] values = new Object[indexColumnValue.length];
				for (int i = 0; i < indexColumnValue.length; i++) {
					if (Date.class.isAssignableFrom(indexColumnValue[i]
							.getClass())) {
						// date
						values[i] = new Date(
								((Date) indexColumnValue[i]).getTime());
					} else if (Timestamp.class
							.isAssignableFrom(indexColumnValue[i].getClass())) {
						// 时间戳
						values[i] = new Timestamp(
								((Date) indexColumnValue[i]).getTime());
					} else {
						values[i] = indexColumnValue[i];
					}
				}

				entity.setDsKey(dsKey);
				entity.setIndexColumn(indexColumn.clone());
				entity.setIndexColumnValue(values);
				entity.setSegemntId(id);
				entity.setTableCount(count);
				entity.setSql(sql);
				indexValue = handle.handleSegment(entity);
				queryRunner
						.update(conn,
								"insert into "
										+ CreateIndexTables.SEGMENT_TABLE_CONFIG
										+ " (ID,INDEX_TABLE_FK,TABLE_NAME,INDEX_VALUE) values (?,?,?,?)",
								id, indexTableID, segmentTableName, indexValue);
			}
			if (segmentTableNameCacheUtils != null) {// 存入缓存里
				segmentTableNameCacheUtils.set(cacheKey, indexValue);
			}
		}
		if (conn != null && !conn.isClosed()) {
			conn.close();
			conn = null;
		}
		return indexValue;
	}

	// private Connection getConnection() throws ClassNotFoundException,
	// InstantiationException, IllegalAccessException, SQLException {
	// return DataSourceRoute.getConnection(this.index, false);
	// }

	/**
	 * 获取新的索引ID<br/>
	 * 
	 * @param id
	 * 
	 * @param count
	 * @return
	 */
	@SuppressWarnings("unused")
	private Integer getNewIndex(long id, int count) {
		return (int) (id % count) + 1;
	}

	private Logger log = LogManager.getLogger(this.getClass());
	private DataSource dataSource = null;
	private final IdWorker idWorker = UniqueIDHolder.getIdWorker();
	// private LRULinkedHashMap<String, Long> LRUCache = null;
	private EhcacheUtils shardingIndexCacheUtils = null;
	private EhcacheUtils segmentTableNameCacheUtils = null;
	private Map<String, List<ColumnAttribute>> indexTableMap = null;

	// 索引表分片号记录
	private Integer index;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setCache(String cache) {
		if (cache.equalsIgnoreCase("true")) {
			// LRUCache = new LRULinkedHashMap<String, Long>(1000);
			shardingIndexCacheUtils = new EhcacheUtils("AOF.ShardingIndexCache");
			segmentTableNameCacheUtils = new EhcacheUtils(
					"AOF.SegmentTableNameCache");
		}
	}

	public void setIndexTableMap(
			Map<String, List<ColumnAttribute>> indexTableMap) {
		this.indexTableMap = indexTableMap;
	}

}
