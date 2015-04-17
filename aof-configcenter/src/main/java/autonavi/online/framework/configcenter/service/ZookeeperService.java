package autonavi.online.framework.configcenter.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.cc.entity.CcBizEntity;
import autonavi.online.framework.configcenter.commons.AppNode;

public interface ZookeeperService {
	/**
	 * 添加应用根
	 * @param appRoot
	 * @param passwd
	 */
	public void addAppRoot(String appRoot,String passwd);
	/**
	 * 校验应用的登录
	 * @param appRoot
	 * @param passwd
	 */
	public void loginAppRoot(String appRoot,String passwd,String sessionId,Boolean isDev);
	/**
	 * 获取节点状态
	 * @param path
	 * @param zk
	 * @return
	 */
	public Stat getAppNodeStat(String path,ZooKeeper zk);
	/**
	 * 拷贝ZK
	 * @param sourcePath
	 * @param desPath
	 * @param zk
	 * @param appName
	 * @param passwd
	 */
	public void copyAppNode(String sourcePath,String desPath,ZooKeeper zk,String appName,String passwd);
	/**
	 * 删除结点--递归
	 * @param path
	 * @param appRoot
	 * @param passwd
	 */
	public void deleteNode(String path,ZooKeeper zk);
	/**
	 * 获取应用树
	 * @param path
	 * @param zk
	 * @return
	 */
	public List<String> getAppNodeTree(String path,ZooKeeper zk);
	/**
	 * 获取数据源配置信息
	 * @param zk
	 * @param appName
	 * @param tempPath
	 * @return
	 */
	public CcBaseEntity getCcBaseEntity(ZooKeeper zk,String appName,String tempPath);
	/**
	 * 获取分表分区信息
	 * @param zk
	 * @param appName
	 * @return
	 */
	public CcBaseEntity getCcShardingEntity(ZooKeeper zk,String appName);
	/**
	 * 存储配置信息到临时目录
	 * @param ccBaseEntity
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBaseConfigToTemp(CcBaseEntity ccBaseEntity,String fileName,ZooKeeper zk,String appName,String password);
	/**
	 * 存储配置信息到临时目录
	 * @param ccBase
	 * @param fileName
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBizConfigToTemp(Map<String,String> ccBase,String fileName,ZooKeeper zk,String appName,String password);
	/**
	 * 存储配置信息到临时目录
	 * @param ccBase
	 * @param fileName
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBizEntityToTemp(Map<String,Map<String,CcBizEntity>> ccBase,String fileName,ZooKeeper zk,String appName,String password);
	/**
	 * 存储配置信息到正式目录
	 * @param ccBaseEntity
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBaseConfig(CcBaseEntity ccBaseEntity,ZooKeeper zk,String appName,String password);
	/**
	 * 存储配置信息到正式目录(旧)
	 * @param ccBase
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBizConfig(Map<String,String> ccBase,ZooKeeper zk,String appName,String password);
	/**
	 * 存储配置信息到正式目录(新)
	 * @param ccBase
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveBizEntity(Map<String,Map<String,CcBizEntity>> ccBase,ZooKeeper zk,String appName,String password);
	/**
	 * 存储分区分表信息到正式目录
	 * @param ccBaseEntity
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void saveShardingConfig(CcBaseEntity ccBaseEntity,ZooKeeper zk,String appName,String password);
	/**
	 * 检测各个服务器节点状态
	 * @param zk
	 * @return
	 */
	public List<AppNode> checkAppNodesStats(ZooKeeper zk,String appName);
	/**
	 * 预激活数据源
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public String preBaseActive(ZooKeeper zk,String appName,List<AppNode> nodeList);
	/**
	 * 预激活BIZ
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public String preBizActive(ZooKeeper zk,String appName,List<AppNode> nodeList);
	/**
	 * 检测预激活状态
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public List<AppNode> checkPreBaseActive(ZooKeeper zk,String appName,List<AppNode> nodeList);
	/**
	 * 正式提交数据源激活
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public String commitBaseActive(ZooKeeper zk,String appName,List<AppNode> nodeList,String tempFile,String pass);
	/**
	 * 正式提交Biz激活
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public String commitBizActive(ZooKeeper zk,String appName,List<AppNode> nodeList,String tempFile,String pass);
	/**
	 * 检测正式提交状态
	 * @param zk
	 * @param appName
	 * @param nodeList
	 * @return
	 */
	public List<AppNode> checkCommitBaseActive(ZooKeeper zk,String appName,List<AppNode> nodeList);
	/**
	 * 获取自定义属性信息
	 * @param zk
	 * @param appName
	 * @return
	 */
	public Map<String,String> getBizInfo(ZooKeeper zk,String appName,String tempPath);
	/**
	 * 获取自定义属性信息
	 * @param zk
	 * @param appName
	 * @return
	 */
	public Map<String,Map<String, CcBizEntity>> getBizInfoComments(ZooKeeper zk,String appName,String tempPath);
	/**
	 * 删除节点服务器
	 * @param zk
	 * @param appName
	 * @param nodeName
	 */
	public void deleteServerNode(ZooKeeper zk,String appName,String nodeName);
	/**
	 * 删除临时配置
	 * @param zk
	 * @param appName
	 * @param nodeName
	 */
	public void deleteTempConfig(ZooKeeper zk,String appName,String nodeName);
	/**应用树键值对
	 * @param path
	 * @param zk
	 * @return
	 */
	public Map<String,String> getAppChildNodeMap(String path,ZooKeeper zk,String nodeName);
	/**
	 * 初始化应用配置
	 * @param f
	 */
	public void initAppProp(File f);
	
	/**
	 * 
	 * @param ccBaseEntity
	 */
	public void saveOrUpdate(CcBaseEntity ccBaseEntity);
	/**
	 * 检测管理员
	 * @param userName
	 * @param pass
	 */
	public Boolean checkAdminUser(String userName,String pass);
	/**
	 * 导入BASE信息
	 * @param ccBaseEntity
	 * @param zk
	 * @param userName
	 * @param pass
	 * @return
	 */
	public void importDssConfig(CcBaseEntity ccBaseEntity,ZooKeeper zk,String userName,String pass);
	/**
	 * 导入BIZ信息
	 * @param ccBase
	 * @param zk
	 * @param userName
	 * @param pass
	 * @return
	 */
	public void importBizConfig(Map<String,String> ccBase,ZooKeeper zk,String userName,String pass);
	/**
	 * 导入BIZ信息
	 * @param ccBase
	 * @param zk
	 * @param userName
	 * @param pass
	 * @return
	 */
	public void importBizConfig(Map<String,CcBizEntity> ccBase,ZooKeeper zk,String userName,String pass,String splitName);
	/**
	 * 通知数据源监控
	 * @param zk
	 * @param appName
	 * @param password
	 */
	public void notifyDssMonitor(ZooKeeper zk,String appName, String password);
	/**
	 * 检测是否有在线的应用
	 * @param zk
	 * @param app
	 * @return
	 */
	public boolean hasActiveApp(ZooKeeper zk,String app);

}
