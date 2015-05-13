package autonavi.online.framework.sharding.dao.ds.strategy;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import autonavi.online.framework.sharding.dao.ds.strategy.annotation.Strategy;
import autonavi.online.framework.util.http.HttpUtils;

/**
 * Weighted round-robin（WRR）加权轮询策略<br/>
 * 假设有一组服务器S = {S0, S1, …, Sn-1}，W(Si)表示服务器Si的权值，一个
 * 指示变量i表示上一次选择的服务器，指示变量cw表示当前调度的权值，max(S)
 * 表示集合S中所有服务器的最大权值，gcd(S)表示集合S中所有服务器权值的最大 公约数。变量i初始化为-1，cw初始化为零。<br/>
 * 
 * while (true) {<br/>
 * i = (i + 1) mod n;<br/>
 * if (i == 0) {<br/>
 * cw = cw - gcd(S); <br/>
 * if (cw <= 0) {<br/>
 * cw = max(S);<br/>
 * if (cw == 0)<br/>
 * return NULL;<br/>
 * }<br/>
 * } <br/>
 * if (W(Si) >= cw) <br/>
 * return Si;<br/>
 * }<br/>
 * 
 * 
 * @author jia.miao
 * 
 */
@Strategy(value = "wrr", description = "Weighted round-robin（WRR）加权轮询策略")
public class WRRLoadBalancingStrategy extends AbstractLoadBalancingStrategy {

	/**
	 * 重写父类方法。将字符串分割成名字+属性列表
	 */
	public void init(List<String> dsNames) {
		super.init(dsNames);
		for (String dsName : dsNames) {
			String[] _dsNameArray = dsName.split("\\?");
			if (_dsNameArray != null && _dsNameArray.length == 2) {
				this.dsNameList.add(_dsNameArray[0]);
				Hashtable<String, String[]> queryParams = HttpUtils
						.parseQueryString(_dsNameArray[1]);
				String[] weightArray = queryParams.get("weight");
				if (weightArray != null && weightArray.length == 1) {
					int weight = Integer.valueOf(weightArray[0]);
					this.weightList.add(weight);
				}
			}
		}
		this.currentIndex = -1;
		this.currentWeight = 0;
		this.serverCount = dsNames.size();
		this.maxWeight = this.getMaxWeight(this.weightList);
		this.gcdWeight = this.getAllGcd(this.weightList);
	}

	@Override
	public String balanc() {
		try {
			this.lock.lock();
			/**
			 * 算法流程： 假设有一组服务 S = {S0, S1, …, Sn-1}
			 * 有相应的权重，变量currentIndex表示上次选择的服务
			 * 权值currentWeight初始化为0，currentIndex初始化为-1 ，当第一次的时候返回 权值取最大的那个服务，
			 * 通过权重的不断递减 寻找 适合的服务返回，直到轮询结束，权值返回为0
			 */
			while (true) {
				this.currentIndex = (this.currentIndex + 1) % this.serverCount;
				if (this.currentIndex == 0) {
					this.currentWeight = this.currentWeight - this.gcdWeight;
					if (this.currentWeight <= 0) {
						this.currentWeight = this.maxWeight;
						if (this.currentWeight == 0)
							return null;
					}
				}
				if (this.weightList.get(this.currentIndex) >= this.currentWeight) {
					return this.dsNameList.get(this.currentIndex);
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * 返回最大公约数
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private int gcd(int a, int b) {
		BigInteger b1 = new BigInteger(String.valueOf(a));
		BigInteger b2 = new BigInteger(String.valueOf(b));
		BigInteger gcd = b1.gcd(b2);
		return gcd.intValue();
	}

	/**
	 * 返回所有服务权重的最大公约数
	 * 
	 * @param serverList
	 * @return
	 */
	private int getAllGcd(List<Integer> weightList) {
		int w = 0;
		for (int i = 0, len = weightList.size(); i < len - 1; i++) {
			if (w == 0) {
				w = gcd(weightList.get(i), weightList.get(i + 1));
			} else {
				w = gcd(w, weightList.get(i + 1));
			}
		}
		return w;
	}

	/**
	 * 返回所有服务中的最大权重
	 * 
	 * @param serverList
	 * @return
	 */
	private int getMaxWeight(List<Integer> weightList) {
		int w = 0;
		for (int i = 0, len = weightList.size(); i < len - 1; i++) {
			if (w == 0) {
				w = Math.max(weightList.get(i), weightList.get(i + 1));
			} else {
				w = Math.max(w, weightList.get(i + 1));
			}
		}
		return w;
	}

	private final ReentrantLock lock = new ReentrantLock();
	private int currentIndex = -1;// 上一次选择的服务
	private int currentWeight = 0;// 当前调度的权值
	private int maxWeight = 0; // 最大权重
	private int gcdWeight = 0; // 所有服务权重的最大公约数
	private int serverCount = 0; // 服务数量
	private List<String> dsNameList = new ArrayList<String>();
	private List<Integer> weightList = new ArrayList<Integer>();
}
