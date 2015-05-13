package autonavi.online.framework.sharding.index;

import java.util.Set;

public class ShardingEntity {
	private Set<Integer> dsKeys;
	private Object[] indexColumnValue;
	private Object[] indexColumn;
	private Long segemntId;
	private int dsKey;
	private int tableCount;
	private String sql;

	public Set<Integer> getDsKeys() {
		return dsKeys;
	}

	public void setDsKeys(Set<Integer> dsKeys) {
		this.dsKeys = dsKeys;
	}

	public Object[] getIndexColumnValue() {
		return indexColumnValue;
	}

	public void setIndexColumnValue(Object[] indexColumnValue) {
		this.indexColumnValue = indexColumnValue;
	}

	public Object[] getIndexColumn() {
		return indexColumn;
	}

	public void setIndexColumn(Object[] indexColumn) {
		this.indexColumn = indexColumn;
	}

	public Long getSegemntId() {
		return segemntId;
	}

	public void setSegemntId(Long segemntId) {
		this.segemntId = segemntId;
	}

	public int getDsKey() {
		return dsKey;
	}

	public void setDsKey(int dsKey) {
		this.dsKey = dsKey;
	}

	public int getTableCount() {
		return tableCount;
	}

	public void setTableCount(int tableCount) {
		this.tableCount = tableCount;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
