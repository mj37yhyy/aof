package autonavi.online.framework.sharding.uniqueid;

import autonavi.online.framework.sharding.uniqueid.support.IdWorkerFromSnowflake;

public class UniqueIDHolder {
	private static IdWorker idWorker = new IdWorkerFromSnowflake();

	public static IdWorker getIdWorker() {
		return idWorker;
	}

	protected static void setIdWorker(IdWorker idWorker) {
		if (idWorker != null
				&& IdWorker.class.isAssignableFrom(idWorker.getClass()))
			UniqueIDHolder.idWorker = idWorker;
	}
}
