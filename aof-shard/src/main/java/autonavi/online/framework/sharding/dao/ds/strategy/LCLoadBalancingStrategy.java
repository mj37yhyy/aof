package autonavi.online.framework.sharding.dao.ds.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autonavi.online.framework.sharding.dao.ds.ConnectCounter;
import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;

/**
 * Lease Connection （LC） 最少连接策略<br/>
 * 最小连接调度（Least-Connection Scheduling）算法是把新的连接请求分配到当前连接数最小的服务器
 * 。最小连接调度是一种动态调度算法，它通过服务器当前所活跃的连接数来估计服务器的负载情况
 * 。调度器需要记录各个服务器已建立连接的数目，当一个请求被调度到某台服务器，其连接数加1；当连接中止或超时，其连接数减一。<br/>
 * 最小连接调度算法流程<br/>
 * 
 * 假设有一组服务器S = {S0, S1, ..., Sn-1}，W(Si)表示服务器Si的权值， C(Si)表示服务器Si的当前连接数。<br/>
 * 
 * for (m = 0; m < n; m++) {<br/>
 * if (W(Sm) > 0) {<br/>
 * for (i = m+1; i < n; i++) {<br/>
 * if (W(Si) <= 0)<br/>
 * continue;<br/>
 * if (C(Si) < C(Sm))<br/>
 * m = i;<br/>
 * }<br/>
 * return Sm;<br/>
 * }<br/>
 * }<br/>
 * return NULL;<br/>
 * 
 * @author jia.miao
 * 
 */
@Strategy(value = "lc", description = "Lease Connection （LC） 最少连接策略")
public class LCLoadBalancingStrategy extends AbstractLoadBalancingStrategy {

	@Override
	public String balanc() {
		List<Object[]> countList = new ArrayList<Object[]>();
		for (final String dsName : super.dsNames) {
			countList.add(new Object[] { dsName,
					ConnectCounter.getCount(dsName) });
		}
		Collections.sort(countList, new Comparator<Object[]>() {

			@Override
			public int compare(Object[] o1, Object[] o2) {
				Object oo1 = o1[1];
				long lo1 = (Long) oo1;
				Object oo2 = o2[1];
				long lo2 = (Long) oo2;
				if (lo1 > lo2)
					return 1;
				else if (lo1 < lo2)
					return -1;
				else
					return 0;
			}
		});
		return countList.get(0)[0].toString();
	}
}
