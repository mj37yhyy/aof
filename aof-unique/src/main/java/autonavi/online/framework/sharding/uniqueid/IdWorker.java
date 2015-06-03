package autonavi.online.framework.sharding.uniqueid;

public interface IdWorker {
	public long nextId(long workerId, long datacenterId);
	public long nextId(long dataId);
}
