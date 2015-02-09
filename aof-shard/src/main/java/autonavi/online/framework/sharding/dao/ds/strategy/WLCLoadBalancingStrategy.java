package autonavi.online.framework.sharding.dao.ds.strategy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import autonavi.online.framework.sharding.dao.ds.ConnectCounter;
import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;
import autonavi.online.framework.util.http.HttpUtils;

/**
 * Weight Least-Connection（WLC） 加权最少连接策略<br/>
 * 加权最小连接调度（Weighted Least-Connection
 * Scheduling）算法是最小连接调度的超集，各个服务器用相应的权值表示其处理性能。服务器的缺省权值为1，系统管理员可以动态地设置服务器的权值。
 * 加权最小连接调度在调度新连接时尽可能使服务器的已建立连接数和其权值成比例。<br/>
 * 加权最小连接调度的算法流程<br/>
 * 
 * 假设有一组服务器S = {S0, S1, ..., Sn-1}，W(Si)表示服务器Si的权值，
 * C(Si)表示服务器Si的当前连接数。所有服务器当前连接数的总和为 CSUM = ΣC(Si) (i=0, 1, .. ,
 * n-1)。当前的新连接请求会被发送服务器Sm， 当且仅当服务器Sm满足以下条件 (C(Sm) / CSUM)/ W(Sm) = min { (C(Si)
 * / CSUM) / W(Si)} (i=0, 1, . , n-1) 其中W(Si)不为零 因为CSUM在这一轮查找中是个常数，所以判断条件可以简化为
 * C(Sm) / W(Sm) = min { C(Si) / W(Si)} (i=0, 1, . , n-1) 其中W(Si)不为零。<br/>
 * 
 * 因为除法所需的CPU周期比乘法多，且在Linux内核中不允许浮点除法，服务器的 权值都大于零，所以判断条件C(Sm) / W(Sm) > C(Si) /
 * W(Si) 可以进一步优化 为C(Sm)*W(Si) > C(Si)* W(Sm)。同时保证服务器的权值为零时，服务器不被调
 * 度。所以，算法只要执行以下流程。<br/>
 * 
 * for (m = 0; m < n; m++) {<br/>
 * if (W(Sm) > 0) {<br/>
 * for (i = m+1; i < n; i++) {<br/>
 * if (C(Sm)*W(Si) > C(Si)*W(Sm))<br/>
 * m = i;<br/>
 * }<br/>
 * return Sm;<br/>
 * }<br/>
 * }<br/>
 * return NULL;<br/>
 * 
 * 
 * 
 * @author jia.miao
 * 
 */
@Strategy(value = "wlc", description = "Weight Least-Connection（WLC） 加权最少连接策略")
public class WLCLoadBalancingStrategy extends AbstractLoadBalancingStrategy {

	/**
	 * 重写父类方法。将字符串分割成名字+属性列表
	 */
	public void init(List<String> dsNames) {
		super.init(dsNames);
		for (String dsName : dsNames) {
			String[] _dsNameArray = dsName.split("\\?");
			if (_dsNameArray != null && _dsNameArray.length == 2) {
				dsNameList.add(_dsNameArray[0]);
				Hashtable<String, String[]> queryParams = HttpUtils
						.parseQueryString(_dsNameArray[1]);
				String[] weightArray = queryParams.get("weight");
				if (weightArray != null && weightArray.length == 1) {
					int weight = Integer.valueOf(weightArray[0]);
					this.weightList.add(weight);
				}
			}
		}
	}

	@Override
	public String balanc() {
		try {
			this.lock.lock();
			for (int m = 0; m < this.dsNameList.size(); m++) {
				if (this.weightList.get(m) > 0) {
					for (int i = m + 1; i < this.dsNameList.size(); i++) {
						if (ConnectCounter.getCount(this.dsNameList.get(m))
								* this.weightList.get(i) > ConnectCounter
								.getCount(this.dsNameList.get(i))
								* this.weightList.get(m))
							m = i;
					}
					return this.dsNameList.get(m);
				}
			}
			return null;
		} finally {
			this.lock.unlock();
		}
	}

	private final ReentrantLock lock = new ReentrantLock();
	private List<String> dsNameList = new ArrayList<String>();
	private List<Integer> weightList = new ArrayList<Integer>();

	public static void main(String[] args) {
		WLCLoadBalancingStrategy wlc = new WLCLoadBalancingStrategy();
		wlc.init(new ArrayList() {
			{
				add("c3p01?weight=10");
				add("c3p02?weight=7");
				add("c3p03?weight=3");
			}
		});
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");
		ConnectCounter.counterIncrement("c3p01");

		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");
		ConnectCounter.counterIncrement("c3p02");

		ConnectCounter.counterIncrement("c3p03");
		ConnectCounter.counterIncrement("c3p03");
		ConnectCounter.counterIncrement("c3p03");
		ConnectCounter.counterIncrement("c3p03");

		for (int i = 0; i < 100; i++)
			System.out.println(wlc.balanc());
	}
}
