package autonavi.online.framework.sharding.transaction;

public interface ShardingTransactionCallback {

	public Object doInTransaction() throws Throwable;

}
