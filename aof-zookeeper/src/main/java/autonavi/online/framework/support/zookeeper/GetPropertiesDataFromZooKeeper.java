package autonavi.online.framework.support.zookeeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.cc.entity.CcBizEntity;
import autonavi.online.framework.property.PropertiesData;
import autonavi.online.framework.support.zookeeper.exception.ZKExistingPropException;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZkUtils;

public class GetPropertiesDataFromZooKeeper implements PropertiesData {
	private Logger log = LogManager.getLogger(getClass());
	private ZooKeeperProp zooKeeperProp;
	
	private Map<String, String> properties = new HashMap<String, String>();
	
	

	public void setZooKeeperProp(ZooKeeperProp zooKeeperProp) {
		this.zooKeeperProp = zooKeeperProp;
	}

    
	@Override
	public Map<String, String> getProperties() throws Exception {
		Map<String, String> properties = new HashMap<String, String>();
		if (zooKeeperProp != null) {// 从zookeeper里获取
			properties.putAll(this.getPropertiesFromAllDS(zooKeeperProp));
		}
		return properties;
	}
	public Map<String, String> getProperties(ZooKeeper zk,String appName,String... tempPath) throws Exception {
		appName="/"+appName;
		String _tempPath=null;
		if(tempPath!=null&&tempPath.length>=1){
			_tempPath="/"+tempPath[0];
		}
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.putAll(this.getPropertiesFromAllDS(zk,appName,_tempPath));
		return properties;
	}
	public Map<String,Map<String, CcBizEntity>> getPropertiesWithComment(ZooKeeper zk,String appName,String... tempPath) throws Exception {
		appName="/"+appName;
		String _tempPath=null;
		if(tempPath!=null&&tempPath.length>=1){
			_tempPath="/"+tempPath[0];
		}
		Map<String,Map<String, CcBizEntity>> properties = new HashMap<String,Map<String, CcBizEntity>>();
		properties.putAll(this.getPropertiesFromAllDSWithComments(zk, appName, _tempPath));
		return properties;
	}
	public Map<String,String> getBizProperties(ZooKeeper zk,String appName)throws Exception{
		String path=SysProps.AOF_ROOT+"/"+appName+SysProps.AOF_APP_BIZ;
		Map<String,String> _r=new HashMap<String,String>();
		Map<String,Map<String, CcBizEntity>> result=this.getBizEntityFromZK(zk, path, false);
		for(String key:result.keySet()){
			for(String _key:result.get(key).keySet()){
				_r.put(_key, result.get(key).get(_key).getValue());
			}
		}
		return _r;
	}

    public Map<String,String> getBizSplitProperties(ZooKeeper zk,String appName)throws Exception{
    	String path=SysProps.AOF_ROOT+"/"+appName+SysProps.AOF_APP_BIZ_SPLIT;
    	Map<String,String> _r=new HashMap<String,String>();
		Map<String,Map<String, CcBizEntity>> result=this.getBizEntityFromZK(zk, path, true);
		for(String key:result.keySet()){
			for(String _key:result.get(key).keySet()){
				if(_r.get(_key)==null){
					_r.put(_key, result.get(key).get(_key).getValue());
				}
				else{
					throw new ZKExistingPropException("在目录["+key+"]中属性["+_key+"]在其他目录已经出现过");
				}
			}
		}
		return _r;
	}
	private Map<String, String> getPropertiesFromAllDS(ZooKeeper zk,String appName,String tempPath) throws Exception{
		String path="";
		if(tempPath!=null&&!tempPath.equals("")){
			path=SysProps.AOF_TEMP_ROOT+appName+tempPath;
		}else{
			path=SysProps.AOF_ROOT+appName+SysProps.AOF_APP_BIZ;
		}
		return getBizFromZK(zk, path);
	}
	private Map<String,Map<String, CcBizEntity>> getPropertiesFromAllDSWithComments(ZooKeeper zk,String appName,String tempPath) throws Exception{
		String path="";
		Map<String,Map<String, CcBizEntity>> result=new HashMap<String,Map<String, CcBizEntity>>();
		//biz情况 
		if(tempPath!=null&&!tempPath.equals("")){
			path=SysProps.AOF_TEMP_ROOT+appName+tempPath;
		}else{
			path=SysProps.AOF_ROOT+appName+SysProps.AOF_APP_BIZ;
		}
		//获取BIZ
		result.putAll(this.getBizEntityFromZK(zk, path, false));
		//biz_split
		if(tempPath!=null&&!tempPath.equals("")){
			path=SysProps.AOF_TEMP_ROOT+appName+appName+"_"+SysProps.AOF_APP_BIZ_SPLIT_STR+tempPath;
		}else{
			path=SysProps.AOF_ROOT+appName+SysProps.AOF_APP_BIZ_SPLIT;
		}
		result.putAll(this.getBizEntityFromZK(zk, path, true));
		
		return result;
	}
	
	/**
	 * 从所有数据源得到属性，这里是多个ZooKeeper
	 * 
	 * @throws Exception
	 */
	private Map<String, String> getPropertiesFromAllDS(ZooKeeperProp zooKeeperProp) throws Exception {
		String address = zooKeeperProp.getAddress();
		int sessionTimeout = zooKeeperProp.getSessionTimeout();
		try {
			/**
			 * 调整BIZ刷新机制，和BASE的方式合并，走与刷新和刷新两个步骤
			 */
//			ZooKeeper zk = ZkUtils.Instance().Init(address, sessionTimeout,
//					new Watcher() {
//
//						/**
//						 * 当zookeeper节点发生变化时
//						 */
//						@Override
//						public void process(WatchedEvent event) {
//							if (!event.getType().equals(EventType.None)
//									&& event.getPath() != null
//									&& isInclude(event.getPath())) {// 如果当前变化的路径包含于配置的路径中
//								try {
//									Log.info("检测到节点变化 重新刷新");
//									PropertiesConfigUtil.refresh();// 当触发事件时，更新属性配置
//									zk_.close();// 关闭连接
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						}
//					});
//			this.zk_=zk;
			ZooKeeper zk = ZkUtils.Instance().Init(address, sessionTimeout,null);
			zk.addAuthInfo("digest", (zooKeeperProp.getProjectName()+":"+zooKeeperProp.getPassword()).getBytes());
//			this.getAllPropertiesFromZK(zk, path,true);// 递归查询下级 监听
			//判定BIZ的配置的模式 如果是老模式 则迁移老模式的BIZ到新模式下（暂时屏蔽）
//			if(zk.exists(path, false)!=null){
//				//存在新节点，为新模式
//				this.getAllPropertiesFromZK(zk, path,false,true);
//			}else{
//				//若是老模式 则迁移数据到新模式下
//				log.warn("目前系统的运行模式为旧版BIZ配置方式,请尽快登陆配置工具将BIZ自动更新到新模式下");
//				path=ROOT + "/"+zooKeeperProp.getProjectName() + BIZ;
//				this.getAllPropertiesFromZK(zk, path,false,false);// 查询 不监听
//				
//			}
			this.getAllPropertiesFromZK(zk);// 查询 不监听
//			MonitorZooKeeper.setBizVersion();
			log.info("关闭BIZ信息ZooKeeper获取连接");
			zk.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return properties;
	}

	private void getAllPropertiesFromZK(ZooKeeper zk)
			throws KeeperException, InterruptedException,Exception {
		//获取未分类的
		Map<String,String> biz=this.getBizProperties(zk, zooKeeperProp.getProjectName());
		//获取分类的
		Map<String,String> bizSplit=this.getBizSplitProperties(zk, zooKeeperProp.getProjectName());
		//合并校验
		properties.clear();
		properties.putAll(biz);
		for(String key:bizSplit.keySet()){
			if(properties.get(key)!=null){
				throw new ZKExistingPropException("在未分类中属性["+key+"]已经存在");
			}
			properties.put(key, bizSplit.get(key));
		}
		
	}
	private Map<String, String> getBizFromZK(ZooKeeper zk, String path)throws KeeperException, InterruptedException,Exception{
		Map<String, String> properties=new HashMap<String, String>();
		List<String> zl = zk.getChildren(path, false, null);
		for (String key : zl) {
			String _path = path + "/" + key;
			byte[] data = zk.getData(_path, false, null);
			String value = null;
			if (data != null)
				value = new String(data,SysProps.CHARSET);// 从ZooKeeper得到孩子下的内容
			properties.put(key, value);
		}
		return properties;
	}
	private Map<String,Map<String, CcBizEntity>> getBizEntityFromZK(ZooKeeper zk, String path,boolean isSplit)throws KeeperException, InterruptedException,Exception{
		Map<String,Map<String, CcBizEntity>> properties=new HashMap<String,Map<String, CcBizEntity>>();
		if(!isSplit){
			Map<String, CcBizEntity> _p=new HashMap<String, CcBizEntity>();
			List<String> zl = zk.getChildren(path, false, null);
			for (String key : zl) {
				CcBizEntity biz=new CcBizEntity();
				String _path = path + "/" + key;
				byte[] data = zk.getData(_path, false, null);
				String value = null;
				String comments="";
				if (data != null)
					value = new String(data,SysProps.CHARSET);// 从ZooKeeper得到孩子下的内容
				biz.setValue(value);
				_path = _path +SysProps.AOF_APP_BIZ_COMMENTS;
				if(zk.exists(_path, false)!=null){
				    data=zk.getData(_path, false, null);
				    if (data != null)
				    	comments= new String(data,SysProps.CHARSET);
				    
				}
				biz.setComments(comments);
				_p.put(key, biz);
			}
			properties.put(SysProps.DEDAULT_BIZ_UNIQUE_NAME, _p);
		}else{
			if(zk.exists(path, false)!=null){
				List<String> zl = zk.getChildren(path, false, null);
				for (String key : zl) {
					Map<String, CcBizEntity> _p=new HashMap<String, CcBizEntity>();
					List<String> zll = zk.getChildren(path+"/"+key, false, null);
					for (String _key : zll) {
						CcBizEntity biz=new CcBizEntity();
						String _path = path + "/" + key+"/"+_key;
						byte[] data = zk.getData(_path, false, null);
						String value = null;
						String comments="";
						if (data != null)
							value = new String(data,SysProps.CHARSET);// 从ZooKeeper得到孩子下的内容
						biz.setValue(value);
						_path = _path +SysProps.AOF_APP_BIZ_COMMENTS;
						if(zk.exists(_path, false)!=null){
						    data=zk.getData(_path, false, null);
						    if (data != null)
						    	comments= new String(data,SysProps.CHARSET);
						   
						}
						 biz.setComments(comments);
						_p.put(_key, biz);
					}
					properties.put(key,_p);
				}
			}
			
		}
		
		return properties;
	}
	


}
