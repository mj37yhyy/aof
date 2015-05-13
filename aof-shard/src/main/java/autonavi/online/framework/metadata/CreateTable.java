package autonavi.online.framework.metadata;

import java.util.Map;

import autonavi.online.framework.metadata.entity.FormTable;

public interface CreateTable {

	/**
	 * 在索引库上创建表
	 * 
	 * @param meta
	 */
	public FormTable createTable2IndexShard() throws Exception;

	/**
	 * 在指定的库上创建表
	 * 
	 * @param meta
	 */
	public Map<Integer, FormTable> createTable2AppointShard() throws Exception;

	/**
	 * 在所有的库上创建表
	 * 
	 * @param meta
	 */
	public FormTable createTable2AllShard() throws Exception;

}
