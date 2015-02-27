package autonavi.online.framework.sharding.holder;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class ConnectionHolder {
	
	public static void cleanAll(){
		cleanAutoCommitHolder();
		cleanConnectionHolder();
		cleanShardConnectionHolder();
	}

	/**
	 * 单连接持有者
	 */
	private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();

	public static void cleanConnectionHolder() {
		connectionHolder.remove();
	}

	public static void setConnectionHolder(Connection conn) {
		connectionHolder.set(conn);
	}

	public static Connection getConnectionHolder() {
		return connectionHolder.get();
	}

	/**
	 * 是否自动提交持有者
	 */
	private static final ThreadLocal<Boolean> isAutoCommitHolder = new ThreadLocal<Boolean>();

	public static void cleanAutoCommitHolder() {
		isAutoCommitHolder.remove();
	}

	public static void setAutoCommit(Boolean isAutoCommit) {
		isAutoCommitHolder.set(isAutoCommit);
	}

	public static Boolean isAutoCommit() {
		Boolean isAutoCommit = isAutoCommitHolder.get();
		if (isAutoCommit == null)
			return true;
		return isAutoCommitHolder.get();
	}

	/**
	 * 多数据源持有者
	 */
	private static final ThreadLocal<ConcurrentHashMap<DataSource, Connection>> shardConnectionHolder = new ThreadLocal<ConcurrentHashMap<DataSource, Connection>>();

	public static void cleanShardConnectionHolder() {
		shardConnectionHolder.remove();
	}

	public static void setShardConnectionHolder(DataSource dataSource,
			Connection connection) {
		ConcurrentHashMap<DataSource, Connection> shardConnectionMap = before();
		shardConnectionMap.put(dataSource, connection);
	}

	public static ConcurrentHashMap<DataSource, Connection> getShardConnectionHolder() {
		return before();
	}

	private static ConcurrentHashMap<DataSource, Connection> before() {
		ConcurrentHashMap<DataSource, Connection> shardConnectionMap = shardConnectionHolder
				.get();
		if (shardConnectionMap == null) {
			shardConnectionMap = new ConcurrentHashMap<DataSource, Connection>();
			shardConnectionHolder.set(shardConnectionMap);
		}
		return shardConnectionMap;
	}
}
