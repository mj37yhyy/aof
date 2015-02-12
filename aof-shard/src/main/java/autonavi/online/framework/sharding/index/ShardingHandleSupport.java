package autonavi.online.framework.sharding.index;

import autonavi.online.framework.sharding.dao.DynamicDataSource;

public class ShardingHandleSupport implements ShardingHandle {
	private static long lastTimestamp;

	@Override
	public Integer handleDataSource(ShardingEntity entity) {
		return DynamicDataSource.getOrderKey();
	}

	/**
	 * 如果ID是一秒钟内生成的，则直接使用ID取余；否则，将ID右移22位，再进行取余<br/>
	 * 该方法只适合Snowflake生成的ID
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
