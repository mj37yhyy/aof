package autonavi.online.framework.sharding.index;

import autonavi.online.framework.sharding.dao.DynamicDataSource;

public class ShardingHandleSupport implements ShardingHandle {
	private static long lastTimestamp;

	/**
	 * <p>
	 * 处理索引字段值
	 * </p>
	 * <p>
	 * 该方法的主要用途为：当使用ID等数据量巨大的片键进行分片时，可能会使索引表也变得很大。为避免这一情况，可以使用该方法将片键进行分组。
	 * </p>
	 * <p>
	 * 比如：一共有1亿个ID，我们可以把他们分为100个片，所以当ID为1-100万时，应当落在片1上，但如果不修改这个值，
	 * 会导致100万个片键信息被存入索引表
	 * 。因此，可以把ID在1-100万范围的直接改为100万。那么，100万以内的ID就只有一个片键信息。这样就大大减少了索引库的存储量
	 * 。当然，使用者也可以不使用该接口而手动进行修改。
	 * </p>
	 * 
	 * @param indexColumnValue
	 * @return 处理过的索引字段值
	 */
	@Override
	public Object[] handleIndexColumnValue(Object[] indexColumnValue) {
		return indexColumnValue;
	}

	/**
	 * 处理分片。使用者可以自定义分片的算法
	 * 
	 * @param entity
	 * @return 分片ID
	 */
	@Override
	public Integer handleDataSource(ShardingEntity entity) {
		return DynamicDataSource.getOrderKey();
	}

	/**
	 * <p>
	 * 处理分表。使用者可以自定义分表的算法
	 * </p>
	 * <p>
	 * 如果ID是一秒钟内生成的，则直接使用ID取余；否则，将ID右移22位，再进行取余
	 * </p>
	 * <p>
	 * <span style="color:#F00;">注意！该方法只适合Snowflake生成的ID</span>
	 * </p>
	 * 
	 * @param entity
	 * @return 分表ID
	 */
	@Override
	public Integer handleSegment(ShardingEntity entity) {
		int dsKey = 0;
		long timestamp = System.currentTimeMillis();
		if (timestamp == lastTimestamp) {
			dsKey = (int) (entity.getSegemntId() % entity.getTableCount()) + 1;
		} else {
			dsKey = (int) ((entity.getSegemntId() >> 22) % entity
					.getTableCount()) + 1;
		}
		lastTimestamp = timestamp;
		return dsKey;
	}

}
