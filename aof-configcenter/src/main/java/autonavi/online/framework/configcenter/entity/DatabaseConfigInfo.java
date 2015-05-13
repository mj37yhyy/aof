package autonavi.online.framework.configcenter.entity;

public class DatabaseConfigInfo {
	private String dbname;
	
	//配置的最大连接数
	private String max_conn;

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getMax_conn() {
		return (null ==max_conn || "".equals(max_conn)) ? "0" : max_conn;
	}

	public void setMax_conn(String max_conn) {
		this.max_conn = max_conn;
	}
	
}
