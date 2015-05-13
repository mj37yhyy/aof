package autonavi.online.framework.configcenter.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import autonavi.online.framework.configcenter.entity.DatabaseConfigInfo;
import autonavi.online.framework.configcenter.entity.MonitorInfo;
import autonavi.online.framework.configcenter.entity.MonitorNodeState;
import autonavi.online.framework.configcenter.entity.ResultEntity;
import autonavi.online.framework.configcenter.service.MonitorService;
import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.configcenter.util.ZooKeeperClientHolder;

/**
 * @author chunsheng.zhang
 *
 */
@Controller
public class DbMonitorController {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Resource
	private MonitorService monitorService;
	
	
	/**
	 * 查询监控节点信息
	 * @param hs
	 * @return
	 */
	@RequestMapping("/monitor/listmonitornode")
	public @ResponseBody ResultEntity listMonitorNode(HttpSession hs) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			
			List<MonitorNodeState> monitorNodes = monitorService.queryMonitorNodes(zk, appName);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			resultEntity.setResult(monitorNodes);
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	/**
	 * 删除监控节点信息
	 * @param hs
	 * @return
	 */
	@RequestMapping("/monitor/delmonitornode")
	public @ResponseBody ResultEntity delMonitorNode(HttpSession hs, String monitorName) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			monitorService.delMonitorNode(zk, appName, monitorName);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	/**
	 * 查询监控节点信息
	 * @param hs
	 * @return
	 */
	@RequestMapping("/monitor/listmonitor")
	public @ResponseBody ResultEntity listMonitor(HttpSession hs) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			List<MonitorInfo> monitorInfos = monitorService.queryMonitorInfo(zk, appName);
			resultEntity.setResult(monitorInfos);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	/**
	 * 查看数据库配置的最大连接数信息
	 * @param hs
	 * @return
	 */
	@RequestMapping("/monitor/listdatabaseconfiginfo")
	public @ResponseBody ResultEntity listDatabaseConfigInfo(HttpSession hs) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			List<DatabaseConfigInfo> databaseConfigInfo = monitorService.queryDatabaseConfigInfo(zk, appName);
		
			resultEntity.setResult(databaseConfigInfo);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	@RequestMapping("/monitor/savedatabaseconfiginfo")
	public @ResponseBody ResultEntity saveDatabaseConfigInfo(HttpSession hs, String dbname, String maxConn) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			monitorService.saveDatabaseConfigInfo(zk, appName, dbname, maxConn);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	
	/**
	 * 查询数据源上线下线信息
	 * @param hs
	 * @return
	 */
	@RequestMapping("/monitor/listdatabasestate")
	public @ResponseBody ResultEntity listDatabaseState(HttpSession hs) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			List<MonitorInfo> monitorInfos =  monitorService.queryDatabaseState(zk, appName);
			resultEntity.setResult(monitorInfos);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
	
	/**
	 * 修改数据库的上线，下线操作
	 * @param hs
	 * @param dbnames 数据库名称
	 * @param ud 0 上线， 1下线
	 * @return
	 */
	@RequestMapping("/monitor/updateDatabaseState")
	public @ResponseBody ResultEntity updateDatabaseState(HttpSession hs, String dbnames, String ud) {
		ResultEntity resultEntity = new ResultEntity();
		try {
			String appName = (String)hs.getAttribute(AofCcProps.SESSION_APP);
			ZooKeeper zk = ZooKeeperClientHolder.getZooKeeper(appName);
			String[] dbs = dbnames.split(",");
			monitorService.updateDatabaseState(zk, appName, dbs, ud);
			resultEntity.setCode("0");
			resultEntity.setMsg("succes");
			return resultEntity;
		}catch(Exception e) {
			logger.error(e);
			resultEntity.setCode("1");
			resultEntity.setMsg("系统内部异常");
			return resultEntity;
		}
	}
	
}
