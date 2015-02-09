package autonavi.online.framework.configcenter.util;

import org.apache.zookeeper.Transaction;
import org.apache.zookeeper.ZooKeeper;

public class AuthHolder {
	public final static ThreadLocal<String> appName=new ThreadLocal<String>();
	public final static ThreadLocal<String> passwd=new ThreadLocal<String>();
	public final static ThreadLocal<Transaction> transaction=new ThreadLocal<Transaction>();
	public final static ThreadLocal<ZooKeeper> zooKeeper=new ThreadLocal<ZooKeeper>();
	public final static ThreadLocal<String> deleteRoot=new ThreadLocal<String>();
	public final static ThreadLocal<Boolean> commit=new ThreadLocal<Boolean>();
	

}
