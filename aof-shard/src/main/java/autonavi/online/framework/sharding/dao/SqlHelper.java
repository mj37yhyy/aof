package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cc.Miscellaneous;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.patch.commons.dbutils.BasicRowProcessorPatch;
import autonavi.online.framework.sharding.dao.constant.RegularExpressions;
import autonavi.online.framework.sharding.dao.constant.ReservedWord;
import autonavi.online.framework.sharding.dao.handlers.ColumnHandler;
import autonavi.online.framework.sharding.dao.handlers.ColumnListHandler;
import autonavi.online.framework.sharding.dao.paging.PagingFactory;
import autonavi.online.framework.sharding.dao.paging.entity.Count;
import autonavi.online.framework.sharding.entry.entity.CollectionType;
import autonavi.online.framework.sharding.index.SegmentEntity;
import autonavi.online.framework.sharding.index.ShardingHandle;
import autonavi.online.framework.sharding.index.ShardingIndexEntity;
import autonavi.online.framework.sharding.index.ShardingSupportType;
import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.IdWorkerType;
import autonavi.online.framework.sharding.uniqueid.UniqueIDFactory;
import autonavi.online.framework.util.bean.PropertyUtils;

/**
 * 用于分片时的SQL语句的执行
 * 
 * @author jia.miao
 * 
 */
public class SqlHelper {

	/**
	 * 获取分区表信息
	 * 
	 * @param tableName
	 * @return
	 */
	protected List<ColumnAttribute> getIndexTable(String tableName) {
		return this.dataSourceRoute.getShardingIndex().getIndexTable(tableName);
	}

	/**
	 * 检测分区字段类型
	 * 
	 * @param column
	 * @param value
	 * @param i
	 */
	protected void checkIndexValue(ColumnAttribute column, Object value, int i) {
		ShardingSupportType type = ShardingSupportType.getSupportByType(column
				.getColumnType());
		if (type == null) {
			throw new RuntimeException("系统不支持类型为[" + column.getColumnType()
					+ "]的分区字段");
		}
		if (!type.getClazz().isAssignableFrom(value.getClass())) {
			throw new RuntimeException("配置的分区字段类型[" + type.getClazz().getName()
					+ "]与提供的入参类型[" + value.getClass().getName() + "]不符 为第["
					+ (i + 1) + "]个分区参数");
		}
		return;
	}

	/**
	 * 检测sql是否合法
	 * 
	 * @param sql
	 */
	protected void checkSqlValid(String sql) {
		Pattern pattern = Pattern.compile("(?i)select\\s{0,}\\*\\s{0,}from");
		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			throw new RuntimeException("语句中不允许出现\"*\",请使用字段名称");
		}
		pattern = Pattern.compile("<>");
		if (matcher.find()) {
			throw new RuntimeException("语句中不允许出现\"<>\"");
		}
		pattern = Pattern.compile("!=");
		if (matcher.find()) {
			throw new RuntimeException("语句中不允许出现\"!=\"");
		}
	}

	/**
	 * 所有查询的通过方法
	 * 
	 * @param author
	 *            用户
	 * @param indexName
	 *            索引表名
	 * @param indexColumn
	 *            索引字段
	 * @param singleDataSourceKey
	 *            单一数据源ID
	 * @param sql
	 *            查询的SQL
	 * @param parameterMap
	 *            参数
	 * @param start
	 *            分页开始
	 * @param limit
	 *            分页结束
	 * @param queryCount
	 *            是否查询总行数
	 * @param collectionType
	 *            返回集合的类型
	 * @param resultType
	 *            返回字段的类型
	 * @param callbackClass
	 *            自定义的Handler
	 * @return
	 * @throws Exception
	 */
	protected Object select(String author, String indexName,
			Object[] indexColumn, Object[] indexColumnValue,
			int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, int startOrSkip,
			int endOrRowSize, boolean isQueryCount,
			CollectionType collectionType, Class<?> resultType,
			ResultSetCallback<?> callback, SegmentEntity sg,
			ShardingHandle handle) throws Exception {

		StringBuffer sqlParameters = new StringBuffer();
		Object result = null;
		ResultSet resultSet = null, resultSet2 = null;
		ResultSetHandler<?> h = null;
		NamedParameterStatement preparedStatement = null, preparedStatement2 = null;
		int dsKey = -1;
		ShardingIndexEntity shardingIndexEntity = null;
		try {
			if (singleDataSourceKey > 0) {
				// 单一数据源
				if (!sg.isSegement()) {
					// 单表 就直接切换到该数据源
					dsKey = singleDataSourceKey;
				} else {
					// 多表
					shardingIndexEntity = dataSourceRoute
							.getShardingIndexEntity(indexName, indexColumn,
									indexColumnValue, singleDataSourceKey,
									handle, sql);
					dsKey = shardingIndexEntity.getShard_id();

				}
			} else {
				// 多数据源
				shardingIndexEntity = dataSourceRoute.getShardingIndexEntity(
						indexName, indexColumn, indexColumnValue,
						singleDataSourceKey, handle, sql);
				dsKey = shardingIndexEntity.getShard_id();
			}

			PagingFactory pagingFactory = new PagingFactory();// 分页工厂
			RouteResult routeResult = dataSourceRoute.getRouteDataSource(
					indexName, indexColumn, indexColumnValue,
					shardingIndexEntity, dsKey, sql, sg, handle);// 得到路由并开启事务
			sql = routeResult.getSql();// 得到替换过表名的SQL
			String selectSQL = sql;
			if (startOrSkip > -1 && endOrRowSize > -1) {// 起始/跳过行和结束/限制行必须都填
				selectSQL = pagingFactory
						.getPaging(routeResult.getConnection()).getPagingSQL(
								sql, startOrSkip, endOrRowSize);
			}
			preparedStatement = new NamedParameterStatement(
					routeResult.getConnection(), selectSQL, parameterMap);// 通过连接得到PS

			this.fillStatement(preparedStatement, selectSQL, parameterMap,
					sqlParameters, -1);// 填充ps
			long startTime = System.currentTimeMillis();
			resultSet = preparedStatement.executeQuery();// 执行查询
			SqlPrint.sqlTimeconsuming(author, startTime, selectSQL);// 打印查询时间
			SqlPrint.sqlAndParamConsuming(getClass(),
					preparedStatement.getParsedQuery(),
					sqlParameters.toString());// 打印SQL和参数
			if (callback != null && !callback.getClass().isInterface()) {// 如果存在回调函数，则进行调用
				result = callback.process(resultSet);
			} else {
				h = this.getResultSetHandler(collectionType, resultType);// 返回ResultSetHandler的实例
				result = h.handle(resultSet);
			}
			if (isQueryCount) {
				sqlParameters = new StringBuffer();
				String countSQL = pagingFactory.getPaging(
						routeResult.getConnection()).getCountSQL(sql, "*");// 得到countSQL
				preparedStatement2 = new NamedParameterStatement(
						routeResult.getConnection(), countSQL, parameterMap);// 通过连接得到PS
				this.fillStatement(preparedStatement2, countSQL, parameterMap,
						sqlParameters, -1);// 填充
				startTime = System.currentTimeMillis();
				resultSet2 = preparedStatement2.executeQuery();// 执行查询
				SqlPrint.sqlTimeconsuming(author, startTime, countSQL);// 打印查询时间
				SqlPrint.sqlAndParamConsuming(getClass(),
						preparedStatement2.getParsedQuery(),
						sqlParameters.toString());// 打印SQL和参数
				h = new BeanHandler<Count>(Count.class);
				Object obj = h.handle(resultSet2);
				if (null == obj) {
					DaoHelper.setCount(0L);
				} else {
					DaoHelper.setCount(((Count) obj).getC());
				}

			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
				resultSet = null;
			}
			if (resultSet2 != null) {
				resultSet2.close();
				resultSet2 = null;
			}
			if (preparedStatement != null) {
				preparedStatement.close();
				preparedStatement = null;
			}
			if (preparedStatement2 != null) {
				preparedStatement2.close();
				preparedStatement2 = null;
			}
		}
		return result;
	}

	/**
	 * 批量查询<br/>
	 * 注意：不支持查询总行数
	 * 
	 * @param author
	 *            用户
	 * @param indexName
	 *            索引表名
	 * @param indexColumn
	 *            索引字段
	 * @param singleDataSourceKey
	 *            单一数据源ID
	 * @param sql
	 *            查询的SQL
	 * @param parameterMap
	 *            参数
	 * @param start
	 *            分页开始，无用，保留
	 * @param limit
	 *            分页结束，无用，保留
	 * @param queryCount
	 *            是否查询总行数
	 * @param collectionType
	 *            返回的集合类型
	 * @param resultType
	 *            返回字段的类型
	 * @param callbackClass
	 *            自定义的Handler
	 * @return List<Object>
	 * @throws Exception
	 */
	protected List<Object> selectBatch(String author, String indexName,
			String[] indexColumn, int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, int start, int limit,
			boolean queryCount, CollectionType collectionType,
			Class<?> resultType, ResultSetCallback<?> callback,
			SegmentEntity sg, ShardingHandle handle) throws Exception {
		List<Object> result = new ArrayList<Object>();
		ResultSetHandler<?> h = null;
		Map<Integer, String> sqls = new HashMap<Integer, String>();// 存放所有的SQL

		NamedParameterStatement preparedStatement = null;
		ResultSet resultSet = null;
		StringBuffer sqlParameters = new StringBuffer();
		String newSql = null;
		long index = 0;

		// 首先进行SQL的拼装，把同一数据源的SQL都用union all 连接起来并放入MAP中
		while (true) {
			Object[] indexColumnValue = null;
			try {
				ShardingIndexEntity shardingIndexEntity = null;
				int dsKey = -1;
				if (singleDataSourceKey > 0) {
					// 单一数据源
					if (!sg.isSegement()) {
						// 单表 直接报错
						throw new RuntimeException(
								"单数据源-单表模式不支持批量SQL查询，请使用or方式查询");
					} else {
						// 多表
						indexColumnValue = getColumnValues(indexName,
								indexColumn, parameterMap, index, true);
						shardingIndexEntity = dataSourceRoute
								.getShardingIndexEntity(indexName, indexColumn,
										indexColumnValue, singleDataSourceKey,
										handle, sql);// 得到数据所在的分片信息
						dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY

					}
				} else {
					// 多数据源
					indexColumnValue = getColumnValues(indexName, indexColumn,
							parameterMap, index, true);
					shardingIndexEntity = dataSourceRoute
							.getShardingIndexEntity(indexName, indexColumn,
									indexColumnValue, singleDataSourceKey,
									handle, sql);// 得到数据所在的分片信息
					dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY
				}

				if (sqls.containsKey(dsKey)) {// 如果同一数据源的SQL存在，则将新的SQL拼到原来SQL的后面
					sqls.put(
							dsKey,
							sqls.get(dsKey)
									+ " union all ( "
									+ dataSourceRoute.replaceSegmentTableName(
											shardingIndexEntity.getId(),
											sql.replace(ReservedWord.index, "["
													+ index + "]"), dsKey, sg,
											handle, indexColumn,
											indexColumnValue) + " ) ");
				} else {// 如果同一数据源的SQL不存在
					sqls.put(
							dsKey,
							" ( "
									+ dataSourceRoute.replaceSegmentTableName(
											shardingIndexEntity.getId(),
											sql.replace(ReservedWord.index, "["
													+ index + "]"), dsKey, sg,
											handle, indexColumn,
											indexColumnValue) + " ) ");

				}
				index++;
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}
		if (sqls.size() == 0) {
			StringBuffer buff = new StringBuffer();
			for (String s : indexColumn) {
				buff.append(s + ",");
			}
			StringBuffer buff1 = new StringBuffer();
			for (String s : parameterMap.keySet()) {
				buff1.append("[" + s + ":" + parameterMap.get(s) + "],");
			}
			log.warn("本次查询使用了分库字段" + buff.toString() + " 但是提供的入参"
					+ buff1.toString() + "可能为空，没有记录可以插入，请检查");
		}
		// 根据根据不同的SQL到不同的数据源上进行查询，最后合并结果
		for (Iterator<Integer> i = sqls.keySet().iterator(); i.hasNext();) {

			try {
				sqlParameters = new StringBuffer();
				int dsKey = i.next();// 得到数据源KEY
				RouteResult routeResult = dataSourceRoute.getRouteDataSource(
						indexName, indexColumn, null, null, dsKey,
						sqls.get(dsKey), sg, handle);// 得到路由
				newSql = routeResult.getSql();// 得到替换过表名的SQL
				preparedStatement = new NamedParameterStatement(
						routeResult.getConnection(), newSql, parameterMap);// 通过SQL得到PS
				this.fillStatement(preparedStatement, newSql, parameterMap,
						sqlParameters, -1);// 填充ps
				SqlPrint.sqlAndParamConsuming(getClass(),
						preparedStatement.getParsedQuery(),
						sqlParameters.toString());// 打印SQL和参数
				long startTime = System.currentTimeMillis();
				resultSet = preparedStatement.executeQuery();
				SqlPrint.sqlTimeconsuming(author, startTime, newSql);// 打印查询时间
				Object _result = null;
				if (callback != null && !callback.getClass().isInterface()) {// 如果存在回调函数，则进行调用
					_result = callback.process(resultSet);
				} else {
					h = this.getResultSetHandler(collectionType, resultType);// 返回ResultSetHandler的实例
					_result = h.handle(resultSet);
				}
				this.processResult(result, _result);// 将结果进行合并
			} catch (Exception e) {
				throw e;
			} finally {
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (preparedStatement != null) {
					preparedStatement.close();
					preparedStatement = null;
				}
			}
		}
		return result;
	}

	/**
	 * 插入
	 * 
	 * @param indexName
	 *            索引名
	 * @param indexColumnValue
	 *            索引值
	 * @param singleDataSourceKey
	 *            给定的数据源key
	 * @param sql
	 *            要执行的SQL
	 * @param parameterMap
	 *            参数
	 * @return
	 * @throws Exception
	 */
	protected int insert(String author, String indexName, Object[] indexColumn,
			Object[] indexColumnValue, int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, SegmentEntity sg,
			ShardingHandle handle) throws Exception {
		return update(author, indexName, indexColumn, indexColumnValue,
				singleDataSourceKey, sql, parameterMap, sg, handle);
	}

	/**
	 * 更新
	 * 
	 * @param indexName
	 *            索引名
	 * @param indexColumnValue
	 *            索引值
	 * @param singleDataSourceKey
	 *            给定的数据源key
	 * @param sql
	 *            要执行的SQL
	 * @param parameterMap
	 *            参数
	 * @return
	 * @throws Exception
	 */
	protected int update(String author, String indexName, Object[] indexColumn,
			Object[] indexColumnValue, int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, SegmentEntity obj,
			ShardingHandle handle) throws Exception {
		int count;
		StringBuffer sqlParameters = new StringBuffer();

		ShardingIndexEntity shardingIndexEntity = null;
		int dsKey = -1;
		if (singleDataSourceKey > 0) {
			// 单一数据源
			if (!obj.isSegement()) {
				// 单表
				dsKey = singleDataSourceKey;
			} else {
				// 多表
				shardingIndexEntity = dataSourceRoute.getShardingIndexEntity(
						indexName, indexColumn, indexColumnValue,
						singleDataSourceKey, handle, sql);// 得到数据所在的分片信息
				dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY

			}
		} else {
			// 多数据源
			shardingIndexEntity = dataSourceRoute.getShardingIndexEntity(
					indexName, indexColumn, indexColumnValue,
					singleDataSourceKey, handle, sql);// 得到数据所在的分片信息
			dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY
		}

		RouteResult routeResult = dataSourceRoute.getRouteDataSource(indexName,
				indexColumn, indexColumnValue, shardingIndexEntity, dsKey, sql,
				obj, handle);// 得到路由并开启事务
		sql = routeResult.getSql();// 得到替换过表名的SQL
		NamedParameterStatement preparedStatement = new NamedParameterStatement(
				routeResult.getConnection(), sql, parameterMap);
		try {
			DaoHelper.setPrimaryKey(this.fillStatement(preparedStatement, sql,
					parameterMap, sqlParameters, -1));// 返回主键（仅适用于insert）
			log.info("开始执行SQL");
			SqlPrint.sqlAndParamConsuming(getClass(),
					preparedStatement.getParsedQuery(),
					sqlParameters.toString());// 打印SQL和参数
			long startTime = System.currentTimeMillis();
			count = preparedStatement.executeUpdate();// 执行更新
			SqlPrint.sqlTimeconsuming(author, startTime, sql);
		} catch (Exception e) {
			throw e;
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
				preparedStatement = null;
			}
		}
		return count;
	}

	/**
	 * 批量更新
	 * 
	 * @param indexName
	 * @param indexColumnValue
	 * @param singleDataSourceKey
	 * @param sqls
	 * @param parameterList
	 * @return
	 * @throws Exception
	 */
	protected int[] updateBatch(String author, String indexName,
			String[] indexColumn, int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, SegmentEntity sg,
			ShardingHandle handle) throws Exception {
		// Map<Integer, int[]> counts = new HashMap<Integer, int[]>();
		List<Long> pkList = new ArrayList<Long>();// 用于存放主键的LIST
		Map<String, NamedParameterStatement> preparedStatements = new HashMap<String, NamedParameterStatement>();// 得到ps组
		Map<String, List<Long>> sqlIndex = new HashMap<String, List<Long>>();
		Map<Long, Integer> processIndex = new HashMap<Long, Integer>();
		NamedParameterStatement preparedStatement = null;
		StringBuffer sqlParameters = new StringBuffer();
		String newSql = null;
		long index = 0;

		while (true) {
			Object[] indexColumnValue = null;
			try {
				ShardingIndexEntity shardingIndexEntity = null;
				int dsKey = -1;
				if (singleDataSourceKey > 0) {
					// 单一数据源
					if (!sg.isSegement()) {
						// 单表
						dsKey = singleDataSourceKey;
						indexColumnValue = getColumnValues(indexName,
								indexColumn, parameterMap, index, false);
					} else {
						// 多表
						indexColumnValue = getColumnValues(indexName,
								indexColumn, parameterMap, index, true);
						shardingIndexEntity = dataSourceRoute
								.getShardingIndexEntity(indexName, indexColumn,
										indexColumnValue, singleDataSourceKey,
										handle, sql);// 得到数据所在的分片信息
						dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY

					}
				} else {
					// 多数据源
					indexColumnValue = getColumnValues(indexName, indexColumn,
							parameterMap, index, true);
					shardingIndexEntity = dataSourceRoute
							.getShardingIndexEntity(indexName, indexColumn,
									indexColumnValue, singleDataSourceKey,
									handle, sql);// 得到数据所在的分片信息
					dsKey = shardingIndexEntity.getShard_id();// 得到数据源的KEY
				}
				if (shardingIndexEntity != null)
					newSql = dataSourceRoute.replaceSegmentTableName(
							shardingIndexEntity.getId(), sql,
							shardingIndexEntity.getShard_id(), sg, handle,
							indexColumn, indexColumnValue);// 得到替换过表名的SQL
				else
					newSql = sql;

				/**
				 * 这里是保存已经存在的PS，
				 */
				String psKey = dsKey + "_" + newSql;
				boolean isExistPs = false;
				// 如果已经存在这个PS，则直接使用，如果不存在，新建一个并加入MAP中
				if (preparedStatements.containsKey(psKey)) {
					preparedStatement = preparedStatements.get(psKey);
					isExistPs = true;
				} else {
					// 传入SQL，得到ps
					preparedStatement = new NamedParameterStatement(
							dataSourceRoute.getConnection(dsKey, true), newSql,
							parameterMap);// 通过SQL得到PS
					preparedStatements.put(psKey, preparedStatement);// 插入到PS组中
					isExistPs = false;
				}
				pkList.add(this.fillStatement(preparedStatement, newSql,
						parameterMap, sqlParameters, index));// 返回主键（仅适用于insert）
				preparedStatement.addBatch();// 加入批处理组
				if (isExistPs) {
					List<Long> l = sqlIndex.get(psKey);
					l.add(index);
				} else {
					List<Long> l = new ArrayList<Long>();
					l.add(index);
					sqlIndex.put(psKey, l);
				}
				index++;
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}

		long startTime = System.currentTimeMillis();
		Iterator<Entry<String, NamedParameterStatement>> it = preparedStatements
				.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, NamedParameterStatement> entry = it.next();
			String psKey = entry.getKey();
			NamedParameterStatement _parameterStatement = entry.getValue();
			if (_parameterStatement != null) {
				SqlPrint.sqlAndParamConsuming(getClass(),
						preparedStatement.getParsedQuery(), null);// 打印SQL和参数
				int[] _counts = _parameterStatement.executeBatch();// 执行批量更新
				List<Long> l = sqlIndex.get(psKey);
				int _index = 0;
				for (Long _l : l) {
					processIndex.put(_l, _counts[_index]);
					_index++;
				}
				// counts.put(
				// Integer.valueOf(psKey.substring(0, psKey.indexOf("_"))),
				// _counts);
			}
		}
		if (preparedStatement == null) {
			StringBuffer buff = new StringBuffer();
			for (String s : indexColumn) {
				buff.append(s + ",");
			}
			StringBuffer buff1 = new StringBuffer();
			for (String s : parameterMap.keySet()) {
				buff1.append("[" + s + ":" + parameterMap.get(s) + "],");
			}
			log.warn("本次更新使用了分库字段" + buff.toString() + " 但是提供的入参"
					+ buff1.toString() + "可能为空，没有记录可以插入，请检查");
		} else {
			SqlPrint.sqlTimeconsuming(author, startTime, newSql);// 打印执行时间
			SqlPrint.sqlAndParamConsuming(getClass(), null,
					sqlParameters.toString());// 打印SQL和参数
		}

		if (pkList != null && !pkList.isEmpty()) {
			// 如果是插入，可以返回ID
			DaoHelper.setPrimaryKeys((Long[]) pkList.toArray(new Long[0]));// 将主键组返回
		}
		// 关闭PS
		if (preparedStatements != null && preparedStatements.size() > 0) {
			Iterator<Entry<String, NamedParameterStatement>> psit = preparedStatements
					.entrySet().iterator();
			while (psit.hasNext()) {
				NamedParameterStatement _parameterStatement = psit.next()
						.getValue();
				if (_parameterStatement != null)
					_parameterStatement.close();
			}
		}
		int[] _result = new int[processIndex.size()];
		for (int i = 0; i < index; i++) {
			_result[i] = processIndex.get(new Long(i));
		}
		return _result;
	}

	/**
	 * 删除
	 * 
	 * @param indexName
	 *            索引名
	 * @param indexColumnValue
	 *            索引值
	 * @param singleDataSourceKey
	 *            给定的数据源key
	 * @param sql
	 *            要执行的SQL
	 * @param parameterMap
	 *            参数
	 * @return
	 * @throws Exception
	 */
	protected int delete(String author, String indexName, Object[] indexColumn,
			Object[] indexColumnValue, int singleDataSourceKey, String sql,
			Map<String, Object> parameterMap, SegmentEntity sg,
			ShardingHandle handle) throws Exception {
		return update(author, indexName, indexColumn, indexColumnValue,
				singleDataSourceKey, sql, parameterMap, sg, handle);
	}

	/**
	 * 初始化NamedParameterStatement
	 * 
	 * @param indexName
	 *            索引名
	 * @param indexColumnValue
	 *            索引值
	 * @param singleDataSourceKey
	 *            给定的数据源key
	 * @param sql
	 *            要执行的SQL
	 * @param parameterMap
	 *            参数
	 * @return
	 * @throws Exception
	 */
	// private NamedParameterStatement initParameterStatement(String indexName,
	// Object[] indexColumnValue, int singleDataSourceKey, String sql,
	// Map<String, Object> parameterMap) throws Exception {
	// RouteResult routeResult = dataSourceRoute.getRouteDataSource(indexName,
	// indexColumnValue, singleDataSourceKey, sql);// 得到路由并开启事务
	// return new NamedParameterStatement(routeResult.getConnection(),
	// routeResult.getSql(), parameterMap);
	// }

	/**
	 * fillStatement
	 * 
	 * @param preparedStatement
	 * @param sql
	 * @param parameterMap
	 * @param sqlParameters
	 * @return
	 * @throws Exception
	 */
	private long fillStatement(final NamedParameterStatement preparedStatement,
			String sql, final Map<String, Object> parameterMap,
			final StringBuffer sqlParameters, final long index)
			throws Exception {
		final long[] uid = new long[1];
		// final boolean isInsert = NamedParameterStatement
		// .isInsert(preparedStatement.getParsedQuery());

		this.fillStatement(sql, new FillStatementHandle() {

			@Override
			public void handle(String key) throws Exception {
				Object val = null;
				if (key.equalsIgnoreCase(ReservedWord.snowflake)
				// && isInsert
				) {// 如果当前字段是@snowflake@，则生成一个ID
					long dsKey = CustomerContextHolder.getCustomerType();// 得到当前的数据源的key
					// 得到uid
					val = idWorker.nextId(dsKey);
					uid[0] = (Long) val;
				} else {
					String realKey = null;
					if (index > -1 && key.indexOf(ReservedWord.index) > -1) {
						realKey = key.replace(ReservedWord.index, "[" + index
								+ "]");
					}
					val = PropertyUtils.getValue(parameterMap,
							realKey == null ? key : realKey);
					if (val == null) {
						String errorMessage = "字段" + key + "的值不存在，请注意";
						log.warn(errorMessage);
					}
				}
				if (sqlParameters.length() < Miscellaneous.max_log_length)
					sqlParameters.append(val == null ? "(NULL)," : val + "("
							+ val.getClass().getName() + "), ");
				// preparedStatement.setObject(key, val);
				fillStatement(preparedStatement, key, val);
			}
		});// 查找SQL中所有绑定变量
		return uid[0];
	}

	/**
	 * fillStatement
	 * 
	 * @param sql
	 * @param fsHandle
	 *            FillStatementHandle
	 * @throws Exception
	 */
	private void fillStatement(String sql, FillStatementHandle fsHandle)
			throws Exception {
		Pattern pattern = Pattern.compile(RegularExpressions.PARAM_RIGHT_ASK);
		Matcher matcher = pattern.matcher(sql);
		while (matcher.find()) {// 找到所有的匹配，调用handle
			String matching = matcher.group();
			fsHandle.handle(matching.substring(2, matching.length() - 1));
		}
	}

	/**
	 * fillStatement
	 * 
	 * @param preparedStatement
	 * @param key
	 * @param val
	 * @throws SQLException
	 */
	private void fillStatement(NamedParameterStatement preparedStatement,
			String key, Object val) throws SQLException {
		if (val == null) {
			preparedStatement.setNull(key);
		} else if (val instanceof Integer) {
			preparedStatement.setInt(key, (Integer) val);
		} else if (val instanceof Long) {
			preparedStatement.setLong(key, (Long) val);
		} else if (val instanceof String) {
			preparedStatement.setString(key, (String) val);
		} else if (val instanceof Date) {
			preparedStatement.setDate(key, (Date) val);
		} else if (val instanceof Timestamp) {
			preparedStatement.setTimestamp(key, (Timestamp) val);
		} else {
			preparedStatement.setObject(key, val);
		}
	}

	/**
	 * 处理批量查询时候的结果集合并 针对MAP 数组 LIST 和BEAN等的处理
	 * 
	 * @param l
	 * @param result
	 */
	private void processResult(List<Object> l, Object result) {
		if (result != null) {
			Class<?> clazz = result.getClass();
			if (clazz.isArray()) {
				Object[] objs = (Object[]) result;
				for (Object obj : objs) {
					l.add(obj);
				}

			} else if (List.class.isAssignableFrom(clazz)) {
				l.addAll((List<?>) result);
			} else if (Map.class.isAssignableFrom(clazz)) {
				l.add(result);
			} else {
				l.add(result);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResultSetHandler getResultSetHandler(CollectionType collectionType,
			Class<?> resultType) {
		if (collectionType == CollectionType.array) {// 如果是array
			return new ArrayHandler();
		} else if (collectionType == CollectionType.arrayList) {// 如果是arrayList
			return new ArrayListHandler();
		} else if (collectionType == CollectionType.column
				|| collectionType == CollectionType.columnList) {
			if (resultType == null)
				throw new RuntimeException("columnType为必填项");
			if (collectionType == CollectionType.column)// 如果是column
				return new ColumnHandler(resultType);
			if (collectionType == CollectionType.columnList)// 如果是columnList
				return new ColumnListHandler(resultType);
		} else if (collectionType == CollectionType.map) {// 如果是Map
			return new MapHandler(new BasicRowProcessorPatch());
		} else if (collectionType == CollectionType.mapList) {// 如果是MapList
			return new MapListHandler(new BasicRowProcessorPatch());
		} else if (collectionType == CollectionType.bean
				|| collectionType == CollectionType.beanList) {
			if (resultType == null)
				throw new RuntimeException("columnType为必填项");
			if (collectionType == CollectionType.bean) {// 如果是bean
				return new BeanHandler(resultType);
			}
			if (collectionType == CollectionType.beanList) {// 如果是beanList
				return new BeanListHandler(resultType);
			}
		}
		return new MapListHandler(new BasicRowProcessorPatch());
	}

	private Object[] getColumnValues(String indexName, String[] indexColumn,
			Map<String, Object> parameterMap, long index, boolean isSegament)
			throws Exception {
		if (parameterMap == null || parameterMap.size() == 0) {
			throw new RuntimeException("需要在方法中提供入参注解 入参注解@SqlParameter未提供");
		}
		Object[] indexColumnValue = new Object[indexColumn.length];
		List<ColumnAttribute> l = getIndexTable(indexName);
		for (int i = 0; i < indexColumn.length; i++) {
			if (indexColumn[i].indexOf(ReservedWord.index) != -1) {
				String[] eArray = indexColumn[i].split("\\.");
				if (parameterMap.get(eArray[0]) == null) {
					throw new RuntimeException(
							"操作使用了批量操作，但是找不到分区字段对应的值，如果是单一数据源，且数据不分表，请不要在批量注解中使用 ReservedWord.index，只在SQL中使用即可");
				} else if (!List.class.isAssignableFrom(parameterMap.get(
						eArray[0]).getClass())
						&& !parameterMap.get(eArray[0]).getClass().isArray()) {
					throw new RuntimeException(
							"操作使用了批量操作，对应的["
									+ eArray[0]
									+ "]必须为LIST或者数组 如果是单一数据源，且数据不分表，请不要在批量注解中使用 ReservedWord.index，只在SQL中使用即可");
				}
			}
			Object value = PropertyUtils.getValue(
					parameterMap,
					indexColumn[i].replace(ReservedWord.index, "[" + index
							+ "]"));
			if (value == null && isSegament) {
				throw new RuntimeException("分区参数"
						+ indexColumn[i].replace(ReservedWord.index, "["
								+ index + "]") + "没有在入参中找到");
			}
			indexColumnValue[i] = value;
			if ((l == null || l.size() == 0 || l.size() != indexColumn.length)
					&& indexColumnValue[i] != null && isSegament) {
				// 如果索引表 找不到 且需要分区或者分表的时候
				if (l == null)
					throw new RuntimeException("索引表名称【" + indexName + "】没有找到");
				else
					throw new RuntimeException("索引表名称【" + indexName + "】分区字段数【"
							+ l.size() + "】与入参字段数【" + indexColumn.length
							+ "】不相等");
			} else if ((l != null && l.size() > 0 && l.size() == indexColumn.length)
					&& indexColumnValue[i] != null && isSegament) {
				checkIndexValue(l.get(i), indexColumnValue[i], i);
			}
		}
		return indexColumnValue;
	}

	protected void executeSql(int dsKey, String author, String sql,
			Map<String, Object> parameterMap, boolean openTx) throws Exception {
		Connection conn = null;
		NamedParameterStatement preparedStatement = null;
		try {
			conn = dataSourceRoute.getConnection(dsKey, openTx);
			preparedStatement = new NamedParameterStatement(conn, sql,
					parameterMap);
			log.info("开始执行SQL" + preparedStatement.getParsedQuery());
			long startTime = System.currentTimeMillis();
			preparedStatement.execute();
			SqlPrint.sqlTimeconsuming(author, startTime, sql);// 打印SQL和参数
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (!openTx && conn != null && !conn.isClosed()) {
				conn.close();
			}
		}
	}

	protected void executeSql(String indexName, String[] indexColumn,
			Object[] indexColumnValue, int dsKey, String author, String sql,
			Map<String, Object> parameterMap, ShardingHandle handle,
			boolean openTx) throws Exception {
		if (dsKey > 0) {
			this.executeSql(dsKey, author, sql, parameterMap, openTx);
		} else {
			ShardingIndexEntity shardingIndexEntity = dataSourceRoute
					.getShardingIndexEntity(indexName, indexColumn,
							indexColumnValue, dsKey, handle, sql);
			dsKey = shardingIndexEntity.getShard_id();
			this.executeSql(dsKey, author, sql, parameterMap, openTx);
		}
	}

	public SegmentEntity isSQLNeedSegment(String sql) throws Exception {
		return dataSourceRoute.isSQLNeedSegment(sql);
	}

	protected Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue, ShardingHandle handle,
			int singleDataSource) throws Exception {
		int dsKey = dataSourceRoute
				.getShardingIndexEntity(indexTableName, new Object[] {},
						indexColumnValue, singleDataSource, handle, "")
				.getShard_id();
		return UniqueIDFactory.getIdWorker(IdWorkerType.snowflake)
				.nextId(dsKey);
	}

	protected void openIndexCache() throws Exception {
		dataSourceRoute.openIndexCache();
	}

	protected void closeIndexCache() throws Exception {
		dataSourceRoute.closeIndexCache();
	}

	private Logger log = LogManager.getLogger(getClass());
	private final IdWorker idWorker = UniqueIDFactory
			.getIdWorker(IdWorkerType.snowflake);
	private AbstractDataSourceRoute dataSourceRoute = null;

	public void setDataSourceRoute(AbstractDataSourceRoute dataSourceRoute) {
		this.dataSourceRoute = dataSourceRoute;
	}

	interface FillStatementHandle {
		public void handle(String key) throws Exception;
	}

}
