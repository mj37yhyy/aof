package autonavi.online.framework.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZkUtils {
	static private ZkUtils static_;
	private ZooKeeper zk_ = null;

	private ZkUtils() {

	}

	/**
	 * 使用CountDownLatch卡住主线程，等带Watcher调用countDown()后主线程可继续运行
	 * 
	 * @param zooKeeper
	 * @param connectedLatch
	 */
	public void waitUntilConnected(ZooKeeper zooKeeper,
			CountDownLatch connectedLatch) {
		if (States.CONNECTING == zooKeeper.getState()) {
			try {
				connectedLatch.await();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Watcher实现。连接后，zookeeper回调接口，计数器减1，主线程恢复执行
	 * 
	 * @author jia.miao
	 * 
	 */
	class ConnectedWatcher implements Watcher {

		private CountDownLatch connectedLatch;
		private Watcher myWatcher;

		ConnectedWatcher(CountDownLatch connectedLatch, Watcher myWatcher) {
			this.connectedLatch = connectedLatch;
			this.myWatcher = myWatcher;
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				connectedLatch.countDown();
			}
			if(myWatcher!=null)
			myWatcher.process(event);
		}
	}

	/**
	 * 单例
	 * 
	 * @return
	 */
	static public ZkUtils Instance() {
		if (static_ == null) {
			static_ = new ZkUtils();
		}
		return static_;
	}

	/**
	 * 初始化
	 * 
	 * @param hostports
	 *            zookeeper连接地址
	 * @param times
	 *            超时时间
	 * @return
	 * @throws Exception
	 */
	public ZooKeeper Init(String hostports, int times, Watcher myWatcher)
			throws Exception {
		try {
			CountDownLatch connectedLatch = new CountDownLatch(1);
			Watcher watcher = new ConnectedWatcher(connectedLatch, myWatcher);
			zk_ = new ZooKeeper(hostports, times, watcher);
			waitUntilConnected(zk_, connectedLatch);
		} catch (Exception e) {
			throw e;
		}
		return zk_;
	}

	public static void main(String args[]) throws InterruptedException {
		String hostports = "10.19.2.10:2181,10.19.2.10:2182,10.19.2.10:2183";
		final List<String> l=new ArrayList<String>(){{
			add("/aof_monitor/demo/servers/MININT-H5DH4LL_10.61.34.12/update");
		}};
		ZooKeeper zk = null;
		try {
			zk = ZkUtils.Instance().Init(hostports, 1000, new Watcher() {
				

				@Override
				public void process(WatchedEvent event) {
					System.out.println("已经触发了" + event.getType() + "事件！"+event.getPath());
				}
			});
//			zk.create("/testRootPath", "testRootData".getBytes(),
//					Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//			// 创建一个子目录节点
//			zk.create("/testRootPath/testChildPathOne",
//					"testChildDataOne".getBytes(), Ids.OPEN_ACL_UNSAFE,
//					CreateMode.PERSISTENT);
//			System.out.println(new String(zk.getData("/testRootPath", false,
//					null)));
//			// 取出子目录节点列表
//			System.out.println(zk.getChildren("/testRootPath", true));
//			// 修改子目录节点数据
//			zk.setData("/testRootPath/testChildPathOne",
//					"modifyChildDataOne".getBytes(), -1);
//			System.out.println("目录节点状态：[" + zk.exists("/testRootPath", true)
//					+ "]");
//			// 创建另外一个子目录节点
//			zk.create("/testRootPath/testChildPathTwo",
//					"testChildDataTwo".getBytes(), Ids.OPEN_ACL_UNSAFE,
//					CreateMode.PERSISTENT);
//			System.out.println(new String(zk.getData(
//					"/testRootPath/testChildPathTwo", true, null)));
//			// 删除子目录节点
//			zk.delete("/testRootPath/testChildPathTwo", -1);
//			zk.delete("/testRootPath/testChildPathOne", -1);
//			// 删除父目录节点
//			zk.delete("/testRootPath", -1);
			//zk.exists("/aof_monitor/demo", true);
			//zk.getData("/aof_monitor/demo/servers/MININT-H5DH4LL_10.61.34.12/update", true, null);
			//zk.setData("/aof_monitor/demo/servers/MININT-H5DH4LL_10.61.34.12/update", "1002".getBytes(), -1);
			//zk.exists("/aof_monitor/demo/servers/MININT-H5DH4LL_10.61.34.12/update", true);
			//zk.setData("/aof_monitor/demo/servers/MININT-H5DH4LL_10.61.34.12/update", "1002".getBytes(), -1);
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}
		}

	}
}
