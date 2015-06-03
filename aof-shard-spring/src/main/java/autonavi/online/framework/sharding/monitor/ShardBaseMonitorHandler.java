package autonavi.online.framework.sharding.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cc.CcConfigUtils;
import autonavi.online.framework.monitor.constant.MonitorConstant;
import autonavi.online.framework.monitor.handler.MonitorHandler;
import autonavi.online.framework.sharding.dao.DaoContextHolder;

/**
 * 监控时更新数据源Handler
 * 
 * @author yaming.xu
 * 
 */
public class ShardBaseMonitorHandler implements MonitorHandler {

	private Logger log = LogManager.getLogger(getClass());

	@Override
	public void handle(String flag) throws Exception {
		// TODO Auto-generated method stub
		if (flag.equals(MonitorConstant.ZK_WATCHER_BASE)
				|| flag.equals(MonitorConstant.ZK_WATCHER_DSS_ACTIVE)) {

			DaoContextHolder.getInstance().getApplicationContext()
					.getBean(CcConfigUtils.class).refresh();
			// CcConfigUtils.getInstance().refresh();
			log.info("刷新数据源配置");
		}

	}

}
