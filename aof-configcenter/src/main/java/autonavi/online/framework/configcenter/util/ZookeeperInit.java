package autonavi.online.framework.configcenter.util;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import autonavi.online.framework.configcenter.exception.AofException;
import autonavi.online.framework.configcenter.exception.AofExceptionEnum;
import autonavi.online.framework.support.zookeeper.ZooKeeperProp;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZkUtils;

public class ZookeeperInit {
	private Logger logger = LogManager.getLogger(this.getClass());
	private ZooKeeperProp zooKeeperProp;
	private ZooKeeper zoo;
	private ACL acl;
	
	public ZooKeeperProp getZooKeeperProp() {
		return zooKeeperProp;
	}

	public void setZooKeeperProp(ZooKeeperProp zooKeeperProp) {
		this.zooKeeperProp = zooKeeperProp;
	}

	public ACL getAcl() {
		return acl;
	}

	public void setAcl(ACL acl) {
		this.acl = acl;
	}

	

	public ZooKeeper getZoo() {
		if(!zoo.getState().isConnected()||!zoo.getState().isAlive()){
			run();
		}
		return zoo;
	}

	public void setZoo(ZooKeeper zoo) {
		this.zoo = zoo;
	}
    @PostConstruct
    private synchronized void run(){
    	try {
			zoo=ZkUtils.Instance().Init(zooKeeperProp.getAddress(), zooKeeperProp.getSessionTimeout(), null);  
			zoo.addAuthInfo("digest", (zooKeeperProp.getProjectName()+":"+zooKeeperProp.getPassword()).getBytes(SysProps.CHARSET));
			Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest(zooKeeperProp.getProjectName()+":"+zooKeeperProp.getPassword()));  
			acl = new ACL(ZooDefs.Perms.ALL, id1);  
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AofException(AofExceptionEnum.INIT_ZOOKEEPER_ERROR);
		}
    }
    public ZooKeeper getNewZoo(){
    	try {
			ZooKeeper nz=ZkUtils.Instance().Init(zooKeeperProp.getAddress(), zooKeeperProp.getSessionTimeout(), null);
			nz.addAuthInfo("digest", (zooKeeperProp.getProjectName()+":"+zooKeeperProp.getPassword()).getBytes(SysProps.CHARSET));
			return nz;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AofException(AofExceptionEnum.INIT_ZOOKEEPER_ERROR);
		}
    }
    public ZooKeeper generateAppZoo(String appName,String passwd){
    	try {
			ZooKeeper nz=ZkUtils.Instance().Init(zooKeeperProp.getAddress(), zooKeeperProp.getSessionTimeout(), null);
			nz.addAuthInfo("digest", (appName+":"+passwd).getBytes(SysProps.CHARSET));
			return nz;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new AofException(AofExceptionEnum.INIT_ZOOKEEPER_ERROR);
		}
    }
	

}
