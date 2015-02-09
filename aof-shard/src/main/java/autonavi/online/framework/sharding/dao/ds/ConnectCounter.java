package autonavi.online.framework.sharding.dao.ds;

import java.util.HashMap;
import java.util.Map;

import autonavi.online.framework.util.AtomicCounter;

/**
 * 连接记数器
 * 
 * @author jia.miao
 * 
 */
public class ConnectCounter {

	/**
	 * 获取当前的计数
	 * 
	 * @param dsName
	 * @return
	 */
	public static long getCount(String dsName) {
		if (!isFromAllowedClass())
			return -1;
		before(dsName);
		return counter.get(dsName).getCount();
	}

	/**
	 * 计数算加1
	 * 
	 * @param dsName
	 */
	public static void counterIncrement(String dsName) {
		if (!isFromAllowedClass())
			return;
		before(dsName);
		counter.get(dsName).counterIncrement();
	}

	/**
	 * 计数算减1
	 * 
	 * @param dsName
	 */
	public static void counterDecrease(String dsName) {
		if (!isFromAllowedClass())
			return;
		before(dsName);
		counter.get(dsName).counterDecrease();
	}

	/**
	 * 初始化
	 * 
	 * @param dsName
	 */
	private static void before(String dsName) {
		if (!counter.containsKey(dsName))
			counter.put(dsName, new AtomicCounter());
	}

	/**
	 * 仅允许本包及子包的类进行调用
	 * 
	 * @return
	 */
	private static boolean isFromAllowedClass() {
		return new Exception().getStackTrace()[2].getClassName().startsWith(
				className.substring(0, className.lastIndexOf(".")));
	}

	private static final String className = ConnectCounter.class.getName();
	private static final Map<String, AtomicCounter> counter = new HashMap<String, AtomicCounter>();
}
