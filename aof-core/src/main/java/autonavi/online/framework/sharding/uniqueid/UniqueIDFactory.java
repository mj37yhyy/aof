package autonavi.online.framework.sharding.uniqueid;

import autonavi.online.framework.sharding.uniqueid.support.IdWorkerFromSnowflake;

public class UniqueIDFactory {

	public static IdWorker getIdWorker(IdWorkerType idWorkerType) {
		if (idWorkerType.equals(IdWorkerType.snowflake)) {
			return new IdWorkerFromSnowflake();
		}
		if (idWorkerType.equals(IdWorkerType.redis)) {
			return null;
		}
		return null;
	}
}
