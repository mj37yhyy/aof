package autonavi.online.framework.configcenter.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.configcenter.util.AofCcProps;

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
			logger.info("关闭应用["+app+"]ZK连接");
			try {
				ZooKeeper zk=(ZooKeeper)se.getSession().getAttribute(AofCcProps.SESSION_ZK);
				if(zk!=null)
				   zk.close();
				logger.info("关闭应用["+app+"]相关结束");
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}

	}

}
