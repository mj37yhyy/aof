package autonavi.online.framework.configcenter.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import autonavi.online.framework.configcenter.entity.DatabaseConfigInfo;
import autonavi.online.framework.configcenter.entity.MonitorInfo;
import autonavi.online.framework.configcenter.entity.MonitorNodeState;
import autonavi.online.framework.configcenter.exception.AofException;
import autonavi.online.framework.configcenter.exception.AofExceptionEnum;
import autonavi.online.framework.configcenter.service.MonitorService;
import autonavi.online.framework.configcenter.util.DBConstants;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZooKeeperUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 监控信息
 * 
 * @author chunsheng.zhang
 * 
 */
@Service("monitorService")
public class MonitorServiceImpl implements MonitorService {
	private Logger logger = LogManager.getLogger(this.getClass());

	
	/**
	 * 修改数据库状态
	 * @param zk
	 * @param appname
	 * @param dbnames 数据库
	 * @param ud
	 * @return
	 */
	public void updateDatabaseState(ZooKeeper zk, String appname, String[] dbnames, String ud) {

		try {
			String dbRoot = SysProps.AOF_ROOT + "/" + appname + SysProps.AOF_APP_BASE + SysProps.AOF_APP_DSS;
			
			if("0".equals(ud)) {
				ud = DBConstants.M_RESULT_STATE_TRUE;
			}else {
				ud = DBConstants.M_RESULT_STATE_FALSE;
			}
			
			String dss_monitor_path = null;
			String s = null;
			ZooKeeperUtils.startTransaction(zk);
			int atimes = 0; //是否有更改操作
			for (String dbname : dbnames) {
				dss_monitor_path = dbRoot + "/" + dbname + SysProps.DSS_MONITOR_BASE;

				byte[] b = zk.getData(dss_monitor_path, false, null);
				s = new String(b, SysProps.CHARSET);
				if (ud.equals(s)) {
					continue;
				}
				atimes++;
				ZooKeeperUtils.setZkNode(dss_monitor_path, ud.getBytes(SysProps.CHARSET));
			}
			
			//如果有修改，则更新目录
			if(atimes != 0) {
				notify3Times(zk, dbRoot);
			}
			
			ZooKeeperUtils.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}finally {
			ZooKeeperUtils.close();
		}
		
	
		
	}
	
	
	/**
	 * 触发三个watcher
	 * @param zooKeeper
	 * @param dbRoot
	 * @throws Exception
	 */
	private void notify3Times(ZooKeeper zooKeeper, String dbRoot) throws Exception {
		for(int i = 1; i <= 3; i++) {
			//刷新dss目录
			ZooKeeperUtils.setZkNode(dbRoot, "2".getBytes(SysProps.CHARSET), zooKeeper);
			Thread.sleep(500 * i);
		}
	}
	
	
	
	/**
	 * 查询数据库状态
	 * @param zk
	 * @param appname
	 * @return
	 */
	public List<MonitorInfo> queryDatabaseState(ZooKeeper zk, String appname) {
		List<MonitorInfo> monitorInfos = new ArrayList<MonitorInfo>();
		try {
			String dbRoot = SysProps.AOF_ROOT + "/" + appname + SysProps.AOF_APP_BASE + SysProps.AOF_APP_DSS;
		
			
			MonitorInfo monitorInfo = null;
			String dss_monitor_path = null;
			List<String> dbnames = zk.getChildren(dbRoot, false);
			for(String dbname : dbnames) {
				monitorInfo = new MonitorInfo();
				monitorInfo.setDbname(dbname);
				dss_monitor_path = dbRoot + "/" + dbname + SysProps.DSS_MONITOR_BASE;
				if(null != zk.exists(dss_monitor_path, false)) {
					byte []b = zk.getData(dss_monitor_path, false, null);
					String state = new String(b, SysProps.CHARSET);
					if(DBConstants.M_RESULT_STATE_TRUE.equals(state)) {
						monitorInfo.setType(0);
					}else {
						monitorInfo.setType(1);
					}
				}
				
				monitorInfos.add(monitorInfo);
			}
			
			
			return monitorInfos;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
	}

	
	
	
	/***
	 * 获取监控结果信息
	 * @param zk
	 * @param appname
	 * @return
	 */
	public List<MonitorInfo> queryMonitorInfo(ZooKeeper zk, String appname) {
		List<MonitorInfo> monitorInfos = new ArrayList<MonitorInfo>();
		try {
			String dbRootPath = DBConstants.DB_ROOT + DBConstants.SEPERATOR + appname;
			List<String> dbnames = zk.getChildren(dbRootPath, false);
			String monitorResultPath = null;
			String monitorMaxConnPath = null;
			MonitorInfo monitorInfo = null;
			String mr = null;
			String max_conn = null;
			String cTimePath = null;
			ObjectMapper objectMapper = new ObjectMapper();
			for (String dbname : dbnames) {
				monitorResultPath = dbRootPath + DBConstants.SEPERATOR + dbname
						+ DBConstants.MONITOR_RESULT;
				Stat stat = zk.exists(monitorResultPath, false);
				if(null == stat) {
					continue;
				}
				
				//监控结果
				mr = new String(zk.getData(monitorResultPath, false, null), DBConstants.CHARSET);
				monitorInfo = objectMapper.readValue(mr, MonitorInfo.class);
				monitorInfo.setUpdateTime(stat.getMtime());
				
				
				//最大连接数阀值
				monitorMaxConnPath = dbRootPath + DBConstants.SEPERATOR + dbname
						+ DBConstants.MAX_CONNECTION;
				max_conn = new String(zk.getData(monitorMaxConnPath, false, null), DBConstants.CHARSET);
				
				monitorInfo.setMax_conn(max_conn);
				
				cTimePath = monitorResultPath + DBConstants.TIMESTAMP;
				Long ctime = getCtime(zk, cTimePath);
				monitorInfo.setSystemTime(ctime);
				monitorInfos.add(monitorInfo);
			}
			
			return monitorInfos;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
		
		
		
	}
	
	
	
	
	
	
	
	/**
	 *	保存数据库的最大连接数
	 * @param zk
	 * @param appName 应用名称
	 * @param dbname 数据库名称
	 * @param maxConn 最大连接数
	 */
	public void saveDatabaseConfigInfo(ZooKeeper zk,String appName, String dbname, String maxConn) {
		try {
			String dbRootPath = DBConstants.DB_ROOT + DBConstants.SEPERATOR + appName;
			String maxConnPath = dbRootPath + DBConstants.SEPERATOR + dbname + DBConstants.MAX_CONNECTION;
			zk.setData(maxConnPath, maxConn.getBytes(DBConstants.CHARSET), -1);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
		
	}
	
	
	/**
	 * 查询数据库配置信息
	 * 
	 * @param zk
	 * @param appName
	 *            应用名称
	 * @return
	 */
	public List<DatabaseConfigInfo> queryDatabaseConfigInfo(ZooKeeper zk,
			String appName) {
		List<DatabaseConfigInfo> resultList = new ArrayList<DatabaseConfigInfo>();

		try {
			String dbRootPath = DBConstants.DB_ROOT + DBConstants.SEPERATOR + appName;
			List<String> dbnames = zk.getChildren(dbRootPath, false);
			String monitorResultPath = null;
			DatabaseConfigInfo databaseConfigInfo = null;
			for (String dbname : dbnames) {
				monitorResultPath = dbRootPath + DBConstants.SEPERATOR + dbname
						+ DBConstants.MAX_CONNECTION;
				
				//TODO 这个地方不需要处理，如果有数据库那么一定有默认的最大连接数设置
				/*if (null == zk.exists(monitorResultPath, false)) {
					zk.create(monitorResultPath, "500".getBytes(DBConstants.CHARSET),
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}*/
				String max_conn = new String(zk.getData(monitorResultPath, false, null), DBConstants.CHARSET);
				databaseConfigInfo = new DatabaseConfigInfo();
				databaseConfigInfo.setDbname(dbname);
				databaseConfigInfo.setMax_conn(max_conn);
				resultList.add(databaseConfigInfo);
			}

			return resultList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}

	}

	/**
	 * 查询监控节点信息
	 * 
	 * @param zk
	 *            zk信息
	 * @param appName
	 *            应用名称
	 * @return 监控节点信息
	 */
	public List<MonitorNodeState> queryMonitorNodes(ZooKeeper zk, String appName) {
		List<MonitorNodeState> monitorNodeStates = new ArrayList<MonitorNodeState>();
		try {
			String monitorPath = DBConstants.REGISTER_PATH
					+ DBConstants.SEPERATOR + appName;
			List<String> cc = zk.getChildren(monitorPath, false);
			MonitorNodeState monitorNodeState = null;
			String monitorNodePath = null;
			String monitorCTimePath = null;
			for (String m : cc) {
				monitorNodePath = monitorPath + DBConstants.SEPERATOR + m;
				monitorCTimePath = monitorNodePath + DBConstants.TIMESTAMP;
				Long uTime = getUtime(zk, monitorNodePath);
				Long cTime = getCtime(zk, monitorCTimePath);
				monitorNodeState = new MonitorNodeState();
				monitorNodeState.setMonitorNodeName(m);
				monitorNodeState.setuTime(uTime);
				monitorNodeState.setcTime(cTime);
				monitorNodeStates.add(monitorNodeState);
			}
			return monitorNodeStates;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
	}

	/**
	 * 删除监控节点信息
	 * 
	 * @param zk
	 * @param appName
	 */
	public void delMonitorNode(ZooKeeper zk, String appName, String monitorName) {
		try {
			String monitorPath = DBConstants.REGISTER_PATH
					+ DBConstants.SEPERATOR + appName;
			String monitorNodePath = monitorPath + DBConstants.SEPERATOR
					+ monitorName;

			ZooKeeperUtils.deleteNodeWithoutTx(monitorNodePath, zk, true);
			// ZooKeeperUtils.deleteNode(monitorNodePath, zk, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}

	}

	/**
	 * 获取更新时间
	 * 
	 * @param zk
	 * @param path
	 * @return
	 */
	public Long getUtime(ZooKeeper zk, String path) {
		try {
			Stat stat = zk.exists(path, false);
			return stat.getMtime();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
	}

	/**
	 * 查询数据库监控结果下的当前时间
	 * 
	 * @param zk
	 * @param c
	 *            数据库目录
	 * @return 当前时间
	 */
	public Long getCtime(ZooKeeper zk, String ctimePath) {
		try {
			if (null == zk.exists(ctimePath, null)) {
				zk.create(ctimePath, null, Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			zk.setData(ctimePath, null, -1);
			Stat stat = zk.exists(ctimePath, null);
			return stat.getMtime();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AofException(AofExceptionEnum.ZOOKEEPER_USE_ERROR);
		}
	}
	
	
}
