package autonavi.online.framework.sharding.dao.ds.strategy;

import java.util.List;
import java.util.Set;

/**
 * 负载均衡策略
 * 
 * @author jia.miao
 * 
 */
public interface LoadBalancingStrategy {
	public void init(List<String> dsNames);
	
	/**
	 * 通过分片ID获取数据源的名字
	 * 
	 * @param shardingKey
	 * @return
	 */
	public String balanc();
}
