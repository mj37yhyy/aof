package autonavi.online.framework.sharding.dao;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import autonavi.online.framework.sharding.index.ShardingHandle;
import autonavi.online.framework.sharding.index.ShardingHandleSupport;
import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.UniqueIDHolder;

public class DaoHelper {

	private static final ThreadLocal<Long> countHolder = new ThreadLocal<Long>();
	private static final ThreadLocal<Long> primaryKeyHolder = new ThreadLocal<Long>();
	private static final ThreadLocal<Long[]> primaryKeysHolder = new ThreadLocal<Long[]>();
	private static final IdWorker idWorker = UniqueIDHolder
			.getIdWorker();
	private static final Random random = new Random();

	public static Long getCount() {
		return countHolder.get();
	}
	public static void  openCache(){
		daoSupport
		.openIndexCache();
	}
	
    public static void  closeCache(){
    	daoSupport
		.closeIndexCache();
	}

	/**
	 * 生成主键
	 * 
	 * @param indexTableName
	 * @param indexColumnValue
	 * @param handle
	 * @return
	 * @throws Exception
	 */
	public static Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue, ShardingHandle handle,
			int singleDateSource) throws Exception {
		if (handle == null) {
			handle = new ShardingHandleSupport();
		}
		return daoSupport
				.createPrimaryKey(indexTableName, indexColumnValue, handle,
						singleDateSource);
	}

	public static Long createPrimaryKey() throws Exception {
		return idWorker.nextId(32);
	}

	public static Long createRandomPrimaryKey() throws Exception {
		return idWorker.nextId(random.nextInt(1024) + 1);
	}

	@Deprecated
	public static Long createPrimaryKey(long dataId) throws Exception {
		return idWorker.nextId(dataId);
	}

	@Deprecated
	public static Long createPrimaryKey(long workerId, long datacenterId)
			throws Exception {
		return idWorker.nextId(workerId, datacenterId);
	}

	/**
	 * 生成主键
	 * 
	 * @param indexTableName
	 * @param indexColumnValue
	 * @return
	 * @throws Exception
	 */
	public static Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue) throws Exception {
		return createPrimaryKey(indexTableName, indexColumnValue, null, -1);
	}

	/**
	 * 生成主键
	 * 
	 * @param indexTableName
	 * @param indexColumnValue
	 * @return
	 * @throws Exception
	 */
	public static Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue, int singleDataSource) throws Exception {
		return createPrimaryKey(indexTableName, indexColumnValue, null,
				singleDataSource);
	}

	/**
	 * 生成主键
	 * 
	 * @param indexTableName
	 * @param indexColumnValue
	 * @return
	 * @throws Exception
	 */
	public static Long createPrimaryKey(String indexTableName,
			Object[] indexColumnValue, ShardingHandle handle) throws Exception {
		return createPrimaryKey(indexTableName, indexColumnValue, handle, -1);
	}

	protected static void setCount(Long count) {
		countHolder.set(count);
	}

	public static Long getPrimaryKey() {
		return primaryKeyHolder.get();
	}

	protected static void setPrimaryKey(Long primaryKey) {
		primaryKeyHolder.set(primaryKey);
	}

	public static Long[] getPrimaryKeys() {
		return primaryKeysHolder.get();
	}

	protected static void setPrimaryKeys(Long[] primaryKeys) {
		primaryKeysHolder.set(primaryKeys);
	}

	/**
	 * 获取所有数据源的ID
	 * 
	 * @return
	 */
	public static Set<Integer> getAllDSKey() {
		CopyOnWriteArraySet<Integer> newSet = new CopyOnWriteArraySet<Integer>();
		newSet.addAll(DynamicDataSource.getKeySet());
		return newSet;
	}
	private static AbstractDaoSupport daoSupport;

	public AbstractDaoSupport getDaoSupport() {
		return daoSupport;
	}
	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		DaoHelper.daoSupport = daoSupport;
	}
	
	

}
