package autonavi.online.framework.configcenter.service;

import java.util.List;

import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.configcenter.entity.DatabaseConfigInfo;
import autonavi.online.framework.configcenter.entity.MonitorInfo;
import autonavi.online.framework.configcenter.entity.MonitorNodeState;

public interface MonitorService {

	
	
	/**
	 * 修改数据库状态
	 * @param zk
	 * @param appname
	 * @param dbnames 数据库
	 * @param ud
	 * @return
	 */
	public void updateDatabaseState(ZooKeeper zk, String appname, String[] dbnames, String ud);
	
	
	
	/**
	 * 查询数据库状态
	 * @param zk
	 * @param appname
	 * @return
	 */
	public List<MonitorInfo> queryDatabaseState(ZooKeeper zk, String appname);
	
	
	
	/***
	 * 获取监控结果信息
	 * @param zk
	 * @param appname
	 * @return
	 */
	public List<MonitorInfo> queryMonitorInfo(ZooKeeper zk, String appname);
	
	
	/**
	 *	保存数据库的最大连接数
	 * @param zk
	 * @param appName 应用名称
	 * @param dbname 数据库名称
	 * @param maxConn 最大连接数
	 */
	public void saveDatabaseConfigInfo(ZooKeeper zk,String appName, String dbname, String maxConn);
	
	
	/**
	 * 查询数据库配置信息
	 * @param zk
	 * @param appName 应用名称
	 * @return
	 */
	public List<DatabaseConfigInfo> queryDatabaseConfigInfo(ZooKeeper zk,String appName);
	
	
	/**
	 * 查询监控节点信息
	 * @param zk zk信息
	 * @param appName 应用名称
	 * @return 监控节点信息
	 */
	public List<MonitorNodeState> queryMonitorNodes(ZooKeeper zk,String appName);
	
	
	/**
	 * 删除监控节点信息
	 * @param zk
	 * @param appName 应用名称
	 * @param monitorName 监控节点名称
	 */
	public void delMonitorNode(ZooKeeper zk,String appName, String monitorName);
	
	
	
	
	
	
}
