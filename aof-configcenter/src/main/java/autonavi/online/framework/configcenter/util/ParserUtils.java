package autonavi.online.framework.configcenter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import autonavi.online.framework.cc.CcBaseEntity;
import autonavi.online.framework.cc.CcDataSource;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.index.SegmentTable;

public class ParserUtils {
	
	private static Logger logger = LogManager.getLogger(ParserUtils.class);
	
	public static CcBaseEntity parserCcBaseEntity(String root, String app, ZooKeeper zooKeeper) {
		String _root = root;
		if (!_root.endsWith("/")) {
			_root = _root + "/";
		}
		
		CcBaseEntity baseEntity = new CcBaseEntity();
		
		//一、/base
		//1、/base/dss
		handDss(app, zooKeeper, _root, baseEntity);
		//2、/base/shard
		//1.1、/base/shard/index
		handIndex(app, zooKeeper, _root, baseEntity);
		//1.2、/base/shard/dss
		//1.3、/base/shard/segment-tables
		handSegmentTables(app, zooKeeper, _root, baseEntity);
		//二、/biz
		
		return baseEntity;
	}

	/**
	 * 处理分表
	 * @param app
	 * @param zooKeeper
	 * @param _root
	 * @param baseEntity
	 */
	private static void handSegmentTables(String app, ZooKeeper zooKeeper,
			String _root, CcBaseEntity baseEntity) {
		String base = _root + app + "/base/shard/segment-tables";
		try {
			List<String> props = zooKeeper.getChildren(base, false);
			if (null != props && ! props.isEmpty()) {
				List<SegmentTable> segmentTables = new ArrayList<SegmentTable>();
				for (String prop: props) {//所有table
					String indexTable = base + "/" + prop;
					
					try {
						SegmentTable segmentTable = new SegmentTable();
						
						segmentTable.setName(prop);
						
						byte[] countDate = zooKeeper.getData(indexTable, null, null);
						if (null != countDate) {
							segmentTable.setCount(new Integer(new String(countDate)));
						} else {
							segmentTable.setCount(0);
						}
						
						segmentTables.add(segmentTable);
					} catch (Exception e) {
						logger.error("解析segment-tables有误");
						logger.error(e.getMessage(), e);
					}
				}
				
				baseEntity.setSegmentTables(segmentTables);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 处理索引
	 * @param app
	 * @param zooKeeper
	 * @param root
	 * @param baseEntity
	 */
	private static void handIndex(String app, ZooKeeper zooKeeper,
			String root, CcBaseEntity baseEntity) {
		String base = root + app + "/base/shard/index/tables";
		try {
			List<String> props = zooKeeper.getChildren(base, false);
			if (null != props && ! props.isEmpty()) {
				Map<String, List<ColumnAttribute>> indexTableMap = new HashMap<String, List<ColumnAttribute>>();
				for (String prop: props) {//所有table
					String indexTable = base + "/" + prop;
					
					try {
						//byte[] data = zooKeeper.getData(indexTable, null, null);
						//String indexKey = new String(data);
						
						List<String> children = zooKeeper.getChildren(indexTable, false);
						List<ColumnAttribute> columnAttributes = new ArrayList<ColumnAttribute>();
						for (String indexName: children) {//table下的所有索引
							String indexPath = indexTable + "/" + indexName;
							
							ColumnAttribute columnAttribute = new ColumnAttribute();
							
							columnAttribute.setName(indexName);
							
							byte[] columnTypeData = zooKeeper.getData(indexPath + "/type", null, null);
							//byte[] columnNameData = zooKeeper.getData(indexPath + "/columnName", null, null);
							byte[] lengthData = zooKeeper.getData(indexPath + "/length", null, null);
							//byte[] columnVlaueData = zooKeeper.getData(indexPath + "/columnVlaue", null, null);
							
							
							columnAttribute.setColumnName(getDateIfNullReturnBlank(indexPath + "/columnName", zooKeeper));
							if (null != columnTypeData)
								columnAttribute.setColumnType(new String(columnTypeData));
							if (null != lengthData)
								columnAttribute.setLength(new Integer(new String(lengthData)));
							//columnAttribute.setColumnVlaue(new String(columnVlaueData));
							
							columnAttributes.add(columnAttribute);
						}
						
						indexTableMap.put(prop, columnAttributes);
					} catch (Exception e) {
						logger.error("解析index有误");
						logger.error(e.getMessage(), e);
					}
					
					
				}
				
				baseEntity.setIndexTableMap(indexTableMap);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	

	/**
	 * 处理数据源
	 * @param app
	 * @param zooKeeper
	 * @param root
	 * @param baseEntity
	 */
	private static void handDss(String app, ZooKeeper zooKeeper, String root,
			CcBaseEntity baseEntity) {
		String base = root + app + "/base/dss";
		try {
			List<String> props = zooKeeper.getChildren(base, false);
			int index = 1;
			if (null != props && ! props.isEmpty()) {
				Map<Integer, CcDataSource> dataSourceMap = new HashMap<Integer, CcDataSource>();
				for (String prop: props) {
					String thisDs = base + "/" + prop;
					CcDataSource dataSource = new CcDataSource();
					try {
						byte[] data = zooKeeper.getData(thisDs, null, null);
						dataSource.setName(prop);
						
						dataSource.setBeanClass(new String(data));
						
						List<String> children = zooKeeper.getChildren(thisDs, false);
						Map<String, Object> propsMap = new HashMap<String, Object>();
						for (String key: children) {
							byte[] cdata = zooKeeper.getData(thisDs + "/" + key, null, null);
							if (null != cdata) {
								propsMap.put(key, new String(cdata));
							}
						}
						
						dataSource.setProps(propsMap);
					} catch (Exception e) {
						logger.error("未指定数据源名称");
						logger.error(e.getMessage(), e);
					}
					
					dataSourceMap.put(index ++, dataSource);
				}
				
				baseEntity.setDataSources(dataSourceMap);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private static String getDateIfNullReturnBlank(String path, ZooKeeper zooKeeper) {
		String result = getData(path, zooKeeper);
		return result == null ? "" : result;
	}
	
	private static String getData(String path, ZooKeeper zooKeeper) {
		String result = null;
		try {
			byte[] data = zooKeeper.getData(path, null, null);
			if (null != data) {
				result = new String(data);
			}
		} catch (KeeperException e) {
			//e.printStackTrace();
			//ignore
		} catch (InterruptedException e) {
			//e.printStackTrace();
			//ignore
		}
		return result;
	}
	
}
