package autonavi.online.framework.support.zookeeper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.monitor.constant.MonitorConstant;
import autonavi.online.framework.monitor.handler.MonitorHandler;
import autonavi.online.framework.property.PropertiesConfigUtil;
/**
 * zk中BIZ信息刷新
 * @author yaming.xu
 *
 */
public class ZkBizMonitorHandler implements MonitorHandler {
	private Logger log = LogManager.getLogger(getClass());
	@Override
	public void handle(String flag) throws Exception {
		if(flag.equals(MonitorConstant.ZK_WATCHER_BIZ)){
			log.info("刷新自定义属性配置");
			PropertiesConfigUtil.refresh();// 当触发事件时，更新属性配置
		}

	}

}
