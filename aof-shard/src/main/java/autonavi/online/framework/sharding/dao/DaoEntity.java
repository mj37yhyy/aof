package autonavi.online.framework.sharding.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import autonavi.online.framework.sharding.entry.entity.CollectionType;
import autonavi.online.framework.sharding.index.ShardingHandle;

public class DaoEntity {
	private Map<String, Object> parameterMap = new HashMap<String, Object>();// 存放所有可能的参数
	private String sql = null;// sql
	private String author = null;// 作者
	private boolean indexShard = false;// 如果为true，说明要在索引片进行查询
	private int singleDataSourceKey = -1;// 唯一key
	private ResultSetCallback<?> callback = null;// 返回类型
	private String indexName = null;// 索引表
	private String[] indexColumn = null;// 索引字段
	private String startOrSkip = null;// 起始行
	private String endOrRowSize = null;// 末行
	CollectionType collectionType = null;// 返回的集合类型
	/*
	 * 字段类型，默认为Object。该字段只对bean, beanList, column, columnList四个集合类型有效，其它的array,
	 * arrayList, map, mapList四个类型使用数据库元数据
	 */
	private Class<?> resultType = null;
	private boolean queryCount = false;// 是否查询行数
	private ShardingHandle shardingHandle;// 分片分表Handle
	private boolean openTx = false;// @SQL @DDL时候是否开启事务

	public boolean isOpenTx() {
		return openTx;
	}

	public void setOpenTx(boolean openTx) {
		this.openTx = openTx;
	}

	public ShardingHandle getShardingHandle() {
		return shardingHandle;
	}

	public void setShardingHandle(ShardingHandle handle) {
		this.shardingHandle = handle;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isIndexShard() {
		return indexShard;
	}

	public void setIndexShard(boolean indexShard) {
		this.indexShard = indexShard;
	}

	public int getSingleDataSourceKey() {
		return singleDataSourceKey;
	}

	public void setSingleDataSourceKey(int singleDataSourceKey) {
		this.singleDataSourceKey = singleDataSourceKey;
	}

	public ResultSetCallback<?> getCallback() {
		return callback;
	}

	public void setCallback(ResultSetCallback<?> callback) {
		this.callback = callback;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String[] getIndexColumn() {
		return indexColumn;
	}

	public void setIndexColumn(String[] indexColumn) {
		this.indexColumn = indexColumn;
	}

	public String getStartOrSkip() {
		return startOrSkip;
	}

	public void setStartOrSkip(String startOrSkip) {
		this.startOrSkip = startOrSkip;
	}

	public String getEndOrRowSize() {
		return endOrRowSize;
	}

	public void setEndOrRowSize(String endOrRowSize) {
		this.endOrRowSize = endOrRowSize;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	public Class<?> getResultType() {
		return resultType;
	}

	public void setResultType(Class<?> resultType) {
		this.resultType = resultType;
	}

	public boolean isQueryCount() {
		return queryCount;
	}

	public void setQueryCount(boolean queryCount) {
		this.queryCount = queryCount;
	}

	@Override
	public String toString() {
		return "DaoEntity [parameterMap=" + parameterMap + ", sql=" + sql
				+ ", author=" + author + ", indexShard=" + indexShard
				+ ", singleDataSourceKey=" + singleDataSourceKey
				+ ", callback=" + callback + ", indexName=" + indexName
				+ ", indexColumn=" + Arrays.toString(indexColumn)
				+ ", startOrSkip=" + startOrSkip + ", endOrRowSize="
				+ endOrRowSize + ", collectionType=" + collectionType
				+ ", resultType=" + resultType + ", queryCount=" + queryCount
				+ ", shardingHandle=" + shardingHandle + ", openTx=" + openTx
				+ "]";
	}

}
