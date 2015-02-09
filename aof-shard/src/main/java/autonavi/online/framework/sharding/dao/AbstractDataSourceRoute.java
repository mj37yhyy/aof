package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.sharding.holder.DataSourceHolder;
import autonavi.online.framework.sharding.index.SegmentEntity;
import autonavi.online.framework.sharding.index.SegmentTable;
import autonavi.online.framework.sharding.index.ShardingHandle;
import autonavi.online.framework.sharding.index.ShardingIndex;
import autonavi.online.framework.sharding.index.ShardingIndexEntity;
import autonavi.online.framework.util.StopWatchLogger;

public abstract class AbstractDataSourceRoute {
	private Logger log = LogManager.getLogger(this.getClass());

	/**
	 * 通过路由字段得到数据源。<br/>
	 * 路由字段将到路由库中查找对应的分片ID，如果不存在表，将新建，并随机计算一个ID给当前的索引；如果表存在但没有记录，同样会随机分配分片ID。
	 * 外理完成后，该方法将切换到这个分片的连接上，并返回Connection (批量处理需要手工管理连接，将不会返回Connection)。
	 * 
	 * @param indexColumnValue
	 * 
	 * @param sharding
	 * @param parameterMap
	 * @param singleDataSourceKey
	 * @return
	 * @throws Exception
	 */
	public RouteResult getRouteDataSource(String indexName,
			Object[] indexColumn, Object[] indexColumnValue,
			ShardingIndexEntity shardingIndexEntity, int dsKey, String sql,
			SegmentEntity obj, ShardingHandle handle) throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("getRouteDataSource");
		RouteResult routeResult = new RouteResult();

		try {

			// if (singleDataSourceKey > 0) {// 如果指定了数据源，就直接切换到该数据源
			// dsKey = singleDataSourceKey;// 根据singleDataSourceKey切换数据源
			// } else {// 如果没有指定，通过索引表进行查找
			// dsKey = shardingIndexEntity.getShard_id();// 根据索引表中的数据切换数据源
			// }

			Connection conn = getConnection(dsKey, true);
			routeResult.setConnection(conn);
			if (shardingIndexEntity != null) {
				routeResult.setSql(this.replaceSegmentTableName(
						shardingIndexEntity.getId(), sql,
						shardingIndexEntity.getShard_id(), obj, handle,
						indexColumn, indexColumnValue));// 将SQL中的表名替换为分表表名
			} else {
				routeResult.setSql(sql);
			}

			swlogger.stop();
			swlogger.writeLog();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		return routeResult;
	}

	/**
	 * 开启事务，得到连接
	 * 
	 * @param shardingIndexEntity
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection getConnection(int dsKey, boolean openTransaction)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, SQLException {
		CustomerContextHolder.setCustomerType(dsKey);
		Map<Integer, String> dsMap = DataSourceHolder.getDataSourceHolder();
		DataSource ds = null;
		String dsName = null;
		// 判断线程中数据源是否已经做过策略计算
		if (dsMap.get(CustomerContextHolder.getCustomerType()) != null) {
			dsName = dsMap.get(CustomerContextHolder.getCustomerType());
			ds = DynamicDataSource.getCurrentProxyDataSource()
					.getRealDataSourceByDsName(dsName);
		} else {
			ds = DynamicDataSource.getCurrentDataSource();// 得到当前的数据源
			dsName = DynamicDataSource.getCurrentProxyDataSource()
					.getDsNameByRealDataSource(ds);
			dsMap.put(dsKey, dsName);
		}
		return this.getConnection(dsKey, ds, openTransaction);
	}

	/**
	 * 要求子类实现的方法
	 * 
	 * @param dsKey
	 * @param ds
	 * @param openTransaction
	 * @return
	 * @throws SQLException
	 */
	public abstract Connection getConnection(int dsKey, DataSource ds,
			boolean openTransaction) throws SQLException;

	/**
	 * 得到ShardingIndexEntity
	 * 
	 * @param indexName
	 * @param indexColumnValue
	 * @param singleDataSourceKey
	 * @return
	 * @throws Exception
	 */
	public ShardingIndexEntity getShardingIndexEntity(String indexName,
			Object[] indexColumn, Object[] indexColumnValue,
			int singleDataSourceKey, ShardingHandle handle, String sql)
			throws Exception {
		return shardingIndex.getShardingIndexEntity(indexName, indexColumn,
				indexColumnValue, singleDataSourceKey, handle, sql,
				this.getConnection(shardingIndex.getIndex(), false));

	}

	/**
	 * 替换sql中的分表表名。<br/>
	 * 1、查看SQL中是否包含分表表名；<br/>
	 * 2、如果存在，查询AOF_SEGMENT_TABLE_CONFIG表中是否已经添加表名；<br/>
	 * 3、如果没有，按照规则（不大于count）生成表名，插入AOF_SEGMENT_TABLE_CONFIG表<br/>
	 * 4、如果存在，返回对应的表名，并替换SQL中的表名 5、循环替换后返回SQL<br/>
	 * 
	 * @param indexTableID
	 *            索引表的ID
	 * @param sql
	 *            代替换的SQL
	 * @param dsKey
	 * @param segmentTables
	 *            分表表名的配置
	 * @return
	 * @throws Exception
	 */
	public String replaceSegmentTableName(long indexTableID, String sql,
			int dsKey, SegmentEntity obj, ShardingHandle handle,
			Object[] indexColumn, Object[] indexColumnValue) throws Exception {
		if (obj.isSegement()) {
			int count = obj.getSegemntCount();
			int index = shardingIndex.getSegmentTableIndexVlaue(indexTableID,
					obj.getSegmentTable(), count, dsKey, handle, indexColumn,
					indexColumnValue,
					this.getConnection(shardingIndex.getIndex(), false));
			sql = sql.replaceAll("(?i)" + obj.getSegmentTable(),
					obj.getSegmentTable() + "_" + index);
		}
		return sql;
	}

	/**
	 * 判断SQL是否需要分表查询
	 * 
	 * @author yaming.xu
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public SegmentEntity isSQLNeedSegment(String sql) throws Exception {
		SegmentEntity entity = new SegmentEntity();
		for (SegmentTable segmentTable : this.segmentTables) {
			String _segmentTableName = segmentTable.getName();
			Pattern pattern = Pattern.compile("(?i)\\s+" + _segmentTableName
					+ "(\\s+|\\({1}|$)");
			Matcher matcher = pattern.matcher(sql);
			if (matcher.find()) {
				entity = new SegmentEntity();
				entity.setSegement(true);
				entity.setSegmentTable(_segmentTableName);
				entity.setSegemntCount(segmentTable.getCount());
				return entity;
			}
		}
		return entity;
	}

	public void openIndexCache() throws Exception {
		shardingIndex.maintainShardingIndexEntity(this.getConnection(
				shardingIndex.getIndex(), false));
	}

	public void closeIndexCache() throws Exception {
		shardingIndex.releaseShardingIndexEntity();
	}

	private ShardingIndex shardingIndex = null;
	private List<SegmentTable> segmentTables = null;

	public void setShardingIndex(ShardingIndex shardingIndex) {
		this.shardingIndex = shardingIndex;
	}

	public void setSegmentTables(List<SegmentTable> segmentTables) {
		this.segmentTables = segmentTables;
	}

	public ShardingIndex getShardingIndex() {
		return shardingIndex;
	}

	private DynamicDataSource dynamicDataSource = null;

	public DynamicDataSource getDynamicDataSource() {
		return this.dynamicDataSource;
	}

	public void setDynamicDataSource(DynamicDataSource dynamicDataSource) {
		this.dynamicDataSource = dynamicDataSource;
	}

}
