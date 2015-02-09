package autonavi.online.framework.configcenter.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.configcenter.util.ZooKeeperClientHolder;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZooKeeperUtils;

public class AofSessionListener implements HttpSessionListener {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		if(se.getSession().getAttribute(AofCcProps.SESSION_APP)!=null){
			String app=(String)se.getSession().getAttribute(AofCcProps.SESSION_APP);
			logger.info("应用["+app+"]调用退出或者session超时");
			ZooKeeper zk=ZooKeeperClientHolder.getZooKeeper(app);
			if(zk!=null){
				String sessionId="";
				try {
					sessionId=ZooKeeperUtils.getZkNode(zk, SysProps.AOF_ROOT + SysProps.AOF_PASS + "/" + app+SysProps.LOGIN_FLAG);
					if(se.getSession().getId().equals(sessionId)){
						//确保是自己创建的连接才释放
						logger.info("关闭应用["+app+"]ZK连接");
						ZooKeeperClientHolder.cleanZooKeeperClinet(app);
					}
				} catch (Exception e) {
					logger.info("ZooKeeper连接存在问题 应用名称["+app+"]");
					//直接释放连接
					logger.info("关闭应用["+app+"]ZK连接");
					ZooKeeperClientHolder.cleanZooKeeperClinet(app);
				}
			}
		}

	}

}
