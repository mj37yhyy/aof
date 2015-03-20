package autonavi.online.framework.sharding.dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.dao.constant.ReservedWord;
import autonavi.online.framework.sharding.index.SegmentEntity;
import autonavi.online.framework.sharding.index.ShardingHandle;
import autonavi.online.framework.util.StopWatchLogger;
import autonavi.online.framework.util.bean.PropertyUtils;

public abstract class AbstractDaoSupport {
	public Object execute(DaoEntity daoEntity, TableOperation tableOperation)
			throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("DaoSupport.execute");
		Object result = null;
		if (daoEntity.isIndexShard()) {// 如果指定了在索引片进行操作，则强制规定数据源
			daoEntity.setSingleDataSourceKey(DynamicDataSource.getIndexKey());
		}
		try {
			if (tableOperation == TableOperation.Ddl) {
				sqlHelper.executeSql(daoEntity.getSingleDataSourceKey(),
						daoEntity.getAuthor(), daoEntity.getSql(),
						daoEntity.getParameterMap(), daoEntity.isOpenTx());
				this.commit();
			} else if (tableOperation == TableOperation.Hbm) {
				hbmHelper.executeHbm(daoEntity.getSingleDataSourceKey(),
						daoEntity.getAuthor(), daoEntity.getSql(),
						daoEntity.getParameterMap());
				this.commit();
			} else if (tableOperation == TableOperation.Sql) {
				SegmentEntity sg = new SegmentEntity();
				Object[] indexColumnValue = getIndexColumnValue(daoEntity, sg);
				if (checkIsAofIndex(daoEntity)) {
					// 批量不支持 因为没有意义
					throw new RuntimeException("注解@Sql不支持批量索引为入参,请采用单一索引入参多次调用");
				} else {
					sqlHelper
							.executeSql(daoEntity.getIndexName(),
									daoEntity.getIndexColumn(),
									indexColumnValue,
									daoEntity.getSingleDataSourceKey(),
									daoEntity.getAuthor(), daoEntity.getSql(),
									daoEntity.getParameterMap(),
									daoEntity.getShardingHandle(),
									daoEntity.isOpenTx());
				}
				this.commit();
			} else {
				sqlHelper.checkSqlValid(daoEntity.getSql());// 校验SQL
				SegmentEntity sg = sqlHelper.isSQLNeedSegment(daoEntity
						.getSql());// 获取分表信息
				if (sg != null && sg.isSegement()
						&& daoEntity.getIndexName() == null
						&& daoEntity.getIndexColumn() == null
						&& daoEntity.getSingleDataSourceKey() > 0) {// 如果配了单一数据源且没有配分片且当前操作为单库分表的，报错
					String errorMessage = "由于该操作已涉及单库分表，在配置了单一数据源后还必须配置分片信息，因为框架要确定该操作是如何分表的。";
					if (log.isErrorEnabled())
						log.error(errorMessage);
					throw new Exception(errorMessage);
				}
				Object[] indexColumnValue = getIndexColumnValue(daoEntity, sg);// 获取索引字段的值
				if (tableOperation == TableOperation.Select) {// 如果是查询，返回查询结果
					int start = -1, limit = -1;// 得到起始行和末行

					if (daoEntity.getSql().indexOf(ReservedWord.index) > -1
							|| checkIsAofIndex(daoEntity)) {// 说明需要多片查询
						result = sqlHelper.selectBatch(daoEntity.getAuthor(),
								daoEntity.getIndexName(),
								daoEntity.getIndexColumn(),
								daoEntity.getSingleDataSourceKey(),
								daoEntity.getSql(),
								daoEntity.getParameterMap(), start, limit,
								daoEntity.isQueryCount(),
								daoEntity.getCollectionType(),
								daoEntity.getResultType(),
								daoEntity.getCallback(), sg,
								daoEntity.getShardingHandle());
					} else {// 单片查询
						if (daoEntity.getStartOrSkip() != null
								&& !daoEntity.getStartOrSkip().equals("")
								&& daoEntity.getEndOrRowSize() != null
								&& !daoEntity.getEndOrRowSize().equals("")) {
							// 说明需要分页
							try {
								start = (Integer) PropertyUtils.getValue(
										daoEntity.getParameterMap(),
										daoEntity.getStartOrSkip());
							} catch (ClassCastException cce) {
								String errorMessage = "转换分页参数startOrSkip时发生异常，请使用正确的类型：int";
								if (log.isErrorEnabled())
									log.error(errorMessage, cce);
								throw new ClassCastException(errorMessage);
							}
							try {
								limit = (Integer) PropertyUtils.getValue(
										daoEntity.getParameterMap(),
										daoEntity.getEndOrRowSize());
							} catch (ClassCastException cce) {
								String errorMessage = "转换分页参数endOrRowSize时发生异常，请使用正确的类型：int";
								if (log.isErrorEnabled())
									log.error(errorMessage, cce);
								throw new ClassCastException(errorMessage);
							}
						}
						result = sqlHelper.select(daoEntity.getAuthor(),
								daoEntity.getIndexName(),
								daoEntity.getIndexColumn(), indexColumnValue,
								daoEntity.getSingleDataSourceKey(),
								daoEntity.getSql(),
								daoEntity.getParameterMap(), start, limit,
								daoEntity.isQueryCount(),
								daoEntity.getCollectionType(),
								daoEntity.getResultType(),
								daoEntity.getCallback(), sg,
								daoEntity.getShardingHandle());
					}
					this.commit();// 提交
					return result;
				} else if (tableOperation == TableOperation.Update
						|| tableOperation == TableOperation.Insert
						|| tableOperation == TableOperation.Delete) {
					// 如果是插入、更新、删除，返回更新条数
					//sqlHelper.checkSqlValid(daoEntity.getSql());// 校验SQL
					int count = 0;
					int[] counts = null;
					// 注意 批量返回的数字有时候会不准 如insert和驱动 连接参数都有关系
					if (daoEntity.getSql().indexOf(ReservedWord.index) > -1
							|| checkIsAofIndex(daoEntity)) {// 说明需要循环处理
						counts = sqlHelper.updateBatch(daoEntity.getAuthor(),
								daoEntity.getIndexName(),
								daoEntity.getIndexColumn(),
								daoEntity.getSingleDataSourceKey(),
								daoEntity.getSql(),
								daoEntity.getParameterMap(), sg,
								daoEntity.getShardingHandle());
						this.commit();// 提交
						return counts;
					} else {// 单条执行
						count = sqlHelper.update(daoEntity.getAuthor(),
								daoEntity.getIndexName(),
								daoEntity.getIndexColumn(), indexColumnValue,
								daoEntity.getSingleDataSourceKey(),
								daoEntity.getSql(),
								daoEntity.getParameterMap(), sg,
								daoEntity.getShardingHandle());
						this.commit();// 提交
						return count;
					}
				}
			}

		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			this.rollback();// 回滚
			throw e;
		} finally {
			this.release();// 释放Holder资源
			swlogger.stop();
			swlogger.writeLog();// 打印耗时
		}
		return result;

	}

	private boolean checkIsAofIndex(DaoEntity daoEntity) {
		String[] indexColumn = daoEntity.getIndexColumn();
		if (indexColumn != null) {
			for (int i = 0; i < indexColumn.length; i++) {
				if (indexColumn[i].indexOf(ReservedWord.index) != -1) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取索引字段的值
	 * 
	 * @param daoEntity
	 * @return
	 * @throws Exception
	 */
	private Object[] getIndexColumnValue(DaoEntity daoEntity, SegmentEntity sg)
			throws Exception {
		// boolean needGet = false;
		Object[] indexColumnValue = null;
		String[] indexColumn = daoEntity.getIndexColumn();
		if (indexColumn != null) {
			indexColumnValue = new Object[indexColumn.length];
			List<ColumnAttribute> l = null;
			boolean needInit = true;

			for (int i = 0; i < indexColumn.length; i++) {
				if (indexColumn[i].indexOf(ReservedWord.index) == -1) {// 如果没有保留字"[AOF.index]"则进行获取，不然批处理循环时获取
					if (needInit) {
						l = sqlHelper.getIndexTable(daoEntity.getIndexName());
						needInit = false;
					}
					Object value = PropertyUtils.getValue(
							daoEntity.getParameterMap(), indexColumn[i]);
					if (value == null) {
						if (!(daoEntity.getSingleDataSourceKey() > 0)) {
							throw new RuntimeException("索引值没有获取到，索引表名称【"
									+ daoEntity.getIndexName() + "】，索引字段【"
									+ indexColumn[i] + "】");
						} else if (daoEntity.getSingleDataSourceKey() > 0
								&& sg.isSegement()) {
							throw new RuntimeException("索引值没有获取到，索引表名称【"
									+ daoEntity.getIndexName() + "】，索引字段【"
									+ indexColumn[i] + "】");
						}
					} else {
						indexColumnValue[i] = value;
					}

					if ((l == null || l.size() == 0 || l.size() != indexColumn.length)
							&& indexColumnValue[i] != null
							&& (!(daoEntity.getSingleDataSourceKey() > 0) || (daoEntity
									.getSingleDataSourceKey() > 0 && sg
									.isSegement()))) {
						// 如果索引表 找不到 且需要分区或者分表的时候
						if (l == null)
							throw new RuntimeException("索引表名称【"
									+ daoEntity.getIndexName() + "】没有找到");
						else
							throw new RuntimeException("索引表名称【"
									+ daoEntity.getIndexName() + "】分区字段数【"
									+ l.size() + "】与入参字段数【"
									+ indexColumn.length + "】不相等");

					} else if ((l != null && l.size() > 0 && l.size() == indexColumn.length)
							&& indexColumnValue[i] != null
							&& (!(daoEntity.getSingleDataSourceKey() > 0) || (daoEntity
									.getSingleDataSourceKey() > 0 && sg
									.isSegement()))) {
						// 找到索引表，需要校验入参信息
						sqlHelper.checkIndexValue(l.get(i),
								indexColumnValue[i], i);
					}

				}
			}
		}
		// if (needGet && indexColumnValue.length == 0) {
		// throw new RuntimeException("索引值没有获取到，索引名称【"
		// + daoEntity.getIndexName() + "】，索引字段【" + indexColumn + "】");
		// }
		return indexColumnValue;
	}

	protected void openIndexCache() {
		try {
			sqlHelper.openIndexCache();
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			throw new RuntimeException("持久化索引信息到缓存错误");
		}
	}

	protected void closeIndexCache() {
		try {
			sqlHelper.closeIndexCache();
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			throw new RuntimeException("从缓存释放索引信息错误");
		}
	}

	/**
	 * 提交事务
	 */
	protected abstract void commit() throws Exception;

	/**
	 * 回滚事务
	 */
	protected abstract void rollback() throws Exception;

	/**
	 * 释放资源
	 */
	protected abstract void release() throws Exception;

	/**
	 * 生成主键
	 * 
	 * @param indexTableName
	 * @param indexColumnValue
	 * @param handle
	 * @return
	 */
	protected Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue, ShardingHandle handle,
			int singleDataSource) throws Exception {
		return sqlHelper.createPrimaryKey(indexTableName, indexColumnValue,
				handle, singleDataSource);
	}

	private Logger log = LogManager.getLogger(this.getClass());
	protected SqlHelper sqlHelper = null;
	protected HbmHelper hbmHelper = null;

	public void setSqlHelper(SqlHelper sqlHelper) {
		this.sqlHelper = sqlHelper;
	}

	public void setHbmHelper(HbmHelper hbmHelper) {
		this.hbmHelper = hbmHelper;
	}

}
