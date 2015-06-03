package autonavi.online.framework.configcenter.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperClientHolder {
	private static Logger logger = LogManager.getLogger(ZooKeeperClientHolder.class);
	private final static Map<String,ZooKeeper> zooKeeperMap=new HashMap<String,ZooKeeper>();
	private final static Map<String,String> zkPassMap=new HashMap<String,String>();

	
	public static ZooKeeper getZooKeeper(String appName){
		return zooKeeperMap.get(appName);
	}
	public static String getZkPass(String appName){
		return zkPassMap.get(appName);
	}


	public synchronized static void addZooKeeper(String appName,ZooKeeper zk) {
		zooKeeperMap.put(appName, zk);
	}
	public synchronized static void addZkPass(String appName,String pass) {
		zkPassMap.put(appName, pass);
	}
	public static void modifyZkPass(String appName,String pass){
		cleanZkPass(appName);
		addZkPass(appName,pass);
	}
	public static void modifyZooKeeper(String appName,ZooKeeper zk){
		cleanZooKeeper(appName);
		addZooKeeper(appName,zk);
		
	}
	public static void cleanZooKeeperClinet(String appName){
		cleanZooKeeper(appName);
		cleanZkPass(appName);
	}
	public synchronized static void cleanZooKeeper(String appName){
		ZooKeeper _zk=zooKeeperMap.get(appName);
		try {
			if(_zk!=null)_zk.close();
		} catch (InterruptedException e) {
			logger.warn("关闭ZK连接出现问题",e);
		}
		zooKeeperMap.remove(appName);
	}
	public synchronized static void cleanZkPass(String appName){
		zkPassMap.remove(appName);
	}
	

}
