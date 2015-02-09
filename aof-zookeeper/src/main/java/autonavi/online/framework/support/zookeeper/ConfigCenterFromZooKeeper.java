package autonavi.online.framework.support.zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.cc.CcBaseEntity;
import autonavi.online.framework.cc.CcDataSource;
import autonavi.online.framework.cc.InitBaseConfig;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.index.SegmentTable;
import autonavi.online.framework.support.zookeeper.exception.ZKExistingDataSourceKeyException;
import autonavi.online.framework.zookeeper.SysProps;
import autonavi.online.framework.zookeeper.ZkUtils;

public class ConfigCenterFromZooKeeper implements InitBaseConfig {
	private Logger log = LogManager.getLogger(getClass());
	
	private ZooKeeperProp zooKeeperProp;
	private String projectName;
	
	

	private final String ROOT = SysProps.AOF_ROOT;

	private final String BASE_DSS = SysProps.AOF_APP_BASE+SysProps.AOF_APP_DSS;
	private final String BASE_SHARD_DSS = SysProps.AOF_APP_BASE+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_DSS;
	private final String BASE_SHARD_INDEX_DS = SysProps.AOF_APP_BASE+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_INDEX+SysProps.AOF_APP_DS;
	private final String BASE_SHARD_INDEX_TABLES = SysProps.AOF_APP_BASE+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_INDEX+SysProps.AOF_APP_TABLES;
	private final String BASE_SHARD_SEGMENT_TABLES = SysProps.AOF_APP_BASE+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_SEG;

	private String JTA_DS = SysProps.JTA_DS;
	private String C3P0_DS =SysProps.C3P0_DS;

	public void setZooKeeperProp(ZooKeeperProp zooKeeperProp) {
		this.zooKeeperProp = zooKeeperProp;
		projectName="/"+zooKeeperProp.getProjectName();
	}
	@SuppressWarnings("unchecked")
	@Override
	public CcBaseEntity getBeseConfig() throws Exception {
		ZooKeeper zk = ZkUtils.Instance().Init(zooKeeperProp.getAddress(), zooKeeperProp.getSessionTimeout(), null);
		zk.addAuthInfo("digest", (zooKeeperProp.getProjectName() + ":" + zooKeeperProp.getPassword()).getBytes());// 加上用户权限
		CcBaseEntity entity = new CcBaseEntity();
		Map<String, CcDataSource> m = this.getDS(zk,projectName);
		Map<Integer,CcDataSource> p_m=this.getProxyDS(zk,projectName);
		entity.setRealDataSources(m);
		entity.setDataSources(p_m);
		entity.setShardIndex(this.initShardIndex(zk, entity.getDataSources(),projectName));
		entity.setIndexTableMap(this.initShardIndex(zk,projectName));
		entity.setSegmentTables(this.initSegmentTable(zk,projectName));
		log.info("关闭核心信息ZooKeeper获取连接");
		zk.close();
		//根据激活状态重构真实数据源和代理数据源
		Map<String, CcDataSource> m_a=new HashMap<String, CcDataSource>();
		for(String key:m.keySet()){
			CcDataSource _ccDataSource=m.get(key);
			if(_ccDataSource.isAcitve()){
				m_a.put(key, _ccDataSource);
			}
		}
		entity.setRealDataSources(m_a);
		for(Integer key:p_m.keySet()){
			CcDataSource _ccDataSource=p_m.get(key);
			List<String> dss=(List<String>)_ccDataSource.getProps().get(SysProps.REAl_DSS);
			List<String> dss_a=new ArrayList<String>();
			for(String dsName:dss){
				if(m_a.containsKey(dsName)){
					dss_a.add(dsName);
				}
			}
			_ccDataSource.getProps().put(SysProps.REAl_DSS, dss_a);
		}
		return entity;
	}
	/**
	 * 从ZOOKEEPER正式环境获取配置信息
	 * @param zk
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	public CcBaseEntity getDssConfig(ZooKeeper zk,String appName) throws Exception {
		CcBaseEntity entity = new CcBaseEntity();
		appName="/"+appName;
		Map<String, CcDataSource> m = this.getDS(zk,appName);
		Map<Integer,CcDataSource> p_m=this.getProxyDS(zk, appName);
		entity.setDataSources(p_m);
		entity.setRealDataSources(m);
		if(entity.getDataSources().size()>0){
			entity.setShardIndex(this.initShardIndex(zk, entity.getDataSources(),appName));
		}else{
			entity.setShardIndex(-1);
		}
		
		log.info("获取ZooKeeper完毕");
		return entity;
	}
	/**
	 * 获取临时目录的ZK配置
	 * @param zk
	 * @param appName
	 * @param tempPath
	 * @return
	 * @throws Exception
	 */
	public CcBaseEntity getDssConfig(ZooKeeper zk,String appName,String tempPath) throws Exception {
		CcBaseEntity entity = new CcBaseEntity();
		appName="/"+appName;
		tempPath="/"+tempPath;
		Map<String, CcDataSource> m = this.getDS(zk,appName,tempPath);
		Map<Integer,CcDataSource> p_m=this.getProxyDS(zk, appName,tempPath);
		entity.setDataSources(p_m);
		entity.setRealDataSources(m);
		if(entity.getDataSources().size()>0){
			entity.setShardIndex(this.initShardIndex(zk, entity.getDataSources(),appName,tempPath));
		}else{
			entity.setShardIndex(-1);
		}
		log.info("获取ZooKeeper完毕");
		return entity;
	}
	/**
	 * 获取分区分表信息
	 * @param zk
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	public CcBaseEntity getShardingConfig(ZooKeeper zk,String appName)throws Exception{
		CcBaseEntity entity = new CcBaseEntity();
		appName="/"+appName;
		Map<String, CcDataSource> m = this.getDS(zk,appName);
		entity.setDataSources(this.initShardDS(zk, m,appName));
		if(entity.getDataSources().size()>0){
			entity.setShardIndex(this.initShardIndex(zk, entity.getDataSources(),appName));
		}else{
			entity.setShardIndex(-1);
		}
		entity.setIndexTableMap(this.initShardIndex(zk,appName));
		entity.setSegmentTables(this.initSegmentTable(zk,appName));
		log.info("获取ZooKeeper 分区分表信息完毕");
		return entity;
	}
	/**
	 * 获取代理数据源
	 * @param zk
	 * @param appName
	 * @param tempPath
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, CcDataSource> getProxyDS(ZooKeeper zk,String appName,String...tempPath) throws Exception{
		Map<Integer, CcDataSource> map = new HashMap<Integer, CcDataSource>();
		String shard_proxy_dss_path = ROOT + appName + BASE_SHARD_DSS;
		if(tempPath!=null&&tempPath.length>=1){
			shard_proxy_dss_path=SysProps.AOF_TEMP_ROOT+appName+tempPath[0]+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_DSS;
		}
		List<String> children = zk.getChildren(shard_proxy_dss_path, false, null);
		for (String child : children) {
			String strategyNamePath=shard_proxy_dss_path+"/"+child+"/"+SysProps.STRATEGY_NAME;
			String realDssPath=shard_proxy_dss_path+"/"+child+"/"+SysProps.REAl_DSS;
			byte[] strategyNameData = zk.getData(strategyNamePath, false, null);
			if(strategyNameData!=null){
				String strategyName=new String(strategyNameData,SysProps.CHARSET);
				List<String> realDssList = zk.getChildren(realDssPath, false, null);
				CcDataSource ds = new CcDataSource();
//				ds.setBeanClass(SysProps.PROXY_DS);
				Map<String,Object> prop=new HashMap<String,Object>();
				prop.put(SysProps.STRATEGY_NAME, strategyName);
				prop.put(SysProps.REAl_DSS, realDssList);
				ds.setProps(prop);
				if(map.containsKey(new Integer(child))){
					throw new ZKExistingDataSourceKeyException("存在重复的数据源key，请检查配置");
				}
				map.put(new Integer(child), ds);
			}
			
		}
		return map;
	}
	
    /**
     * 获取数据源
     * @param zk
     * @return
     * @throws Exception
     */
	private Map<String, CcDataSource> getDS(ZooKeeper zk,String appName,String...tempPath) throws Exception {
		Map<String, CcDataSource> map = new HashMap<String, CcDataSource>();
		String base_dss_path = ROOT + appName + BASE_DSS;
        if(tempPath!=null&&tempPath.length>=1){
        	base_dss_path=SysProps.AOF_TEMP_ROOT+appName+tempPath[0]+SysProps.AOF_APP_DSS;
		}
		List<String> children = zk.getChildren(base_dss_path, false, null);
		for (String child : children) {
			String thisPath = base_dss_path + "/" + child;
			byte[] dsData = zk.getData(thisPath, false, null);
			if (dsData != null) {
				String dsDataStr = new String(dsData);
				String clazz = dsDataStr;// 取出class属性
				CcDataSource ds = new CcDataSource();
				ds.setAcitve(true);
				ds.setName(child);
				if (clazz.equalsIgnoreCase(C3P0_DS)) {
					ds.setBeanClass(C3P0_DS);
					// 从子节点添加属性
					List<String> attrs = zk.getChildren(thisPath, false, null);
					Map<String, Object> props = new HashMap<String, Object>();
					if (attrs != null) {
						for (String attr : attrs) {
							if(attr.equals(SysProps.DSS_MONITOR_BASE.substring(1))){
								//监控路径 特殊处理 兼容不存在的情况
								String path=thisPath + "/"
										+ attr+SysProps.DSS_MONITOR_ACTIVE;
								if(zk.exists(path, false)!=null){
									byte[] attrData = zk.getData(thisPath + "/"
											+ attr+SysProps.DSS_MONITOR_ACTIVE, false, null);
									//设置数据源是否为激活状态
									ds.setAcitve(Boolean.valueOf(new String(attrData,SysProps.CHARSET)));
								}
								
							}
							else if (!attr.equals("isIndex")
									&& !attr.equals("isActive")
									&& !attr.equals("unique")) {
								byte[] attrData = zk.getData(thisPath + "/"
										+ attr, false, null);
								if (attrData != null) {
									String attrArray = new String(attrData,
											SysProps.CHARSET);
									props.put(attr, attrArray);
								}
							}

						}
					}
					ds.setProps(props);
				} else if (clazz.equalsIgnoreCase(JTA_DS)) {
					ds.setBeanClass(JTA_DS);
					List<String> attrs = zk.getChildren(thisPath, false, null);
					if (attrs != null) {
						Map<String, Object> props = new HashMap<String, Object>();
						Properties prop = new Properties();
						for (String attr : attrs) {
							if(attr.equals(SysProps.DSS_MONITOR_BASE.substring(1))){
								//监控路径 特殊处理 兼容不存在的情况
								String path=thisPath + "/"
										+ attr+SysProps.DSS_MONITOR_ACTIVE;
								if(zk.exists(path, false)!=null){
									byte[] attrData = zk.getData(thisPath + "/"
											+ attr+SysProps.DSS_MONITOR_ACTIVE, false, null);
									//设置数据源是否为激活状态
									ds.setAcitve(Boolean.valueOf(new String(attrData,SysProps.CHARSET)));
								}
								
							}
							else if (!attr.equals("isIndex")
									&& !attr.equals("isActive")
									&& !attr.equals("unique")
									&& !attr.equals("URL")
									&& !attr.equals("user")
									&& !attr.equals("password")) {
								byte[] attrData = zk.getData(thisPath + "/"
										+ attr, false, null);
								if (attrData != null) {
									String attrArray = new String(attrData,"utf-8");
									props.put(attr,
											attrArray);
								}
							} else if (attr.equals("unique")) {
								byte[] attrData = zk.getData(thisPath + "/"
										+ attr, false, null);
								if (attrData != null) {
									String attrArray = new String(attrData,"utf-8");
									props.put("uniqueResourceName", attrArray);
								}
							} else if (attr.equals("URL")
									|| attr.equals("user")
									|| attr.equals("password")) {

								byte[] attrData = zk.getData(thisPath + "/"
										+ attr, false, null);
								if (attrData != null) {
									String attrArray = new String(attrData,"utf-8");
									prop.put(attr, attrArray);
								}

							}

						}
						props.put("xaProperties", prop);
						ds.setProps(props);
					}
					
				} else {
					log.warn("数据源类型[" + clazz + "],不是框架支持的类型，不予处理，请检查配置");
				}
				map.put(ds.getName(), ds);
			}
		}
		return map;
	}
    /**
     * 获取分库信息
     * @param zk
     * @param map
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws ZKExistingDataSourceKeyException
     * @throws Exception
     */
	private Map<Integer, CcDataSource> initShardDS(ZooKeeper zk,
			Map<String, CcDataSource> map,String appName,String...tempPath) throws KeeperException,
			InterruptedException, ZKExistingDataSourceKeyException, Exception {
		Map<Integer, CcDataSource> result = new HashMap<Integer, CcDataSource>();
		String path = ROOT + appName + BASE_SHARD_DSS;
		if(tempPath!=null&&tempPath.length>=1){
			path=SysProps.AOF_TEMP_ROOT+appName+tempPath[0]+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_DSS;
		}
		List<String> children = zk.getChildren(path, false, null);
		for (String child : children) {
			Integer key = Integer.valueOf(child);
			if (result.containsKey(key))
				throw new ZKExistingDataSourceKeyException("存在重复的数据源key，请检查配置");
			byte[] data = zk.getData(path + "/" + child, false, null);
			if (data != null) {
				String dataStr = new String(data, "utf-8");
				result.put(key, map.get(dataStr));
			}
		}
		return result;
	}
    /**
     * 获取分库索引表信息
     * @param zk
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws Exception
     */
	private Map<String, List<ColumnAttribute>> initShardIndex(ZooKeeper zk,String appName)
			throws KeeperException, InterruptedException, Exception {

		String path = ROOT + appName + BASE_SHARD_INDEX_TABLES;
		List<String> children = zk.getChildren(path, false, null);

		Map<String, List<ColumnAttribute>> indexTableMap = new HashMap<String, List<ColumnAttribute>>();
		for (String tableName : children) {
			ColumnAttribute columnAttribute = null;
			columnAttribute = new ColumnAttribute();
			List<String> children1 = zk.getChildren(path + "/" + tableName,
					false, null);
			List<ColumnAttribute> columnAttributeList = new ArrayList<ColumnAttribute>();
			for (String _columnName : children1) {
				columnAttribute.setColumnName(_columnName);
				List<String> children2 = zk.getChildren(path + "/" + tableName
						+ "/" + _columnName, false, null);
				for (String _attr : children2) {
					byte[] data = zk.getData(path + "/" + tableName + "/"
							+ _columnName + "/" + _attr, false, null);
					if (data != null) {

						String dataStrs = new String(data,"utf-8");
						if (_attr.equals(SysProps.AOF_INDEX_TYPE.replaceAll("/","" ))) {
							columnAttribute.setColumnType(dataStrs);
						} else if (_attr.equals(SysProps.AOF_INDEX_LENGTH.replaceAll("/","" ))) {
							columnAttribute
									.setLength(Integer.valueOf(dataStrs));
						}else if(_attr.equals(SysProps.AOF_INDEX_NAME.replaceAll("/","" ))){
							columnAttribute.setName(dataStrs);
						}
					}
				}
				columnAttributeList.add(columnAttribute);
			}

			indexTableMap.put(tableName, columnAttributeList);
		}
		return indexTableMap;
	}
    /**
     * 获取分库索引数据源
     * @param zk
     * @param map
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws Exception
     */
	private Integer initShardIndex(ZooKeeper zk, Map<Integer, CcDataSource> map,String appName,String... tempPath )
			throws KeeperException, InterruptedException, Exception {
		String path=ROOT + appName + BASE_SHARD_INDEX_DS;
		if(tempPath!=null&&tempPath.length>=1){
			path=SysProps.AOF_TEMP_ROOT+appName+tempPath[0]+SysProps.AOF_APP_SHARD+SysProps.AOF_APP_INDEX+SysProps.AOF_APP_DS;
		}
		/**
		 * 得到索datasource的配置
		 */
		byte[] data = zk.getData(path,
				false, null);
		String dataStr = "";
		if (data != null) {
			dataStr = new String(data,SysProps.CHARSET);
//			for (Integer a : map.keySet()) {
//				if (map.get(a).getName().equals(dataStr)) {
//					return a;
//				}
//			}
			if(map.containsKey(new Integer(dataStr))){
				return new Integer(dataStr);
			}
			
		}
		log.warn("请注意,索引表数据源[]没有在数据源配置中找到,可能由于没有配置数据源或者数据源名称错误");
		return new Integer(-1);
		
	}
    /**
     * 获取分表信息
     * @param zk
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws Exception
     */
	private List<SegmentTable> initSegmentTable(ZooKeeper zk,String appName)
			throws KeeperException, InterruptedException, Exception {
		List<SegmentTable> segmentTables = new ArrayList<SegmentTable>();
		String path = ROOT + appName + BASE_SHARD_SEGMENT_TABLES;
		List<String> children = zk.getChildren(path, false, null);
		for (String tableName : children) {
			SegmentTable segmentTable = new SegmentTable();
			segmentTable.setName(tableName);
			byte[] data = zk.getData(path + "/" + tableName, false, null);
			if (data != null) {
				String countStr = new String(data,"utf-8");
				if (countStr != null && !countStr.isEmpty())
					segmentTable.setCount(Integer.valueOf(countStr));
			}
			segmentTables.add(segmentTable);
		}
		return segmentTables;
	}

}
