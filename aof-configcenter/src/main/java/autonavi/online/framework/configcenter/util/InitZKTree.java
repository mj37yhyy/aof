package autonavi.online.framework.configcenter.util;

import javax.annotation.PostConstruct;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZooKeeperUtils;
@Component 
public class InitZKTree {
	@Autowired
	private ZookeeperInit zookeeperInit;
	@PostConstruct
	public void init(){
		ZooKeeper zk=zookeeperInit.getZoo();
		ZooKeeperUtils.startTransaction(zk);
		if(!ZooKeeperUtils.checkZKNodeIsExist(zookeeperInit.getZoo(), SysProps.AOF_ROOT, false)){
			ZooKeeperUtils.createSafeZKNode(SysProps.AOF_ROOT, null, zk);
		}
		if(!ZooKeeperUtils.checkZKNodeIsExist(zookeeperInit.getZoo(), SysProps.AOF_MONITOR_ROOT, false)){
			ZooKeeperUtils.createSafeZKNode(SysProps.AOF_MONITOR_ROOT, null, zk);
		}
		if(!ZooKeeperUtils.checkZKNodeIsExist(zookeeperInit.getZoo(), SysProps.AOF_TEMP_ROOT, false)){
			ZooKeeperUtils.createSafeZKNode(SysProps.AOF_TEMP_ROOT, null, zk);
		}
		if(!ZooKeeperUtils.checkZKNodeIsExist(zookeeperInit.getZoo(), SysProps.AOF_ROOT+SysProps.AOF_PASS, false)){
			ZooKeeperUtils.createSafeZKNode(SysProps.AOF_ROOT+SysProps.AOF_PASS, null, zk);
		}
	}
	 

}
