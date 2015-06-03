package autonavi.online.framework.test.unique;

import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.UniqueIDHolder;

/**
 * 该类展示了两种主键的生成方式
 * 
 * @author jia.miao
 *
 */
public class Unique {

	/**
	 * 使用雪花
	 */
	public void BySonwflake() {
		IdWorker worker = UniqueIDHolder.getIdWorker();
		worker.nextId(1, 1);
	}
}
