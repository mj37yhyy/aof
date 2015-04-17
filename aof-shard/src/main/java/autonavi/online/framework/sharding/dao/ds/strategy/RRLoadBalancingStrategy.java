package autonavi.online.framework.sharding.dao.ds.strategy;

import java.util.concurrent.locks.ReentrantLock;

import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;

/**
 * Round-robin（RR）轮询策略<br/>
 * 轮询调度（Round Robin Scheduling）算法就是以轮询的方式依次将请求调度不同的服务器，即每次调度执行i = (i + 1) mod
 * n，并选出第i台服务器。算法的优点是其简洁性，它无需记录当前所有连接的状态，所以它是一种无状态调度。 <br/>
 * 轮叫调度算法流程<br/>
 * 
 * 假设有一组服务器S = {S0, S1, …, Sn-1}，一个指示变量i表示上一次选择的
 * 服务器，W(Si)表示服务器Si的权值。变量i被初始化为n-1，其中n > 0。<br/>
 * 
 * j = i;<br/>
 * do {<br/>
 * j = (j + 1) mod n;<br/>
 * if (W(Sj) > 0) {<br/>
 * i = j;<br/>
 * return Si;<br/>
 * }<br/>
 * } while (j != i);<br/>
 * return NULL;<br/>
 * 
 * 
 * 
 * @author jia.miao
 * 
 */
@Strategy(value = "rr", description = "Round-robin（RR）轮询策略")
public class RRLoadBalancingStrategy extends AbstractLoadBalancingStrategy {
	public final ReentrantLock lock = new ReentrantLock();
	private String previous = null;

	@Override
	public String balanc() {
		try {
			lock.lock();
			if (previous == null) {
				previous = super.dsNames.get(0);
			} else {
				int index = super.dsNames.indexOf(previous);
				if (index == super.dsNames.size() - 1)
					index = -1;
				previous = super.dsNames.get(index + 1);
			}
		} finally {
			lock.unlock();
		}
		return previous;
	}
}
