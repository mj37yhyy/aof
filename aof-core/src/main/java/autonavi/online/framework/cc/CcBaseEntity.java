package autonavi.online.framework.cc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.sharding.index.SegmentTable;

public final class CcBaseEntity {
	private Map<Integer, CcDataSource> dataSources;
	private Integer shardIndex;
	private Map<String, List<ColumnAttribute>> indexTableMap;
	private List<SegmentTable> segmentTables;
	private Map<String,CcDataSource> realDataSources;
	private List<CcDaoEntity> daos;


	/**
	 * 默认构造函数 必须存在 JSON转化使用
	 */
	
	public CcBaseEntity(){
		
	}
	

	public CcBaseEntity(boolean... isDefalut) {
		//在某些情况下需要初始化
		if (isDefalut.length > 0 && isDefalut[0]) {
			dataSources = new HashMap<Integer, CcDataSource>();
			shardIndex = -1;
			indexTableMap = new HashMap<String, List<ColumnAttribute>>();
			segmentTables = new ArrayList<SegmentTable>();
			realDataSources=new HashMap<String, CcDataSource>();
			daos=new ArrayList<CcDaoEntity>();
		}
	}
	
	
	
	public List<CcDaoEntity> getDaos() {
		return daos;
	}


	public void setDaos(List<CcDaoEntity> daos) {
		this.daos = daos;
	}

	public Map<String, CcDataSource> getRealDataSources() {
		return realDataSources;
	}


	public void setRealDataSources(Map<String, CcDataSource> realDataSources) {
		this.realDataSources = realDataSources;
	}


	public Map<Integer, CcDataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(Map<Integer, CcDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public Integer getShardIndex() {
		return shardIndex;
	}

	public void setShardIndex(Integer shardIndex) {
		this.shardIndex = shardIndex;
	}

	public Map<String, List<ColumnAttribute>> getIndexTableMap() {
		return indexTableMap;
	}

	public void setIndexTableMap(
			Map<String, List<ColumnAttribute>> indexTableMap) {
		this.indexTableMap = indexTableMap;
	}

	public List<SegmentTable> getSegmentTables() {
		return segmentTables;
	}

	public void setSegmentTables(List<SegmentTable> segmentTables) {
		this.segmentTables = segmentTables;
	}
}
