package autonavi.online.framework.sharding.index;


/**
 * 分片方法接口
 * @author Xuyaming-iMac
 *
 */
public interface ShardingHandle {
	/**
	 * 处理分片
	 * @param dsKey
	 * @param objs
	 * @return
	 */
	public Integer handleDataSource(ShardingEntity entity);
	/**
	 * 处理分表
	 * @param snowFlowerId
	 * @param objs
	 * @return
	 */
	public Integer handleSegment(ShardingEntity entity);

}
