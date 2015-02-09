package autonavi.online.framework.cc;

/**
 * 该类包含一些重要但又不成体系的属性
 * 
 * @author jia.miao
 * 
 */
public class Miscellaneous {
	static public String aof_messageListener_localhost = null;// 消息监听时的本地地址
	static public int aof_messageListener_port = -1;// 消息监听时的端口号
	static public String aof_messageListener_netInterfaceName = null;// 消息监听时的netInterfaceName
	static public boolean show_sql = false;// 是否打印SQL
	static public long max_log_length = 1000;// 单条日志的最大打印长度（针对SQL和参数）
	static public final int minDsKey = 1;// dskey的最小值
	static public final int maxDsKey = 32;// dskey的最大值

	static public final int minNodeKey = 1;// 节点的最小值
	static public final int maxNodeKey = 32;// 节点的最大值

	static private int myid = 0;//

	public static int getNodeIndex() {
		return myid;
	}

	public static boolean setMyid(int myid) {
		if (myid >= minNodeKey && myid <= maxNodeKey) {
			Miscellaneous.myid = myid;
			return true;
		}
		return false;
	}

	public String getAof_messageListener_localhost() {
		return aof_messageListener_localhost;
	}

	public void setAof_messageListener_localhost(
			String aof_messageListener_localhost) {
		Miscellaneous.aof_messageListener_localhost = aof_messageListener_localhost;
	}

	public int getAof_messageListener_port() {
		return aof_messageListener_port;
	}

	public void setAof_messageListener_port(int aof_messageListener_port) {
		Miscellaneous.aof_messageListener_port = aof_messageListener_port;
	}

	public String getAof_messageListener_netInterfaceName() {
		return aof_messageListener_netInterfaceName;
	}

	public void setAof_messageListener_netInterfaceName(
			String aof_messageListener_netInterfaceName) {
		Miscellaneous.aof_messageListener_netInterfaceName = aof_messageListener_netInterfaceName;
	}

	public boolean isShow_sql() {
		return show_sql;
	}

	public void setShow_sql(boolean show_sql) {
		Miscellaneous.show_sql = show_sql;
	}

	public long getMax_log_length() {
		return max_log_length;
	}

	public void setMax_log_length(long max_log_length) {
		Miscellaneous.max_log_length = max_log_length;
	}

	public static int getMindskey() {
		return minDsKey;
	}

	public static int getMaxdskey() {
		return maxDsKey;
	}

	/**
	 * 判断dskey是否在区间范围内
	 * 
	 * @param dsKey
	 * @return
	 */
	public static boolean isInDsKeyInterval(int dsKey) {
		if (dsKey >= minDsKey && dsKey <= maxDsKey)
			return true;
		return false;
	}
}
