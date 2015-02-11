package autonavi.online.framework.sharding.index;

import java.util.Random;

import autonavi.online.framework.sharding.dao.DynamicDataSource;

public class ShardingHandleSupport implements ShardingHandle {

	@Override
	public Integer handleDataSource(ShardingEntity entity) {
		return DynamicDataSource.getOrderKey();
	}

	@Override
	public Integer handleSegment(ShardingEntity entity) {
		return new Random().nextInt(entity.getTableCount()) + 1;
		// return (int) (entity.getSegemntId() % entity.getTableCount()) + 1;
	}
}
