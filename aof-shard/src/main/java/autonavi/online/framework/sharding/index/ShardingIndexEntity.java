package autonavi.online.framework.sharding.index;

import java.io.Serializable;

public class ShardingIndexEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -255352558031467410L;
	private long id = 0L;
	private int shard_id = 0;
	private long migration_lock = 0L;
	private long old_shard_id = 0L;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getShard_id() {
		return shard_id;
	}

	public void setShard_id(int shard_id) {
		this.shard_id = shard_id;
	}

	public long getMigration_lock() {
		return migration_lock;
	}

	public void setMigration_lock(long migration_lock) {
		this.migration_lock = migration_lock;
	}

	public long getOld_shard_id() {
		return old_shard_id;
	}

	public void setOld_shard_id(long old_shard_id) {
		this.old_shard_id = old_shard_id;
	}

}
