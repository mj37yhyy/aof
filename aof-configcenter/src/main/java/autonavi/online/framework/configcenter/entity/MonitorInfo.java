package autonavi.online.framework.configcenter.entity;

public class MonitorInfo {
	
	private String dbname;
	
	/**
	 * 当前连接数
	 */
	private Long cur_conn;
	
	
	/**
	 * 最大连接数
	 */
	private String max_conn;
	
	/**
	 * 数据库状态
	 * 0正常  1不正常,或者 0线上，1下线
	 */
	private Integer type;
	
	/**
	 * 系统时间
	 */
	private Long systemTime;
	
	/**
	 * 更新时间
	 */
	private Long updateTime;

	public Long getCur_conn() {
		return cur_conn;
	}

	public void setCur_conn(Long cur_conn) {
		this.cur_conn = cur_conn;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public Long getSystemTime() {
		return systemTime;
	}

	public void setSystemTime(Long systemTime) {
		this.systemTime = systemTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getMax_conn() {
		return max_conn;
	}

	public void setMax_conn(String max_conn) {
		this.max_conn = max_conn;
	}
	
}
