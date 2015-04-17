package autonavi.online.framework.support.zookeeper.holder;

import java.util.List;

import org.apache.zookeeper.Transaction;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

public class ZooKeeperHolder {
	public final static ThreadLocal<Transaction> transaction=new ThreadLocal<Transaction>();
	public final static ThreadLocal<Boolean> biz=new ThreadLocal<Boolean>();
	public final static ThreadLocal<Boolean> use=new ThreadLocal<Boolean>();
	
	public final static ThreadLocal<String> appName=new ThreadLocal<String>();
	public final static ThreadLocal<String> passwd=new ThreadLocal<String>();
	public final static ThreadLocal<String> deleteRoot=new ThreadLocal<String>();
	public final static ThreadLocal<Boolean> commit=new ThreadLocal<Boolean>();
	public final static ThreadLocal<ZooKeeper> zooKeeper=new ThreadLocal<ZooKeeper>();
	public final static ThreadLocal<List<ACL>> acls=new ThreadLocal<List<ACL>>();
	static{
		use.set(true);
		biz.set(true);
	}
	public  static void clearHolder(){
		transaction.remove();
		appName.remove();
		passwd.remove();
		deleteRoot.remove();
		commit.remove();
		zooKeeper.remove();
		acls.remove();
		use.set(true);
		biz.set(true);
	}

}
