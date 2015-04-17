package autonavi.online.framework.sharding.dao;

import java.sql.Connection;

public class RouteResult {
	private String sql = "";
	private Connection connection = null;
//	private DynamicDataSource dynamicDataSource = null;

	/**
	 * @return the sql
	 */
	public String getSql() {
		return this.sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

//	public DynamicDataSource getDynamicDataSource() {
//		return dynamicDataSource;
//	}
//
//	public void setDynamicDataSource(DynamicDataSource dynamicDataSource) {
//		this.dynamicDataSource = dynamicDataSource;
//	}
}
