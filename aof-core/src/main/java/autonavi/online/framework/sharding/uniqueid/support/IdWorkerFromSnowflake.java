package autonavi.online.framework.sharding.uniqueid.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cc.Miscellaneous;
import autonavi.online.framework.sharding.uniqueid.IdWorker;

public class IdWorkerFromSnowflake implements IdWorker {
	private Logger log = LogManager.getLogger(this.getClass());
	private long workerId;
	private long datacenterId;
	private long sequence = 0L;
	private long twepoch = 1288834974657L;
	private long workerIdBits = 5L;
	private long datacenterIdBits = 5L;
	private long maxWorkerId = -1L ^ (-1L << workerIdBits);
	private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
	private long sequenceBits = 12L;
	private long workerIdShift = sequenceBits;
	private long datacenterIdShift = sequenceBits + workerIdBits;
	private long timestampLeftShift = sequenceBits + workerIdBits
			+ datacenterIdBits;
	private long sequenceMask = -1L ^ (-1L << sequenceBits);
	private long lastTimestamp = -1L;

	/*
	 * public IdWorkerFromSnowflake(long workerId, long datacenterId) { //
	 * sanity check for workerId if (workerId > maxWorkerId || workerId < 0) {
	 * throw new IllegalArgumentException(String.format(
	 * "worker Id can't be greater than %d or less than 0", maxWorkerId)); } if
	 * (datacenterId > maxDatacenterId || datacenterId < 0) { throw new
	 * IllegalArgumentException(String.format(
	 * "datacenter Id can't be greater than %d or less than 0",
	 * maxDatacenterId)); } this.workerId = workerId; this.datacenterId =
	 * datacenterId; log.info(String .format(
	 * "worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d"
	 * , timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits,
	 * workerId)); }
	 */

	protected synchronized long nextId() {
		long timestamp = timeGen();
		if (timestamp < lastTimestamp) {
			log.error(String.format(
					"clock is moving backwards.  Rejecting requests until %d.",
					lastTimestamp));
			throw new RuntimeException(
					String.format(
							"Clock moved backwards.  Refusing to generate id for %d milliseconds",
							lastTimestamp - timestamp));
		}

		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}
		lastTimestamp = timestamp;
		return ((timestamp - twepoch) << timestampLeftShift)
				| (datacenterId << datacenterIdShift)
				| (workerId << workerIdShift) | sequence;
	}

	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	protected long timeGen() {
		return System.currentTimeMillis();
	}

	@Override
	public synchronized long nextId(long workerId, long datacenterId) {
		// sanity check for workerId
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format(
					"worker Id can't be greater than %d or less than 0",
					maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(String.format(
					"datacenter Id can't be greater than %d or less than 0",
					maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		log.info(String
				.format("worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
						timestampLeftShift, datacenterIdBits, workerIdBits,
						sequenceBits, workerId));

		return this.nextId();
	}

	@Override
	public long nextId(long dataId) {
		// return this.nextId((long) Math
		// .ceil(dataId % 32 > 0 ? dataId % 32 - 1 : 0),// 计算workerId，范围从0到31
		// (long) Math.ceil(dataId / 32 > 0 ? dataId / 32 - 1
		// : 0)// 计算datacenterId，范围从0到31
		// );
		/*
		 * 由于某些同学提出的问题
		 * 使用纯数据源的主键生成方式可能会有重复 尽管概率非常的低
		 * 目前改为纯采用雪花的原生算法计算支持32台主机乘以32台数据库的支持
		 */
		return this.nextId((long) Miscellaneous.getNodeIndex() - 1,
				(long) dataId - 1);
	}
}
