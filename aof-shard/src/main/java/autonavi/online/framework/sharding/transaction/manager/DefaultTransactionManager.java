package autonavi.online.framework.sharding.transaction.manager;

import java.sql.Connection;
import java.sql.SQLException;

import autonavi.online.framework.sharding.holder.ConnectionHolder;

/**
 * 默认事务管理器，只实现最简单的手工事务管理，如果想使用完整的事务管理，请使用事务依赖包
 * 
 * @author jia.miao
 * 
 */
public class DefaultTransactionManager {

	/**
	 * 开始事务
	 */
	public static void begin() {
		ConnectionHolder.setAutoCommit(false);
	}

	/**
	 * 提交事务
	 * 
	 * @throws SQLException
	 */
	public static void commit() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()) {
					conn.commit();
				}
			}
		}
	}

	/**
	 * 回滚事物
	 * 
	 * @throws SQLException
	 */
	public static void rollback() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()) {
					conn.rollback();
				}
			}
		}
	}

	/**
	 * 释放连接
	 * 
	 * @throws SQLException
	 */
	public static void release() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			}
			ConnectionHolder.cleanAutoCommitHolder();
		}
	}

}
